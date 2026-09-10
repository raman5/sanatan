package com.bhakti.app.core.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay

/**
 * There is no audio backend yet (no CMS-hosted files), so playback is
 * simulated with a timer-driven progress bar. Play/pause/seek state and the
 * start/completion callbacks are real - only the audio decoding is stubbed,
 * which is exactly the seam a media player (e.g. Media3 ExoPlayer) plugs
 * into later.
 */
@Composable
fun SimulatedAudioPlayer(
    durationSec: Int,
    modifier: Modifier = Modifier,
    onStarted: () -> Unit = {},
    onCompleted: () -> Unit = {}
) {
    var isPlaying by remember { mutableStateOf(false) }
    var progressSec by remember { mutableFloatStateOf(0f) }
    var hasStarted by remember { mutableStateOf(false) }
    var hasCompleted by remember { mutableStateOf(false) }

    LaunchedEffect(isPlaying) {
        while (isPlaying && progressSec < durationSec) {
            delay(250)
            progressSec = (progressSec + 0.25f).coerceAtMost(durationSec.toFloat())
            if (progressSec >= durationSec) {
                isPlaying = false
                if (!hasCompleted) {
                    hasCompleted = true
                    onCompleted()
                }
            }
        }
    }

    Column(modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = {
                if (!hasStarted) {
                    hasStarted = true
                    onStarted()
                }
                if (progressSec >= durationSec) {
                    progressSec = 0f
                    hasCompleted = false
                }
                isPlaying = !isPlaying
            }) {
                Icon(
                    imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play"
                )
            }
            Slider(
                value = progressSec,
                onValueChange = { progressSec = it.coerceIn(0f, durationSec.toFloat()) },
                valueRange = 0f..durationSec.coerceAtLeast(1).toFloat(),
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "${formatDuration(progressSec.toInt())} / ${formatDuration(durationSec)}",
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

private fun formatDuration(totalSec: Int): String {
    val m = totalSec / 60
    val s = totalSec % 60
    return "%d:%02d".format(m, s)
}
