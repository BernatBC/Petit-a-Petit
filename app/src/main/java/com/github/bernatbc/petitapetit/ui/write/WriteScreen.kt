package com.github.bernatbc.petitapetit.ui.write

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.github.bernatbc.petitapetit.R
import com.github.bernatbc.petitapetit.domain.PromptBuilder
import com.github.bernatbc.petitapetit.ui.components.AppTextField
import com.github.bernatbc.petitapetit.ui.components.Hairline
import com.github.bernatbc.petitapetit.ui.components.InkButton
import com.github.bernatbc.petitapetit.ui.components.QuietButton

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
    Column(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
        ) {
            Text(
                text = stringResource(R.string.write_intro),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(24.dp))
            AppTextField(
                value = topic,
                onValueChange = onTopicChange,
                label = stringResource(R.string.write_topic_label),
                singleLine = true,
            )
            Spacer(Modifier.height(16.dp))
            AppTextField(
                value = summary,
                onValueChange = onSummaryChange,
                label = stringResource(R.string.write_summary_label),
                textStyle = MaterialTheme.typography.bodyLarge,
                supportingText = stringResource(R.string.write_word_count, wordCount, PromptBuilder.MIN_SUMMARY_WORDS),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                minLines = 8,
                modifier = Modifier.heightIn(min = 240.dp),
            )
            Spacer(Modifier.height(16.dp))
        }
        Hairline()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            QuietButton(text = stringResource(R.string.back), onClick = onBack)
            InkButton(
                text = stringResource(R.string.next),
                onClick = onNext,
                enabled = canContinue,
                modifier = Modifier.weight(1f),
            )
        }
    }
}
