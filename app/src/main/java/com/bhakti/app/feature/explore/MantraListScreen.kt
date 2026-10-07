package com.bhakti.app.feature.explore

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.bhakti.app.core.di.LocalAppContainer
import com.bhakti.app.core.navigation.Routes
import com.bhakti.app.core.ui.DeityFilterRow
import com.bhakti.app.core.ui.imageFor
import com.bhakti.app.data.model.ContentType
import com.bhakti.app.data.model.Deity
import com.bhakti.app.data.model.Mantra

@Composable
fun MantraListScreen(navController: NavHostController) {
    val container = LocalAppContainer.current
    var selectedDeity by remember { mutableStateOf<Deity?>(null) }
    var mantras by remember { mutableStateOf<List<Mantra>>(emptyList()) }

    LaunchedEffect(selectedDeity) {
        mantras = container.contentRepository.mantras(selectedDeity)
    }

    Column(Modifier.fillMaxSize().padding(top = 12.dp, start = 16.dp, end = 16.dp)) {
        IconButton(onClick = { navController.popBackStack() }) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }
        Text("Mantras", style = MaterialTheme.typography.headlineMedium)
        DeityFilterRow(
            selected = selectedDeity,
            onSelect = { selectedDeity = it },
            modifier = Modifier.padding(vertical = 12.dp)
        )

        LazyColumn {
            items(mantras, key = { it.id }) { mantra ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                        .clickable { navController.navigate(Routes.contentDetail(ContentType.MANTRA, mantra.id)) },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = imageFor(mantra.deity)),
                            contentDescription = mantra.deity.displayName,
                            contentScale = ContentScale.Crop,
                            alignment = Alignment.TopCenter,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                        )
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 14.dp)
                        ) {
                            Text(mantra.title, style = MaterialTheme.typography.titleMedium)
                            Text(mantra.devanagari, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "${mantra.purpose} • ${mantra.category}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
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
