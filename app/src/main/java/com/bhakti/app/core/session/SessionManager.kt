package com.bhakti.app.core.session

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.bhakti.app.data.model.AuthMethod
import com.bhakti.app.data.model.Subscription
import com.bhakti.app.data.model.SubscriptionStatus
import com.bhakti.app.data.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

private val Context.dataStore by preferencesDataStore(name = "bhakti_session")

data class SessionState(
    val user: User?,
    val subscription: Subscription,
    val favouriteIds: Set<String>,
    val language: String,
    val notifMorning: Boolean,
    val notifEvening: Boolean,
    val notifMantra: Boolean,
    val notifStatus: Boolean,
    val notifFestival: Boolean,
    /** Japa (chant repetition) lifetime tap totals, keyed by counter id (mantra id, "naam-<deity>"). */
    val japaCounts: Map<String, Int> = emptyMap(),
    /** Per-counter daily japa progress + streak, keyed the same way as [japaCounts]. */
    val japaDaily: Map<String, JapaDay> = emptyMap(),
    /** Ids of today's routine steps (see [HomeScreen]) already completed; resets each calendar day. */
    val routineCompletedSteps: Set<String> = emptySet(),
    /** Ids of the routine modules the user has chosen to show on Home; defaults to all of them. */
    val routineModules: Set<String> = DEFAULT_ROUTINE_MODULES,
    /** Routine completion per day, last [HISTORY_DAYS] days only (today included, kept live). */
    val routineHistory: Map<LocalDate, DayRoutine> = emptyMap(),
    /** Total japa repetitions (all counters) per day, last [HISTORY_DAYS] days only. */
    val japaHistory: Map<LocalDate, Int> = emptyMap()
) {
    val isLoggedIn: Boolean get() = user != null
    val hasActiveAccess: Boolean get() =
        subscription.status == SubscriptionStatus.ACTIVE || subscription.status == SubscriptionStatus.TRIAL

    companion object {
        // Kept as plain ids (not a reference to feature.home.RoutineModule) so core/session
        // doesn't depend on the feature layer; feature/home.RoutineModule.ALL_IDS must match this.
        val DEFAULT_ROUTINE_MODULES = setOf("naam-japa", "mantra", "wallpaper", "status", "pooja")

        /** Progress history is kept for 3 months (92 days covers any 3 calendar months); older days are pruned. */
        const val HISTORY_DAYS = 92L
    }
}

/**
 * One day of the routine: which steps were completed, out of the steps the
 * user had turned on that day. [enabled] is captured per day so turning a
 * module off later doesn't rewrite how complete past days were.
 */
data class DayRoutine(val done: Set<String>, val enabled: Set<String>) {
    private val counted: Set<String> get() = enabled.intersect(SessionState.DEFAULT_ROUTINE_MODULES)
    val doneCount: Int get() = done.intersect(counted).size
    val total: Int get() = counted.size
    val fraction: Float get() = if (total == 0) 0f else doneCount.toFloat() / total
    val isComplete: Boolean get() = total > 0 && doneCount >= total
}

/**
 * The last day a counter was chanted on, how many repetitions that day, and
 * the run of consecutive days ending on it. Stored as-is; [todayCount] and
 * [currentStreak] interpret it relative to the actual current date.
 */
data class JapaDay(val dateIso: String, val count: Int, val streak: Int) {
    fun todayCount(today: LocalDate = LocalDate.now()): Int =
        if (dateIso == today.toString()) count else 0

    /** Still alive if chanted today or yesterday; broken otherwise. */
    fun currentStreak(today: LocalDate = LocalDate.now()): Int =
        if (dateIso == today.toString() || dateIso == today.minusDays(1).toString()) streak else 0
}

/**
 * Local-only session store. Stands in for a server session today; swap the
 * write sites for real API calls once auth/billing/backends exist without
 * touching the screens that read [state].
 */
class SessionManager(private val context: Context) {

