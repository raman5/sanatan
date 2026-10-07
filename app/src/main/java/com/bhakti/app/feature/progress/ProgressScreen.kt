package com.bhakti.app.feature.progress

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.bhakti.app.core.di.LocalAppContainer
import com.bhakti.app.ui.theme.Ink
import com.bhakti.app.ui.theme.Marigold
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.roundToInt

private enum class ProgressView(val label: String) { WEEK("Week"), MONTH("Month") }

// One hue (marigold) for every mark. Text never takes this color - it stays in the
// theme's ink - and the selected/complete state is also shown by shape, not color alone.
private val TrackColor = Color(0x26FFFFFF)
private val BarColor = Marigold.copy(alpha = 0.62f)
private val BarSelected = Marigold

private val shortDate = DateTimeFormatter.ofPattern("d MMM")

private fun pct(fraction: Float) = "${(fraction * 100).roundToInt()}%"

/** Consistency tracker: how regularly the daily routine was completed over the last 3 months. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(@Suppress("UNUSED_PARAMETER") navController: NavHostController) {
    val container = LocalAppContainer.current
    val session by container.sessionManager.state.collectAsState(initial = null)
    val stats = remember(session) {
        ProgressStats(session?.routineHistory.orEmpty(), session?.japaHistory.orEmpty())
    }
    var view by remember { mutableStateOf(ProgressView.WEEK) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text("Your Progress", style = MaterialTheme.typography.displaySmall)
        Text(
            "How consistently you've kept your routine - last 3 months",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
        )

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatTile("Current streak", "${stats.currentStreak}", "full days in a row", Modifier.weight(1f))
            StatTile("Best streak", "${stats.bestStreak}", "full days in a row", Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(top = 10.dp)) {
            StatTile("Average", pct(stats.averageFraction), "of routine done per day", Modifier.weight(1f))
            StatTile("Full days", "${stats.completeDays}", "of ${stats.trackedDayCount} tracked", Modifier.weight(1f))
        }

        SingleChoiceSegmentedButtonRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp, bottom = 16.dp)
        ) {
            ProgressView.entries.forEachIndexed { index, option ->
                SegmentedButton(
                    selected = view == option,
                    onClick = { view = option },
                    shape = SegmentedButtonDefaults.itemShape(index, ProgressView.entries.size)
                ) { Text(option.label) }
            }
        }

        when (view) {
            ProgressView.WEEK -> WeekView(stats)
            ProgressView.MONTH -> MonthView(stats)
        }
    }
}

@Composable
private fun StatTile(label: String, value: String, caption: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
            Text(caption, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** "‹ title ›" header for stepping between weeks/months within the 3-month window. */
@Composable
private fun PeriodHeader(title: String, canGoBack: Boolean, canGoForward: Boolean, onBack: () -> Unit, onForward: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack, enabled = canGoBack) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous")
        }
        Text(title, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
        IconButton(onClick = onForward, enabled = canGoForward) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next")
        }
    }
}

// --- Week: % of the routine done each day, Monday to Sunday ------------------

@Composable
private fun WeekView(stats: ProgressStats) {
    var weekStart by remember { mutableStateOf(stats.currentWeekStart) }
    val days = stats.weekDays(weekStart)
    val week = stats.weekStat(weekStart)
    val chartHeight = 150.dp

    SectionCard {
        PeriodHeader(
            title = if (weekStart == stats.currentWeekStart) "This week"
            else "${weekStart.format(shortDate)} – ${weekStart.plusDays(6).format(shortDate)}",
            canGoBack = weekStart.isAfter(stats.earliestWeekStart),
            canGoForward = weekStart.isBefore(stats.currentWeekStart),
            onBack = { weekStart = weekStart.minusWeeks(1) },
            onForward = { weekStart = weekStart.plusWeeks(1) }
        )
        Text(
            if (week.trackedDays == 0) "Not tracked yet"
            else "${pct(week.averageFraction)} average · full routine on ${week.completeDays} of ${week.trackedDays} days",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 14.dp)
        )

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            days.forEach { day ->
                val selected = day.date == stats.today
                Column(
                    Modifier
                        .weight(1f)
                        .padding(vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        if (day.inRange) pct(day.fraction) else "–",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        color = if (day.inRange) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                    Box(
                        Modifier
                            .padding(top = 4.dp)
                            .fillMaxWidth(0.7f)
                            .height(chartHeight)
                            .clip(RoundedCornerShape(4.dp))
                            .background(TrackColor),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        if (day.inRange && day.fraction > 0f) {
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(day.fraction)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (selected) BarSelected else BarColor)
                            )
                        }
                    }
                    Text(
                        day.date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                    Text(
                        "${day.date.dayOfMonth}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// --- Month: calendar with full-routine days highlighted ----------------------

@Composable
private fun MonthView(stats: ProgressStats) {
    var month by remember { mutableStateOf(stats.currentMonth) }
    val days = stats.monthDays(month)
    val summary = stats.monthStat(month)
    val today = stats.today

    SectionCard {
        PeriodHeader(
            title = "${month.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${month.year}",
            canGoBack = month.isAfter(stats.earliestMonth),
            canGoForward = month.isBefore(stats.currentMonth),
            onBack = { month = month.minusMonths(1) },
            onForward = { month = month.plusMonths(1) }
        )
        Text(
            if (summary.trackedDays == 0) "Not tracked yet"
            else "Full routine on ${summary.completeDays} of ${summary.trackedDays} days · ${summary.japaCount} japa",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        )

        // Weekday header, Monday first.
        Row(Modifier.fillMaxWidth()) {
            listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach {
                Text(
                    it,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Leading blanks so the 1st lands under its weekday.
        val leading = month.atDay(1).dayOfWeek.value - 1
        val cells: List<DayStat?> = List(leading) { null } + days
        cells.chunked(7).forEach { week ->
            Row(Modifier.fillMaxWidth().padding(top = 6.dp)) {
                (0 until 7).forEach { i ->
                    val day = week.getOrNull(i)
                    Box(Modifier.weight(1f).aspectRatio(1f), contentAlignment = Alignment.Center) {
                        if (day != null) DayCell(day, isToday = day.date == today)
                    }
                }
            }
        }

        Row(Modifier.padding(top = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(12.dp).clip(CircleShape).background(Marigold))
            Text(
                "Full routine completed",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 6.dp)
            )
        }
    }
}

@Composable
private fun DayCell(day: DayStat, isToday: Boolean) {
    val ring = if (isToday) Marigold else Color.Transparent
    Box(
        Modifier
            .fillMaxSize(0.82f)
            .clip(CircleShape)
            .background(if (day.isComplete) Marigold else Color.Transparent)
            .border(2.dp, ring, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "${day.date.dayOfMonth}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (day.isComplete) FontWeight.Bold else FontWeight.Normal,
            color = when {
                day.isComplete -> Ink
                day.inRange -> MaterialTheme.colorScheme.onSurface
                else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            }
        )
    }
}

@Composable
private fun SectionCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(16.dp)) { content() }
    }
}
