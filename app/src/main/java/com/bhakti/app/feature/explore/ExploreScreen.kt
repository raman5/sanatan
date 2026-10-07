package com.bhakti.app.feature.explore

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.bhakti.app.core.navigation.Routes
import com.bhakti.app.core.ui.IconBadge
import com.bhakti.app.ui.theme.Marigold
import com.bhakti.app.ui.theme.SaffronDeep
import com.bhakti.app.ui.theme.Vermilion

private data class ExploreTile(val title: String, val subtitle: String, val route: String, val icon: ImageVector, val tint: Color)

private val tiles = listOf(
    // Same order as Home's routine (feature/home/RoutineModules.kt) - keep in sync.
    ExploreTile("Daily Pooja", "A virtual shrine - bell, milk, water, flowers", Routes.POOJA_DEITY_SELECT, Icons.Filled.Spa, Vermilion),
    ExploreTile("Naam Japa", "Repeat a deity's name, mala style", Routes.NAAM_JAPA_LIST, Icons.Filled.Repeat, Marigold),
    ExploreTile("Mantras", "Chants with meaning and audio", Routes.MANTRA_LIST, Icons.Filled.SelfImprovement, Vermilion),
    ExploreTile("WhatsApp Status", "Image, video and text status", Routes.STATUS_LIST, Icons.AutoMirrored.Filled.Chat, SaffronDeep),
    ExploreTile("Wallpapers", "Deities, festivals, themes and styles", Routes.WALLPAPER_LIST, Icons.Filled.Wallpaper, Marigold)
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
            modifier = Modifier.padding(top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            tiles.forEach { tile ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { navController.navigate(tile.route) },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconBadge(icon = tile.icon, tint = tile.tint)
                        Column(modifier = Modifier.weight(1f).padding(horizontal = 14.dp)) {
                            Text(tile.title, style = MaterialTheme.typography.titleMedium)
                            Text(tile.subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
