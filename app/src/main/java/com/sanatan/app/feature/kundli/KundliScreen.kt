package com.sanatan.app.feature.kundli

// OWNER: Raman
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.sanatan.app.core.ui.PlaceholderScreen

@Composable
fun KundliScreen(modifier: Modifier = Modifier) {
    PlaceholderScreen(
        title = "Kundli",
        subtitle = "Birth details in, chart out: lagna, the twelve bhavas, " +
            "planetary positions and the dasha timeline.",
        owner = "Raman",
        modifier = modifier
    )
}
