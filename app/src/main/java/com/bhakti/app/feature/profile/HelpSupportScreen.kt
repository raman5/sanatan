package com.bhakti.app.feature.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@Composable
fun HelpSupportScreen(navController: NavHostController) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        IconButton(onClick = { navController.popBackStack() }) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }
        Text("Help & Support", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(bottom = 12.dp))
        Text(
            "Questions, feedback or content suggestions? " +
                "Write to us at maneeshanegi30@gmail.com and we'll get back to you soon.",
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            "\nFrequently asked\n\n" +
                "• Is Bhakti free? Yes - every feature is free to use right now.\n" +
                "• Why isn't the chant spoken in Hindi? Install the Hindi voice for Google Text-to-Speech " +
                "(Settings > Accessibility > Text-to-speech) and turn the speaker on in Naam Japa or Mantras.\n" +
                "• Where is my progress saved? On this phone. Uninstalling the app or clearing its data resets it.\n" +
                "• Can I choose what's in my routine? Yes - tap the pencil icon on the Your Routine card on Home.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 12.dp)
        )
    }
}
