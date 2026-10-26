package com.github.bernatbc.petitapetit.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.github.bernatbc.petitapetit.PetitApplication
import com.github.bernatbc.petitapetit.domain.ReminderSettings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as PetitApplication
    private val repository = app.settingsRepository

    val reminder: StateFlow<ReminderSettings> = repository.reminder
        .stateIn(viewModelScope, SharingStarted.Eagerly, ReminderSettings())

    fun setEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.setReminderEnabled(enabled)
            app.rescheduleReminder()
        }
    }

    fun setTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            repository.setReminderTime(hour, minute)
            app.rescheduleReminder()
        }
    }
}
