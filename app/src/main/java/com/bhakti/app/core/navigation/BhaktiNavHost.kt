package com.bhakti.app.core.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.bhakti.app.feature.auth.AuthScreen
import com.bhakti.app.feature.auth.OtpScreen
import com.bhakti.app.feature.explore.ContentDetailScreen
import com.bhakti.app.feature.explore.ExploreScreen
import com.bhakti.app.feature.explore.MantraListScreen
import com.bhakti.app.feature.explore.NaamJapaDetailScreen
import com.bhakti.app.feature.explore.NaamJapaListScreen
import com.bhakti.app.feature.explore.StatusListScreen
import com.bhakti.app.feature.explore.WallpaperListScreen
import com.bhakti.app.feature.favourites.FavouritesScreen
import com.bhakti.app.feature.home.HomeScreen
import com.bhakti.app.feature.home.RoutineSettingsScreen
import com.bhakti.app.feature.payment.PaymentScreen
import com.bhakti.app.feature.paywall.PaywallScreen
import com.bhakti.app.feature.pooja.PoojaDeitySelectScreen
import com.bhakti.app.feature.pooja.PoojaTempleScreen
import com.bhakti.app.feature.profile.HelpSupportScreen
import com.bhakti.app.feature.progress.ProgressScreen
import com.bhakti.app.feature.profile.LanguageScreen
import com.bhakti.app.feature.profile.LegalScreen
import com.bhakti.app.feature.profile.NotificationSettingsScreen
import com.bhakti.app.feature.profile.ProfileScreen
import com.bhakti.app.feature.search.SearchScreen
import com.bhakti.app.feature.splash.SplashScreen

@Composable
fun BhaktiApp(
    pendingDeepLink: String? = null,
    onDeepLinkConsumed: () -> Unit = {}
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = currentRoute in Routes.bottomBarRoutes

    LaunchedEffect(pendingDeepLink) {
        if (pendingDeepLink != null) {
            // A notification scheduled by an older app version can carry a route that has
            // since been removed (e.g. the old Bhajans screen) - ignore it rather than crash.
            runCatching { navController.navigate(pendingDeepLink) { launchSingleTop = true } }
            onDeepLinkConsumed()
        }
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    BottomDestination.all.forEach { destination ->
                        NavigationBarItem(
                            selected = currentRoute == destination.route,
                            onClick = {
                                if (currentRoute != destination.route) {
                                    if (destination == BottomDestination.Home) {
                                        // Home is the base of the tab stack - pop straight back to
                                        // it rather than relying on popUpTo+navigate landing on the
                                        // same route we're popping to, which was unreliable.
                                        val poppedToHome = navController.popBackStack(Routes.HOME, inclusive = false)
                                        if (!poppedToHome) {
                                            navController.navigate(Routes.HOME) { launchSingleTop = true }
                                        }
                                    } else {
                                        navController.navigate(destination.route) {
                                            popUpTo(Routes.HOME) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                }
                            },
                            icon = { Icon(destination.icon, contentDescription = destination.label) },
                            label = { Text(destination.label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.SPLASH,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.SPLASH) { SplashScreen(navController) }
            composable(Routes.AUTH) { AuthScreen(navController) }
            composable(
                Routes.OTP,
                arguments = listOf(navArgument("phone") { type = NavType.StringType })
            ) { entry ->
                OtpScreen(navController, phone = entry.arguments?.getString("phone").orEmpty())
            }
            composable(Routes.PAYWALL) { PaywallScreen(navController) }
            composable(
                Routes.PAYMENT,
                arguments = listOf(navArgument("planId") { type = NavType.StringType })
            ) { entry ->
                PaymentScreen(navController, planId = entry.arguments?.getString("planId").orEmpty())
            }

            composable(Routes.HOME) { HomeScreen(navController) }
            composable(Routes.ROUTINE_SETTINGS) { RoutineSettingsScreen(navController) }
            composable(Routes.EXPLORE) { ExploreScreen(navController) }
            composable(Routes.PROGRESS) { ProgressScreen(navController) }
            composable(Routes.PROFILE) { ProfileScreen(navController) }

            composable(Routes.SEARCH) { SearchScreen(navController) }
            composable(Routes.FAVOURITES) { FavouritesScreen(navController) }

            composable(
                Routes.WALLPAPER_LIST_PATTERN,
                arguments = listOf(
                    navArgument("deity") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { entry ->
                WallpaperListScreen(
                    navController,
                    initialDeity = entry.arguments?.getString("deity")?.let { name ->
                        com.bhakti.app.data.model.Deity.entries.firstOrNull { it.name == name }
                    }
                )
            }
            composable(Routes.MANTRA_LIST) { MantraListScreen(navController) }
            composable(Routes.STATUS_LIST) { StatusListScreen(navController) }
            composable(Routes.NAAM_JAPA_LIST) { NaamJapaListScreen(navController) }
            composable(
                Routes.NAAM_JAPA_DETAIL,
                arguments = listOf(navArgument("deity") { type = NavType.StringType })
            ) { entry ->
                NaamJapaDetailScreen(
                    navController,
                    deityName = entry.arguments?.getString("deity").orEmpty()
                )
            }
            composable(Routes.POOJA_DEITY_SELECT) { PoojaDeitySelectScreen(navController) }
            composable(
                Routes.POOJA_TEMPLE,
                arguments = listOf(navArgument("deity") { type = NavType.StringType })
            ) { entry ->
                PoojaTempleScreen(
                    navController,
                    deityName = entry.arguments?.getString("deity").orEmpty()
                )
            }

            composable(
                Routes.CONTENT_DETAIL,
                arguments = listOf(
                    navArgument("type") { type = NavType.StringType },
                    navArgument("id") { type = NavType.StringType }
                )
            ) { entry ->
                ContentDetailScreen(
                    navController,
                    typeName = entry.arguments?.getString("type").orEmpty(),
                    id = entry.arguments?.getString("id").orEmpty()
                )
            }

            composable(Routes.NOTIFICATION_SETTINGS) { NotificationSettingsScreen(navController) }
            composable(Routes.LANGUAGE) { LanguageScreen(navController) }
            composable(Routes.HELP_SUPPORT) { HelpSupportScreen(navController) }
            composable(Routes.LEGAL) { LegalScreen(navController) }
        }
    }
}
