package com.bhakti.app.feature.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
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
                "• How much does Bhakti cost? ₹99 a month or ₹599 a year. It renews automatically until you cancel.\n" +
                "• How do I cancel? Profile > Subscription > Cancel subscription. You won't be charged again " +
                "and keep access until the end of the period you paid for.\n" +
                "• I was charged by mistake. Email us within 7 days with the payment details and we'll refund it.\n" +
                "• I got a new phone. Sign in with the same Google account and your subscription carries over.\n" +
                "• Why isn't the chant spoken in Hindi? Install the Hindi voice for Google Text-to-Speech " +
                "(Settings > Accessibility > Text-to-speech) and turn the speaker on in Naam Japa or Mantras.\n" +
                "• Where is my progress saved? On this phone. Uninstalling the app or clearing its data resets it.\n" +
                "• Can I choose what's in my routine? Yes - tap the pencil icon on the Your Routine card on Home.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 12.dp)
        )
    }
}
