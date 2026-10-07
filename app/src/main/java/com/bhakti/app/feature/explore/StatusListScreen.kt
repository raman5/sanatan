package com.bhakti.app.feature.explore

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import com.bhakti.app.core.ui.DeityFilterRow
import com.bhakti.app.core.ui.PlaceholderArt
import com.bhakti.app.data.model.ContentType
import com.bhakti.app.data.model.Deity
import com.bhakti.app.data.model.WhatsAppStatus

@Composable
fun StatusListScreen(navController: NavHostController) {
    val container = LocalAppContainer.current
    var selectedDeity by remember { mutableStateOf<Deity?>(null) }
    var statuses by remember { mutableStateOf<List<WhatsAppStatus>>(emptyList()) }

    LaunchedEffect(selectedDeity) {
        statuses = container.contentRepository.statuses(selectedDeity)
    }

    Column(Modifier.fillMaxSize().padding(top = 12.dp)) {
        Column(Modifier.padding(horizontal = 16.dp)) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text("WhatsApp Status", style = MaterialTheme.typography.headlineMedium)
            DeityFilterRow(
                selected = selectedDeity,
                onSelect = { selectedDeity = it },
                modifier = Modifier.padding(vertical = 12.dp)
            )
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(statuses, key = { it.id }) { status ->
                Column(
                    Modifier.clickable {
                        navController.navigate(Routes.contentDetail(ContentType.STATUS, status.id))
                    }
                ) {
                    PlaceholderArt(deity = status.deity, label = status.title, aspectRatio = 3f / 4f, imageVariant = status.imageVariant, imageUrl = status.imageUrl)
                    Text(
                        "${status.title} • ${status.mediaType.name.lowercase()}",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}
