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
fun LegalScreen(navController: NavHostController) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        IconButton(onClick = { navController.popBackStack() }) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }
        Text("Terms & Privacy Policy", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(bottom = 12.dp))
        Text(
            "Placeholder legal copy. Replace with reviewed Terms of Service and " +
                "Privacy Policy text - including UPI AutoPay mandate terms, cancellation " +
                "rights, refund policy for the ₹1 trial charge, and data handling " +
                "disclosures - before this app is published.",
            style = MaterialTheme.typography.bodyMedium
        )

        Text(
            "Audio credits",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
        )
        Text(
            "\"Shri Vyadeshwar Aarti\" by Gsmodak, via Wikimedia Commons, " +
                "licensed under CC BY-SA 3.0 (creativecommons.org/licenses/by-sa/3.0). " +
                "Used for the Shiva bhajan.",
            style = MaterialTheme.typography.bodySmall
        )
        Text(
            "\"Aarti Shri Radha Govind Dev Ji\" (Jaipur), via Wikimedia Commons, " +
                "dedicated to the public domain under CC0 1.0. Used for the Radha bhajan.",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}
