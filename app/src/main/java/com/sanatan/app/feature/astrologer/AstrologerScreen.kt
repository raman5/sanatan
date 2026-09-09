package com.sanatan.app.feature.astrologer

// OWNER: Raman
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.sanatan.app.core.ui.PlaceholderScreen

@Composable
fun AstrologerScreen(modifier: Modifier = Modifier) {
    PlaceholderScreen(
        title = "Ask",
        subtitle = "Chat with the AI astrologer. It reads the user's saved birth " +
            "chart and answers questions in plain language.",
        owner = "Raman",
        modifier = modifier
    )
}
