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
import com.bhakti.app.feature.explore.BhajanListScreen
import com.bhakti.app.feature.explore.ContentDetailScreen
import com.bhakti.app.feature.explore.ExploreScreen
import com.bhakti.app.feature.explore.MantraListScreen
import com.bhakti.app.feature.explore.StatusListScreen
import com.bhakti.app.feature.explore.WallpaperListScreen
import com.bhakti.app.feature.favourites.FavouritesScreen
import com.bhakti.app.feature.home.HomeScreen
import com.bhakti.app.feature.payment.PaymentScreen
import com.bhakti.app.feature.paywall.PaywallScreen
import com.bhakti.app.feature.profile.HelpSupportScreen
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
            navController.navigate(pendingDeepLink) { launchSingleTop = true }
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
                                    navController.navigate(destination.route) {
                                        popUpTo(Routes.HOME) { saveState = true }
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
            composable(Routes.EXPLORE) { ExploreScreen(navController) }
            composable(Routes.PROFILE) { ProfileScreen(navController) }

            composable(Routes.SEARCH) { SearchScreen(navController) }
            composable(Routes.FAVOURITES) { FavouritesScreen(navController) }

            composable(Routes.WALLPAPER_LIST) { WallpaperListScreen(navController) }
            composable(Routes.BHAJAN_LIST) { BhajanListScreen(navController) }
            composable(Routes.MANTRA_LIST) { MantraListScreen(navController) }
            composable(Routes.STATUS_LIST) { StatusListScreen(navController) }

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
