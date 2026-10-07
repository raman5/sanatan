package com.bhakti.app.feature.pooja

import android.media.MediaPlayer
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.bhakti.app.R
import com.bhakti.app.core.di.LocalAppContainer
import com.bhakti.app.core.ui.MandalaMotif
import com.bhakti.app.core.ui.DeityArtImage
import com.bhakti.app.core.ui.imageFor
import com.bhakti.app.data.model.Deity
import com.bhakti.app.ui.theme.Ink
import com.bhakti.app.ui.theme.Marigold
import com.bhakti.app.ui.theme.NightBrown
import com.bhakti.app.ui.theme.Starlight
import com.bhakti.app.ui.theme.StoneDeep
import com.bhakti.app.ui.theme.StoneLit
import com.bhakti.app.ui.theme.StoneWarm
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

private data class PoojaOffering(val id: String, val label: String, val icon: ImageVector, val emoji: String)

// Ritual order: bell first to invite the deity's attention, then a pranam,
// then the offerings themselves (flowers, then the two liquids).
private val OFFERINGS = listOf(
    PoojaOffering("pooja-bell", "Ring Bell", Icons.Filled.NotificationsActive, "🔔"),
    PoojaOffering("pooja-namaste", "Fold Hands", Icons.Filled.VolunteerActivism, "🙏"),
    PoojaOffering("pooja-flowers", "Offer Flowers", Icons.Filled.LocalFlorist, "🌸"),
    PoojaOffering("pooja-water", "Offer Water", Icons.Filled.WaterDrop, "💧"),
    PoojaOffering("pooja-milk", "Offer Milk", Icons.Filled.LocalDrink, "🥛")
)

/**
 * A virtual shrine for [deityName] - tap each ritual to make today's offering.
 * Progress is tracked through the same daily-reset mechanism as Home's
 * routine steps (see [com.bhakti.app.core.session.SessionManager.markRoutineStepDone]),
 * just with per-offering ids instead of one per module.
 */
