package com.github.bernatbc.petitapetit.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.github.bernatbc.petitapetit.data.DraftRepository
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val draftRepository = DraftRepository(application)

    // Every daily run starts with an empty summary, whatever happened to the previous one.
    fun startDaily(onReady: () -> Unit) {
        viewModelScope.launch {
            draftRepository.clear()
            onReady()
        }
    }
}
