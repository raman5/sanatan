package com.bhakti.app.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bhakti.app.data.model.Deity

/**
 * Stands in for a real image/video asset from the CMS. Every wallpaper,
 * bhajan cover and status card renders one of these until real media exists.
 */
@Composable
fun PlaceholderArt(
    deity: Deity,
    label: String,
    modifier: Modifier = Modifier,
    aspectRatio: Float = 9f / 16f
) {
    val accent = Color(deity.accentHex)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(aspectRatio)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(listOf(accent.copy(alpha = 0.85f), accent.copy(alpha = 0.35f)))
            )
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Filled.SelfImprovement,
                contentDescription = null,
                tint = Color.White
            )
            Text(
                text = label,
                color = Color.White,
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
    }
}