@Composable
fun PoojaTempleScreen(navController: NavHostController, deityName: String) {
    val container = LocalAppContainer.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current
    val deity = remember(deityName) { Deity.entries.firstOrNull { it.name == deityName } ?: Deity.entries.first() }
    val session by container.sessionManager.state.collectAsState(initial = null)
    val completed = session?.routineCompletedSteps ?: emptySet()
    val doneCount = OFFERINGS.count { it.id in completed }

    var floatingOfferings by remember { mutableStateOf(listOf<FloatingOffering>()) }
    var nextFloatingId by remember { mutableStateOf(0L) }
    var flowerPetals by remember { mutableStateOf(listOf<FlowerPetal>()) }
    var nextPetalId by remember { mutableStateOf(0L) }
    var idolImageUrl by remember(deity) { mutableStateOf<String?>(null) }
    var bellSoundUrl by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(deity) {
        idolImageUrl = container.contentRepository.deityPortraits()[deity]?.primaryUrl
        bellSoundUrl = container.contentRepository.bellSoundUrl()
    }

    val infiniteTransition = rememberInfiniteTransition(label = "shrineMotion")
    val mandalaRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 40000, easing = LinearEasing)),
        label = "shrineMandalaRotation"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        TempleBackdrop(modifier = Modifier.fillMaxSize())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            IconButton(onClick = { navController.popBackStack() }, modifier = Modifier.padding(top = 4.dp)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Starlight)
            }
            Text(
                "${deity.displayName}'s Shrine",
                style = MaterialTheme.typography.headlineMedium,
                color = Starlight
            )
            Text(
                "Offer today's pooja - tap each ritual below.",
                style = MaterialTheme.typography.bodyMedium,
                color = Starlight.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 4.dp, bottom = 22.dp)
            )

            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .fillMaxWidth(0.8f)
                    .aspectRatio(0.82f)
                    .clip(
                        RoundedCornerShape(
                            topStart = 160.dp,
                            topEnd = 160.dp,
                            bottomStart = 20.dp,
                            bottomEnd = 20.dp
                        )
                    )
                    .background(Brush.radialGradient(listOf(Marigold.copy(alpha = 0.4f), Color.Transparent))),
                contentAlignment = Alignment.Center
            ) {
                MandalaMotif(
                    modifier = Modifier
                        .fillMaxSize(0.92f)
                        .graphicsLayer { rotationZ = mandalaRotation },
                    color = Marigold,
                    alpha = 0.28f,
                    petals = 16
                )
                DeityArtImage(
                    url = idolImageUrl,
                    fallbackRes = imageFor(deity),
                    contentDescription = "${deity.displayName} idol",
                    contentScale = ContentScale.Crop,
                    alignment = Alignment.TopCenter,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(
                            RoundedCornerShape(
                                topStart = 160.dp,
                                topEnd = 160.dp,
                                bottomStart = 20.dp,
                                bottomEnd = 20.dp
                            )
                        )
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.55f)))
                        )
                        .padding(vertical = 14.dp)
                )
                // Rising offering emoji, stacked above whatever was just tapped.
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
                    floatingOfferings.forEach { offering ->
                        key(offering.id) {
                            FloatingOfferingBadge(
                                offering = offering,
                                onFinished = {
                                    floatingOfferings = floatingOfferings.filterNot { it.id == offering.id }
                                }
                            )
                        }
                    }
                }
                // Phool varsha - a burst of petals falling from above, only for
                // the flowers offering (the other offerings keep the single
                // rising badge above).
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                    flowerPetals.forEach { petal ->
                        key(petal.id) {
                            FallingPetal(
                                petal = petal,
                                onFinished = {
                                    flowerPetals = flowerPetals.filterNot { it.id == petal.id }
                                }
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 26.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(StoneWarm.copy(alpha = 0.45f))
                    .padding(vertical = 16.dp, horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OFFERINGS.forEach { offering ->
                    val done = offering.id in completed
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                if (offering.id == "pooja-bell") {
                                    playOneShotSound(context, bellSoundUrl, R.raw.bell_ghanti)
                                }
                                if (offering.id == "pooja-flowers") {
                                    // Phool varsha - a shower of petals instead of the single rising emoji.
                                    // Positions are evenly spaced slots (with a little jitter) rather than
                                    // pure random, so petals don't randomly cluster/overlap each other.
                                    val petalCount = 12
                                    val halfSpread = 210f
                                    val slotWidth = (2 * halfSpread) / petalCount
                                    val jitterRange = (slotWidth / 3f).toInt().coerceAtLeast(1)
                                    val newPetals = List(petalCount) { index ->
                                        val slotCenter = -halfSpread + slotWidth * (index + 0.5f)
                                        val jitter = Random.nextInt(-jitterRange, jitterRange)
                                        FlowerPetal(
                                            id = nextPetalId++,
                                            emoji = PETAL_EMOJI.random(),
                                            startXPx = slotCenter + jitter,
                                            delayMs = Random.nextInt(0, 500),
                                            fontSizeSp = Random.nextInt(20, 34),
                                            spinDegrees = Random.nextInt(160, 520).toFloat() *
                                                (if (Random.nextBoolean()) 1f else -1f)
                                        )
                                    }
                                    flowerPetals = flowerPetals + newPetals
                                } else {
                                    val id = nextFloatingId++
                                    floatingOfferings = floatingOfferings + FloatingOffering(
                                        id = id,
                                        emoji = offering.emoji
                                    )
                                }
                                val nowDone = completed + offering.id
                                scope.launch {
                                    container.sessionManager.markRoutineStepDone(offering.id)
                                    // Id kept as a literal, not RoutineModule.POOJA.id, so this
                                    // screen (feature/pooja) doesn't need to depend on
                                    // feature/home just for one string - must stay in sync with it.
                                    if (OFFERINGS.all { it.id in nowDone }) {
                                        container.sessionManager.markRoutineStepDone("pooja")
                                    }
                                }
                            }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(if (done) Marigold else Marigold.copy(alpha = 0.16f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                offering.icon,
                                contentDescription = offering.label,
                                tint = if (done) Ink else Marigold,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Text(
                            offering.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = Starlight,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .padding(top = 20.dp, bottom = 32.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(StoneWarm.copy(alpha = 0.32f))
                    .padding(16.dp)
            ) {
                Text(
                    "$doneCount/${OFFERINGS.size} offerings made today",
                    style = MaterialTheme.typography.titleSmall,
                    color = Starlight
                )
                LinearProgressIndicator(
                    progress = { doneCount / OFFERINGS.size.toFloat() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    color = Marigold,
                    trackColor = Starlight.copy(alpha = 0.15f)
                )
                if (doneCount == OFFERINGS.size) {
                    Text(
                        "🙏 Your pooja is complete for today",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Marigold,
                        modifier = Modifier.padding(top = 10.dp)
                    )
                }
            }
        }
    }
}

/**
 * The sanctum backdrop: two carved-stone pillars, a temple-spire silhouette
 * along the top edge, and a warm floor glow - replaces the old flat
 * two-color gradient with something that actually reads as "inside a
 * temple" rather than just "dark screen".
 */
@Composable
private fun TempleBackdrop(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        drawRect(brush = Brush.verticalGradient(listOf(StoneWarm.copy(alpha = 0.55f), NightBrown, Ink)))

        drawSpireValance(w, h * 0.09f)

        val pillarWidth = w * 0.085f
        drawPillar(0f, pillarWidth, h)
        drawPillar(w - pillarWidth, pillarWidth, h)

        // Warm floor glow.
        drawRect(
            brush = Brush.verticalGradient(
                listOf(Color.Transparent, Marigold.copy(alpha = 0.12f)),
                startY = h * 0.62f,
                endY = h
            ),
            topLeft = Offset(0f, h * 0.62f),
            size = Size(w, h * 0.38f)
        )
    }
}

private fun DrawScope.drawPillar(left: Float, width: Float, height: Float) {
    drawRoundRect(
        brush = Brush.horizontalGradient(
            listOf(StoneDeep, StoneWarm, StoneLit, StoneWarm, StoneDeep),
            startX = left,
            endX = left + width
        ),
        topLeft = Offset(left, height * 0.06f),
        size = Size(width, height * 0.88f),
        cornerRadius = CornerRadius(width * 0.15f)
    )
    val capWidth = width * 1.35f
    val capLeft = left - (capWidth - width) / 2f
    val bandHeight = 5.dp.toPx()
    drawRoundRect(
        color = StoneLit,
        topLeft = Offset(capLeft, height * 0.05f),
        size = Size(capWidth, bandHeight),
        cornerRadius = CornerRadius(bandHeight / 2f)
    )
    drawRoundRect(
        color = StoneLit,
        topLeft = Offset(capLeft, height * 0.92f),
        size = Size(capWidth, bandHeight),
        cornerRadius = CornerRadius(bandHeight / 2f)
    )
    val grooveStroke = 2.dp.toPx()
    listOf(height * 0.35f, height * 0.65f).forEach { grooveY ->
        drawLine(
            StoneDeep.copy(alpha = 0.6f),
            Offset(left, grooveY),
            Offset(left + width, grooveY),
            strokeWidth = grooveStroke
        )
    }
}

private fun DrawScope.drawSpireValance(w: Float, maxDepth: Float) {
    val path = Path().apply {
        moveTo(0f, 0f)
        lineTo(0f, maxDepth * 0.22f)
        quadraticTo(w * 0.12f, maxDepth * 0.50f, w * 0.28f, maxDepth * 0.30f)
        quadraticTo(w * 0.38f, maxDepth * 0.12f, w * 0.50f, maxDepth * 0.95f)
        quadraticTo(w * 0.62f, maxDepth * 0.12f, w * 0.72f, maxDepth * 0.30f)
        quadraticTo(w * 0.88f, maxDepth * 0.50f, w, maxDepth * 0.22f)
        lineTo(w, 0f)
        close()
    }
    drawPath(path, brush = Brush.verticalGradient(listOf(StoneDeep, StoneWarm)))
}

private data class FloatingOffering(val id: Long, val emoji: String)

@Composable
private fun FloatingOfferingBadge(offering: FloatingOffering, onFinished: () -> Unit) {
    val riseY = remember { Animatable(0f) }
    val fadeAlpha = remember { Animatable(1f) }

    // Was 1100/900ms (~1.15s total) - extended 2s longer per request.
    LaunchedEffect(offering.id) {
        launch { riseY.animateTo(-190f, tween(durationMillis = 3100, easing = LinearOutSlowInEasing)) }
        fadeAlpha.animateTo(0f, tween(durationMillis = 2900, delayMillis = 250))
        onFinished()
    }

    // Bigger and always centered (no horizontal scatter) - same treatment
    // for every offering's floating emoji: bell, namaste, flowers, water, milk.
    Text(
        offering.emoji,
        fontSize = 56.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .padding(bottom = 18.dp)
            .graphicsLayer {
                translationY = riseY.value
                alpha = fadeAlpha.value
            }
    )
}

private val PETAL_EMOJI = listOf("🌸", "🌺", "🌼", "🏵️")

private data class FlowerPetal(
    val id: Long,
    val emoji: String,
    val startXPx: Float,
    val delayMs: Int,
    val fontSizeSp: Int,
    val spinDegrees: Float
)

/** One petal in the phool varsha burst: drifts down from just above the shrine, spinning, then fades. */
@Composable
private fun FallingPetal(petal: FlowerPetal, onFinished: () -> Unit) {
    val fallY = remember { Animatable(-40f) }
    val fadeAlpha = remember { Animatable(1f) }
    val spin = remember { Animatable(0f) }

    LaunchedEffect(petal.id) {
        delay(petal.delayMs.toLong())
        launch { fallY.animateTo(420f, tween(durationMillis = 2600, easing = LinearEasing)) }
        launch { spin.animateTo(petal.spinDegrees, tween(durationMillis = 2600, easing = LinearEasing)) }
        delay(1900)
        fadeAlpha.animateTo(0f, tween(durationMillis = 700))
        onFinished()
    }

    Text(
        petal.emoji,
        fontSize = petal.fontSizeSp.sp,
        modifier = Modifier.graphicsLayer {
            translationX = petal.startXPx
            translationY = fallY.value
            rotationZ = spin.value
            alpha = fadeAlpha.value
        }
    )
}

/**
 * Fire-and-forget sound effect: [url] (backend-hosted) when present, else
 * the bundled [fallbackRes]. Self-releases on completion either way.
 */
private fun playOneShotSound(context: android.content.Context, url: String?, fallbackRes: Int) {
    if (url != null) {
        MediaPlayer().apply {
            setOnPreparedListener { it.start() }
            setOnCompletionListener { it.release() }
            setDataSource(url)
            prepareAsync()
        }
    } else {
        MediaPlayer.create(context, fallbackRes).apply {
            setOnCompletionListener { it.release() }
            start()
        }
    }
}
