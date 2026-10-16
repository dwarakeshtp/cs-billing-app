package com.crumbsandsoul.billing

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.util.Log
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

object AutoBackupScheduler {
    private const val TAG = "AutoBackup"
    private const val RC_AUTO_BACKUP = 91001
    /** PendingIntent for [AlarmManager.AlarmClockInfo] “show alarm” UI — must not reuse the backup broadcast PI. */
    private const val RC_ALARM_CLOCK_SHOW = 91002

    /** Single canonical backup filename — manual and automatic backups overwrite this same file. */
    const val FULL_BACKUP_ZIP_NAME = "CrumbsAndSoul_full_backup.zip"
    const val BACKUP_FOLDER_NAME = "CrumbsAndSoulBackups"

    /** Legacy path used by older builds; deleted after migration. */
    private const val LEGACY_AUTO_BACKUP_ZIP_NAME = "CrumbsAndSoul_auto_full_backup.zip"

    /**
     * The one app-local backup file (overwritten on every successful manual or automatic backup).
     * Mirrored to public Download/[BACKUP_FOLDER_NAME]/ for user access.
     */
    fun primaryBackupZipFile(context: Context): File {
        val dir = File(
            context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
            BACKUP_FOLDER_NAME
        ).apply { mkdirs() }
        return File(dir, FULL_BACKUP_ZIP_NAME)
    }

    /** @deprecated Use [primaryBackupZipFile] — kept as a clear alias for call sites. */
    fun autoBackupZipFile(context: Context): File = primaryBackupZipFile(context)

    fun legacyAutoBackupZipFile(context: Context): File =
        File(context.filesDir, LEGACY_AUTO_BACKUP_ZIP_NAME)

    /** Next occurrence of [hour]:[minute] (local) strictly after [afterMillis]. */
    fun nextSlotAtTimeStrictlyAfter(afterMillis: Long, hour: Int, minute: Int): Long {
        val h = hour.coerceIn(0, 23)
        val m = minute.coerceIn(0, 59)
        val zone = ZoneId.systemDefault()
        val after = ZonedDateTime.ofInstant(Instant.ofEpochMilli(afterMillis), zone)
        var candidate = after.toLocalDate().atTime(h, m).atZone(zone)
        if (!candidate.isAfter(after)) {
            candidate = candidate.plusDays(1)
        }
        return candidate.toInstant().toEpochMilli()
    }

    fun computeNextAfterBackup(
        intervalDays: Int,
        completedAtMillis: Long,
        hour: Int,
        minute: Int
    ): Long {
        val n = intervalDays.coerceIn(1, 30)
        val h = hour.coerceIn(0, 23)
        val min = minute.coerceIn(0, 59)
        if (n == 1) return nextSlotAtTimeStrictlyAfter(completedAtMillis, h, min)
        val zone = ZoneId.systemDefault()
        val z = ZonedDateTime.ofInstant(Instant.ofEpochMilli(completedAtMillis), zone)
        var nextDate = z.toLocalDate().plusDays(n.toLong())
        var next = nextDate.atTime(h, min).atZone(zone)
        while (!next.isAfter(z)) {
            nextDate = nextDate.plusDays(n.toLong())
            next = nextDate.atTime(h, min).atZone(zone)
        }
        return next.toInstant().toEpochMilli()
    }

    /**
     * True when automatic backup is enabled and a run is due now (missed alarm, first enable after
     * today's slot, or interval elapsed).
     */
    fun isBackupOverdue(storage: BillingStorage, nowMillis: Long = System.currentTimeMillis()): Boolean {
        if (!storage.isAutoBackupEnabled()) return false
        val last = storage.getLastAutoBackupSuccessMillis()
        val interval = storage.getAutoBackupIntervalDays()
        val h = storage.getAutoBackupHour()
        val m = storage.getAutoBackupMinute()
        if (last <= 0L) {
            val zone = ZoneId.systemDefault()
            val todaySlot = ZonedDateTime.ofInstant(Instant.ofEpochMilli(nowMillis), zone)
                .toLocalDate()
                .atTime(h, m)
                .atZone(zone)
                .toInstant()
                .toEpochMilli()
            return nowMillis >= todaySlot
        }
        return computeNextAfterBackup(interval, last, h, m) <= nowMillis
    }

