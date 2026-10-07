package com.bhakti.app.core.media

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

/**
 * Copies image bytes into the app's cache and hands back a content:// URI
 * another app (WhatsApp, etc.) can actually read - a plain android.resource://
 * URI, or a Bitmap held only in memory, isn't resolvable outside this process.
 */
object ShareImageProvider {

    fun uriFor(context: Context, drawableRes: Int, fileName: String): Uri =
        uriForFile(context, fileName) { file ->
            context.resources.openRawResource(drawableRes).use { input ->
                file.outputStream().use { output -> input.copyTo(output) }
            }
        }

    /** For personalized status art rendered at share-time (see [StatusArtRenderer]). */
    fun uriForBitmap(context: Context, bitmap: Bitmap, fileName: String): Uri =
        uriForFile(context, fileName) { file ->
            file.outputStream().use { output -> bitmap.compress(Bitmap.CompressFormat.JPEG, 92, output) }
        }

    private fun uriForFile(context: Context, fileName: String, write: (File) -> Unit): Uri {
        val dir = File(context.cacheDir, "shared_images").apply { mkdirs() }
        val file = File(dir, "$fileName.jpg")
        write(file)
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }
}
