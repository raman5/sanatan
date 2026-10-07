package com.bhakti.app.feature.progress

import com.bhakti.app.core.session.DayRoutine
import com.bhakti.app.core.session.SessionState
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.TemporalAdjusters

/** How one day went - [fraction] of the routine done, plus japa repetitions. */
data class DayStat(
    val date: LocalDate,
    val routine: DayRoutine?,
    val japaCount: Int,
    /** False before the user's first recorded day or after today - not "missed", just not applicable. */
    val inRange: Boolean
) {
    val fraction: Float get() = routine?.fraction ?: 0f
    val isComplete: Boolean get() = routine?.isComplete == true
}

data class WeekStat(val weekStart: LocalDate, val averageFraction: Float, val completeDays: Int, val trackedDays: Int)

data class MonthStat(
    val month: YearMonth,
    val averageFraction: Float,
    val completeDays: Int,
    val trackedDays: Int,
    val japaCount: Int
)

/**
 * Consistency stats over the last 3 months (the retention window - see
 * [SessionState.HISTORY_DAYS]). Days before the user's first recorded
 * routine activity are left out rather than counted as missed, so a new
 * user isn't greeted with 0% for weeks they hadn't installed the app yet.
 */
class ProgressStats(
    private val history: Map<LocalDate, DayRoutine>,
    private val japa: Map<LocalDate, Int>,
    val today: LocalDate = LocalDate.now()
) {
    private val windowStart: LocalDate = today.minusDays(SessionState.HISTORY_DAYS - 1)

    /** First day with any activity (or today), clipped to the window. */
    private val trackingStart: LocalDate = run {
        val firstActive = (history.filterValues { it.doneCount > 0 }.keys + japa.filterValues { it > 0 }.keys).minOrNull()
        maxOf(windowStart, firstActive ?: today)
    }

    fun day(date: LocalDate) = DayStat(
        date = date,
        routine = history[date],
        japaCount = japa[date] ?: 0,
        inRange = !date.isBefore(trackingStart) && !date.isAfter(today)
    )

    private fun trackedDays(from: LocalDate, to: LocalDate): List<DayStat> =
        generateSequence(maxOf(from, trackingStart)) { it.plusDays(1) }
            .takeWhile { !it.isAfter(minOf(to, today)) }
            .map(::day)
            .toList()

    private val allTracked: List<DayStat> by lazy { trackedDays(trackingStart, today) }

    val averageFraction: Float get() = allTracked.map { it.fraction }.average().toFloat().takeIf { !it.isNaN() } ?: 0f
    val completeDays: Int get() = allTracked.count { it.isComplete }
    val trackedDayCount: Int get() = allTracked.size
    val totalJapa: Int get() = allTracked.sumOf { it.japaCount }

    /** Consecutive full-routine days ending today - or yesterday, since today may still be in progress. */
    val currentStreak: Int
        get() {
            var cursor = if (day(today).isComplete) today else today.minusDays(1)
            var streak = 0
            while (!cursor.isBefore(trackingStart) && day(cursor).isComplete) {
                streak++
                cursor = cursor.minusDays(1)
            }
            return streak
        }

    val bestStreak: Int
        get() {
            var best = 0
            var run = 0
            allTracked.forEach { if (it.isComplete) { run++; best = maxOf(best, run) } else run = 0 }
            return best
        }

    // --- Week view -----------------------------------------------------------

    val currentWeekStart: LocalDate = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    /** Oldest week that still has days inside the 3-month window. */
    val earliestWeekStart: LocalDate = windowStart.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

    /** Monday to Sunday of the week starting [weekStart]. */
    fun weekDays(weekStart: LocalDate): List<DayStat> = (0L until 7L).map { day(weekStart.plusDays(it)) }

    fun weekStat(weekStart: LocalDate): WeekStat {
        val tracked = weekDays(weekStart).filter { it.inRange }
        return WeekStat(
            weekStart = weekStart,
            averageFraction = if (tracked.isEmpty()) 0f else tracked.map { it.fraction }.average().toFloat(),
            completeDays = tracked.count { it.isComplete },
            trackedDays = tracked.size
        )
    }

    // --- Month view ----------------------------------------------------------

    val currentMonth: YearMonth = YearMonth.from(today)
    /** Oldest month that still has days inside the 3-month window. */
    val earliestMonth: YearMonth = YearMonth.from(windowStart)

    fun monthDays(month: YearMonth): List<DayStat> = (1..month.lengthOfMonth()).map { day(month.atDay(it)) }

    fun monthStat(month: YearMonth): MonthStat {
        val tracked = trackedDays(month.atDay(1), month.atEndOfMonth())
        return MonthStat(
            month = month,
            averageFraction = if (tracked.isEmpty()) 0f else tracked.map { it.fraction }.average().toFloat(),
            completeDays = tracked.count { it.isComplete },
            trackedDays = tracked.size,
            japaCount = tracked.sumOf { it.japaCount }
        )
    }
}