    private fun advanceToNextFutureTrigger(
        storage: BillingStorage,
        lastSuccessMillis: Long,
        nowMillis: Long
    ): Long {
        val interval = storage.getAutoBackupIntervalDays()
        val h = storage.getAutoBackupHour()
        val m = storage.getAutoBackupMinute()
        if (lastSuccessMillis <= 0L) {
            return nextSlotAtTimeStrictlyAfter(nowMillis, h, m)
        }
        var w = computeNextAfterBackup(interval, lastSuccessMillis, h, m)
        var guard = 0
        while (w <= nowMillis && guard < 500) {
            w = computeNextAfterBackup(interval, w, h, m)
            guard++
        }
        return w
    }

    fun cancel(context: Context) {
        val app = context.applicationContext
        val am = app.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pi = pendingIntent(app)
        am.cancel(pi)
        pi.cancel()
    }

    private fun pendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, AutoBackupReceiver::class.java)
        return PendingIntent.getBroadcast(
            context,
            RC_AUTO_BACKUP,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun alarmClockShowPendingIntent(context: Context): PendingIntent {
        val open = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            context,
            RC_ALARM_CLOCK_SHOW,
            open,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    /**
     * Restore or create the alarm (app start, boot, or after changing settings).
     * If a run was missed, starts [AutoBackupService] promptly instead of waiting another day.
     */
    fun reschedule(context: Context) {
        val app = context.applicationContext
        val storage = BillingStorage(app)
        if (!storage.isAutoBackupEnabled()) {
            cancel(app)
            return
        }
        val now = System.currentTimeMillis()
        if (isBackupOverdue(storage, now)) {
            Log.i(TAG, "Auto backup overdue — starting catch-up now")
            if (AutoBackupService.isRunning()) {
                // In-flight run will call scheduleNextAfterBackup when finished.
                Log.d(TAG, "Catch-up skipped — backup already running")
                return
            }
            // Service schedules the next alarm when the run finishes (success or failure).
            // Only schedule a distant retry if the service could not be started at all.
            if (!AutoBackupService.start(app)) {
                scheduleAlarmAt(app, now + 15 * 60_000L)
            }
            return
        }
        val last = storage.getLastAutoBackupSuccessMillis()
        val whenMillis = advanceToNextFutureTrigger(storage, last, now)
        scheduleAlarmAt(app, whenMillis)
    }

    fun scheduleNextAfterBackup(context: Context, completedAtMillis: Long) {
        val app = context.applicationContext
        val storage = BillingStorage(app)
        if (!storage.isAutoBackupEnabled()) {
            cancel(app)
            return
        }
        val now = System.currentTimeMillis()
        var whenMillis = computeNextAfterBackup(
            storage.getAutoBackupIntervalDays(),
            completedAtMillis,
            storage.getAutoBackupHour(),
            storage.getAutoBackupMinute()
        )
        var guard = 0
        while (whenMillis <= now && guard < 500) {
            whenMillis = computeNextAfterBackup(
                storage.getAutoBackupIntervalDays(),
                whenMillis,
                storage.getAutoBackupHour(),
                storage.getAutoBackupMinute()
            )
            guard++
        }
        scheduleAlarmAt(app, whenMillis)
    }

    private fun scheduleAlarmAt(context: Context, triggerAtMillis: Long) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pi = pendingIntent(context)
        try {
            // setAlarmClock is not subject to exact-alarm quota restrictions and survives Doze.
            val showPi = alarmClockShowPendingIntent(context)
            am.setAlarmClock(
                AlarmManager.AlarmClockInfo(triggerAtMillis, showPi),
                pi
            )
            Log.d(TAG, "Scheduled auto backup at $triggerAtMillis")
        } catch (e: Exception) {
            Log.e(TAG, "setAlarmClock failed, trying setAndAllowWhileIdle", e)
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi)
                } else {
                    @Suppress("DEPRECATION")
                    am.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi)
                }
                Log.d(TAG, "Scheduled auto backup (fallback) at $triggerAtMillis")
            } catch (e2: Exception) {
                Log.e(TAG, "Failed to schedule auto backup", e2)
            }
        }
    }
}
