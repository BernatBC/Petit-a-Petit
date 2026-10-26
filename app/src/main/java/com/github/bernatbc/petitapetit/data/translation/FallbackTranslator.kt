package com.github.bernatbc.petitapetit.data.translation

import kotlin.coroutines.cancellation.CancellationException

class FallbackTranslator(
    private val primary: WordTranslator,
    private val fallback: WordTranslator,
) : WordTranslator {

    override suspend fun translate(text: String): String {
        val result = try {
            primary.translate(text).trim()
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            ""
        }
        return result.ifEmpty { fallback.translate(text) }
    }
}
