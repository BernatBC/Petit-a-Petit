package com.github.bernatbc.petitapetit.ui.prompt

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.bernatbc.petitapetit.R

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
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(stringResource(R.string.prompt_intro), style = MaterialTheme.typography.bodyLarge)

        OutlinedTextField(
            value = prompt,
            onValueChange = onPromptChange,
            label = { Text(stringResource(R.string.prompt_label)) },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 240.dp),
        )

        Button(
            onClick = {
                copyPrompt(context, prompt)
                openGemini(context)
            },
            modifier = Modifier.fillMaxWidth(),
        ) { Text(stringResource(R.string.prompt_copy_and_open)) }

        OutlinedButton(
            onClick = { copyPrompt(context, prompt) },
            modifier = Modifier.fillMaxWidth(),
        ) { Text(stringResource(R.string.prompt_copy_only)) }

        TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.back))
        }
        TextButton(onClick = onRestart, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.prompt_restart))
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
