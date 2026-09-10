package com.bhakti.app.feature.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.bhakti.app.core.di.LocalAppContainer
import com.bhakti.app.core.navigation.Routes
import com.bhakti.app.core.session.SessionState
import com.bhakti.app.core.ui.PlaceholderArt
import com.bhakti.app.core.ui.SectionCard
import com.bhakti.app.data.model.Bhajan
import com.bhakti.app.data.model.ContentType
import com.bhakti.app.data.model.Mantra
import com.bhakti.app.data.model.Wallpaper
import com.bhakti.app.data.model.WhatsAppStatus
import com.bhakti.app.notifications.syncScheduledReminders
import kotlinx.coroutines.flow.first
import java.time.LocalTime

private data class HomeContent(
    val wallpaper: Wallpaper,
    val mantra: Mantra,
    val bhajan: Bhajan,
    val status: WhatsAppStatus
)

@Composable
fun HomeScreen(navController: NavHostController, modifier: Modifier = Modifier) {
    val container = LocalAppContainer.current
    var content by remember { mutableStateOf<HomeContent?>(null) }
    var session by remember { mutableStateOf<SessionState?>(null) }

    LaunchedEffect(Unit) {
        content = HomeContent(
            wallpaper = container.contentRepository.featuredWallpaper(),
            mantra = container.contentRepository.mantraOfTheDay(),
            bhajan = container.contentRepository.bhajanOfTheDay(),
            status = container.contentRepository.statusOfTheDay()
        )
    }
    LaunchedEffect(Unit) {
        container.sessionManager.state.collect { session = it }
    }
    LaunchedEffect(Unit) {
        // Reminder toggles default to on, so reconcile AlarmManager here rather
        // than only when the user visits the Notifications settings screen.
        val initialSession = container.sessionManager.state.first()
        syncScheduledReminders(initialSession, container.contentRepository, container.notificationScheduler)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(greeting(), style = MaterialTheme.typography.displaySmall)
                Text(
                    session?.user?.displayName?.takeIf { it.isNotBlank() } ?: "Welcome back, Bhakt",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            IconButton(onClick = { navController.navigate(Routes.SEARCH) }) {
                Icon(Icons.Filled.Search, contentDescription = "Search")
            }
        }

        val current = content
        if (current == null) {
            Text("Loading today's darshan...", style = MaterialTheme.typography.bodyMedium)
        } else {
            SectionCard(title = "Today's Featured Deity") {
                PlaceholderArt(
                    deity = current.wallpaper.deity,
                    label = current.wallpaper.title,
                    aspectRatio = 16f / 9f,
                    modifier = Modifier
                        .padding(top = 10.dp)
                        .clickable {
                            navController.navigate(Routes.contentDetail(ContentType.WALLPAPER, current.wallpaper.id))
                        }
                )
            }

            SectionCard(
                title = "Today's Mantra",
                modifier = Modifier.clickable {
                    navController.navigate(Routes.contentDetail(ContentType.MANTRA, current.mantra.id))
                }
            ) {
                Text(current.mantra.devanagari, style = MaterialTheme.typography.titleMedium)
                Text(current.mantra.transliteration, style = MaterialTheme.typography.bodyMedium)
            }

            SectionCard(
                title = "Today's Aarti / Bhajan",
                modifier = Modifier.clickable {
                    navController.navigate(Routes.contentDetail(ContentType.BHAJAN, current.bhajan.id))
                }
            ) {
                Text(current.bhajan.title, style = MaterialTheme.typography.bodyMedium)
            }

            SectionCard(
                title = "Daily Devotional Wallpaper",
                modifier = Modifier.clickable {
                    navController.navigate(Routes.contentDetail(ContentType.WALLPAPER, current.wallpaper.id))
                }
            ) {
                Text(current.wallpaper.title, style = MaterialTheme.typography.bodyMedium)
            }

            SectionCard(
                title = "Daily WhatsApp Status",
                modifier = Modifier.clickable {
                    navController.navigate(Routes.contentDetail(ContentType.STATUS, current.status.id))
                }
            ) {
                Text(current.status.title, style = MaterialTheme.typography.bodyMedium)
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = { navController.navigate(Routes.EXPLORE) }, modifier = Modifier.weight(1f)) {
                Icon(Icons.Filled.Explore, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
                Text("Explore")
            }
            OutlinedButton(onClick = { navController.navigate(Routes.FAVOURITES) }, modifier = Modifier.weight(1f)) {
                Icon(Icons.Filled.Favorite, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
                Text("Favourites")
            }
        }
    }
}

private fun greeting(): String {
    val hour = LocalTime.now().hour
    return when {
        hour < 12 -> "Good Morning"
        hour < 17 -> "Good Afternoon"
        else -> "Good Evening"
    }
}
