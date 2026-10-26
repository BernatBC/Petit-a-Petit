package com.github.bernatbc.petitapetit.ui.review

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.github.bernatbc.petitapetit.R
import com.github.bernatbc.petitapetit.domain.Card
import com.github.bernatbc.petitapetit.domain.Grade

@Composable
fun ReviewScreen(
    state: ReviewUiState,
    onReveal: () -> Unit,
    onGrade: (Grade) -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        val card = state.current
        when {
            state.loading -> CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))

            card == null -> {
                Text(
                    text = if (state.total == 0) {
                        stringResource(R.string.review_nothing)
                    } else {
                        pluralStringResource(R.plurals.review_done, state.completed, state.completed)
                    },
                    style = MaterialTheme.typography.titleMedium,
                )
                Button(onClick = onContinue, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.review_continue))
                }
            }

            else -> {
                Text(
                    stringResource(R.string.review_progress, state.completed + 1, state.total),
                    style = MaterialTheme.typography.labelLarge,
                )
                LinearProgressIndicator(
                    progress = { state.completed.toFloat() / state.total },
                    modifier = Modifier.fillMaxWidth(),
                )
                CardFace(card = card, revealed = state.revealed)
                if (state.revealed) {
                    GradeButtons(onGrade)
                } else {
                    Button(onClick = onReveal, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.review_reveal))
                    }
                }
                TextButton(onClick = onContinue, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.review_skip))
                }
            }
        }
    }
}

@Composable
private fun CardFace(card: Card, revealed: Boolean) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = card.word,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            if (revealed) {
                Text(
                    text = card.translation,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                )
                if (card.sentence.isNotBlank()) {
                    Text(
                        text = card.sentence,
                        style = MaterialTheme.typography.bodyMedium,
                        fontStyle = FontStyle.Italic,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

@Composable
private fun GradeButtons(onGrade: (Grade) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            GradeButton(R.string.grade_again, Modifier.weight(1f)) { onGrade(Grade.AGAIN) }
            GradeButton(R.string.grade_hard, Modifier.weight(1f)) { onGrade(Grade.HARD) }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            GradeButton(R.string.grade_good, Modifier.weight(1f)) { onGrade(Grade.GOOD) }
            GradeButton(R.string.grade_easy, Modifier.weight(1f)) { onGrade(Grade.EASY) }
        }
    }
}

@Composable
private fun GradeButton(label: Int, modifier: Modifier, onClick: () -> Unit) {
    FilledTonalButton(onClick = onClick, modifier = modifier) { Text(stringResource(label)) }
}
