package com.bhakti.app.core.media

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Sets a wallpaper image as the device's actual home/lock screen wallpaper. */
object WallpaperSetter {

    val TARGET_HOME = WallpaperManager.FLAG_SYSTEM
    val TARGET_LOCK = WallpaperManager.FLAG_LOCK
    val TARGET_BOTH = WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK

    suspend fun setAsWallpaper(context: Context, drawableRes: Int, which: Int): Result<Unit> =
        setAsWallpaper(context, BitmapFactory.decodeResource(context.resources, drawableRes), which)

    /** Same as above, for backend-hosted artwork already downloaded to a [Bitmap] - see [BitmapLoader]. */
    suspend fun setAsWallpaper(context: Context, bitmap: Bitmap, which: Int): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                WallpaperManager.getInstance(context).setBitmap(bitmap, null, true, which)
                Unit
            }
        }
}
