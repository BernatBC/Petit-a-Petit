package com.github.bernatbc.petitapetit.ui.dictionary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import com.github.bernatbc.petitapetit.R
import com.github.bernatbc.petitapetit.domain.Card

@Composable
fun DictionaryScreen(
    cards: List<Card>,
    word: String,
    translation: String,
    busy: Boolean,
    message: Int?,
    onWordChange: (String) -> Unit,
    onTranslationChange: (String) -> Unit,
    onTranslate: () -> Unit,
    onAdd: () -> Unit,
    onDelete: (Card) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = word,
                    onValueChange = onWordChange,
                    label = { Text(stringResource(R.string.dictionary_word_label)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = translation,
                    onValueChange = onTranslationChange,
                    label = { Text(stringResource(R.string.dictionary_translation_label)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = onTranslate,
                        enabled = word.isNotBlank() && !busy,
                        modifier = Modifier.weight(1f),
                    ) { Text(stringResource(R.string.dictionary_translate)) }
                    Button(
                        onClick = onAdd,
                        enabled = word.isNotBlank() && !busy,
                        modifier = Modifier.weight(1f),
                    ) { Text(stringResource(R.string.dictionary_add)) }
                }
                if (message != null) {
                    Text(stringResource(message), style = MaterialTheme.typography.bodyMedium)
                }
                HorizontalDivider(Modifier.padding(top = 8.dp))
                Text(
                    text = if (cards.isEmpty()) {
                        stringResource(R.string.dictionary_empty)
                    } else {
                        pluralStringResource(R.plurals.dictionary_count, cards.size, cards.size)
                    },
                    style = MaterialTheme.typography.titleSmall,
                )
            }
        }
        items(cards, key = { it.id }) { card -> CardRow(card, onDelete = { onDelete(card) }) }
    }
}

@Composable
private fun CardRow(card: Card, onDelete: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${card.word} — ${card.translation}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
            )
            if (card.sentence.isNotBlank()) {
                Text(
                    text = card.sentence,
                    style = MaterialTheme.typography.bodySmall,
                    fontStyle = FontStyle.Italic,
                    maxLines = 2,
                )
            }
        }
        IconButton(onClick = onDelete) {
            Icon(
                imageVector = Icons.Filled.Delete,
                contentDescription = stringResource(R.string.dictionary_delete),
            )
        }
    }
}
