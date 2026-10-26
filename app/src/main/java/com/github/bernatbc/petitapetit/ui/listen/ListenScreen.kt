package com.github.bernatbc.petitapetit.ui.listen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.github.bernatbc.petitapetit.R
import com.github.bernatbc.petitapetit.ui.components.Hairline
import com.github.bernatbc.petitapetit.ui.components.InkButton
import com.github.bernatbc.petitapetit.ui.components.QuietButton
import java.util.Locale

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
        Box(modifier = Modifier.weight(1f)) {
            when {
                state.loading -> Status(stringResource(R.string.listen_loading), loading = true)

                state.episode == null -> Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(
                        stringResource(R.string.listen_error),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                    QuietButton(text = stringResource(R.string.retry), onClick = onRetry)
                }

                else -> Column(modifier = Modifier.fillMaxSize()) {
                    PlayerControls(
                        state = state,
                        onTogglePlayback = onTogglePlayback,
                        onSeek = onSeek,
                        onSkip = onSkip,
                        onCycleSpeed = onCycleSpeed,
                        modifier = Modifier.padding(horizontal = 24.dp),
                    )
                    Hairline()
                    when {
                        state.transcriptLoading -> Status(stringResource(R.string.transcript_loading), loading = true)

                        state.transcript.isEmpty() -> Status(stringResource(R.string.transcript_unavailable))

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
        }

        Hairline()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            QuietButton(
                text = stringResource(R.string.open_website),
                onClick = { onOpenWebsite(state.episode?.pageUrl) },
            )
            InkButton(
                text = stringResource(R.string.listened_done),
                onClick = onDone,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun Status(message: String, loading: Boolean = false) {
    Row(
        modifier = Modifier.padding(24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 1.5.dp,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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
    val ink = MaterialTheme.colorScheme.onSurface
    val sliderColors = SliderDefaults.colors(
        thumbColor = ink,
        activeTrackColor = ink,
        inactiveTrackColor = MaterialTheme.colorScheme.outlineVariant,
        disabledThumbColor = MaterialTheme.colorScheme.outlineVariant,
        disabledActiveTrackColor = MaterialTheme.colorScheme.outlineVariant,
        disabledInactiveTrackColor = MaterialTheme.colorScheme.outlineVariant,
    )

    Column(modifier = modifier.padding(top = 4.dp, bottom = 8.dp)) {
        Text(episode.title, style = MaterialTheme.typography.titleMedium, maxLines = 2)

        Slider(
            value = dragPosition ?: state.positionMs.toFloat().coerceAtMost(duration),
            onValueChange = { dragPosition = it },
            onValueChangeFinished = {
                dragPosition?.let { onSeek(it.toLong()) }
                dragPosition = null
            },
            valueRange = 0f..duration,
            enabled = state.durationMs > 0,
            colors = sliderColors,
            thumb = { Box(Modifier.size(12.dp).background(ink, CircleShape)) },
            track = { sliderState ->
                SliderDefaults.Track(
                    sliderState = sliderState,
                    modifier = Modifier.height(2.dp),
                    colors = sliderColors,
                    thumbTrackGapSize = 0.dp,
                    trackInsideCornerSize = 0.dp,
                    drawStopIndicator = null,
                )
            },
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            val muted = MaterialTheme.colorScheme.onSurfaceVariant
            Text(formatTime((dragPosition ?: state.positionMs.toFloat()).toLong()), style = MaterialTheme.typography.labelSmall, color = muted)
            Text(formatTime(state.durationMs), style = MaterialTheme.typography.labelSmall, color = muted)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            QuietButton(text = "−10 s", onClick = { onSkip(-10_000) })
            PlayPauseButton(
                isPlaying = state.isPlaying,
                label = stringResource(if (state.isPlaying) R.string.pause else R.string.play),
                onClick = onTogglePlayback,
            )
            QuietButton(text = "+10 s", onClick = { onSkip(10_000) })
            QuietButton(text = "${state.speed}×".uppercase(Locale.ROOT), onClick = onCycleSpeed)
        }
    }
}

@Composable
private fun PlayPauseButton(isPlaying: Boolean, label: String, onClick: () -> Unit) {
    val ink = MaterialTheme.colorScheme.onSurface
    Box(
        modifier = Modifier
            .size(60.dp)
            .clip(CircleShape)
            .border(1.dp, ink, CircleShape)
            .clickable(onClickLabel = label, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.size(22.dp)) {
            if (isPlaying) {
                val bar = size.width * 0.28f
                drawRect(ink, Offset(size.width * 0.12f, 0f), Size(bar, size.height))
                drawRect(ink, Offset(size.width * 0.60f, 0f), Size(bar, size.height))
            } else {
                val triangle = Path().apply {
                    moveTo(size.width * 0.15f, 0f)
                    lineTo(size.width, size.height / 2)
                    lineTo(size.width * 0.15f, size.height)
                    close()
                }
                drawPath(triangle, ink)
            }
        }
    }
}

internal fun formatTime(millis: Long): String {
    val totalSeconds = (millis / 1000).coerceAtLeast(0)
    return "%d:%02d".format(totalSeconds / 60, totalSeconds % 60)
}
