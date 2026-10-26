package com.github.bernatbc.petitapetit.data

class EpisodeRepository(private val feedUrl: String = FEED_URL) {

    suspend fun fetchEpisodes(): List<Episode> = httpGet(feedUrl) { stream ->
        RssParser.parse(stream).sortedByDescending { it.publishedAtMillis ?: 0L }
    }

    companion object {
        const val FEED_URL =
            "https://francaisfacile.rfi.fr/fr/podcasts/journal-en-fran%C3%A7ais-facile/podcast"
        const val WEBSITE_URL =
            "https://francaisfacile.rfi.fr/fr/podcasts/journal-en-fran%C3%A7ais-facile/"
    }
}