    private object Keys {
        val USER_ID = stringPreferencesKey("user_id")
        val USER_NAME = stringPreferencesKey("user_name")
        val USER_EMAIL = stringPreferencesKey("user_email")
        val USER_PHONE = stringPreferencesKey("user_phone")
        val AUTH_METHOD = stringPreferencesKey("auth_method")
        val PLAN_ID = stringPreferencesKey("plan_id")
        val SUB_STATUS = stringPreferencesKey("sub_status")
        val RENEWAL_DATE = stringPreferencesKey("renewal_date")
        val MANDATE_ID = stringPreferencesKey("mandate_id")
        val FAVOURITES = stringSetPreferencesKey("favourite_ids")
        val LANGUAGE = stringPreferencesKey("language")
        val NOTIF_MORNING = booleanPreferencesKey("notif_morning")
        val NOTIF_EVENING = booleanPreferencesKey("notif_evening")
        val NOTIF_MANTRA = booleanPreferencesKey("notif_mantra")
        val NOTIF_STATUS = booleanPreferencesKey("notif_status")
        val NOTIF_FESTIVAL = booleanPreferencesKey("notif_festival")
        val JAPA_COUNTS = stringPreferencesKey("japa_counts")
        val JAPA_DAILY = stringPreferencesKey("japa_daily")
        val ROUTINE_HISTORY = stringPreferencesKey("routine_history")
        val JAPA_HISTORY = stringPreferencesKey("japa_history")
        val ROUTINE_DATE = stringPreferencesKey("routine_date")
        val ROUTINE_DONE = stringSetPreferencesKey("routine_done")
        val ROUTINE_MODULES = stringSetPreferencesKey("routine_modules")
    }

    // mantraId=count pairs, joined by ";" - simple enough that JSON would be overkill,
    // and mantra ids (e.g. "mn-shiva-1") never contain '=' or ';'.
    private fun decodeJapaCounts(raw: String?): Map<String, Int> =
        raw.orEmpty().split(';').filter { it.isNotBlank() }.associate { entry ->
            val (id, count) = entry.split('=', limit = 2)
            id to (count.toIntOrNull() ?: 0)
        }

    private fun encodeJapaCounts(map: Map<String, Int>): String =
        map.entries.joinToString(";") { "${it.key}=${it.value}" }

    // id=date|count|streak entries joined by ";" - same reasoning as japaCounts above.
    private fun decodeJapaDaily(raw: String?): Map<String, JapaDay> =
        raw.orEmpty().split(';').filter { it.isNotBlank() }.mapNotNull { entry ->
            val (id, rest) = entry.split('=', limit = 2).takeIf { it.size == 2 } ?: return@mapNotNull null
            val parts = rest.split('|')
            if (parts.size != 3) return@mapNotNull null
            id to JapaDay(parts[0], parts[1].toIntOrNull() ?: 0, parts[2].toIntOrNull() ?: 0)
        }.toMap()

    private fun encodeJapaDaily(map: Map<String, JapaDay>): String =
        map.entries.joinToString(";") { (id, d) -> "$id=${d.dateIso}|${d.count}|${d.streak}" }

    // date=done1,done2|enabled1,enabled2 entries joined by ";" - ids never contain these separators.
    private fun decodeRoutineHistory(raw: String?): Map<LocalDate, DayRoutine> =
        raw.orEmpty().split(';').filter { it.isNotBlank() }.mapNotNull { entry ->
            val (date, rest) = entry.split('=', limit = 2).takeIf { it.size == 2 } ?: return@mapNotNull null
            val parts = rest.split('|')
            if (parts.size != 2) return@mapNotNull null
            val day = runCatching { LocalDate.parse(date) }.getOrNull() ?: return@mapNotNull null
            fun ids(s: String) = s.split(',').filter { it.isNotBlank() }.toSet()
            day to DayRoutine(ids(parts[0]), ids(parts[1]))
        }.toMap()

    private fun encodeRoutineHistory(map: Map<LocalDate, DayRoutine>): String =
        map.entries.joinToString(";") { (d, r) -> "$d=${r.done.joinToString(",")}|${r.enabled.joinToString(",")}" }

    private fun decodeJapaHistory(raw: String?): Map<LocalDate, Int> =
        raw.orEmpty().split(';').filter { it.isNotBlank() }.mapNotNull { entry ->
            val (date, count) = entry.split('=', limit = 2).takeIf { it.size == 2 } ?: return@mapNotNull null
            val day = runCatching { LocalDate.parse(date) }.getOrNull() ?: return@mapNotNull null
            day to (count.toIntOrNull() ?: 0)
        }.toMap()

    private fun encodeJapaHistory(map: Map<LocalDate, Int>): String =
        map.entries.joinToString(";") { "${it.key}=${it.value}" }

    /** Drops everything older than the 3-month retention window. */
    private fun <V> Map<LocalDate, V>.pruned(): Map<LocalDate, V> {
        val oldest = LocalDate.now().minusDays(SessionState.HISTORY_DAYS - 1)
        return filterKeys { !it.isBefore(oldest) }
    }

