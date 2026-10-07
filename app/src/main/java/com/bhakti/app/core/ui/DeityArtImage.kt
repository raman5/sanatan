package com.bhakti.app.core.ui

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage

/**
 * Backend artwork with the bundled drawable standing in until it arrives.
 * The bundled art shows instantly as the placeholder - and stays if the
 * download fails or the phone is offline - so a screen never sits blank
 * waiting on the network. With no [url] (local-only content) it's just the
 * bundled drawable.
 */
@Composable
fun DeityArtImage(
    url: String?,
    @DrawableRes fallbackRes: Int,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    alignment: Alignment = Alignment.TopCenter
) {
    val fallback = painterResource(fallbackRes)
    AsyncImage(
        model = url ?: fallbackRes,
        contentDescription = contentDescription,
        placeholder = fallback,
        error = fallback,
        contentScale = contentScale,
        alignment = alignment,
        modifier = modifier
    )
}
