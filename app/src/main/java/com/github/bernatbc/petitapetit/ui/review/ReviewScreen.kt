package com.github.bernatbc.petitapetit.ui.review

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.github.bernatbc.petitapetit.R
import com.github.bernatbc.petitapetit.domain.Card
import com.github.bernatbc.petitapetit.domain.Grade
import com.github.bernatbc.petitapetit.ui.components.Hairline
import com.github.bernatbc.petitapetit.ui.components.InkButton
import com.github.bernatbc.petitapetit.ui.components.QuietButton
import java.util.Locale

@Composable
fun ReviewScreen(
    state: ReviewUiState,
    onReveal: () -> Unit,
    onGrade: (Grade) -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val card = state.current
    Column(modifier = modifier.fillMaxSize()) {
        when {
            state.loading -> Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onSurface)
            }

            card == null -> {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = if (state.total == 0) {
                            stringResource(R.string.review_nothing)
                        } else {
                            pluralStringResource(R.plurals.review_done, state.completed, state.completed)
                        },
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center,
                    )
                }
                Hairline()
                InkButton(
                    text = stringResource(R.string.review_continue),
                    onClick = onContinue,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                )
            }

            else -> {
                Progress(state)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    contentAlignment = Alignment.Center,
                ) { CardFace(card = card, revealed = state.revealed) }
                Hairline()
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (state.revealed) {
                        GradeBar(onGrade)
                    } else {
                        InkButton(
                            text = stringResource(R.string.review_reveal),
                            onClick = onReveal,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    QuietButton(
                        text = stringResource(R.string.review_skip),
                        onClick = onContinue,
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                    )
                }
            }
        }
    }
}

@Composable
private fun Progress(state: ReviewUiState) {
    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
        Text(
            text = stringResource(R.string.review_progress, state.completed + 1, state.total).uppercase(Locale.ROOT),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outlineVariant),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(state.completed.toFloat() / state.total.coerceAtLeast(1))
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.onSurface),
            )
        }
    }
}

@Composable
private fun CardFace(card: Card, revealed: Boolean) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text(
            text = card.word,
            style = MaterialTheme.typography.displayMedium,
            textAlign = TextAlign.Center,
        )
        if (revealed) {
            Box(
                modifier = Modifier
                    .width(32.dp)
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.onSurfaceVariant),
            )
            Text(
                text = card.translation,
                style = MaterialTheme.typography.headlineSmall,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.secondary,
                textAlign = TextAlign.Center,
            )
            if (card.sentence.isNotBlank()) {
                Text(
                    text = card.sentence,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

// Four equal cells in one outlined bar.
@Composable
private fun GradeBar(onGrade: (Grade) -> Unit) {
    val line = MaterialTheme.colorScheme.outlineVariant
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .border(BorderStroke(1.dp, line), MaterialTheme.shapes.medium),
    ) {
        val cells = listOf(
            Triple(R.string.grade_again, Grade.AGAIN, MaterialTheme.colorScheme.error),
            Triple(R.string.grade_hard, Grade.HARD, MaterialTheme.colorScheme.onSurface),
            Triple(R.string.grade_good, Grade.GOOD, MaterialTheme.colorScheme.onSurface),
            Triple(R.string.grade_easy, Grade.EASY, MaterialTheme.colorScheme.onSurface),
        )
        cells.forEachIndexed { index, (label, grade, color) ->
            if (index > 0) Box(Modifier.width(1.dp).fillMaxHeight().background(line))
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable { onGrade(grade) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(label).uppercase(Locale.ROOT),
                    style = MaterialTheme.typography.labelMedium,
                    color = color,
                    maxLines = 1,
                )
            }
        }
    }
}
