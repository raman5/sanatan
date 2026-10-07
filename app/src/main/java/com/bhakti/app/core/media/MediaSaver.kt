package com.bhakti.app.core.media

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

/** Saves devotional artwork into the device's Pictures gallery. */
object MediaSaver {

    suspend fun saveDrawableToGallery(context: Context, drawableRes: Int, displayName: String): Result<Unit> =
        saveToGallery(context, displayName) { out ->
            context.resources.openRawResource(drawableRes).use { input -> input.copyTo(out) }
        }

    /** For personalized status art rendered at share-time (see [StatusArtRenderer]). */
    suspend fun saveBitmapToGallery(context: Context, bitmap: Bitmap, displayName: String): Result<Unit> =
        saveToGallery(context, displayName) { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
        }

    private suspend fun saveToGallery(context: Context, displayName: String, write: (OutputStream) -> Unit): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    saveViaMediaStore(context, displayName, write)
                } else {
                    saveViaLegacyFile(context, displayName, write)
                }
            }
        }

    private fun saveViaMediaStore(context: Context, displayName: String, write: (OutputStream) -> Unit) {
        val resolver = context.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "$displayName.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/Bhakti")
        }
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            ?: error("Could not create a gallery entry")
        resolver.openOutputStream(uri)?.use(write) ?: error("Could not open the gallery entry for writing")
    }

    private fun saveViaLegacyFile(context: Context, displayName: String, write: (OutputStream) -> Unit) {
        val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val bhaktiDir = File(picturesDir, "Bhakti").apply { mkdirs() }
        val file = File(bhaktiDir, "$displayName.jpg")
        FileOutputStream(file).use(write)
        MediaScannerConnection.scanFile(context, arrayOf(file.absolutePath), arrayOf("image/jpeg"), null)
    }
}
