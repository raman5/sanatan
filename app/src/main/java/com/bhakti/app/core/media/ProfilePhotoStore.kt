package com.bhakti.app.core.media

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File

/**
 * A single profile photo the user can set for personalizing shared status
 * art. Copied into app-internal storage immediately on pick so it survives
 * app restarts without needing persistable URI permissions.
 */
object ProfilePhotoStore {
    private const val FILE_NAME = "profile_photo.jpg"

    private fun file(context: Context) = File(context.filesDir, FILE_NAME)

    fun exists(context: Context): Boolean = file(context).exists()

    fun save(context: Context, sourceUri: Uri): Boolean = runCatching {
        context.contentResolver.openInputStream(sourceUri)?.use { input ->
            file(context).outputStream().use { output -> input.copyTo(output) }
        } ?: error("Could not open the picked photo")
    }.isSuccess

    fun loadBitmap(context: Context): Bitmap? =
        if (exists(context)) BitmapFactory.decodeFile(file(context).path) else null
}
