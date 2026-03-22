package com.crumbsandsoul.billing

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

object AutoBackupScheduler {
    private const val TAG = "AutoBackup"
    private const val RC_AUTO_BACKUP = 91001

    fun autoBackupZipFile(context: Context): File =
        File(context.filesDir, "CrumbsAndSoul_auto_full_backup.zip")

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

    /**
     * Restore or create the alarm (app start, boot, or after changing settings).
     */
    fun reschedule(context: Context) {
        val app = context.applicationContext
        val storage = BillingStorage(app)
        if (!storage.isAutoBackupEnabled()) {
            cancel(app)
            return
        }
        val last = storage.getLastAutoBackupSuccessMillis()
        val now = System.currentTimeMillis()
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                am.setAlarmClock(
                    AlarmManager.AlarmClockInfo(triggerAtMillis, pi),
                    pi
                )
            } else {
                @Suppress("DEPRECATION")
                am.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi)
            }
            Log.d(TAG, "Scheduled auto backup at $triggerAtMillis")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule auto backup", e)
        }
    }
}
