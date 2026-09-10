package com.bhakti.app.notifications

import android.Manifest
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.bhakti.app.MainActivity
import com.bhakti.app.R

/** Fires for both daily repeating reminders and one-off festival alerts. */
class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val requestCode = intent.getIntExtra(EXTRA_REQUEST_CODE, 0)
        val title = intent.getStringExtra(EXTRA_TITLE).orEmpty()
        val body = intent.getStringExtra(EXTRA_BODY).orEmpty()
        val route = intent.getStringExtra(EXTRA_ROUTE)
        val channelId = intent.getStringExtra(EXTRA_CHANNEL) ?: NotificationChannels.DAILY_DEVOTIONAL

        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            route?.let { putExtra(MainActivity.EXTRA_DEEP_LINK_ROUTE, it) }
        }
        val pendingIntent = PendingIntent.getActivity(
            context, requestCode, contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) return
        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            .notify(requestCode, notification)
    }

    companion object {
        const val EXTRA_REQUEST_CODE = "request_code"
        const val EXTRA_TITLE = "title"
        const val EXTRA_BODY = "body"
        const val EXTRA_ROUTE = "route"
        const val EXTRA_CHANNEL = "channel"
    }
}
