package com.github.bernatbc.petitapetit.ui.settings

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
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
import com.github.bernatbc.petitapetit.ui.components.Hairline
import com.github.bernatbc.petitapetit.ui.components.QuietButton

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
            .padding(horizontal = 24.dp),
    ) {
        Hairline()
        SettingRow(label = stringResource(R.string.settings_reminder)) {
            Switch(
                checked = reminder.enabled,
                onCheckedChange = { enabled ->
                    onEnabledChange(enabled)
                    if (enabled && !notificationsAllowed && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.background,
                    checkedTrackColor = MaterialTheme.colorScheme.onSurface,
                    checkedBorderColor = MaterialTheme.colorScheme.onSurface,
                    uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    uncheckedTrackColor = MaterialTheme.colorScheme.background,
                    uncheckedBorderColor = MaterialTheme.colorScheme.outline,
                ),
            )
        }
        Hairline()
        SettingRow(
            label = stringResource(R.string.settings_reminder_time),
            onClick = if (reminder.enabled) ({ showTimePicker = true }) else null,
        ) {
            Text(
                text = "%02d:%02d".format(reminder.hour, reminder.minute),
                style = MaterialTheme.typography.headlineSmall,
                color = if (reminder.enabled) {
                    MaterialTheme.colorScheme.secondary
                } else {
                    MaterialTheme.colorScheme.outlineVariant
                },
            )
        }
        Hairline()

        if (reminder.enabled && !notificationsAllowed) {
            Spacer(Modifier.height(20.dp))
            Text(
                text = stringResource(R.string.settings_notifications_blocked),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
            QuietButton(
                text = stringResource(R.string.settings_open_notification_settings),
                onClick = { context.startActivity(notificationSettingsIntent(context.packageName)) },
            )
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

@Composable
private fun SettingRow(
    label: String,
    onClick: (() -> Unit)? = null,
    trailing: @Composable () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = label, style = MaterialTheme.typography.titleMedium)
        trailing()
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
    val scheme = MaterialTheme.colorScheme
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = scheme.background,
        shape = MaterialTheme.shapes.large,
        tonalElevation = 0.dp,
        title = { Text(stringResource(R.string.settings_reminder_time), style = MaterialTheme.typography.titleLarge) },
        text = {
            TimePicker(
                state = state,
                colors = TimePickerDefaults.colors(
                    containerColor = scheme.background,
                    clockDialColor = scheme.surfaceVariant,
                    clockDialSelectedContentColor = scheme.onPrimary,
                    clockDialUnselectedContentColor = scheme.onSurface,
                    selectorColor = scheme.onSurface,
                    timeSelectorSelectedContainerColor = scheme.primaryContainer,
                    timeSelectorUnselectedContainerColor = scheme.surfaceVariant,
                    timeSelectorSelectedContentColor = scheme.onPrimaryContainer,
                    timeSelectorUnselectedContentColor = scheme.onSurface,
                ),
            )
        },
        confirmButton = {
            QuietButton(text = stringResource(R.string.ok), onClick = { onConfirm(state.hour, state.minute) })
        },
        dismissButton = {
            QuietButton(text = stringResource(R.string.cancel), onClick = onDismiss)
        },
    )
}