    private fun currentModules(prefs: Preferences): Set<String> =
        prefs[Keys.ROUTINE_MODULES] ?: SessionState.DEFAULT_ROUTINE_MODULES

    private fun todayIso(): String = LocalDate.now().toString()

    val state: Flow<SessionState> = context.dataStore.data.map { prefs ->
        val userId = prefs[Keys.USER_ID]
        val user = if (userId != null) {
            User(
                id = userId,
                displayName = prefs[Keys.USER_NAME].orEmpty(),
                email = prefs[Keys.USER_EMAIL],
                phone = prefs[Keys.USER_PHONE],
                authMethod = prefs[Keys.AUTH_METHOD]?.let { AuthMethod.valueOf(it) } ?: AuthMethod.MOBILE_OTP
            )
        } else null

        SessionState(
            user = user,
            subscription = Subscription(
                planId = prefs[Keys.PLAN_ID],
                status = prefs[Keys.SUB_STATUS]?.let { SubscriptionStatus.valueOf(it) } ?: SubscriptionStatus.NONE,
                renewalDateIso = prefs[Keys.RENEWAL_DATE],
                mandateId = prefs[Keys.MANDATE_ID]
            ),
            favouriteIds = prefs[Keys.FAVOURITES] ?: emptySet(),
            language = prefs[Keys.LANGUAGE] ?: "Hindi",
            notifMorning = prefs[Keys.NOTIF_MORNING] ?: true,
            notifEvening = prefs[Keys.NOTIF_EVENING] ?: true,
            notifMantra = prefs[Keys.NOTIF_MANTRA] ?: true,
            notifStatus = prefs[Keys.NOTIF_STATUS] ?: true,
            notifFestival = prefs[Keys.NOTIF_FESTIVAL] ?: true,
            japaCounts = decodeJapaCounts(prefs[Keys.JAPA_COUNTS]),
            japaDaily = decodeJapaDaily(prefs[Keys.JAPA_DAILY]),
            // Steps only count as "done" if they were logged today - a new day starts a fresh routine.
            routineCompletedSteps = if (prefs[Keys.ROUTINE_DATE] == todayIso()) {
                prefs[Keys.ROUTINE_DONE] ?: emptySet()
            } else {
                emptySet()
            },
            routineModules = prefs[Keys.ROUTINE_MODULES] ?: SessionState.DEFAULT_ROUTINE_MODULES,
            routineHistory = run {
                val history = decodeRoutineHistory(prefs[Keys.ROUTINE_HISTORY]).pruned()
                val todayDone = if (prefs[Keys.ROUTINE_DATE] == todayIso()) prefs[Keys.ROUTINE_DONE] ?: emptySet() else emptySet()
                history + (LocalDate.now() to DayRoutine(todayDone, currentModules(prefs)))
            },
            japaHistory = decodeJapaHistory(prefs[Keys.JAPA_HISTORY]).pruned()
        )
    }

    suspend fun signIn(user: User) {
        context.dataStore.edit { prefs ->
            prefs[Keys.USER_ID] = user.id
            prefs[Keys.USER_NAME] = user.displayName
            user.email?.let { prefs[Keys.USER_EMAIL] = it }
            user.phone?.let { prefs[Keys.USER_PHONE] = it }
            prefs[Keys.AUTH_METHOD] = user.authMethod.name
        }
    }

    suspend fun signOut() {
        context.dataStore.edit { it.clear() }
    }

    suspend fun updateDisplayName(name: String) {
        context.dataStore.edit { prefs -> prefs[Keys.USER_NAME] = name }
    }

    suspend fun saveSubscription(subscription: Subscription) {
        context.dataStore.edit { prefs ->
            subscription.planId?.let { prefs[Keys.PLAN_ID] = it }
            prefs[Keys.SUB_STATUS] = subscription.status.name
            subscription.renewalDateIso?.let { prefs[Keys.RENEWAL_DATE] = it }
            subscription.mandateId?.let { prefs[Keys.MANDATE_ID] = it }
        }
    }

