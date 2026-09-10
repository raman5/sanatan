package com.bhakti.app.core.session

import android.content.Context
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
    val notifFestival: Boolean
) {
    val isLoggedIn: Boolean get() = user != null
    val hasActiveAccess: Boolean get() =
        subscription.status == SubscriptionStatus.ACTIVE || subscription.status == SubscriptionStatus.TRIAL
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
    }

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
            notifFestival = prefs[Keys.NOTIF_FESTIVAL] ?: true
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
