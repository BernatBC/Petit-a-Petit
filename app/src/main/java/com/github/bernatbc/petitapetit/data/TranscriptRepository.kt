package com.github.bernatbc.petitapetit.data

import com.github.bernatbc.petitapetit.domain.TranscriptSegment

class TranscriptRepository {

    suspend fun fetchTranscript(episodeId: String): List<TranscriptSegment> =
        httpGet("$MEDIA_URL/$episodeId/0", TranscriptParser::parse)

    private companion object {
        const val MEDIA_URL = "https://francaisfacile.rfi.fr/fr/_ws/media/editions"
    }
}
