package com.bhakti.app.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object NotificationChannels {
    const val DAILY_DEVOTIONAL = "daily_devotional"
    const val FESTIVAL = "festival"

    fun ensureCreated(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        manager.createNotificationChannel(
            NotificationChannel(DAILY_DEVOTIONAL, "Daily devotional reminders", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "Morning, evening pooja, mantra and status reminders"
            }
        )
        manager.createNotificationChannel(
            NotificationChannel(FESTIVAL, "Festival notifications", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "Alerts for upcoming festivals"
            }
        )
    }
}

/** Stable request codes so scheduling the same reminder twice replaces, not duplicates. */
object ReminderIds {
    const val MORNING = 1001
    const val EVENING = 1002
    const val MANTRA_OF_DAY = 1003
    const val STATUS_OF_DAY = 1004
    const val FESTIVAL_BASE = 2000
}
