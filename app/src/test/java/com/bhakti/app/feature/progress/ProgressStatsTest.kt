package com.bhakti.app.feature.progress

import com.bhakti.app.core.session.DayRoutine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class ProgressStatsTest {

    private val today = LocalDate.of(2026, 10, 4) // a Sunday
    private val enabled = setOf("pooja", "naam-japa", "mantra", "status", "wallpaper")
    private fun full() = DayRoutine(enabled, enabled)
    private fun partial(vararg done: String) = DayRoutine(done.toSet(), enabled)

    @Test
    fun `current streak counts back from yesterday when today is still in progress`() {
        val history = mapOf(
            today to partial("pooja"),
            today.minusDays(1) to full(),
            today.minusDays(2) to full(),
            today.minusDays(3) to partial("mantra"),
            today.minusDays(4) to full()
        )
        val stats = ProgressStats(history, emptyMap(), today)
        assertEquals(2, stats.currentStreak)
        assertEquals(2, stats.bestStreak)
    }

    @Test
    fun `current streak includes today once today is complete`() {
        val history = mapOf(today to full(), today.minusDays(1) to full())
        assertEquals(2, ProgressStats(history, emptyMap(), today).currentStreak)
    }

    @Test
    fun `days before the first activity are not counted as missed`() {
        val history = mapOf(today.minusDays(1) to full(), today to partial("pooja", "mantra"))
        val stats = ProgressStats(history, emptyMap(), today)
        assertEquals(2, stats.trackedDayCount)
        assertEquals(1, stats.completeDays)
        assertEquals((1f + 0.4f) / 2, stats.averageFraction, 0.001f)
        assertFalse(stats.day(today.minusDays(5)).inRange)
    }

    @Test
    fun `history older than three months is ignored`() {
        val old = today.minusDays(120)
        val history = mapOf(old to full(), today to full())
        val stats = ProgressStats(history, mapOf(old to 500), today)
        // Active before the window, so all 92 days in it are tracked (gaps count as missed)...
        assertEquals(92, stats.trackedDayCount)
        // ...but the old day's own data doesn't count toward anything.
        assertEquals(1, stats.completeDays)
        assertEquals(0, stats.totalJapa)
    }

    @Test
    fun `removed modules do not count toward a day's total`() {
        val withRemoved = DayRoutine(done = enabled, enabled = enabled + "bhajan")
        assertTrue(withRemoved.isComplete)
        assertEquals(5, withRemoved.total)
    }

    @Test
    fun `week view runs monday to sunday and only goes back as far as the window`() {
        val stats = ProgressStats(mapOf(today to full()), emptyMap(), today)
        assertEquals(LocalDate.of(2026, 9, 28), stats.currentWeekStart) // Monday of today's (Sunday's) week
        val days = stats.weekDays(stats.currentWeekStart)
        assertEquals(7, days.size)
        assertEquals(today, days.last().date)
        assertEquals(1, stats.weekStat(stats.currentWeekStart).completeDays)
        // 92-day window from 4 Oct starts 5 Jul (a Sunday) -> earliest week starts Monday 29 Jun.
        assertEquals(LocalDate.of(2026, 6, 29), stats.earliestWeekStart)
    }

    @Test
    fun `month view covers every day of the month with japa totals`() {
        val history = mapOf(
            today to full(),
            LocalDate.of(2026, 9, 15) to partial("pooja"),
            LocalDate.of(2026, 9, 20) to full()
        )
        val japa = mapOf(today to 108, LocalDate.of(2026, 9, 15) to 216)
        val stats = ProgressStats(history, japa, today)
        val september = java.time.YearMonth.of(2026, 9)
        assertEquals(30, stats.monthDays(september).size)
        assertEquals(1, stats.monthStat(september).completeDays)
        assertEquals(216, stats.monthStat(september).japaCount)
        assertEquals(108, stats.monthStat(stats.currentMonth).japaCount)
        assertEquals(java.time.YearMonth.of(2026, 7), stats.earliestMonth)
    }
}
