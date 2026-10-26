package com.github.bernatbc.petitapetit.ui.dictionary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.github.bernatbc.petitapetit.R
import com.github.bernatbc.petitapetit.domain.Card
import com.github.bernatbc.petitapetit.ui.components.AppTextField
import com.github.bernatbc.petitapetit.ui.components.Hairline
import com.github.bernatbc.petitapetit.ui.components.InkButton
import com.github.bernatbc.petitapetit.ui.components.LineButton
import java.util.Locale

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
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
    ) {
        item {
            Column {
                AppTextField(
                    value = word,
                    onValueChange = onWordChange,
                    label = stringResource(R.string.dictionary_word_label),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                )
                Spacer(Modifier.height(8.dp))
                AppTextField(
                    value = translation,
                    onValueChange = onTranslationChange,
                    label = stringResource(R.string.dictionary_translation_label),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                )
                Spacer(Modifier.height(20.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    LineButton(
                        text = stringResource(R.string.dictionary_translate),
                        onClick = onTranslate,
                        enabled = word.isNotBlank() && !busy,
                        modifier = Modifier.weight(1f),
                    )
                    InkButton(
                        text = stringResource(R.string.dictionary_add),
                        onClick = onAdd,
                        enabled = word.isNotBlank() && !busy,
                        modifier = Modifier.weight(1f),
                    )
                }
                if (message != null) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = stringResource(message),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(Modifier.height(32.dp))
                Text(
                    text = if (cards.isEmpty()) {
                        stringResource(R.string.dictionary_empty)
                    } else {
                        pluralStringResource(R.plurals.dictionary_count, cards.size, cards.size)
                    }.uppercase(Locale.ROOT),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
                Hairline()
            }
        }
        items(cards, key = { it.id }) { card ->
            CardRow(card, onDelete = { onDelete(card) })
            Hairline()
        }
    }
}

@Composable
private fun CardRow(card: Card, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = card.word, style = MaterialTheme.typography.titleMedium)
            Text(
                text = card.translation,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary,
            )
            if (card.sentence.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = card.sentence,
                    style = MaterialTheme.typography.bodySmall,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                )
            }
        }
        IconButton(onClick = onDelete) {
            Icon(
                imageVector = Icons.Filled.Delete,
                contentDescription = stringResource(R.string.dictionary_delete),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
