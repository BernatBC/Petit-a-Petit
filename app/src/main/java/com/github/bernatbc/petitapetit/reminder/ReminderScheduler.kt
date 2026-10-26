package com.github.bernatbc.petitapetit.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.github.bernatbc.petitapetit.domain.ReminderSettings
import com.github.bernatbc.petitapetit.domain.nextReminderTime

class ReminderScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    private fun pendingIntent(): PendingIntent = PendingIntent.getBroadcast(
        context,
        REQUEST_CODE,
        Intent(context, ReminderReceiver::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    // One inexact alarm at a time; the receiver books the next day's alarm when it fires.
    // Inexact alarms need no special permission and may be delayed a few minutes in Doze mode.
    fun schedule(settings: ReminderSettings) {
        val pending = pendingIntent()
        alarmManager.cancel(pending)
        if (!settings.enabled) return
        val trigger = nextReminderTime(System.currentTimeMillis(), settings.hour, settings.minute)
        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger, pending)
    }

    private companion object {
        const val REQUEST_CODE = 1
    }
}
