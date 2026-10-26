package com.github.bernatbc.petitapetit.data

import com.github.bernatbc.petitapetit.domain.TranscriptSegment
import com.github.bernatbc.petitapetit.domain.TranscriptWord
import org.json.JSONObject
import java.io.InputStream

object TranscriptParser {

    // Word timings are expressed in frames of a 25 fps clock.
    private const val FRAMES_PER_SECOND = 25L

    fun parse(input: InputStream): List<TranscriptSegment> {
        val root = JSONObject(input.bufferedReader().use { it.readText() })
        val transcriptions = root.optJSONObject("content")
            ?.optJSONObject("sound")
            ?.optJSONArray("transcriptions")
            ?: return emptyList()

        return (0 until transcriptions.length()).mapNotNull { index ->
            val parts = transcriptions.optJSONObject(index)
                ?.optJSONObject("description")
                ?.optJSONArray("parts")
                ?: return@mapNotNull null
            val words = (0 until parts.length()).mapNotNull { partIndex ->
                val part = parts.getJSONObject(partIndex)
                val text = part.optString("word").trim()
                if (text.isEmpty()) return@mapNotNull null
                val start = part.optString("start").toLongOrNull() ?: return@mapNotNull null
                val duration = part.optString("duration").toLongOrNull() ?: 0L
                TranscriptWord(
                    text = text,
                    startMs = start * 1000 / FRAMES_PER_SECOND,
                    endMs = (start + duration) * 1000 / FRAMES_PER_SECOND,
                )
            }
            words.takeIf { it.isNotEmpty() }?.let(::TranscriptSegment)
        }
    }
}
