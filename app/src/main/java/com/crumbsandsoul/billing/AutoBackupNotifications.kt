package com.crumbsandsoul.billing

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

object AutoBackupNotifications {
    private const val TAG = "AutoBackup"
    const val CHANNEL_ID = "auto_backup"
    private const val NOTIF_ID_SUCCESS = 92001
    private const val NOTIF_ID_FAILURE = 92002

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Automatic backup",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Success and failure for scheduled full backups"
            }
            nm.createNotificationChannel(channel)
        }
    }

    fun canShowNotifications(context: Context): Boolean {
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            return false
        }
        if (Build.VERSION.SDK_INT >= 33) {
            return ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        }
        return true
    }

    fun notifySuccess(context: Context, zipFile: java.io.File) {
        val app = context.applicationContext
        ensureChannel(app)
        if (!canShowNotifications(app)) {
            Log.w(TAG, "Skipping success notification: notifications disabled or POST_NOTIFICATIONS not granted")
            return
        }
        val size = zipFile.length()
        val detail = buildString {
            append(zipFile.name)
            append(" (")
            append(formatBytes(size))
            append(")")
        }
        val openIntent = Intent(app, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pi = PendingIntent.getActivity(
            app,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notif = NotificationCompat.Builder(app, CHANNEL_ID)
            .setSmallIcon(R.drawable.app_logo_clean)
            .setContentTitle("Backup completed")
            .setContentText("Automatic full backup saved: $detail")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Automatic full backup finished successfully.\n\nFile: $detail\n\nPath: ${zipFile.absolutePath}")
            )
            .setContentIntent(pi)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        try {
            NotificationManagerCompat.from(app).notify(NOTIF_ID_SUCCESS, notif)
        } catch (_: SecurityException) {
            // POST_NOTIFICATIONS not granted on API 33+
        }
    }

    fun notifyFailure(context: Context, reason: String?) {
        val app = context.applicationContext
        ensureChannel(app)
        if (!canShowNotifications(app)) {
            Log.w(TAG, "Skipping failure notification: notifications disabled or POST_NOTIFICATIONS not granted")
            return
        }
        val text = reason?.trim()?.takeIf { it.isNotBlank() }
            ?: "Could not create the automatic backup file. Check free storage and try a manual export."
        val openIntent = Intent(app, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pi = PendingIntent.getActivity(
            app,
            1,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notif = NotificationCompat.Builder(app, CHANNEL_ID)
            .setSmallIcon(R.drawable.app_logo_clean)
            .setContentTitle("Backup failed")
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setContentIntent(pi)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        try {
            NotificationManagerCompat.from(app).notify(NOTIF_ID_FAILURE, notif)
        } catch (_: SecurityException) {
        }
    }

    private fun formatBytes(bytes: Long): String {
        if (bytes < 1024) return "$bytes B"
        val kb = bytes / 1024.0
        if (kb < 1024) return String.format(java.util.Locale.US, "%.1f KB", kb)
        val mb = kb / 1024.0
        if (mb < 1024) return String.format(java.util.Locale.US, "%.1f MB", mb)
        return String.format(java.util.Locale.US, "%.2f GB", mb / 1024.0)
    }
}
