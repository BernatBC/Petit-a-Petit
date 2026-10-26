package com.github.bernatbc.petitapetit.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class EpisodeRepository(private val feedUrl: String = FEED_URL) {

    suspend fun fetchEpisodes(): List<Episode> = withContext(Dispatchers.IO) {
        val connection = URL(feedUrl).openConnection() as HttpURLConnection
        try {
            connection.connectTimeout = TIMEOUT_MS
            connection.readTimeout = TIMEOUT_MS
            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                throw IOException("HTTP ${connection.responseCode}")
            }
            RssParser.parse(connection.inputStream)
                .sortedByDescending { it.publishedAtMillis ?: 0L }
        } finally {
            connection.disconnect()
        }
    }

    companion object {
        const val FEED_URL =
            "https://francaisfacile.rfi.fr/fr/podcasts/journal-en-fran%C3%A7ais-facile/podcast"
        const val WEBSITE_URL =
            "https://francaisfacile.rfi.fr/fr/podcasts/journal-en-fran%C3%A7ais-facile/"
        private const val TIMEOUT_MS = 15_000
    }
}
