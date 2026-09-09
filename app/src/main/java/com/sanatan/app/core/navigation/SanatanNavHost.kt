package com.sanatan.app.core.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sanatan.app.feature.astrologer.AstrologerScreen
import com.sanatan.app.feature.home.HomeScreen
import com.sanatan.app.feature.kundli.KundliScreen
import com.sanatan.app.feature.mantra.MantraScreen
import com.sanatan.app.feature.panchang.PanchangScreen

@Composable
fun SanatanApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val current = Destination.fromRoute(backStackEntry?.destination?.route)

    Scaffold(
        bottomBar = {
            NavigationBar {
                Destination.bottomBar.forEach { destination ->
                    NavigationBarItem(
                        selected = current == destination,
                        onClick = {
                            if (current != destination) {
                                navController.navigate(destination.route) {
                                    popUpTo(Destination.Home.route) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = { Icon(destination.icon, contentDescription = destination.label) },
                        label = { Text(destination.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Destination.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Destination.Home.route) { HomeScreen() }
            composable(Destination.Panchang.route) { PanchangScreen() }
            composable(Destination.Kundli.route) { KundliScreen() }
            composable(Destination.Mantra.route) { MantraScreen() }
            composable(Destination.Astrologer.route) { AstrologerScreen() }
        }
    }
}
