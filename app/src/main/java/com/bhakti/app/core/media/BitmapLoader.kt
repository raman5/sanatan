package com.bhakti.app.core.media

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult

/**
 * Resolves either a bundled drawable or a backend-hosted image URL to an
 * actual [Bitmap] - the one place share/download/set-as-wallpaper need to
 * branch on where the artwork is coming from, so [com.bhakti.app.feature.explore.ContentDetailScreen]
 * and friends don't each reimplement it.
 */
object BitmapLoader {

    suspend fun load(context: Context, imageUrl: String?, fallbackDrawableRes: Int): Bitmap {
        if (imageUrl != null) {
            val request = ImageRequest.Builder(context)
                .data(imageUrl)
                .allowHardware(false) // hardware bitmaps can't be saved/shared/set as wallpaper directly
                .build()
            val result = context.imageLoader.execute(request)
            if (result is SuccessResult) {
                val bitmap = (result.drawable as? android.graphics.drawable.BitmapDrawable)?.bitmap
                if (bitmap != null) return bitmap
            }
            // Falls through to the bundled drawable below on any failure (offline, 404, etc.)
            // rather than crashing the share/download/set-wallpaper action.
        }
        return BitmapFactory.decodeResource(context.resources, fallbackDrawableRes)
    }
}
