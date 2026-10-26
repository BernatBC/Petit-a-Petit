package com.github.bernatbc.petitapetit.domain

import java.util.Calendar

data class ReminderSettings(
    val enabled: Boolean = true,
    val hour: Int = DEFAULT_HOUR,
    val minute: Int = 0,
) {
    companion object {
        const val DEFAULT_HOUR = 18
    }
}

// Next occurrence of hour:minute strictly after `now`, in the device time zone.
fun nextReminderTime(now: Long, hour: Int, minute: Int): Long {
    val trigger = Calendar.getInstance().apply {
        timeInMillis = now
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    if (trigger.timeInMillis <= now) trigger.add(Calendar.DAY_OF_YEAR, 1)
    return trigger.timeInMillis
}
