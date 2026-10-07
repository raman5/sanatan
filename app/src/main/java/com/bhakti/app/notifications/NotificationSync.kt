package com.bhakti.app.notifications

import com.bhakti.app.core.navigation.Routes
import com.bhakti.app.core.session.SessionState
import com.bhakti.app.data.repository.ContentRepository
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Reconciles AlarmManager against the user's saved toggle state. Notification
 * prefs default to "on" in [SessionManager][com.bhakti.app.core.session.SessionManager],
 * so something has to actually schedule those alarms the first time a user
 * reaches Home - flipping a switch on the settings screen isn't the only path
 * that should result in a scheduled reminder.
 */
suspend fun syncScheduledReminders(
    session: SessionState,
    contentRepository: ContentRepository,
    scheduler: NotificationScheduler
) {
    fun apply(enabled: Boolean, requestCode: Int, hour: Int, minute: Int, title: String, body: String, route: String) {
        if (enabled) {
            scheduler.scheduleDaily(requestCode, hour, minute, title, body, route)
        } else {
            scheduler.cancel(requestCode)
        }
    }

    apply(session.notifMorning, ReminderIds.MORNING, 7, 0, "Good morning, Bhakt", "Start your day with a moment of devotion.", Routes.HOME)
    apply(session.notifEvening, ReminderIds.EVENING, 18, 30, "Evening Pooja", "It's time for your evening pooja.", Routes.POOJA_DEITY_SELECT)
    apply(session.notifMantra, ReminderIds.MANTRA_OF_DAY, 9, 0, "Today's Mantra", "A new mantra is ready for you.", Routes.MANTRA_LIST)
    apply(session.notifStatus, ReminderIds.STATUS_OF_DAY, 20, 0, "New Status Ready", "Today's devotional status is ready to share.", Routes.STATUS_LIST)

    val festivals = contentRepository.festivals()
    festivals.forEachIndexed { index, festival ->
        val requestCode = ReminderIds.FESTIVAL_BASE + index
        if (session.notifFestival) {
            val triggerAtMillis = LocalDate.parse(festival.dateIso, DateTimeFormatter.ISO_LOCAL_DATE)
                .atTime(8, 0)
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
            scheduler.scheduleFestival(
                requestCode, triggerAtMillis, festival.name, "Today is ${festival.name}. Explore special content.", Routes.EXPLORE
            )
        } else {
            scheduler.cancel(requestCode)
        }
    }
}
