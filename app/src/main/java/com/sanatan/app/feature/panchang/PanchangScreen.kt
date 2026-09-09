package com.sanatan.app.feature.panchang

// OWNER: Maneesha
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.sanatan.app.core.ui.PlaceholderScreen

@Composable
fun PanchangScreen(modifier: Modifier = Modifier) {
    PlaceholderScreen(
        title = "Panchang",
        subtitle = "Tithi, vaar, nakshatra, yoga and karana for the selected date, " +
            "plus sunrise, sunset and the day's shubh and ashubh muhurat.",
        owner = "Maneesha",
        modifier = modifier
    )
}
