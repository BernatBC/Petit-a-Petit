package com.github.bernatbc.petitapetit

import com.github.bernatbc.petitapetit.data.TranscriptParser
import com.github.bernatbc.petitapetit.domain.TranscriptSegment
import com.github.bernatbc.petitapetit.domain.TranscriptWord
import com.github.bernatbc.petitapetit.domain.activeSegmentIndex
import org.junit.Assert.assertEquals
import org.junit.Test

class TranscriptTest {

    private val json = """
        {"content":{"sound":{"transcriptions":[
          {"content":"Il est","description":{"parts":[
            {"start":"217","duration":"7","word":"Il "},
            {"start":"225","duration":"4","word":"est "}
          ]}},
          {"content":"vide","description":{"parts":[]}},
          {"content":"Merci","description":{"parts":[
            {"start":"293","duration":"9","word":"Merci "},
            {"start":"302","duration":"10","word":"  "}
          ]}}
        ]}}}
    """.trimIndent()

    @Test
    fun parsesWordsAndConvertsFramesToMilliseconds() {
        val segments = TranscriptParser.parse(json.byteInputStream())
        assertEquals(2, segments.size)
        assertEquals(listOf("Il", "est"), segments[0].words.map { it.text })
        assertEquals(TranscriptWord("Il", 8680, 8960), segments[0].words[0])
        assertEquals(listOf("Merci"), segments[1].words.map { it.text })
    }

    @Test
    fun missingTranscriptionsYieldsEmptyList() {
        assertEquals(emptyList<TranscriptSegment>(), TranscriptParser.parse("""{"content":{}}""".byteInputStream()))
    }

    private val segments = listOf(
        TranscriptSegment(listOf(TranscriptWord("a", 1000, 1500), TranscriptWord("b", 1500, 2000))),
        TranscriptSegment(listOf(TranscriptWord("c", 3000, 3500))),
    )

    @Test
    fun findsActiveSegment() {
        assertEquals(-1, segments.activeSegmentIndex(500))
        assertEquals(0, segments.activeSegmentIndex(1000))
        assertEquals(0, segments.activeSegmentIndex(2999))
        assertEquals(1, segments.activeSegmentIndex(3000))
        assertEquals(1, segments.activeSegmentIndex(999_999))
    }

    @Test
    fun countsSpokenWords() {
        assertEquals(0, segments[0].spokenWords(999))
        assertEquals(1, segments[0].spokenWords(1200))
        assertEquals(2, segments[0].spokenWords(1500))
    }
}
