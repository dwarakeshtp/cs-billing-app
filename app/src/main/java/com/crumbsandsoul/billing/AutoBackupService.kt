package com.crumbsandsoul.billing

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Runs the scheduled full backup as a foreground service so large ZIP builds are not killed
 * when [AutoBackupReceiver]'s short broadcast window expires.
 */
class AutoBackupService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Always enter foreground first — required whenever startForegroundService() was used.
        // Duplicate starts must also call startForeground or Android kills the process.
        if (!promoteToForeground()) {
            RUNNING.set(false)
            stopSelf()
            return START_NOT_STICKY
        }

        if (!RUNNING.compareAndSet(false, true)) {
            Log.d(TAG, "Backup already in progress; ignoring duplicate start")
            // Do not stopSelf() here — that would tear down the in-flight backup when this
            // startId is the newest. The active run will stop the service when finished.
            return START_NOT_STICKY
        }

        val app = applicationContext
        EXECUTOR.execute {
            try {
                runAutomaticBackup(app)
            } finally {
                try {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                } catch (_: Exception) {
                }
                RUNNING.set(false)
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    private fun promoteToForeground(): Boolean {
        return try {
            AutoBackupNotifications.ensureChannel(this)
            val runningNotif = NotificationCompat.Builder(this, AutoBackupNotifications.CHANNEL_ID)
                .setSmallIcon(R.drawable.app_logo_clean)
                .setContentTitle("Backing up…")
                .setContentText("Creating automatic full backup")
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build()
            if (Build.VERSION.SDK_INT >= 34) {
                startForeground(
                    NOTIF_ID_RUNNING,
                    runningNotif,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
                )
            } else {
                startForeground(NOTIF_ID_RUNNING, runningNotif)
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "startForeground failed", e)
            false
        }
    }

    companion object {
        private const val TAG = "AutoBackup"
        private const val NOTIF_ID_RUNNING = 92000
        private val RUNNING = AtomicBoolean(false)
        private val EXECUTOR = Executors.newSingleThreadExecutor()

        fun isRunning(): Boolean = RUNNING.get()

        /** @return false only if the service could not be requested (caller may schedule a retry). */
        fun start(context: Context): Boolean {
            if (RUNNING.get()) {
                Log.d(TAG, "Skip start — backup already running")
                return true
            }
            val app = context.applicationContext
            val intent = Intent(app, AutoBackupService::class.java)
            return try {
                ContextCompat.startForegroundService(app, intent)
                true
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start AutoBackupService", e)
                false
            }
        }

        /**
         * Builds/overwrites the single canonical backup file, mirrors it to public Download,
         * removes other backup zips, then schedules the next run.
         */
        fun runAutomaticBackup(context: Context) {
            val app = context.applicationContext
            val storage = BillingStorage(app)
            if (!storage.isAutoBackupEnabled()) {
                AutoBackupScheduler.cancel(app)
                return
            }
            val zip = AutoBackupScheduler.primaryBackupZipFile(app)
            val now = System.currentTimeMillis()
            try {
                Log.d(TAG, "Automatic backup starting → ${zip.absolutePath}")
                val ok = buildFullBackupZipToFile(app, storage, zip)
                if (ok) {
                    pruneExtraBackupFiles(app, zip)
                    runCatching { copyBackupZipToPublicDownloads(app, zip) }
                    storage.setLastAutoBackupSuccessMillis(now)
                    Log.i(TAG, "Auto backup OK: ${zip.absolutePath} (${zip.length()} bytes)")
                    AutoBackupNotifications.notifySuccess(app, zip)
                } else {
                    Log.e(TAG, "Auto backup failed to write zip")
                    AutoBackupNotifications.notifyFailure(
                        app,
                        "Could not write the automatic backup file."
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Auto backup error", e)
                AutoBackupNotifications.notifyFailure(app, e.message)
            }
            AutoBackupScheduler.scheduleNextAfterBackup(app, now)
        }
    }
}
