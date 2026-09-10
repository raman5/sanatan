package com.bhakti.app.feature.explore

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.bhakti.app.core.navigation.Routes

private data class ExploreTile(val title: String, val subtitle: String, val route: String)

private val tiles = listOf(
    ExploreTile("Wallpapers", "Deities, festivals, themes and styles", Routes.WALLPAPER_LIST),
    ExploreTile("Bhajans", "Aartis, chalisas and stotras", Routes.BHAJAN_LIST),
    ExploreTile("Mantras", "Chants with meaning and audio", Routes.MANTRA_LIST),
    ExploreTile("WhatsApp Status", "Image, video and text status", Routes.STATUS_LIST)
)

@Composable
fun ExploreScreen(navController: NavHostController, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize().padding(20.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Explore", style = MaterialTheme.typography.displaySmall)
            IconButton(onClick = { navController.navigate(Routes.SEARCH) }) {
                Icon(Icons.Filled.Search, contentDescription = "Search")
            }
        }

        Column(
            modifier = Modifier.padding(top = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            tiles.forEach { tile ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { navController.navigate(tile.route) },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(Modifier.padding(18.dp)) {
                        Text(tile.title, style = MaterialTheme.typography.titleMedium)
                        Text(tile.subtitle, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
