package com.github.bernatbc.petitapetit.ui.write

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.github.bernatbc.petitapetit.data.Draft
import com.github.bernatbc.petitapetit.data.DraftRepository
import com.github.bernatbc.petitapetit.domain.PromptBuilder
import kotlinx.coroutines.launch

class WriteViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = DraftRepository(application)

    var topic by mutableStateOf("")
        private set
    var summary by mutableStateOf("")
        private set

    val wordCount: Int get() = PromptBuilder.countWords(summary)
    val canContinue: Boolean get() = wordCount >= PromptBuilder.MIN_SUMMARY_WORDS

    init {
        viewModelScope.launch {
            val saved = repository.load()
            if (topic.isEmpty() && summary.isEmpty()) {
                topic = saved.topic
                summary = saved.summary
            }
        }
    }

    fun onTopicChange(value: String) {
        topic = value
        persist()
    }

    fun onSummaryChange(value: String) {
        summary = value
        persist()
    }

    private fun persist() {
        val draft = Draft(topic = topic, summary = summary)
        viewModelScope.launch { repository.save(draft) }
    }
}
