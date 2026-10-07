package com.bhakti.app.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.Calendar

/**
 * Schedules the local reminders from the PRD (morning, evening pooja, daily
 * mantra, daily status, festival alerts). Fully on-device today; swap for a
 * push-notification backend later without changing call sites.
 */
interface NotificationScheduler {
    fun scheduleDaily(requestCode: Int, hour: Int, minute: Int, title: String, body: String, deepLinkRoute: String)
    fun scheduleFestival(requestCode: Int, epochMillis: Long, title: String, body: String, deepLinkRoute: String)
    fun cancel(requestCode: Int)
}

class AndroidNotificationScheduler(private val context: Context) : NotificationScheduler {

    private val alarmManager by lazy { context.getSystemService(Context.ALARM_SERVICE) as AlarmManager }

    override fun scheduleDaily(requestCode: Int, hour: Int, minute: Int, title: String, body: String, deepLinkRoute: String) {
        NotificationChannels.ensureCreated(context)
        val triggerAt = nextOccurrenceOf(hour, minute)
        val pendingIntent = buildPendingIntent(requestCode, title, body, deepLinkRoute, NotificationChannels.DAILY_DEVOTIONAL)
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, triggerAt, AlarmManager.INTERVAL_DAY, pendingIntent)
    }

    override fun scheduleFestival(requestCode: Int, epochMillis: Long, title: String, body: String, deepLinkRoute: String) {
        NotificationChannels.ensureCreated(context)
        val pendingIntent = buildPendingIntent(requestCode, title, body, deepLinkRoute, NotificationChannels.FESTIVAL)
        alarmManager.set(AlarmManager.RTC_WAKEUP, epochMillis, pendingIntent)
    }

    override fun cancel(requestCode: Int) {
        val pendingIntent = buildPendingIntent(requestCode, "", "", null, NotificationChannels.DAILY_DEVOTIONAL)
        alarmManager.cancel(pendingIntent)
    }

    private fun buildPendingIntent(requestCode: Int, title: String, body: String, route: String?, channel: String): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(ReminderReceiver.EXTRA_REQUEST_CODE, requestCode)
            putExtra(ReminderReceiver.EXTRA_TITLE, title)
            putExtra(ReminderReceiver.EXTRA_BODY, body)
            putExtra(ReminderReceiver.EXTRA_CHANNEL, channel)
            route?.let { putExtra(ReminderReceiver.EXTRA_ROUTE, it) }
        }
        return PendingIntent.getBroadcast(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun nextOccurrenceOf(hour: Int, minute: Int): Long {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (target.before(now)) target.add(Calendar.DAY_OF_YEAR, 1)
        return target.timeInMillis
    }
}
