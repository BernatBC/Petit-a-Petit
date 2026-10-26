package com.github.bernatbc.petitapetit.ui.listen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
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
    val edgeMargin = with(LocalDensity.current) { 16.dp.roundToPx() }
    Popup(
        popupPositionProvider = WordPopupPositionProvider(anchor, edgeMargin),
        properties = PopupProperties(focusable = false, dismissOnClickOutside = false),
    ) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.inverseSurface,
            contentColor = MaterialTheme.colorScheme.inverseOnSurface,
            shadowElevation = 8.dp,
            modifier = Modifier.clickable(onClick = onDismiss),
        ) {
            Row(
                modifier = Modifier.padding(start = 16.dp, top = 6.dp, bottom = 6.dp, end = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    Text(
                        text = translation.word,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.inverseOnSurface.copy(alpha = 0.65f),
                    )
                    when {
                        translation.translation != null -> Text(
                            text = translation.translation,
                            style = MaterialTheme.typography.titleMedium,
                            fontStyle = FontStyle.Italic,
                        )

                        translation.failed -> Text(
                            text = stringResource(R.string.translation_failed),
                            style = MaterialTheme.typography.bodyMedium,
                        )

                        else -> CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 1.5.dp,
                            color = MaterialTheme.colorScheme.inverseOnSurface,
                        )
                    }
                }
                if (translation.translation != null) {
                    IconButton(onClick = onSave, enabled = !saved) {
                        Icon(
                            imageVector = if (saved) Icons.Filled.Check else Icons.Filled.Add,
                            contentDescription = stringResource(
                                if (saved) R.string.word_saved else R.string.save_word,
                            ),
                            tint = MaterialTheme.colorScheme.inverseOnSurface.copy(alpha = if (saved) 0.6f else 1f),
                        )
                    }
                }
            }
        }
    }
}

private class WordPopupPositionProvider(
    private val word: Rect,
    private val edgeMargin: Int,
) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset {
        val gap = 8
        val centerX = anchorBounds.left + word.center.x.toInt()
        val x = (centerX - popupContentSize.width / 2)
            .coerceIn(edgeMargin, (windowSize.width - popupContentSize.width - edgeMargin).coerceAtLeast(edgeMargin))
        val above = anchorBounds.top + word.top.toInt() - popupContentSize.height - gap
        val y = if (above >= 0) above else anchorBounds.top + word.bottom.toInt() + gap
        return IntOffset(x, y)
    }
}
