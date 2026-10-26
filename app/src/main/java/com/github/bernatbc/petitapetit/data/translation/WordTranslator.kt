package com.github.bernatbc.petitapetit.data.translation

interface WordTranslator {
    suspend fun translate(text: String): String
}
