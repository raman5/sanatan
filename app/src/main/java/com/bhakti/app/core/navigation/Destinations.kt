package com.bhakti.app.core.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector
import com.bhakti.app.data.model.ContentType

/** Every route in the app. Params are encoded positionally, decoded via NavBackStackEntry.arguments. */
object Routes {
    const val SPLASH = "splash"
    const val AUTH = "auth"
    const val OTP = "otp/{phone}"
    fun otp(phone: String) = "otp/$phone"
    const val PAYWALL = "paywall"
    const val PAYMENT = "payment/{planId}"
    fun payment(planId: String) = "payment/$planId"

    const val HOME = "home"
    const val EXPLORE = "explore"
    const val PROFILE = "profile"

    const val SEARCH = "search"
    const val FAVOURITES = "favourites"

    const val WALLPAPER_LIST = "explore/wallpapers"
    const val BHAJAN_LIST = "explore/bhajans"
    const val MANTRA_LIST = "explore/mantras"
    const val STATUS_LIST = "explore/statuses"

    const val CONTENT_DETAIL = "content/{type}/{id}"
    fun contentDetail(type: ContentType, id: String) = "content/${type.name}/$id"

    const val NOTIFICATION_SETTINGS = "profile/notifications"
    const val LANGUAGE = "profile/language"
    const val HELP_SUPPORT = "profile/help"
    const val LEGAL = "profile/legal"

    /** Routes that show the bottom nav bar. */
    val bottomBarRoutes = setOf(HOME, EXPLORE, PROFILE)
}

/** The three bottom-nav destinations from the PRD (Home / Explore / Profile). */
enum class BottomDestination(val route: String, val label: String, val icon: ImageVector) {
    Home(Routes.HOME, "Home", Icons.Filled.Home),
    Explore(Routes.EXPLORE, "Explore", Icons.Filled.Explore),
    Profile(Routes.PROFILE, "Profile", Icons.Filled.Person);

    companion object {
        val all = listOf(Home, Explore, Profile)
        fun fromRoute(route: String?): BottomDestination? = entries.firstOrNull { it.route == route }
    }
}
