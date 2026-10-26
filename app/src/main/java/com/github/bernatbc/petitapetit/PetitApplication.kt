package com.github.bernatbc.petitapetit

import android.app.Application
import com.github.bernatbc.petitapetit.data.SettingsRepository
import com.github.bernatbc.petitapetit.data.WordRepository
import com.github.bernatbc.petitapetit.data.translation.FallbackTranslator
import com.github.bernatbc.petitapetit.data.translation.MlKitTranslator
import com.github.bernatbc.petitapetit.data.translation.MyMemoryTranslator
import com.github.bernatbc.petitapetit.data.translation.WordTranslator
import com.github.bernatbc.petitapetit.reminder.ReminderNotification
import com.github.bernatbc.petitapetit.reminder.ReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class PetitApplication : Application() {

    val wordRepository: WordRepository by lazy { WordRepository(this) }

    val settingsRepository: SettingsRepository by lazy { SettingsRepository(this) }

    val reminderScheduler: ReminderScheduler by lazy { ReminderScheduler(this) }

    val translator: WordTranslator by lazy {
        FallbackTranslator(MlKitTranslator(), MyMemoryTranslator())
    }

    override fun onCreate() {
        super.onCreate()
        // Starts the on-device model download and opens the database early.
        translator
        wordRepository
        ReminderNotification.createChannel(this)
        CoroutineScope(SupervisorJob() + Dispatchers.Default).launch { rescheduleReminder() }
    }

    suspend fun rescheduleReminder() {
        reminderScheduler.schedule(settingsRepository.reminder.first())
    }
}
