package com.github.bernatbc.petitapetit

import com.github.bernatbc.petitapetit.domain.ReminderSettings
import com.github.bernatbc.petitapetit.domain.nextReminderTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class ReminderTest {

    private fun at(hour: Int, minute: Int, second: Int = 0): Long = Calendar.getInstance().apply {
        clear()
        set(2026, Calendar.OCTOBER, 26, hour, minute, second)
    }.timeInMillis

    private fun Long.hourMinute(): Pair<Int, Int> = Calendar.getInstance().let {
        it.timeInMillis = this
        it.get(Calendar.HOUR_OF_DAY) to it.get(Calendar.MINUTE)
    }

    @Test
    fun defaultsToEighteenHundredEnabled() {
        val defaults = ReminderSettings()
        assertTrue(defaults.enabled)
        assertEquals(18, defaults.hour)
        assertEquals(0, defaults.minute)
    }

    @Test
    fun picksTodayWhenTheTimeIsStillAhead() {
        val trigger = nextReminderTime(at(9, 30), hour = 18, minute = 0)
        assertEquals(at(18, 0), trigger)
    }

    @Test
    fun picksTomorrowWhenTheTimeHasPassed() {
        val trigger = nextReminderTime(at(18, 0, 1), hour = 18, minute = 0)
        assertEquals(at(18, 0) + 24 * 3_600_000L, trigger)
        assertEquals(18 to 0, trigger.hourMinute())
    }

    @Test
    fun exactlyNowMovesToTomorrow() {
        val now = at(18, 0)
        assertTrue(nextReminderTime(now, 18, 0) > now)
    }
}
