package com.github.bernatbc.petitapetit.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.bernatbc.petitapetit.R
import java.util.Locale

@Composable
fun AppTopBar(
    title: String,
    modifier: Modifier = Modifier,
    step: Int? = null,
    stepCount: Int = 4,
    onBack: (() -> Unit)? = null,
) {
    Column(modifier = modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.statusBars)) {
        if (step != null) {
            StepIndicator(step = step, total = stepCount, modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 12.dp))
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(start = if (onBack != null) 8.dp else 24.dp, end = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back),
                    )
                }
            }
            Column {
                if (step != null) {
                    Text(
                        text = stringResource(R.string.step_caption, step, stepCount).uppercase(Locale.ROOT),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(text = title, style = MaterialTheme.typography.titleLarge)
            }
        }
    }
}
