package com.bhakti.app.feature.favourites

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.bhakti.app.core.di.LocalAppContainer
import com.bhakti.app.core.navigation.Routes
import com.bhakti.app.data.model.ContentType
import com.bhakti.app.data.repository.FavouriteContent

@Composable
fun FavouritesScreen(navController: NavHostController) {
    val container = LocalAppContainer.current
    var favourites by remember { mutableStateOf<FavouriteContent?>(null) }

    LaunchedEffect(Unit) { favourites = container.favouritesRepository.favourites() }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        IconButton(onClick = { navController.popBackStack() }) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }
        Text("Favourites", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(bottom = 12.dp))

        val current = favourites
        if (current == null) {
            Text("Loading...", style = MaterialTheme.typography.bodyMedium)
        } else if (current.isEmpty) {
            Text(
                "Nothing favourited yet. Tap the heart on any wallpaper, mantra or status to save it here.",
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                current.wallpapers.forEach { w ->
                    FavouriteRow(w.title, w.deity.displayName) { navController.navigate(Routes.contentDetail(ContentType.WALLPAPER, w.id)) }
                }
                current.mantras.forEach { m ->
                    FavouriteRow(m.title, m.deity.displayName) { navController.navigate(Routes.contentDetail(ContentType.MANTRA, m.id)) }
                }
                current.statuses.forEach { s ->
                    FavouriteRow(s.title, s.deity.displayName) { navController.navigate(Routes.contentDetail(ContentType.STATUS, s.id)) }
                }
            }
        }
    }
}

@Composable
private fun FavouriteRow(title: String, subtitle: String, onClick: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp)
    ) {
        Text(title, style = MaterialTheme.typography.bodyMedium)
        Text(subtitle, style = MaterialTheme.typography.labelSmall)
    }
}
