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
            "Questions about your subscription, AutoPay mandate or content? " +
                "Write to us at support@bhaktiapp.example and we'll get back within 24 hours.",
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            "\nFrequently asked\n\n" +
                "• How do I cancel AutoPay? Go to Profile > Subscription and cancel the mandate from your UPI app.\n" +
                "• Why was I charged ₹1? That's the trial verification charge before your plan's regular billing begins.\n" +
                "• Can I change my language later? Yes, anytime from Profile > Language.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 12.dp)
        )
    }
}