    suspend fun toggleFavourite(contentId: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.FAVOURITES] ?: emptySet()
            prefs[Keys.FAVOURITES] = if (contentId in current) current - contentId else current + contentId
        }
    }

    suspend fun setLanguage(language: String) {
        context.dataStore.edit { it[Keys.LANGUAGE] = language }
    }

    /**
     * Adds one repetition to [counterId]'s lifetime total and today's count
     * (rolling the streak forward on the first tap of a new day), and
     * returns today's new count.
     */
    suspend fun incrementJapaCount(counterId: String): Int {
        var todayCount = 0
        context.dataStore.edit { prefs ->
            val totals = decodeJapaCounts(prefs[Keys.JAPA_COUNTS])
            prefs[Keys.JAPA_COUNTS] = encodeJapaCounts(totals + (counterId to (totals[counterId] ?: 0) + 1))

            val today = LocalDate.now()
            val daily = decodeJapaDaily(prefs[Keys.JAPA_DAILY])
            val previous = daily[counterId]
            val next = when (previous?.dateIso) {
                today.toString() -> previous.copy(count = previous.count + 1)
                today.minusDays(1).toString() -> JapaDay(today.toString(), 1, previous.streak + 1)
                else -> JapaDay(today.toString(), 1, 1)
            }
            todayCount = next.count
            val japaHistory = decodeJapaHistory(prefs[Keys.JAPA_HISTORY])
            prefs[Keys.JAPA_HISTORY] = encodeJapaHistory(
                (japaHistory + (today to (japaHistory[today] ?: 0) + 1)).pruned()
            )
            prefs[Keys.JAPA_DAILY] = encodeJapaDaily(daily + (counterId to next))
        }
        return todayCount
    }

    /** Undoes today's repetitions for [counterId] - clears today's count and takes it back off the lifetime total. */
    suspend fun resetJapaToday(counterId: String) {
        context.dataStore.edit { prefs ->
            val daily = decodeJapaDaily(prefs[Keys.JAPA_DAILY])
            val todayCount = daily[counterId]?.todayCount() ?: 0
            if (todayCount == 0) return@edit
            val totals = decodeJapaCounts(prefs[Keys.JAPA_COUNTS])
            val newTotal = ((totals[counterId] ?: 0) - todayCount).coerceAtLeast(0)
            prefs[Keys.JAPA_COUNTS] = encodeJapaCounts(totals + (counterId to newTotal))
            prefs[Keys.JAPA_DAILY] = encodeJapaDaily(daily + (counterId to daily.getValue(counterId).copy(count = 0)))
            val today = LocalDate.now()
            val japaHistory = decodeJapaHistory(prefs[Keys.JAPA_HISTORY])
            prefs[Keys.JAPA_HISTORY] = encodeJapaHistory(
                japaHistory + (today to ((japaHistory[today] ?: 0) - todayCount).coerceAtLeast(0))
            )
        }
    }

    /** Marks a daily-routine step as completed for today; the set clears automatically on a new day. */
    suspend fun markRoutineStepDone(stepId: String) {
        context.dataStore.edit { prefs ->
            val today = todayIso()
            val current = if (prefs[Keys.ROUTINE_DATE] == today) prefs[Keys.ROUTINE_DONE] ?: emptySet() else emptySet()
            prefs[Keys.ROUTINE_DATE] = today
            prefs[Keys.ROUTINE_DONE] = current + stepId

            val history = decodeRoutineHistory(prefs[Keys.ROUTINE_HISTORY])
            prefs[Keys.ROUTINE_HISTORY] = encodeRoutineHistory(
                (history + (LocalDate.now() to DayRoutine(current + stepId, currentModules(prefs)))).pruned()
            )
        }
    }

    /** Replaces the set of routine modules shown on Home's "Your Routine" checklist. */
    suspend fun setRoutineModules(moduleIds: Set<String>) {
        context.dataStore.edit { prefs ->
            prefs[Keys.ROUTINE_MODULES] = moduleIds
            // Keep today's history entry in step, so today's % reflects what's actually turned on.
            val history = decodeRoutineHistory(prefs[Keys.ROUTINE_HISTORY])
            history[LocalDate.now()]?.let { day ->
                prefs[Keys.ROUTINE_HISTORY] = encodeRoutineHistory(history + (LocalDate.now() to day.copy(enabled = moduleIds)))
            }
        }
    }

    suspend fun setNotificationPref(key: NotificationPrefKey, enabled: Boolean) {
        context.dataStore.edit { prefs ->
            val dsKey = when (key) {
                NotificationPrefKey.MORNING -> Keys.NOTIF_MORNING
                NotificationPrefKey.EVENING -> Keys.NOTIF_EVENING
                NotificationPrefKey.MANTRA -> Keys.NOTIF_MANTRA
                NotificationPrefKey.STATUS -> Keys.NOTIF_STATUS
                NotificationPrefKey.FESTIVAL -> Keys.NOTIF_FESTIVAL
            }
            prefs[dsKey] = enabled
        }
    }
}

enum class NotificationPrefKey { MORNING, EVENING, MANTRA, STATUS, FESTIVAL }
