package com.github.bernatbc.petitapetit.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.github.bernatbc.petitapetit.PetitApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private fun BroadcastReceiver.rescheduleReminder(context: Context) {
    val application = context.applicationContext as PetitApplication
    val pending = goAsync()
    CoroutineScope(Dispatchers.Default).launch {
        try {
            application.rescheduleReminder()
        } finally {
            pending.finish()
        }
    }
}

// Shows the reminder, then books tomorrow's.
class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        ReminderNotification.show(context)
        rescheduleReminder(context)
    }
}

// Alarms are cleared on reboot and app updates, so they are booked again here.
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            rescheduleReminder(context)
        }
    }
}
