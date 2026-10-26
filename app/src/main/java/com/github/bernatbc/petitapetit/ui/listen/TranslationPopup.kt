package com.github.bernatbc.petitapetit.ui.listen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import com.github.bernatbc.petitapetit.R

// Non-focusable so that taps elsewhere (and playback) are never intercepted by the popup.
@Composable
fun TranslationPopup(
    anchor: Rect,
    translation: WordTranslation,
    saved: Boolean,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
) {
    Popup(
        popupPositionProvider = WordPopupPositionProvider(anchor),
        properties = PopupProperties(focusable = false, dismissOnClickOutside = false),
    ) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.inverseSurface,
            contentColor = MaterialTheme.colorScheme.inverseOnSurface,
            shadowElevation = 6.dp,
            modifier = Modifier.clickable(onClick = onDismiss),
        ) {
            Row(
                modifier = Modifier.padding(start = 12.dp, top = 4.dp, bottom = 4.dp, end = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    Text(
                        text = translation.word,
                        style = MaterialTheme.typography.labelMedium,
                    )
                    when {
                        translation.translation != null -> Text(
                            text = translation.translation,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )

                        translation.failed -> Text(
                            text = stringResource(R.string.translation_failed),
                            style = MaterialTheme.typography.bodyMedium,
                        )

                        else -> CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    }
                }
                if (translation.translation != null) {
                    IconButton(onClick = onSave, enabled = !saved) {
                        Icon(
                            imageVector = if (saved) Icons.Filled.Check else Icons.Filled.Add,
                            contentDescription = stringResource(
                                if (saved) R.string.word_saved else R.string.save_word,
                            ),
                        )
                    }
                }
            }
        }
    }
}

private class WordPopupPositionProvider(private val word: Rect) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset {
        val gap = 8
        val centerX = anchorBounds.left + word.center.x.toInt()
        val x = (centerX - popupContentSize.width / 2)
            .coerceIn(0, (windowSize.width - popupContentSize.width).coerceAtLeast(0))
        val above = anchorBounds.top + word.top.toInt() - popupContentSize.height - gap
        val y = if (above >= 0) above else anchorBounds.top + word.bottom.toInt() + gap
        return IntOffset(x, y)
    }
}
