package com.github.bernatbc.petitapetit

import com.github.bernatbc.petitapetit.data.RssParser
import com.github.bernatbc.petitapetit.domain.PromptBuilder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PromptBuilderTest {

    @Test
    fun promptContainsSummaryAndTopic() {
        val prompt = PromptBuilder.build(summary = "  Le président a parlé.  ", topic = "Politique")
        assertTrue(prompt.contains("« Le président a parlé. »"))
        assertTrue(prompt.contains("Politique"))
        assertTrue(prompt.contains("Étape 1"))
        assertTrue(prompt.contains("Étape 2"))
    }

    @Test
    fun promptOmitsTopicWhenBlank() {
        val prompt = PromptBuilder.build(summary = "Texte", topic = "   ")
        assertTrue(!prompt.contains("L'actualité concerne"))
    }

    @Test
    fun countsWordsIgnoringExtraWhitespace() {
        assertEquals(0, PromptBuilder.countWords("   "))
        assertEquals(3, PromptBuilder.countWords(" un  deux\ntrois "))
    }
}

class RssParserTest {

    private val feed = """
        <?xml version="1.0" encoding="utf-8"?>
        <rss xmlns:itunes="http://www.itunes.com/dtds/podcast-1.0.dtd" version="2.0"><channel>
        <title>Journal</title>
        <item>
          <title>Épisode un</title>
          <link>https://example.org/ep1</link>
          <itunes:subtitle>
        Le journal du vendredi.
        </itunes:subtitle>
          <enclosure url="https://example.org/ep1.mp3?a=1&amp;b=2" type="audio/mpeg" length="0"/>
          <guid isPermaLink="false">guid-1</guid>
          <pubDate>Fri, 23 Oct 2026 20:47:19 +0200</pubDate>
        </item>
        <item><title>Sans audio</title></item>
        </channel></rss>
    """.trimIndent()

    @Test
    fun parsesEpisodesWithAudioOnly() {
        val episodes = RssParser.parse(feed.byteInputStream())
        assertEquals(1, episodes.size)
        val episode = episodes.single()
        assertEquals("guid-1", episode.id)
        assertEquals("Épisode un", episode.title)
        assertEquals("Le journal du vendredi.", episode.description)
        assertEquals("https://example.org/ep1.mp3?a=1&b=2", episode.audioUrl)
        assertEquals("https://example.org/ep1", episode.pageUrl)
        assertEquals(1792781239000L, episode.publishedAtMillis)
    }
}
