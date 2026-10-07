package com.bhakti.app.feature.explore

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.bhakti.app.core.di.LocalAppContainer
import com.bhakti.app.core.media.rememberJapaChanter
import com.bhakti.app.core.ui.IconBadge
import com.bhakti.app.core.ui.JapaCounter
import com.bhakti.app.core.ui.PlaceholderArt
import com.bhakti.app.data.model.Deity
import com.bhakti.app.data.model.naamJapaNameFor

@Composable
fun NaamJapaDetailScreen(navController: NavHostController, deityName: String) {
    val container = LocalAppContainer.current
    val deity = remember(deityName) { Deity.entries.firstOrNull { it.name == deityName } ?: Deity.entries.first() }
    val name = naamJapaNameFor(deity)
    var portraitUrl by remember(deity) { mutableStateOf<String?>(null) }
    var naamAudioUrl by remember(deity) { mutableStateOf<String?>(null) }
    LaunchedEffect(deity) {
        val portrait = container.contentRepository.deityPortraits()[deity]
        portraitUrl = portrait?.primaryUrl
        naamAudioUrl = portrait?.naamJapaAudioUrl
    }
    // On by default; per-visit toggle for anyone who'd rather tap silently.
    var soundOn by remember { mutableStateOf(true) }
    val chanter = rememberJapaChanter(devanagariText = name, recordedUrl = naamAudioUrl)

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        IconButton(onClick = { navController.popBackStack() }) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }

        PlaceholderArt(deity = deity, label = "${deity.displayName} Naam Japa", aspectRatio = 16f / 9f, imageUrl = portraitUrl)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconBadge(icon = Icons.Filled.SelfImprovement, tint = Color(deity.accentHex))
            Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
                Text("${deity.displayName} Naam Japa", style = MaterialTheme.typography.headlineMedium)
                Text(
                    "Repeat the name, not the full mantra",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = { soundOn = !soundOn }) {
                Icon(
                    if (soundOn) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                    contentDescription = if (soundOn) "Turn off chant sound" else "Turn on chant sound"
                )
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 18.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "$name $name $name",
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center
                )
            }
        }

        JapaCounter(
            counterId = "naam-${deity.name}",
            target = 108,
            onTap = { if (soundOn) chanter.chant() },
            routineStepId = "naam-japa"
        )
    }
}
