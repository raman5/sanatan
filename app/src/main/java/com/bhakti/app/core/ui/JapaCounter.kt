package com.bhakti.app.core.ui

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.bhakti.app.core.di.LocalAppContainer
import com.bhakti.app.ui.theme.Marigold
import com.bhakti.app.ui.theme.Vermilion
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * A tap-to-count japa counter toward [target] repetitions, rendered as a
 * traditional mala. Progress is persisted under [counterId] (a mantra id, a
 * "naam-<deity>" id, etc.) so it survives navigating away - see
 * [com.bhakti.app.core.session.SessionManager.incrementJapaCount].
 */
@Composable
fun JapaCounter(
    counterId: String,
    target: Int,
    modifier: Modifier = Modifier,
    /** Fired on every repetition tap, e.g. to chant the name aloud - see NaamJapaDetailScreen. */
    onTap: () -> Unit = {},
    /** Home routine step (see RoutineModule ids) to tick off once a full mala is completed today. */
    routineStepId: String? = null
) {
    val container = LocalAppContainer.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current
    val session by container.sessionManager.state.collectAsState(initial = null)

    val safeTarget = target.coerceAtLeast(1)
    val totalCount = session?.japaCounts?.get(counterId) ?: 0
    val day = session?.japaDaily?.get(counterId)
    val todayCount = day?.todayCount() ?: 0
    val streak = day?.currentStreak() ?: 0
    // Jap and malas counted separately: malas = full rounds today, the ring
    // shows progress through the current (unfinished) round only.
    val todayMalas = todayCount / safeTarget
    val inCurrentMala = todayCount % safeTarget

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 14.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Japa Counter", style = MaterialTheme.typography.titleMedium)
                Text(
                    "🔥 $streak",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Marigold.copy(alpha = 0.18f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }

            Box(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .size(220.dp)
                    .clip(CircleShape)
                    .clickable {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onTap()
                        scope.launch {
                            val newToday = container.sessionManager.incrementJapaCount(counterId)
                            if (newToday % safeTarget == 0) {
                                Toast.makeText(context, "Mala ${newToday / safeTarget} complete! Starting the next one.", Toast.LENGTH_SHORT).show()
                                // A full mala is what completes this practice in the daily routine.
                                routineStepId?.let { container.sessionManager.markRoutineStepDone(it) }
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                MalaBeadRing(
                    filledBeads = inCurrentMala,
                    totalBeads = safeTarget,
                    modifier = Modifier.fillMaxSize()
                )
                Text("$inCurrentMala / $safeTarget", style = MaterialTheme.typography.headlineMedium)
            }

            Text(
                "Today's",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 14.dp)
            )
            Text(
                "Count: $todayCount  |  Malas: $todayMalas",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 2.dp)
            )
            Text(
                "Total: $totalCount",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp)
            )

            Text(
                "Tap the mala for each repetition",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 12.dp)
            )

            TextButton(
                onClick = { scope.launch { container.sessionManager.resetJapaToday(counterId) } },
                enabled = todayCount > 0
            ) {
                Text("Reset today")
            }
        }
    }
}

/**
 * A traditional mala: [totalBeads] small counting beads in a ring plus one
 * larger guru bead at the top, [filledBeads] of them lit up to mark progress.
 */
@Composable
private fun MalaBeadRing(filledBeads: Int, totalBeads: Int, modifier: Modifier = Modifier) {
    val filledColor = Vermilion
    val emptyColor = MaterialTheme.colorScheme.surfaceVariant
    val threadColor = MaterialTheme.colorScheme.outlineVariant
    val guruColor = Marigold

    Canvas(modifier = modifier) {
        val ringRadius = size.minDimension / 2f * 0.86f
        val center = Offset(size.width / 2f, size.height / 2f)
        val totalSlots = totalBeads + 1
        val circumference = 2f * PI.toFloat() * ringRadius
        val spacing = circumference / totalSlots
        val beadRadius = spacing / 2f * 0.82f
        val guruRadius = beadRadius * 1.9f

        drawCircle(
            color = threadColor,
            radius = ringRadius,
            center = center,
            style = Stroke(width = beadRadius * 0.3f)
        )

        for (slot in 0 until totalSlots) {
            val angle = Math.toRadians((-90f + slot * (360f / totalSlots)).toDouble())
            val beadCenter = Offset(
                center.x + ringRadius * cos(angle).toFloat(),
                center.y + ringRadius * sin(angle).toFloat()
            )
            if (slot == 0) {
                drawCircle(color = guruColor, radius = guruRadius, center = beadCenter)
            } else {
                val filled = slot <= filledBeads
                drawCircle(
                    color = if (filled) filledColor else emptyColor,
                    radius = beadRadius,
                    center = beadCenter
                )
                if (!filled) {
                    drawCircle(
                        color = threadColor,
                        radius = beadRadius,
                        center = beadCenter,
                        style = Stroke(width = beadRadius * 0.3f)
                    )
                }
            }
        }
    }
}
