package com.github.bernatbc.petitapetit.domain

data class TranscriptWord(val text: String, val startMs: Long, val endMs: Long)

data class TranscriptSegment(val words: List<TranscriptWord>) {
    val startMs: Long get() = words.first().startMs

    // Number of words that have started by the given playback position.
    fun spokenWords(positionMs: Long): Int = words.count { it.startMs <= positionMs }
}

// Index of the last segment that has started at the given position, or -1 before the first one.
fun List<TranscriptSegment>.activeSegmentIndex(positionMs: Long): Int {
    var low = 0
    var high = lastIndex
    var result = -1
    while (low <= high) {
        val mid = (low + high) ushr 1
        if (this[mid].startMs <= positionMs) {
            result = mid
            low = mid + 1
        } else {
            high = mid - 1
        }
    }
    return result
}
