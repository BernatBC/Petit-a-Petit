package com.github.bernatbc.petitapetit.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.draftDataStore by preferencesDataStore(name = "draft")

data class Draft(val topic: String = "", val summary: String = "")

class DraftRepository(context: Context) {

    private val dataStore = context.applicationContext.draftDataStore

    suspend fun load(): Draft {
        val preferences = dataStore.data.first()
        return Draft(
            topic = preferences[TOPIC].orEmpty(),
            summary = preferences[SUMMARY].orEmpty(),
        )
    }

    suspend fun save(draft: Draft) {
        dataStore.edit {
            it[TOPIC] = draft.topic
            it[SUMMARY] = draft.summary
        }
    }

    suspend fun clear() {
        dataStore.edit { it.clear() }
    }

    private companion object {
        val TOPIC = stringPreferencesKey("topic")
        val SUMMARY = stringPreferencesKey("summary")
    }
}
