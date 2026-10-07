package com.bhakti.app.core.media

import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Shader

/**
 * Composites a WhatsApp-status-ready image: the deity artwork, a bottom
 * scrim, a bold greeting, an optional short spiritual line, and a
 * personalization bar (photo + name) - one flattened bitmap so preview,
 * download and share are all guaranteed to look identical.
 */
object StatusArtRenderer {

    fun render(
        source: Bitmap,
        greeting: String,
        shloka: String?,
        userName: String,
        userPhoto: Bitmap?
    ): Bitmap {
        // Crop to a taller, WhatsApp-status-like 9:16 frame (full height, centered width crop)
        // rather than the source artwork's native ~3:4 - this is baked into the exported bitmap
        // itself so the preview, download and share all agree on the same larger frame.
        val targetAspect = 9f / 16f
        val cropWidth = (source.height * targetAspect).toInt().coerceIn(1, source.width)
        val cropLeft = (source.width - cropWidth) / 2
        val cropped = Bitmap.createBitmap(source, cropLeft, 0, cropWidth, source.height)
        val output = cropped.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(output)
        val w = output.width.toFloat()
        val h = output.height.toFloat()

        // Bottom scrim so white text stays legible over any artwork.
        val scrimPaint = Paint().apply {
            shader = LinearGradient(0f, h * 0.5f, 0f, h, Color.TRANSPARENT, Color.argb(210, 0, 0, 0), Shader.TileMode.CLAMP)
        }
        canvas.drawRect(0f, h * 0.5f, w, h, scrimPaint)

        // Personalization bar: circular avatar + name, pinned to the bottom.
        val avatarRadius = w * 0.135f
        val marginX = w * 0.05f
        val barBottom = h - h * 0.035f
        val avatarCx = marginX + avatarRadius
        val avatarCy = barBottom - avatarRadius

        val avatarBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
        canvas.drawCircle(avatarCx, avatarCy, avatarRadius, avatarBgPaint)

        if (userPhoto != null) {
            drawCircularBitmap(canvas, userPhoto, avatarCx, avatarCy, avatarRadius - w * 0.008f)
        } else {
            val initialPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.DKGRAY
                textSize = avatarRadius
                textAlign = Paint.Align.CENTER
                isFakeBoldText = true
            }
            val initial = userName.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "B"
            canvas.drawText(initial, avatarCx, avatarCy + avatarRadius * 0.35f, initialPaint)
        }

        val nameSize = w * 0.056f
        val namePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = nameSize
            isFakeBoldText = true
            textAlign = Paint.Align.LEFT
            setShadowLayer(6f, 0f, 1f, Color.argb(160, 0, 0, 0))
        }
        canvas.drawText(
            userName.ifBlank { "Bhakt" },
            avatarCx + avatarRadius + marginX * 0.6f,
            avatarCy + nameSize * 0.35f,
            namePaint
        )

        // Bold greeting, above the personalization bar.
        val greetingSize = w * 0.095f
        val greetingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = greetingSize
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
            setShadowLayer(10f, 0f, 3f, Color.argb(190, 0, 0, 0))
        }
        val greetingY = avatarCy - avatarRadius - h * 0.05f
        canvas.drawText(greeting, w / 2f, greetingY, greetingPaint)

        // Optional short spiritual line above the greeting.
        if (!shloka.isNullOrBlank()) {
            val shlokaSize = w * 0.05f
            val shlokaPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                textSize = shlokaSize
                textAlign = Paint.Align.CENTER
                setShadowLayer(8f, 0f, 2f, Color.argb(170, 0, 0, 0))
            }
            canvas.drawText(shloka, w / 2f, greetingY - greetingSize * 1.35f, shlokaPaint)
        }

        return output
    }

    private fun drawCircularBitmap(canvas: Canvas, bitmap: Bitmap, cx: Float, cy: Float, radius: Float) {
        val shader = BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        val scale = (radius * 2f) / minOf(bitmap.width, bitmap.height)
        val matrix = Matrix().apply {
            setScale(scale, scale)
            postTranslate(cx - bitmap.width * scale / 2f, cy - bitmap.height * scale / 2f)
        }
        shader.setLocalMatrix(matrix)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.shader = shader }
        canvas.drawCircle(cx, cy, radius, paint)
    }
}
