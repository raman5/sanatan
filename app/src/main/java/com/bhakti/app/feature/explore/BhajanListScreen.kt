package com.bhakti.app.feature.explore

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.bhakti.app.core.di.LocalAppContainer
import com.bhakti.app.core.navigation.Routes
import com.bhakti.app.core.ui.DeityFilterRow
import com.bhakti.app.data.model.Bhajan
import com.bhakti.app.data.model.ContentType
import com.bhakti.app.data.model.Deity

@Composable
fun BhajanListScreen(navController: NavHostController) {
    val container = LocalAppContainer.current
    var selectedDeity by remember { mutableStateOf<Deity?>(null) }
    var bhajans by remember { mutableStateOf<List<Bhajan>>(emptyList()) }

    LaunchedEffect(selectedDeity) {
        bhajans = container.contentRepository.bhajans(selectedDeity).sortedByDescending { it.playCount }
    }

    Column(Modifier.fillMaxSize().padding(top = 12.dp, start = 16.dp, end = 16.dp)) {
        IconButton(onClick = { navController.popBackStack() }) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }
        Text("Bhajans", style = MaterialTheme.typography.headlineMedium)
        DeityFilterRow(
            selected = selectedDeity,
            onSelect = { selectedDeity = it },
            modifier = Modifier.padding(vertical = 12.dp)
        )

        LazyColumn {
            items(bhajans, key = { it.id }) { bhajan ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                        .clickable { navController.navigate(Routes.contentDetail(ContentType.BHAJAN, bhajan.id)) },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(Modifier.padding(14.dp)) {
                        Text(bhajan.title, style = MaterialTheme.typography.titleMedium)
                        Text(
                            "${bhajan.category} • ${bhajan.durationSec / 60}:${(bhajan.durationSec % 60).toString().padStart(2, '0')} • ${bhajan.playCount} plays",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }
    }
}
