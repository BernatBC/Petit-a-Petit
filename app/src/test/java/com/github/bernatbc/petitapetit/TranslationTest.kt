package com.github.bernatbc.petitapetit

import com.github.bernatbc.petitapetit.data.translation.FallbackTranslator
import com.github.bernatbc.petitapetit.data.translation.MyMemoryTranslator
import com.github.bernatbc.petitapetit.data.translation.WordTranslator
import com.github.bernatbc.petitapetit.domain.WordLookup
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.io.IOException

class WordLookupTest {

    @Test
    fun stripsPunctuation() {
        assertEquals("Hébron", WordLookup.normalize("Hébron,"))
        assertEquals("causé", WordLookup.normalize("[causé]"))
        assertEquals("", WordLookup.normalize("—"))
    }

    @Test
    fun stripsElidedArticles() {
        assertEquals("attaque", WordLookup.normalize("l'attaque"))
        assertEquals("Hébron", WordLookup.normalize("d'Hébron,"))
        assertEquals("inquiétude", WordLookup.normalize("L’inquiétude"))
        assertEquals("quelqu'un", WordLookup.normalize("quelqu'un"))
        assertEquals("aujourd'hui", WordLookup.normalize("aujourd'hui"))
    }

    @Test
    fun lowercasesShoutedTranslationsOfLowercaseWords() {
        assertEquals("protesters", WordLookup.matchCase("manifestants", "PROTESTERS"))
        assertEquals("NATO", WordLookup.matchCase("OTAN", "NATO"))
        assertEquals("Hebron", WordLookup.matchCase("Hébron", "Hebron"))
    }
}

class TranslatorTest {

    private class Fake(private val result: Result<String>) : WordTranslator {
        var calls = 0
        override suspend fun translate(text: String): String {
            calls++
            return result.getOrThrow()
        }
    }

    @Test
    fun usesPrimaryWhenItWorks() = runTest {
        val primary = Fake(Result.success("attack"))
        val fallback = Fake(Result.success("stroke"))
        assertEquals("attack", FallbackTranslator(primary, fallback).translate("attaque"))
        assertEquals(0, fallback.calls)
    }

    @Test
    fun fallsBackWhenPrimaryFailsOrIsBlank() = runTest {
        val fallback = Fake(Result.success("attack"))
        val failing = Fake(Result.failure(IllegalStateException("model missing")))
        assertEquals("attack", FallbackTranslator(failing, fallback).translate("attaque"))
        val blank = Fake(Result.success("  "))
        assertEquals("attack", FallbackTranslator(blank, fallback).translate("attaque"))
    }

    @Test
    fun propagatesFallbackFailure() = runTest {
        val failing = Fake(Result.failure(IOException("offline")))
        assertThrows(IOException::class.java) {
            kotlinx.coroutines.runBlocking { FallbackTranslator(failing, failing).translate("x") }
        }
    }

    @Test
    fun parsesMyMemoryResponse() {
        val ok = """{"responseData":{"translatedText":" attack ","match":0.9},"quotaFinished":false,"responseStatus":200}"""
        assertEquals("attack", MyMemoryTranslator().parse(ok.byteInputStream()))
    }

    @Test
    fun rejectsMyMemoryQuotaErrors() {
        val quota = """{"responseData":{"translatedText":"x"},"quotaFinished":true,"responseStatus":200}"""
        assertThrows(IOException::class.java) { MyMemoryTranslator().parse(quota.byteInputStream()) }
        val error = """{"responseData":{"translatedText":"x"},"responseStatus":429}"""
        assertThrows(IOException::class.java) { MyMemoryTranslator().parse(error.byteInputStream()) }
    }
}
