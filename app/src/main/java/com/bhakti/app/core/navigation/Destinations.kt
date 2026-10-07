package com.bhakti.app.core.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector
import com.bhakti.app.BuildConfig
import com.bhakti.app.data.model.ContentType

/** Every route in the app. Params are encoded positionally, decoded via NavBackStackEntry.arguments. */
object Routes {
    const val SPLASH = "splash"
    const val AUTH = "auth"
    const val OTP = "otp/{phone}"
    fun otp(phone: String) = "otp/$phone"
    const val PAYWALL = "paywall"
    /** After onboarding: the paywall only while subscriptions are switched on, else straight in. */
    val AFTER_ONBOARDING: String get() = if (BuildConfig.SUBSCRIPTIONS_ENABLED) PAYWALL else HOME
    const val PAYMENT = "payment/{planId}"
    fun payment(planId: String) = "payment/$planId"

    const val HOME = "home"
    const val ROUTINE_SETTINGS = "home/routine-settings"
    const val EXPLORE = "explore"
    const val PROGRESS = "progress"
    const val PROFILE = "profile"

    const val SEARCH = "search"
    const val FAVOURITES = "favourites"

    const val WALLPAPER_LIST = "explore/wallpapers"
    /** Route pattern with an optional deity filter; plain [WALLPAPER_LIST] still matches it. */
    const val WALLPAPER_LIST_PATTERN = "explore/wallpapers?deity={deity}"
    fun wallpaperList(deityName: String) = "explore/wallpapers?deity=$deityName"
    const val MANTRA_LIST = "explore/mantras"
    const val STATUS_LIST = "explore/statuses"

    const val NAAM_JAPA_LIST = "explore/naam-japa"
    const val NAAM_JAPA_DETAIL = "explore/naam-japa/{deity}"
    fun naamJapaDetail(deityName: String) = "explore/naam-japa/$deityName"

    const val POOJA_DEITY_SELECT = "pooja"
    const val POOJA_TEMPLE = "pooja/{deity}"
    fun poojaTemple(deityName: String) = "pooja/$deityName"

    const val CONTENT_DETAIL = "content/{type}/{id}"
    fun contentDetail(type: ContentType, id: String) = "content/${type.name}/$id"

    const val NOTIFICATION_SETTINGS = "profile/notifications"
    const val LANGUAGE = "profile/language"
    const val HELP_SUPPORT = "profile/help"
    const val LEGAL = "profile/legal"

    /** Routes that show the bottom nav bar. */
    val bottomBarRoutes = setOf(HOME, EXPLORE, PROGRESS, PROFILE)
}

/** Bottom-nav destinations: Home / Explore / Progress / Profile. */
enum class BottomDestination(val route: String, val label: String, val icon: ImageVector) {
    Home(Routes.HOME, "Home", Icons.Filled.Home),
    Explore(Routes.EXPLORE, "Explore", Icons.Filled.Explore),
    Progress(Routes.PROGRESS, "Progress", Icons.Filled.Insights),
    Profile(Routes.PROFILE, "Profile", Icons.Filled.Person);

    companion object {
        val all = listOf(Home, Explore, Progress, Profile)
        fun fromRoute(route: String?): BottomDestination? = entries.firstOrNull { it.route == route }
    }
}
