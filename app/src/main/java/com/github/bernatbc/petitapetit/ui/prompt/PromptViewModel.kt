package com.github.bernatbc.petitapetit.ui.prompt

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.github.bernatbc.petitapetit.data.DraftRepository
import com.github.bernatbc.petitapetit.domain.PromptBuilder
import kotlinx.coroutines.launch

class PromptViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = DraftRepository(application)

    var prompt by mutableStateOf("")
        private set
    var loaded by mutableStateOf(false)
        private set

    init {
        viewModelScope.launch {
            val draft = repository.load()
            prompt = PromptBuilder.build(summary = draft.summary, topic = draft.topic)
            loaded = true
        }
    }

    fun onPromptChange(value: String) {
        prompt = value
    }

    fun restart(onDone: () -> Unit) {
        viewModelScope.launch {
            repository.clear()
            onDone()
        }
    }
}
