package com.bhakti.app.feature.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.bhakti.app.core.di.LocalAppContainer
import kotlinx.coroutines.launch

private val languages = listOf("Hindi", "English", "Marathi", "Tamil", "Telugu", "Bengali", "Gujarati", "Kannada")

@Composable
fun LanguageScreen(navController: NavHostController) {
    val container = LocalAppContainer.current
    val scope = rememberCoroutineScope()
    val session by container.sessionManager.state.collectAsState(initial = null)

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        IconButton(onClick = { navController.popBackStack() }) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }
        Text("Language", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(bottom = 12.dp))

        languages.forEach { language ->
            val selected = session?.language == language
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(selected = selected, onClick = { scope.launch { container.sessionManager.setLanguage(language) } })
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selected = selected, onClick = { scope.launch { container.sessionManager.setLanguage(language) } })
                Text(language, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(start = 6.dp))
            }
        }
    }
}
