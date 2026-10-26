package com.github.bernatbc.petitapetit.ui.dictionary

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.github.bernatbc.petitapetit.PetitApplication
import com.github.bernatbc.petitapetit.R
import com.github.bernatbc.petitapetit.domain.Card
import com.github.bernatbc.petitapetit.domain.WordLookup
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DictionaryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as PetitApplication).wordRepository
    private val translator = (application as PetitApplication).translator

    val cards: StateFlow<List<Card>> = repository.cards

    var word by mutableStateOf("")
        private set
    var translation by mutableStateOf("")
        private set
    var busy by mutableStateOf(false)
        private set
    var message by mutableStateOf<Int?>(null)
        private set

    fun onWordChange(value: String) {
        word = value
        message = null
    }

    fun onTranslationChange(value: String) {
        translation = value
        message = null
    }

    fun translate() {
        val lookup = WordLookup.normalize(word)
        if (lookup.isEmpty() || busy) return
        viewModelScope.launch { fetchTranslation(lookup) }
    }

    fun add() {
        val entry = word.trim()
        if (entry.isEmpty() || busy) return
        viewModelScope.launch {
            if (translation.isBlank() && !fetchTranslation(WordLookup.normalize(entry))) return@launch
            val added = repository.add(entry, translation, sentence = "", now = System.currentTimeMillis())
            if (added) {
                word = ""
                translation = ""
                message = R.string.dictionary_added
            } else {
                message = R.string.dictionary_duplicate
            }
        }
    }

    fun delete(card: Card) {
        viewModelScope.launch { repository.delete(card.id) }
    }

    private suspend fun fetchTranslation(lookup: String): Boolean {
        busy = true
        message = null
        try {
            translation = WordLookup.matchCase(lookup, translator.translate(lookup))
            return true
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            message = R.string.dictionary_translation_failed
            return false
        } finally {
            busy = false
        }
    }
}
