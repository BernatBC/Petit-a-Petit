package com.github.bernatbc.petitapetit.ui.review

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.github.bernatbc.petitapetit.PetitApplication
import com.github.bernatbc.petitapetit.domain.Card
import com.github.bernatbc.petitapetit.domain.Grade
import com.github.bernatbc.petitapetit.domain.Scheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ReviewUiState(
    val loading: Boolean = true,
    val current: Card? = null,
    val revealed: Boolean = false,
    val total: Int = 0,
    val completed: Int = 0,
)

class ReviewViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as PetitApplication).wordRepository
    private val queue = ArrayDeque<Card>()
    private val _state = MutableStateFlow(ReviewUiState())
    val state: StateFlow<ReviewUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val endOfToday = Scheduler.endOfDay(now)
            val session = Scheduler.selectSession(repository.dueCards(endOfToday), now, endOfToday)
            queue.addAll(session)
            _state.value = ReviewUiState(loading = false, current = queue.firstOrNull(), total = session.size)
        }
    }

    fun reveal() {
        _state.value = _state.value.copy(revealed = true)
    }

    fun grade(grade: Grade) {
        val card = queue.removeFirstOrNull() ?: return
        val updated = Scheduler.review(card, grade, System.currentTimeMillis())
        viewModelScope.launch { repository.update(updated) }
        // A forgotten card comes back before the end of the session.
        if (grade == Grade.AGAIN) queue.addLast(updated)
        _state.value = _state.value.copy(
            current = queue.firstOrNull(),
            revealed = false,
            completed = _state.value.completed + if (grade == Grade.AGAIN) 0 else 1,
        )
    }
}
