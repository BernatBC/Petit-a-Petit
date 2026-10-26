package com.github.bernatbc.petitapetit.ui.listen

import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.github.bernatbc.petitapetit.R
import com.github.bernatbc.petitapetit.data.WordRepository
import com.github.bernatbc.petitapetit.domain.TranscriptSegment
import com.github.bernatbc.petitapetit.domain.activeSegmentIndex
import kotlinx.coroutines.delay

private const val TRANSLATION_VISIBLE_MS = 6_000L

private data class WordSelection(val segment: Int, val word: Int)

@Composable
fun TranscriptView(
    segments: List<TranscriptSegment>,
    positionMs: Long,
    wordTranslation: WordTranslation?,
    savedWords: Set<String>,
    onWordTap: (String) -> Unit,
    onSaveWord: (String) -> Unit,
    onDismissTranslation: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    var following by remember { mutableStateOf(true) }
    var selection by remember { mutableStateOf<WordSelection?>(null) }
    val activeIndex = segments.activeSegmentIndex(positionMs)

    fun dismiss() {
        selection = null
        onDismissTranslation()
    }

    // A manual drag pauses auto-scroll until the user taps "Suivre la lecture".
    LaunchedEffect(listState) {
        listState.interactionSource.interactions.collect { interaction ->
            if (interaction is DragInteraction.Start) {
                following = false
                dismiss()
            }
        }
    }

    LaunchedEffect(activeIndex, following) {
        if (!following || activeIndex < 0) return@LaunchedEffect
        val target = listState.layoutInfo.visibleItemsInfo.firstOrNull { it.index == activeIndex }
        if (target == null) {
            listState.animateScrollToItem(activeIndex)
        } else {
            val anchor = listState.layoutInfo.viewportSize.height / 4
            val delta = target.offset - anchor
            if (delta != 0) listState.animateScrollBy(delta.toFloat())
        }
    }

    val translationDone = wordTranslation != null &&
        (wordTranslation.translation != null || wordTranslation.failed)
    LaunchedEffect(selection, translationDone) {
        if (selection != null && translationDone) {
            delay(TRANSLATION_VISIBLE_MS)
            dismiss()
        }
    }

    Box(modifier = modifier) {
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        ) {
            itemsIndexed(segments) { index, segment ->
                val spokenWords = when {
                    index < activeIndex -> segment.words.size
                    index == activeIndex -> segment.spokenWords(positionMs)
                    else -> 0
                }
                val selectedWord = selection?.takeIf { it.segment == index }?.word ?: -1
                SegmentText(
                    segment = segment,
                    spokenWords = spokenWords,
                    currentWord = if (index == activeIndex) spokenWords - 1 else -1,
                    selectedWord = selectedWord,
                    translation = if (selectedWord >= 0) wordTranslation else null,
                    saved = selectedWord >= 0 && wordTranslation != null &&
                        WordRepository.keyOf(wordTranslation.word) in savedWords,
                    onSaveWord = onSaveWord,
                    onWordTap = { wordIndex ->
                        if (selection == WordSelection(index, wordIndex)) {
                            dismiss()
                        } else {
                            selection = WordSelection(index, wordIndex)
                            onWordTap(segment.words[wordIndex].text)
                        }
                    },
                    onDismissTranslation = ::dismiss,
                )
            }
        }

        if (!following) {
            FilledTonalButton(
                onClick = { following = true },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp),
            ) { Text(stringResource(R.string.transcript_follow)) }
        }
    }
}

private class WordRanges(val ranges: List<IntRange>)

@Composable
private fun SegmentText(
    segment: TranscriptSegment,
    spokenWords: Int,
    currentWord: Int,
    selectedWord: Int,
    translation: WordTranslation?,
    saved: Boolean,
    onSaveWord: (String) -> Unit,
    onWordTap: (Int) -> Unit,
    onDismissTranslation: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    var layout by remember { mutableStateOf<TextLayoutResult?>(null) }
    var tappedBox by remember { mutableStateOf<Rect?>(null) }
    val currentOnWordTap by rememberUpdatedState(onWordTap)

    val built = remember(segment, spokenWords, currentWord, selectedWord, colors) {
        val ranges = ArrayList<IntRange>(segment.words.size)
        val text = buildAnnotatedString {
            segment.words.forEachIndexed { index, word ->
                val style = when {
                    index == currentWord -> SpanStyle(
                        color = colors.onPrimaryContainer,
                        background = colors.primaryContainer,
                        fontWeight = FontWeight.Bold,
                    )

                    index == selectedWord -> SpanStyle(
                        color = colors.onSecondaryContainer,
                        background = colors.secondaryContainer,
                    )

                    index < spokenWords -> SpanStyle(color = colors.onSurface)
                    else -> SpanStyle(color = colors.onSurface.copy(alpha = 0.6f))
                }
                val start = length
                withStyle(style) { append(word.text) }
                ranges += start until length
                append(" ")
            }
        }
        text to WordRanges(ranges)
    }
    val (text, wordRanges) = built

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            onTextLayout = { layout = it },
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(wordRanges) {
                    detectTapGestures { position ->
                        val result = layout ?: return@detectTapGestures
                        val offset = result.getOffsetForPosition(position)
                        val index = wordRanges.ranges.indexOfFirst { offset >= it.first && offset <= it.last + 1 }
                        if (index >= 0) {
                            val range = wordRanges.ranges[index]
                            tappedBox = result.getBoundingBox(offset.coerceIn(range.first, range.last))
                            currentOnWordTap(index)
                        }
                    }
                },
        )

        val anchor = tappedBox
        if (selectedWord >= 0 && translation != null && anchor != null) {
            TranslationPopup(
                anchor = anchor,
                translation = translation,
                saved = saved,
                onSave = { onSaveWord(segment.words.joinToString(" ") { it.text }) },
                onDismiss = onDismissTranslation,
            )
        }
    }
}
