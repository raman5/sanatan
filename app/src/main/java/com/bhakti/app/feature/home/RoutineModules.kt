package com.bhakti.app.feature.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.bhakti.app.ui.theme.Marigold
import com.bhakti.app.ui.theme.SaffronDeep
import com.bhakti.app.ui.theme.Vermilion

/**
 * The toggleable building blocks of Home's "Your Routine" checklist. [id]
 * is the stable key persisted in [com.bhakti.app.core.session.SessionState.routineModules]
 * (which ones are on) and reused as the routine-step id for daily completion
 * tracking, so it must never change once shipped.
 */
enum class RoutineModule(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val tint: Color,
    /**
     * True if just opening the step counts as done. False for practices the
     * screen itself marks complete: Pooja (all 5 offerings) and the two japa
     * steps (one full mala) - see PoojaTempleScreen and JapaCounter.
     */
    val completesOnVisit: Boolean = true
) {
    // Declaration order is display order - Home's checklist and the
    // Customize Routine screen both iterate entries as-is. Keep in sync
    // with ExploreScreen's tile order.
    POOJA(
        id = "pooja",
        title = "Daily Pooja",
        description = "Ring the bell, offer milk, water & flowers",
        icon = Icons.Filled.Spa,
        tint = Vermilion,
        completesOnVisit = false
    ),
    NAAM_JAPA(
        id = "naam-japa",
        title = "Naam Japa",
        description = "Complete one mala (108) of a deity's name",
        icon = Icons.Filled.Repeat,
        tint = Marigold,
        completesOnVisit = false
    ),
    MANTRA(
        id = "mantra",
        title = "Today's Mantra",
        description = "A daily chant with its meaning",
        icon = Icons.Filled.SelfImprovement,
        tint = Vermilion,
        completesOnVisit = false
    ),
    STATUS(
        id = "status",
        title = "Daily WhatsApp Status",
        description = "Share a status with today's blessing",
        icon = Icons.AutoMirrored.Filled.Chat,
        tint = SaffronDeep
    ),
    WALLPAPER(
        id = "wallpaper",
        title = "Devotional Darshan",
        description = "Today's featured deity wallpaper",
        icon = Icons.Filled.Wallpaper,
        tint = Marigold
    );

    companion object {
        val ALL_IDS: Set<String> = entries.map { it.id }.toSet()
    }
}
