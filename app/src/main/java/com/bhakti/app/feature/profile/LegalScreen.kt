package com.bhakti.app.feature.profile

import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

/** Full documents live on the website so they can be updated without an app release. */
object LegalLinks {
    const val PRIVACY_POLICY = "https://bhaktt.com/privacy-policy.html"
    const val TERMS = "https://bhaktt.com/terms.html"
    const val DELETE_ACCOUNT = "https://bhaktt.com/delete-account.html"
}

fun Context.openUrl(url: String) {
    runCatching { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
}

@Composable
fun LegalScreen(navController: NavHostController) {
    val context = LocalContext.current
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

        Text("In short", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 6.dp))
        Text(
            "• Bhakti costs ₹99 a month or ₹599 a year, paid through Razorpay. It renews automatically until you cancel.\n" +
                "• Cancel any time in Profile. You won't be charged again and keep access until the end of the period you paid for.\n" +
                "• Payments for a period that has started aren't refunded. Charges made by mistake are refunded if you email us within 7 days.\n" +
                "• You sign in with Google so your subscription stays linked to you. We get your name and email address, never your payment details.\n" +
                "• Your routine, japa counts and progress stay on this phone.",
            style = MaterialTheme.typography.bodyMedium
        )

        TextButton(onClick = { context.openUrl(LegalLinks.TERMS) }, modifier = Modifier.padding(top = 12.dp)) {
            Text("Read the full Terms of Service")
        }
        TextButton(onClick = { context.openUrl(LegalLinks.PRIVACY_POLICY) }) {
            Text("Read the full Privacy Policy")
        }
        TextButton(onClick = { context.openUrl(LegalLinks.DELETE_ACCOUNT) }) {
            Text("How to delete your account")
        }

        Text(
            "Questions: maneeshanegi30@gmail.com",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 12.dp)
        )
    }
}
