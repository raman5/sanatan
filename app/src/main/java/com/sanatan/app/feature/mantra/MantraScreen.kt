package com.sanatan.app.feature.mantra

// OWNER: Maneesha
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.sanatan.app.core.ui.PlaceholderScreen

@Composable
fun MantraScreen(modifier: Modifier = Modifier) {
    PlaceholderScreen(
        title = "Mantra",
        subtitle = "A library of mantras and stotras with Devanagari text, " +
            "transliteration, meaning, audio playback and a japa counter.",
        owner = "Maneesha",
        modifier = modifier
    )
}
