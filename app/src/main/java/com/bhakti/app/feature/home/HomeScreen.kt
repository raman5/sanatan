package com.bhakti.app.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.bhakti.app.core.di.LocalAppContainer
import com.bhakti.app.core.navigation.Routes
import com.bhakti.app.core.session.SessionState
import com.bhakti.app.core.ui.MandalaMotif
import com.bhakti.app.core.ui.PlaceholderArt
import com.bhakti.app.core.ui.DeityArtImage
import com.bhakti.app.core.ui.imageFor
import com.bhakti.app.data.model.ContentType
import com.bhakti.app.data.model.Deity
import com.bhakti.app.data.model.DeityPortrait
import com.bhakti.app.data.model.Mantra
import com.bhakti.app.data.model.Wallpaper
import com.bhakti.app.data.model.WhatsAppStatus
import com.bhakti.app.notifications.syncScheduledReminders
import com.bhakti.app.ui.theme.Marigold
import com.bhakti.app.ui.theme.SaffronDeep
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalTime

private data class HomeContent(
    val wallpaper: Wallpaper,
    val mantra: Mantra,
    val status: WhatsAppStatus
)

@Composable
fun HomeScreen(navController: NavHostController, modifier: Modifier = Modifier) {
    val container = LocalAppContainer.current
    val scope = rememberCoroutineScope()
    var content by remember { mutableStateOf<HomeContent?>(null) }
    var session by remember { mutableStateOf<SessionState?>(null) }
    var deityPortraits by remember { mutableStateOf<Map<Deity, DeityPortrait>>(emptyMap()) }

    LaunchedEffect(Unit) {
        deityPortraits = container.contentRepository.deityPortraits()
    }
    LaunchedEffect(Unit) {
        // Fetched in parallel - three independent collections, so no reason to wait on each in turn.
        content = coroutineScope {
            val wallpaper = async { container.contentRepository.featuredWallpaper() }
            val mantra = async { container.contentRepository.mantraOfTheDay() }
            val status = async { container.contentRepository.statusOfTheDay() }
            HomeContent(wallpaper = wallpaper.await(), mantra = mantra.await(), status = status.await())
        }
    }
    LaunchedEffect(Unit) {
        container.sessionManager.state.collect { session = it }
    }
    LaunchedEffect(Unit) {
        // Reminder toggles default to on, so reconcile AlarmManager here rather
        // than only when the user visits the Notifications settings screen.
        val initialSession = container.sessionManager.state.first()
        syncScheduledReminders(initialSession, container.contentRepository, container.notificationScheduler)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        HomeHero(
            greeting = greeting(),
            name = session?.user?.displayName?.takeIf { it.isNotBlank() } ?: "Bhakt",
            onSearch = { navController.navigate(Routes.SEARCH) }
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(top = 18.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            DeityQuickRow(
                portraits = deityPortraits,
                onDeityClick = { deity -> navController.navigate(Routes.wallpaperList(deity.name)) }
            )

            val current = content
            val enabledModules = session?.routineModules ?: SessionState.DEFAULT_ROUTINE_MODULES
            if (current == null) {
                Text("Loading today's darshan...", style = MaterialTheme.typography.bodyMedium)
            } else {
                val steps = remember(current, enabledModules) {
                    RoutineModule.entries
                        .filter { it.id in enabledModules }
                        .map { module ->
                            val (subtitle, route) = when (module) {
                                RoutineModule.NAAM_JAPA ->
                                    module.description to Routes.NAAM_JAPA_LIST
                                RoutineModule.MANTRA ->
                                    "${current.mantra.devanagari}  •  ${current.mantra.transliteration}" to
                                        Routes.contentDetail(ContentType.MANTRA, current.mantra.id)
                                RoutineModule.WALLPAPER ->
                                    current.wallpaper.title to Routes.contentDetail(ContentType.WALLPAPER, current.wallpaper.id)
                                RoutineModule.STATUS ->
                                    current.status.title to Routes.contentDetail(ContentType.STATUS, current.status.id)
                                RoutineModule.POOJA ->
                                    module.description to Routes.POOJA_DEITY_SELECT
                            }
                            RoutineStep(
                                id = module.id,
                                icon = module.icon,
                                tint = module.tint,
                                title = module.title,
                                subtitle = subtitle,
                                route = route
                            )
                        }
                }

                TodaysRoutineBanner(
                    steps = steps,
                    completedIds = session?.routineCompletedSteps ?: emptySet(),
                    onStepClick = { step ->
                        // Simple steps count as done on a visit. Pooja and the two japa
                        // steps are practices - their screens mark them done once actually
                        // completed (all offerings / one full mala), so don't pre-empt that.
                        if (RoutineModule.entries.firstOrNull { it.id == step.id }?.completesOnVisit != false) {
                            scope.launch { container.sessionManager.markRoutineStepDone(step.id) }
                        }
                        navController.navigate(step.route)
                    },
                    onEditClick = { navController.navigate(Routes.ROUTINE_SETTINGS) }
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                HomeActionTile(
                    icon = Icons.Filled.Explore,
                    label = "Explore",
                    modifier = Modifier.weight(1f),
                    onClick = { navController.navigate(Routes.EXPLORE) }
                )
                HomeActionTile(
                    icon = Icons.Filled.Favorite,
                    label = "Favourites",
                    modifier = Modifier.weight(1f),
                    onClick = { navController.navigate(Routes.FAVOURITES) }
                )
            }
        }
    }
}

@Composable
private fun HomeHero(greeting: String, name: String, onSearch: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(listOf(SaffronDeep, Marigold)),
                shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)
            )
    ) {
        MandalaMotif(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(160.dp)
                .padding(top = 8.dp, end = 8.dp),
            color = Color.White,
            alpha = 0.16f
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 28.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column {
                Text(greeting, style = MaterialTheme.typography.displaySmall, color = Color.White)
                Text(
                    name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            IconButton(onClick = onSearch) {
                Icon(Icons.Filled.Search, contentDescription = "Search", tint = Color.White)
            }
        }
    }
}

@Composable
private fun DeityQuickRow(portraits: Map<Deity, DeityPortrait>, onDeityClick: (Deity) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        items(Deity.entries) { deity ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .width(64.dp)
                    .clickable { onDeityClick(deity) }
            ) {
                DeityArtImage(
                    url = portraits[deity]?.primaryUrl,
                    fallbackRes = imageFor(deity),
                    contentDescription = deity.displayName,
                    contentScale = ContentScale.Crop,
                    alignment = Alignment.TopCenter,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .border(2.dp, Color(deity.accentHex), CircleShape)
                )
                Text(
                    deity.displayName,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

private data class RoutineStep(
    val id: String,
    val icon: ImageVector,
    val tint: Color,
    val title: String,
    val subtitle: String,
    val route: String
)

/** "Your Routine" banner: a devotional checklist with a shared progress bar, replacing a loose card stack. */
@Composable
private fun TodaysRoutineBanner(
    steps: List<RoutineStep>,
    completedIds: Set<String>,
    onStepClick: (RoutineStep) -> Unit,
    onEditClick: () -> Unit
) {
    val doneCount = steps.count { it.id in completedIds }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Your Routine", style = MaterialTheme.typography.titleLarge)
                    Text(
                        "Complete each step for today's blessings",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (steps.isNotEmpty()) {
                        Text(
                            "$doneCount/${steps.size}",
                            style = MaterialTheme.typography.titleMedium,
                            color = SaffronDeep,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                    }
                    IconButton(onClick = onEditClick) {
                        Icon(
                            Icons.Filled.Edit,
                            contentDescription = "Customize routine",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            if (steps.isEmpty()) {
                Text(
                    "No modules turned on yet. Tap the pencil above to add some to your routine.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
                return@Card
            }
            LinearProgressIndicator(
                progress = { doneCount / steps.size.toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .clip(RoundedCornerShape(8.dp)),
                color = SaffronDeep,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Column(modifier = Modifier.padding(top = 10.dp)) {
                steps.forEach { step ->
                    RoutineStepRow(
                        step = step,
                        done = step.id in completedIds,
                        onClick = { onStepClick(step) }
                    )
                }
            }
        }
    }
}

@Composable
private fun RoutineStepRow(step: RoutineStep, done: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(if (done) step.tint else step.tint.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                if (done) Icons.Filled.Check else step.icon,
                contentDescription = null,
                tint = if (done) Color.White else step.tint,
                modifier = Modifier.size(18.dp)
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)
        ) {
            Text(
                step.title,
                style = MaterialTheme.typography.titleSmall,
                textDecoration = if (done) TextDecoration.LineThrough else TextDecoration.None,
                color = if (done) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
            )
            Text(
                step.subtitle,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun HomeActionTile(icon: ImageVector, label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(8.dp))
            Text(label, style = MaterialTheme.typography.titleMedium)
        }
    }
}

private fun greeting(): String {
    val hour = LocalTime.now().hour
    return when {
        hour < 12 -> "Good Morning"
        hour < 17 -> "Good Afternoon"
        else -> "Good Evening"
    }
}
