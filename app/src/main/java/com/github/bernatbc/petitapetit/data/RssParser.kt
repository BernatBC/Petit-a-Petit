package com.github.bernatbc.petitapetit.data

import org.w3c.dom.Element
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Locale
import javax.xml.parsers.DocumentBuilderFactory

object RssParser {

    fun parse(input: InputStream): List<Episode> {
        val factory = DocumentBuilderFactory.newInstance()
        val document = input.use { factory.newDocumentBuilder().parse(it) }
        val items = document.getElementsByTagName("item")
        return (0 until items.length).mapNotNull { index ->
            toEpisode(items.item(index) as Element)
        }
    }

    private fun toEpisode(item: Element): Episode? {
        val audioUrl = item.child("enclosure")?.getAttribute("url")?.takeIf { it.isNotBlank() }
            ?: return null
        val title = item.text("title").orEmpty()
        val guid = item.text("guid") ?: audioUrl
        return Episode(
            id = guid,
            title = title,
            description = item.text("itunes:subtitle")?.trim().orEmpty(),
            audioUrl = audioUrl,
            pageUrl = item.text("link").orEmpty(),
            publishedAtMillis = item.text("pubDate")?.let(::parseDate),
        )
    }

    private fun Element.child(tag: String): Element? =
        getElementsByTagName(tag).item(0) as? Element

    private fun Element.text(tag: String): String? =
        child(tag)?.textContent?.trim()?.takeIf { it.isNotEmpty() }

    private fun parseDate(value: String): Long? = runCatching {
        SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH).parse(value)?.time
    }.getOrNull()
}
