package com.github.bernatbc.petitapetit.ui.prompt

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.bernatbc.petitapetit.R
import com.github.bernatbc.petitapetit.ui.components.AppTextField
import com.github.bernatbc.petitapetit.ui.components.Hairline
import com.github.bernatbc.petitapetit.ui.components.InkButton
import com.github.bernatbc.petitapetit.ui.components.LineButton
import com.github.bernatbc.petitapetit.ui.components.QuietButton

private const val GEMINI_PACKAGE = "com.google.android.apps.bard"
private const val GEMINI_WEB_URL = "https://gemini.google.com/app"

@Composable
fun PromptScreen(
    prompt: String,
    onPromptChange: (String) -> Unit,
    onBack: () -> Unit,
    onRestart: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    Column(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
        ) {
            Text(
                text = stringResource(R.string.prompt_intro),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(20.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                AppTextField(
                    value = prompt,
                    onValueChange = onPromptChange,
                    label = stringResource(R.string.prompt_label),
                    textStyle = MaterialTheme.typography.bodyMedium,
                    minLines = 10,
                )
            }
            Spacer(Modifier.height(16.dp))
        }
        Hairline()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            InkButton(
                text = stringResource(R.string.prompt_copy_and_open),
                onClick = {
                    copyPrompt(context, prompt)
                    openGemini(context)
                },
                modifier = Modifier.fillMaxWidth(),
            )
            LineButton(
                text = stringResource(R.string.prompt_copy_only),
                onClick = { copyPrompt(context, prompt) },
                modifier = Modifier.fillMaxWidth(),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                QuietButton(text = stringResource(R.string.back), onClick = onBack)
                QuietButton(text = stringResource(R.string.prompt_restart), onClick = onRestart)
            }
        }
    }
}

private fun copyPrompt(context: Context, prompt: String) {
    val clipboard = context.getSystemService(ClipboardManager::class.java)
    clipboard.setPrimaryClip(
        ClipData.newPlainText(context.getString(R.string.prompt_clipboard_label), prompt),
    )
    Toast.makeText(context, R.string.prompt_copied, Toast.LENGTH_SHORT).show()
}

private fun openGemini(context: Context) {
    val launchIntent = context.packageManager.getLaunchIntentForPackage(GEMINI_PACKAGE)
    try {
        if (launchIntent != null) {
            context.startActivity(launchIntent)
        } else {
            Toast.makeText(context, R.string.prompt_gemini_missing, Toast.LENGTH_SHORT).show()
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(GEMINI_WEB_URL)))
        }
    } catch (_: ActivityNotFoundException) {
        // No Gemini app and no browser: the prompt is still on the clipboard.
    }
}
