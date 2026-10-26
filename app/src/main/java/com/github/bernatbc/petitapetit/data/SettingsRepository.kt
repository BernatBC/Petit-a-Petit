package com.github.bernatbc.petitapetit.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.github.bernatbc.petitapetit.domain.ReminderSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore(name = "settings")

class SettingsRepository(context: Context) {

    private val dataStore = context.applicationContext.settingsDataStore

    val reminder: Flow<ReminderSettings> = dataStore.data.map { preferences ->
        val defaults = ReminderSettings()
        ReminderSettings(
            enabled = preferences[ENABLED] ?: defaults.enabled,
            hour = preferences[HOUR] ?: defaults.hour,
            minute = preferences[MINUTE] ?: defaults.minute,
        )
    }

    suspend fun setReminderEnabled(enabled: Boolean) {
        dataStore.edit { it[ENABLED] = enabled }
    }

    suspend fun setReminderTime(hour: Int, minute: Int) {
        dataStore.edit {
            it[HOUR] = hour
            it[MINUTE] = minute
        }
    }

    private companion object {
        val ENABLED = booleanPreferencesKey("reminder_enabled")
        val HOUR = intPreferencesKey("reminder_hour")
        val MINUTE = intPreferencesKey("reminder_minute")
    }
}
