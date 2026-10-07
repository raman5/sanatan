package com.bhakti.app.core.ui

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate

/**
 * A drawn lotus/mandala flourish standing in for real illustration assets -
 * pure vector, no drawables needed, so it scales cleanly and tints with
 * whatever accent color the surface underneath needs.
 */
@Composable
fun MandalaMotif(
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    alpha: Float = 0.2f,
    petals: Int = 12
) {
    Canvas(modifier = modifier) {
        val radius = size.minDimension / 2f
        val center = Offset(size.width / 2f, size.height / 2f)
        repeat(petals) { i ->
            val angle = (360f / petals) * i
            rotate(angle, pivot = center) {
                drawOval(
                    color = color.copy(alpha = alpha),
                    topLeft = Offset(center.x - radius * 0.11f, center.y - radius),
                    size = Size(radius * 0.22f, radius * 0.85f)
                )
            }
        }
        drawCircle(color = color.copy(alpha = alpha), radius = radius * 0.16f, center = center)
        drawCircle(
            color = color.copy(alpha = alpha * 0.6f),
            radius = radius * 0.94f,
            center = center,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = radius * 0.02f)
        )
    }
}
