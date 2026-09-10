package com.bhakti.app.feature.search

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
import androidx.compose.material3.OutlinedTextField
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
import com.bhakti.app.data.repository.SearchResults
import kotlinx.coroutines.delay

@Composable
fun SearchScreen(navController: NavHostController) {
    val container = LocalAppContainer.current
    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<SearchResults?>(null) }

    LaunchedEffect(query) {
        delay(200) // small debounce
        results = container.contentRepository.search(query)
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        IconButton(onClick = { navController.popBackStack() }) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("Search deities, mantras, bhajans, status...") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            if (query.isBlank()) "Popular & Featured" else "Results for \"$query\"",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
        )

        val current = results
        if (current != null) {
            if (current.isEmpty) {
                Text("No matches. Try a deity name like Shiva or Krishna.", style = MaterialTheme.typography.bodyMedium)
            } else {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    if (current.wallpapers.isNotEmpty()) {
                        SearchSection("Wallpapers")
                        current.wallpapers.forEach { w ->
                            SearchRow(w.title, w.deity.displayName) {
                                navController.navigate(Routes.contentDetail(ContentType.WALLPAPER, w.id))
                            }
                        }
                    }
                    if (current.bhajans.isNotEmpty()) {
                        SearchSection("Bhajans")
                        current.bhajans.forEach { b ->
                            SearchRow(b.title, b.deity.displayName) {
                                navController.navigate(Routes.contentDetail(ContentType.BHAJAN, b.id))
                            }
                        }
                    }
                    if (current.mantras.isNotEmpty()) {
                        SearchSection("Mantras")
                        current.mantras.forEach { m ->
                            SearchRow(m.title, m.deity.displayName) {
                                navController.navigate(Routes.contentDetail(ContentType.MANTRA, m.id))
                            }
                        }
                    }
                    if (current.statuses.isNotEmpty()) {
                        SearchSection("WhatsApp Status")
                        current.statuses.forEach { s ->
                            SearchRow(s.title, s.deity.displayName) {
                                navController.navigate(Routes.contentDetail(ContentType.STATUS, s.id))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchSection(title: String) {
    Text(title, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(top = 12.dp, bottom = 4.dp))
}

@Composable
private fun SearchRow(title: String, subtitle: String, onClick: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp)
    ) {
        Text(title, style = MaterialTheme.typography.bodyMedium)
        Text(subtitle, style = MaterialTheme.typography.labelSmall)
    }
}
