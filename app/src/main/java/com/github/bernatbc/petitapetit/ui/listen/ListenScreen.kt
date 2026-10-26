package com.github.bernatbc.petitapetit.ui.listen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
    onRetry: () -> Unit,
    onOpenWebsite: (String?) -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(stringResource(R.string.listen_intro), style = MaterialTheme.typography.bodyLarge)

        when {
            state.loading -> Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                CircularProgressIndicator()
                Text(stringResource(R.string.listen_loading))
            }

            state.episode == null -> {
                Text(
                    stringResource(R.string.listen_error),
                    color = MaterialTheme.colorScheme.error,
                )
                Button(onClick = onRetry) { Text(stringResource(R.string.retry)) }
            }

            else -> Player(state, onTogglePlayback, onSeek, onSkip, onCycleSpeed)
        }

        OutlinedButton(
            onClick = { onOpenWebsite(state.episode?.pageUrl) },
            modifier = Modifier.fillMaxWidth(),
        ) { Text(stringResource(R.string.open_website)) }

        Button(onClick = onDone, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.listened_done))
        }
    }
}

@Composable
private fun Player(
    state: ListenUiState,
    onTogglePlayback: () -> Unit,
    onSeek: (Long) -> Unit,
    onSkip: (Long) -> Unit,
    onCycleSpeed: () -> Unit,
) {
    val episode = checkNotNull(state.episode)
    var dragPosition by remember { mutableStateOf<Float?>(null) }
    val duration = state.durationMs.coerceAtLeast(1L).toFloat()

    Text(episode.title, style = MaterialTheme.typography.titleMedium)
    if (episode.description.isNotEmpty()) {
        Text(episode.description, style = MaterialTheme.typography.bodyMedium)
    }

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
    }
    TextButton(onClick = onCycleSpeed, modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.playback_speed, "${state.speed}×"))
    }
}

internal fun formatTime(millis: Long): String {
    val totalSeconds = (millis / 1000).coerceAtLeast(0)
    return "%d:%02d".format(totalSeconds / 60, totalSeconds % 60)
}
