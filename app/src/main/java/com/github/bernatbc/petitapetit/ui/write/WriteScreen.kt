package com.github.bernatbc.petitapetit.ui.write

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.github.bernatbc.petitapetit.R
import com.github.bernatbc.petitapetit.domain.PromptBuilder

@Composable
fun WriteScreen(
    topic: String,
    summary: String,
    wordCount: Int,
    canContinue: Boolean,
    onTopicChange: (String) -> Unit,
    onSummaryChange: (String) -> Unit,
    onBack: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(stringResource(R.string.write_intro), style = MaterialTheme.typography.bodyLarge)

        OutlinedTextField(
            value = topic,
            onValueChange = onTopicChange,
            label = { Text(stringResource(R.string.write_topic_label)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        OutlinedTextField(
            value = summary,
            onValueChange = onSummaryChange,
            label = { Text(stringResource(R.string.write_summary_label)) },
            supportingText = {
                Text(stringResource(R.string.write_word_count, wordCount, PromptBuilder.MIN_SUMMARY_WORDS))
            },
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            minLines = 8,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 200.dp),
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.back))
            }
            Button(onClick = onNext, enabled = canContinue, modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.next))
            }
        }
    }
}
