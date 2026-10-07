package com.bhakti.app.feature.splash

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.bhakti.app.BuildConfig
import com.bhakti.app.core.di.LocalAppContainer
import com.bhakti.app.core.navigation.Routes
import com.bhakti.app.core.ui.MandalaMotif
import com.bhakti.app.ui.theme.CosmicDeep
import com.bhakti.app.ui.theme.CosmicIndigo
import com.bhakti.app.ui.theme.CosmicPurple
import com.bhakti.app.ui.theme.Marigold
import com.bhakti.app.ui.theme.Starlight
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun SplashScreen(navController: NavHostController) {
    val container = LocalAppContainer.current

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    LaunchedEffect(Unit) {
        delay(1300)
        val session = container.sessionManager.state.first()
        val destination = when {
            !session.isLoggedIn -> Routes.AUTH
            BuildConfig.SUBSCRIPTIONS_ENABLED && !session.hasActiveAccess -> Routes.PAYWALL
            else -> Routes.HOME
        }
        navController.navigate(destination) {
            popUpTo(Routes.SPLASH) { inclusive = true }
        }
    }

    val contentAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 900),
        label = "splashContentAlpha"
    )
    val contentScale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.82f,
        animationSpec = tween(durationMillis = 900),
        label = "splashContentScale"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "splashMotion")
    val mandalaRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 32000, easing = LinearEasing)),
        label = "mandalaRotation"
    )
    // A 0..1 phase that loops seamlessly; each star's alpha rides its own
    // integer multiple of this phase so several twinkle rates emerge from
    // one shared animation instead of one per star.
    val starPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 4000, easing = LinearEasing), repeatMode = RepeatMode.Restart),
        label = "starPhase"
    )

    val stars = remember {
        val random = Random(42)
        List(90) {
            Star(
                x = random.nextFloat(),
                y = random.nextFloat(),
                radius = random.nextFloat() * 1.6f + 0.6f,
                baseAlpha = random.nextFloat() * 0.35f + 0.25f,
                twinkleSpeed = random.nextInt(2, 6),
                phaseOffset = random.nextFloat()
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.radialGradient(listOf(CosmicPurple, CosmicIndigo, CosmicDeep)))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            stars.forEach { star ->
                val twinkle = 0.5f + 0.5f * sin(2f * PI.toFloat() * (star.twinkleSpeed * starPhase + star.phaseOffset))
                drawCircle(
                    color = Starlight.copy(alpha = (star.baseAlpha * twinkle).coerceIn(0f, 1f)),
                    radius = star.radius.dp.toPx(),
                    center = Offset(star.x * size.width, star.y * size.height)
                )
            }
        }

        MandalaMotif(
            modifier = Modifier
                .align(Alignment.Center)
                .size(280.dp)
                .graphicsLayer { rotationZ = mandalaRotation }
                .alpha(contentAlpha),
            color = Marigold,
            alpha = 0.22f,
            petals = 16
        )

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .alpha(contentAlpha)
                .scale(contentScale)
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(148.dp)
                        .background(
                            Brush.radialGradient(listOf(Marigold.copy(alpha = 0.45f), Color.Transparent)),
                            shape = CircleShape
                        )
                )
                Icon(
                    imageVector = Icons.Filled.SelfImprovement,
                    contentDescription = null,
                    tint = Starlight,
                    modifier = Modifier.size(64.dp)
                )
            }
            Text(
                "Bhakti",
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp
                ),
                color = Starlight,
                modifier = Modifier.padding(top = 18.dp)
            )
            Text(
                "Your devotional journey begins",
                style = MaterialTheme.typography.bodyLarge,
                color = Starlight.copy(alpha = 0.75f),
                modifier = Modifier.padding(top = 6.dp)
            )
        }
    }
}

private data class Star(
    val x: Float,
    val y: Float,
    val radius: Float,
    val baseAlpha: Float,
    val twinkleSpeed: Int,
    val phaseOffset: Float
)
