package com.sanatan.app.core.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Every screen in the app. Adding a destination touches this file,
 * so add yours in one commit and tell the other person.
 */
enum class Destination(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    Home("home", "Home", Icons.Filled.Home),
    Panchang("panchang", "Panchang", Icons.Filled.CalendarMonth),
    Kundli("kundli", "Kundli", Icons.Filled.AutoAwesome),
    Mantra("mantra", "Mantra", Icons.Filled.SelfImprovement),
    Astrologer("astrologer", "Ask", Icons.Filled.Chat);

    companion object {
        val bottomBar = listOf(Home, Panchang, Kundli, Mantra, Astrologer)
        fun fromRoute(route: String?): Destination =
            entries.firstOrNull { it.route == route } ?: Home
    }
}
