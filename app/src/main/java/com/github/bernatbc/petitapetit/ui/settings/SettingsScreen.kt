package com.github.bernatbc.petitapetit.ui.settings

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.github.bernatbc.petitapetit.R
import com.github.bernatbc.petitapetit.domain.ReminderSettings

@Composable
fun SettingsScreen(
    reminder: ReminderSettings,
    onEnabledChange: (Boolean) -> Unit,
    onTimeChange: (hour: Int, minute: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var notificationsAllowed by remember {
        mutableStateOf(NotificationManagerCompat.from(context).areNotificationsEnabled())
    }
    var showTimePicker by remember { mutableStateOf(false) }

    // The user may change the permission in the system settings and come back.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        notificationsAllowed = NotificationManagerCompat.from(context).areNotificationsEnabled()
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { notificationsAllowed = NotificationManagerCompat.from(context).areNotificationsEnabled() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(stringResource(R.string.settings_reminder), style = MaterialTheme.typography.titleMedium)
            Switch(
                checked = reminder.enabled,
                onCheckedChange = { enabled ->
                    onEnabledChange(enabled)
                    if (enabled && !notificationsAllowed && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                },
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(stringResource(R.string.settings_reminder_time))
            OutlinedButton(onClick = { showTimePicker = true }, enabled = reminder.enabled) {
                Text("%02d:%02d".format(reminder.hour, reminder.minute))
            }
        }

        if (reminder.enabled && !notificationsAllowed) {
            Text(
                stringResource(R.string.settings_notifications_blocked),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )
            OutlinedButton(
                onClick = { context.startActivity(notificationSettingsIntent(context.packageName)) },
            ) { Text(stringResource(R.string.settings_open_notification_settings)) }
        }
    }

    if (showTimePicker) {
        TimeDialog(
            hour = reminder.hour,
            minute = reminder.minute,
            onConfirm = { hour, minute ->
                showTimePicker = false
                onTimeChange(hour, minute)
            },
            onDismiss = { showTimePicker = false },
        )
    }
}

private fun notificationSettingsIntent(packageName: String): Intent =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
    } else {
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", packageName, null))
    }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimeDialog(
    hour: Int,
    minute: Int,
    onConfirm: (Int, Int) -> Unit,
    onDismiss: () -> Unit,
) {
    val state = rememberTimePickerState(initialHour = hour, initialMinute = minute, is24Hour = true)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_reminder_time)) },
        text = { TimePicker(state = state) },
        confirmButton = {
            TextButton(onClick = { onConfirm(state.hour, state.minute) }) {
                Text(stringResource(R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        },
    )
}
