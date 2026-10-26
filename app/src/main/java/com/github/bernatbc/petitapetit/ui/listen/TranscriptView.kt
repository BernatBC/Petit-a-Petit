package com.github.bernatbc.petitapetit.ui.listen

import androidx.compose.foundation.gestures.animateScrollBy
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.github.bernatbc.petitapetit.R
import com.github.bernatbc.petitapetit.domain.TranscriptSegment
import com.github.bernatbc.petitapetit.domain.activeSegmentIndex

@Composable
fun TranscriptView(
    segments: List<TranscriptSegment>,
    positionMs: Long,
    onWordClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    var following by remember { mutableStateOf(true) }
    val activeIndex = segments.activeSegmentIndex(positionMs)

    // A manual drag pauses auto-scroll until the user taps "Suivre la lecture".
    LaunchedEffect(listState) {
        listState.interactionSource.interactions.collect { interaction ->
            if (interaction is DragInteraction.Start) following = false
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
                val currentWord = if (index == activeIndex) spokenWords - 1 else -1
                SegmentText(
                    segment = segment,
                    spokenWords = spokenWords,
                    currentWord = currentWord,
                    onWordClick = onWordClick,
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

@Composable
private fun SegmentText(
    segment: TranscriptSegment,
    spokenWords: Int,
    currentWord: Int,
    onWordClick: (Long) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val text = remember(segment, spokenWords, currentWord, colors, onWordClick) {
        buildAnnotatedString {
            segment.words.forEachIndexed { index, word ->
                val style = when {
                    index == currentWord -> SpanStyle(
                        color = colors.onPrimaryContainer,
                        background = colors.primaryContainer,
                        fontWeight = FontWeight.Bold,
                    )

                    index < spokenWords -> SpanStyle(color = colors.onSurface)
                    else -> SpanStyle(color = colors.onSurface.copy(alpha = 0.6f))
                }
                val link = LinkAnnotation.Clickable(tag = "word-$index") { onWordClick(word.startMs) }
                withLink(link) { withStyle(style) { append(word.text) } }
                append(" ")
            }
        }
    }
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
    )
}
