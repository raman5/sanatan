package com.bhakti.app.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.bhakti.app.data.model.Deity

/**
 * Devotional artwork card for a deity - [imageUrl] from the backend when
 * present (see [com.bhakti.app.data.repository.FirebaseContentRepository]),
 * else the bundled drawable via [imageFor] - with a bottom scrim so the
 * title stays legible over either. Every wallpaper, mantra and status
 * card renders one of these.
 */
@Composable
fun PlaceholderArt(
    deity: Deity,
    label: String,
    modifier: Modifier = Modifier,
    aspectRatio: Float = 9f / 16f,
    imageVariant: Int = 0,
    imageUrl: String? = null
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(aspectRatio)
            .clip(RoundedCornerShape(16.dp))
    ) {
        DeityArtImage(
            url = imageUrl,
            fallbackRes = imageFor(deity, imageVariant),
            contentDescription = "${deity.displayName} devotional artwork",
            contentScale = ContentScale.Crop,
            alignment = Alignment.TopCenter,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.65f))
                    )
                )
                .padding(12.dp)
        ) {
            Text(
                text = label,
                color = Color.White,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}
