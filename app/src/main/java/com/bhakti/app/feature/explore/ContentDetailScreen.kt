package com.bhakti.app.feature.explore

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.bhakti.app.core.di.LocalAppContainer
import com.bhakti.app.core.ui.PlaceholderArt
import com.bhakti.app.core.ui.SimulatedAudioPlayer
import com.bhakti.app.data.model.Bhajan
import com.bhakti.app.data.model.ContentType
import com.bhakti.app.data.model.DevotionalContent
import com.bhakti.app.data.model.Mantra
import com.bhakti.app.data.model.Wallpaper
import com.bhakti.app.data.model.WhatsAppStatus
import kotlinx.coroutines.launch

@Composable
fun ContentDetailScreen(navController: NavHostController, typeName: String, id: String) {
    val container = LocalAppContainer.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val contentType = remember(typeName) { ContentType.valueOf(typeName) }

    var item by remember { mutableStateOf<DevotionalContent?>(null) }
    LaunchedEffect(contentType, id) { item = container.contentRepository.byId(contentType, id) }

    val session by container.sessionManager.state.collectAsState(initial = null)
    val isFavourite = session?.favouriteIds?.contains(id) == true

    fun shareText(text: String) {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(sendIntent, "Share via"))
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Row {
                IconButton(onClick = { scope.launch { container.sessionManager.toggleFavourite(id) } }) {
                    Icon(
                        imageVector = if (isFavourite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = "Favourite"
                    )
                }
            }
        }

        when (val current = item) {
            null -> Text("Loading...", style = MaterialTheme.typography.bodyMedium)
            is Wallpaper -> WallpaperDetail(current, onShare = { shareText("Jai ${current.deity.displayName}! Check out this Bhakti wallpaper: ${current.title}") })
            is Bhajan -> BhajanDetail(current, onShare = { shareText("Listening to ${current.title} on Bhakti.") })
            is Mantra -> MantraDetail(current, onShare = { shareText("${current.devanagari}\n${current.transliteration}\n- via Bhakti app") })
            is WhatsAppStatus -> StatusDetail(current, onShare = { shareText(current.caption) })
        }
    }
}

@Composable
private fun WallpaperDetail(wallpaper: Wallpaper, onShare: () -> Unit) {
    PlaceholderArt(deity = wallpaper.deity, label = wallpaper.title, aspectRatio = 9f / 16f)
    Text(wallpaper.title, style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(top = 12.dp))
    Text(
        "${wallpaper.deity.displayName} • ${wallpaper.category} • ${wallpaper.theme} • ${wallpaper.style}",
        style = MaterialTheme.typography.bodyMedium
    )
    Text("Resolution: ${wallpaper.resolution} • ${wallpaper.language}", style = MaterialTheme.typography.labelSmall)
    DetailActionsRow(onShare = onShare, showDownload = true)
}

@Composable
private fun BhajanDetail(bhajan: Bhajan, onShare: () -> Unit) {
    Text(bhajan.title, style = MaterialTheme.typography.headlineMedium)
    Text(
        "${bhajan.deity.displayName} • ${bhajan.category}${bhajan.singer?.let { " • $it" } ?: ""}",
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
    )
    SimulatedAudioPlayer(
        durationSec = bhajan.durationSec,
        onStarted = { /* TODO: send play-start analytics event once a backend exists */ },
        onCompleted = { /* TODO: send play-completion analytics event once a backend exists */ }
    )
    DetailActionsRow(onShare = onShare, showDownload = false)
}

@Composable
private fun MantraDetail(mantra: Mantra, onShare: () -> Unit) {
    Text(mantra.title, style = MaterialTheme.typography.headlineMedium)
    Text(
        "${mantra.deity.displayName} • ${mantra.purpose} • ${mantra.category}",
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
    )
    Text(mantra.devanagari, style = MaterialTheme.typography.headlineMedium)
    Text(mantra.transliteration, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 6.dp))
    mantra.meaning?.let {
        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
        Text("Meaning", style = MaterialTheme.typography.titleMedium)
        Text(it, style = MaterialTheme.typography.bodyMedium)
    }
    Text(
        "Recommended: ${mantra.recommendedCount} repetitions",
        style = MaterialTheme.typography.labelSmall,
        modifier = Modifier.padding(top = 8.dp)
    )
    if (mantra.hasAudio) {
        SimulatedAudioPlayer(
            durationSec = mantra.durationSec,
            modifier = Modifier.padding(top = 12.dp),
            onStarted = { /* TODO: track mantra listen-start for habit features */ },
            onCompleted = { /* TODO: track mantra completion for habit/streak features */ }
        )
    }
    DetailActionsRow(onShare = onShare, showDownload = false)
}

@Composable
private fun StatusDetail(status: WhatsAppStatus, onShare: () -> Unit) {
    PlaceholderArt(deity = status.deity, label = status.title, aspectRatio = 3f / 4f)
    Text(status.title, style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(top = 12.dp))
    Text("${status.deity.displayName} • ${status.category} • ${status.mediaType.name.lowercase()}", style = MaterialTheme.typography.bodyMedium)
    Text(status.caption, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp))
    DetailActionsRow(onShare = onShare, showDownload = true)
}

@Composable
private fun DetailActionsRow(onShare: () -> Unit, showDownload: Boolean) {
    Row(modifier = Modifier.padding(top = 16.dp)) {
        if (showDownload) {
            IconButton(onClick = { /* TODO: persist real media once CMS assets exist */ }) {
                Icon(Icons.Filled.Download, contentDescription = "Download")
            }
        }
        IconButton(onClick = onShare) {
            Icon(Icons.Filled.Share, contentDescription = "Share")
        }
    }
}
