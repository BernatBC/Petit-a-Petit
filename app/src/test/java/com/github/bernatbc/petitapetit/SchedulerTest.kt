package com.github.bernatbc.petitapetit

import com.github.bernatbc.petitapetit.domain.Card
import com.github.bernatbc.petitapetit.domain.Grade
import com.github.bernatbc.petitapetit.domain.Scheduler
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SchedulerTest {

    private val now = 1_800_000_000_000L
    private val day = Scheduler.DAY_MS

    private fun card(
        id: Long = 1,
        dueAt: Long = now,
        interval: Double = 0.0,
        ease: Double = Scheduler.INITIAL_EASE,
        repetitions: Int = 0,
        lapses: Int = 0,
        reviewed: Boolean = false,
    ) = Card(
        id = id, word = "mot$id", translation = "word", sentence = "", addedAt = now - day,
        dueAt = dueAt, intervalDays = interval, ease = ease, repetitions = repetitions,
        lapses = lapses, lastReviewedAt = if (reviewed) now - day else null,
    )

    @Test
    fun goodAnswersGrowTheIntervalOneDayThreeDaysThenByEase() {
        val first = Scheduler.review(card(), Grade.GOOD, now)
        assertEquals(1.0, first.intervalDays, 0.0)
        assertEquals(now + day, first.dueAt)
        val second = Scheduler.review(first, Grade.GOOD, now)
        assertEquals(3.0, second.intervalDays, 0.0)
        val third = Scheduler.review(second, Grade.GOOD, now)
        assertEquals(7.5, third.intervalDays, 0.0001)
        assertEquals(3, third.repetitions)
    }

    @Test
    fun againCountsALapseAndLowersEase() {
        val reviewed = card(interval = 10.0, repetitions = 4, ease = 2.5, reviewed = true)
        val lapsed = Scheduler.review(reviewed, Grade.AGAIN, now)
        assertEquals(1, lapsed.lapses)
        assertEquals(0, lapsed.repetitions)
        assertEquals(0.0, lapsed.intervalDays, 0.0)
        assertEquals(2.3, lapsed.ease, 0.0001)
        assertTrue(lapsed.dueAt - now < day)
    }

    @Test
    fun easeNeverDropsBelowTheMinimum() {
        var current = card(ease = 1.4)
        repeat(5) { current = Scheduler.review(current, Grade.AGAIN, now) }
        assertEquals(Scheduler.MIN_EASE, current.ease, 0.0)
    }

    @Test
    fun easyIsLongerThanGoodAndRaisesEase() {
        val good = Scheduler.review(card(), Grade.GOOD, now)
        val easy = Scheduler.review(card(), Grade.EASY, now)
        assertTrue(easy.intervalDays > good.intervalDays)
        assertEquals(2.65, easy.ease, 0.0001)
    }

    @Test
    fun hardKeepsAtLeastOneDay() {
        val hard = Scheduler.review(card(), Grade.HARD, now)
        assertEquals(1.0, hard.intervalDays, 0.0)
        assertEquals(2.35, hard.ease, 0.0001)
    }

    @Test
    fun sessionSkipsCardsNotDueYet() {
        val endOfToday = now + day / 2
        val due = card(id = 1)
        val later = card(id = 2, dueAt = now + 3 * day, reviewed = true)
        assertEquals(listOf(1L), Scheduler.selectSession(listOf(due, later), now, endOfToday).map { it.id })
    }

    @Test
    fun sessionPutsHardWordsFirstAndRespectsTheLimit() {
        val endOfToday = now + day / 2
        val easy = card(id = 1, ease = 2.8, repetitions = 5, reviewed = true)
        val hard = card(id = 2, ease = 1.5, lapses = 3, repetitions = 1, reviewed = true)
        val fresh = card(id = 3)
        val ordered = Scheduler.selectSession(listOf(easy, hard, fresh), now, endOfToday).map { it.id }
        assertEquals(listOf(2L, 3L, 1L), ordered)
        assertEquals(2, Scheduler.selectSession(listOf(easy, hard, fresh), now, endOfToday, limit = 2).size)
    }

    @Test
    fun endOfDayIsLaterThanNowButWithinADay() {
        val end = Scheduler.endOfDay(now)
        assertTrue(end >= now)
        assertTrue(end - now < day + 3_600_000)
    }
}
