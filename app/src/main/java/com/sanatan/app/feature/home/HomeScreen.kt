package com.sanatan.app.feature.home

// OWNER: Maneesha
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sanatan.app.core.ui.SectionCard
import com.sanatan.app.ui.theme.SanatanTheme

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Sanatan", style = MaterialTheme.typography.displaySmall)
        Text(
            "Aaj ka panchang, mantra aur margdarshan",
            style = MaterialTheme.typography.bodyMedium
        )

        SectionCard(title = "Today's Panchang") {
            Text("Tithi, nakshatra and muhurat go here.")
        }
        SectionCard(title = "Mantra of the Day") {
            Text("A short chant with audio and a japa counter.")
        }
        SectionCard(title = "Ask the Astrologer") {
            Text("Open a chat with the AI astrologer.")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomePreview() {
    SanatanTheme { HomeScreen() }
}
