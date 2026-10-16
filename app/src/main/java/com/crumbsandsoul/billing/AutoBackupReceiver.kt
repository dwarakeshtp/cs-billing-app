package com.crumbsandsoul.billing

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * Alarm callback — hands work to [AutoBackupService] immediately so the backup is not limited
 * by the short [goAsync] window.
 */
class AutoBackupReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        Log.d(TAG, "Alarm fired, starting AutoBackupService")
        AutoBackupService.start(context.applicationContext)
    }

    companion object {
        private const val TAG = "AutoBackup"
    }
}
