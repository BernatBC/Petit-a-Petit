package com.github.bernatbc.petitapetit.domain

import java.util.Calendar

enum class Grade { AGAIN, HARD, GOOD, EASY }

data class Card(
    val id: Long,
    val word: String,
    val translation: String,
    val sentence: String,
    val addedAt: Long,
    val dueAt: Long,
    val intervalDays: Double = 0.0,
    val ease: Double = Scheduler.INITIAL_EASE,
    val repetitions: Int = 0,
    val lapses: Int = 0,
    val lastReviewedAt: Long? = null,
) {
    val isNew: Boolean get() = lastReviewedAt == null
}

// Simplified SM-2: each answer updates the ease and the interval, and "À revoir" counts as a lapse.
object Scheduler {

    const val INITIAL_EASE = 2.5
    const val MIN_EASE = 1.3
    const val DAY_MS = 24L * 60 * 60 * 1000
    const val SESSION_SIZE = 10
    private const val RETRY_DELAY_MS = 10L * 60 * 1000

    fun review(card: Card, grade: Grade, now: Long): Card {
        val ease = when (grade) {
            Grade.AGAIN -> (card.ease - 0.2).coerceAtLeast(MIN_EASE)
            Grade.HARD -> (card.ease - 0.15).coerceAtLeast(MIN_EASE)
            Grade.GOOD -> card.ease
            Grade.EASY -> card.ease + 0.15
        }
        if (grade == Grade.AGAIN) {
            return card.copy(
                ease = ease,
                intervalDays = 0.0,
                repetitions = 0,
                lapses = card.lapses + 1,
                dueAt = now + RETRY_DELAY_MS,
                lastReviewedAt = now,
            )
        }
        val interval = when (grade) {
            Grade.HARD -> maxOf(1.0, card.intervalDays * 1.2)
            Grade.GOOD -> when (card.repetitions) {
                0 -> 1.0
                1 -> 3.0
                else -> maxOf(card.intervalDays * ease, card.intervalDays + 1)
            }

            else -> when (card.repetitions) {
                0 -> 3.0
                1 -> 6.0
                else -> maxOf(card.intervalDays * ease * 1.3, card.intervalDays + 2)
            }
        }
        return card.copy(
            ease = ease,
            intervalDays = interval,
            repetitions = card.repetitions + 1,
            dueAt = now + (interval * DAY_MS).toLong(),
            lastReviewedAt = now,
        )
    }

    // Higher means harder: frequent lapses, low ease, never reviewed, or long overdue.
    fun difficulty(card: Card, now: Long): Double {
        val overdueDays = ((now - card.dueAt).coerceAtLeast(0) / DAY_MS.toDouble()).coerceAtMost(10.0)
        return card.lapses * 2.0 +
            (INITIAL_EASE - card.ease) * 2.0 +
            (if (card.isNew) 3.0 else 0.0) +
            overdueDays * 0.2
    }

    fun selectSession(
        cards: List<Card>,
        now: Long,
        endOfToday: Long,
        limit: Int = SESSION_SIZE,
    ): List<Card> = cards
        .filter { it.dueAt <= endOfToday }
        .sortedWith(compareByDescending<Card> { difficulty(it, now) }.thenBy { it.dueAt })
        .take(limit)

    fun endOfDay(now: Long): Long = Calendar.getInstance().apply {
        timeInMillis = now
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
        set(Calendar.MILLISECOND, 999)
    }.timeInMillis
}
