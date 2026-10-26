package com.github.bernatbc.petitapetit

import android.app.Application
import com.github.bernatbc.petitapetit.data.WordRepository
import com.github.bernatbc.petitapetit.data.translation.FallbackTranslator
import com.github.bernatbc.petitapetit.data.translation.MlKitTranslator
import com.github.bernatbc.petitapetit.data.translation.MyMemoryTranslator
import com.github.bernatbc.petitapetit.data.translation.WordTranslator

class PetitApplication : Application() {

    val wordRepository: WordRepository by lazy { WordRepository(this) }

    val translator: WordTranslator by lazy {
        FallbackTranslator(MlKitTranslator(), MyMemoryTranslator())
    }

    override fun onCreate() {
        super.onCreate()
        // Starts the on-device model download and opens the database early.
        translator
        wordRepository
    }
}
