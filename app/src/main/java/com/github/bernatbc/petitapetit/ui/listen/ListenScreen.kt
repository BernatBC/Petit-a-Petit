package com.github.bernatbc.petitapetit.ui.listen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.bernatbc.petitapetit.R

@Composable
fun ListenScreen(
    state: ListenUiState,
    onTogglePlayback: () -> Unit,
    onSeek: (Long) -> Unit,
    onSkip: (Long) -> Unit,
    onCycleSpeed: () -> Unit,
    onWordTap: (String) -> Unit,
    onSaveWord: (String) -> Unit,
    onDismissTranslation: () -> Unit,
    onRetry: () -> Unit,
    onOpenWebsite: (String?) -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        when {
            state.loading -> StatusRow(
                modifier = Modifier.padding(16.dp),
                message = stringResource(R.string.listen_loading),
            )

            state.episode == null -> Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    stringResource(R.string.listen_error),
                    color = MaterialTheme.colorScheme.error,
                )
                Button(onClick = onRetry) { Text(stringResource(R.string.retry)) }
            }

            else -> {
                PlayerControls(
                    state = state,
                    onTogglePlayback = onTogglePlayback,
                    onSeek = onSeek,
                    onSkip = onSkip,
                    onCycleSpeed = onCycleSpeed,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
                HorizontalDivider()
                when {
                    state.transcriptLoading -> StatusRow(
                        modifier = Modifier.padding(16.dp),
                        message = stringResource(R.string.transcript_loading),
                    )

                    state.transcript.isEmpty() -> Text(
                        stringResource(R.string.transcript_unavailable),
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                    )

                    else -> TranscriptView(
                        segments = state.transcript,
                        positionMs = state.positionMs,
                        wordTranslation = state.wordTranslation,
                        savedWords = state.savedWords,
                        onWordTap = onWordTap,
                        onSaveWord = onSaveWord,
                        onDismissTranslation = onDismissTranslation,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        if (state.episode == null || state.transcript.isEmpty()) {
            Column(Modifier.weight(1f)) {}
        }
        HorizontalDivider()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedButton(
                onClick = { onOpenWebsite(state.episode?.pageUrl) },
                modifier = Modifier.weight(1f),
            ) { Text(stringResource(R.string.open_website)) }
            Button(onClick = onDone, modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.listened_done))
            }
        }
    }
}

@Composable
private fun StatusRow(message: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CircularProgressIndicator()
        Text(message)
    }
}

@Composable
private fun PlayerControls(
    state: ListenUiState,
    onTogglePlayback: () -> Unit,
    onSeek: (Long) -> Unit,
    onSkip: (Long) -> Unit,
    onCycleSpeed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val episode = checkNotNull(state.episode)
    var dragPosition by remember { mutableStateOf<Float?>(null) }
    val duration = state.durationMs.coerceAtLeast(1L).toFloat()

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            episode.title,
            style = MaterialTheme.typography.titleSmall,
            maxLines = 2,
        )

        Slider(
            value = dragPosition ?: state.positionMs.toFloat().coerceAtMost(duration),
            onValueChange = { dragPosition = it },
            onValueChangeFinished = {
                dragPosition?.let { onSeek(it.toLong()) }
                dragPosition = null
            },
            valueRange = 0f..duration,
            enabled = state.durationMs > 0,
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(formatTime((dragPosition ?: state.positionMs.toFloat()).toLong()))
            Text(formatTime(state.durationMs))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = { onSkip(-10_000) }) { Text("−10 s") }
            Button(onClick = onTogglePlayback) {
                Text(stringResource(if (state.isPlaying) R.string.pause else R.string.play))
            }
            TextButton(onClick = { onSkip(10_000) }) { Text("+10 s") }
            TextButton(onClick = onCycleSpeed) { Text("${state.speed}×") }
        }
    }
}

internal fun formatTime(millis: Long): String {
    val totalSeconds = (millis / 1000).coerceAtLeast(0)
    return "%d:%02d".format(totalSeconds / 60, totalSeconds % 60)
}
