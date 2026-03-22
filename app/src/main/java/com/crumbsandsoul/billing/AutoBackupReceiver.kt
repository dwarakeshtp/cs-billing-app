package com.crumbsandsoul.billing

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import java.util.concurrent.Executors

class AutoBackupReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val pendingResult = goAsync()
        EXECUTOR.execute {
            try {
                val app = context.applicationContext
                val storage = BillingStorage(app)
                if (!storage.isAutoBackupEnabled()) {
                    AutoBackupScheduler.cancel(app)
                    return@execute
                }
                val zip = AutoBackupScheduler.autoBackupZipFile(app)
                val now = System.currentTimeMillis()
                try {
                    val ok = buildFullBackupZipToFile(app, storage, zip)
                    if (ok) {
                        storage.setLastAutoBackupSuccessMillis(now)
                        Log.i(TAG, "Auto backup OK: ${zip.absolutePath}")
                        AutoBackupNotifications.notifySuccess(app, zip)
                    } else {
                        Log.e(TAG, "Auto backup failed to write zip")
                        AutoBackupNotifications.notifyFailure(app, "Could not write the automatic backup file.")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Auto backup error", e)
                    AutoBackupNotifications.notifyFailure(app, e.message)
                }
                AutoBackupScheduler.scheduleNextAfterBackup(app, now)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        private const val TAG = "AutoBackup"
        private val EXECUTOR = Executors.newSingleThreadExecutor()
    }
}
