package com.example.penny

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import java.util.Calendar

/**
 * A reusable Time Picker Dialog component
 *
 * @param onDismiss Callback when dialog is dismissed
 * @param onConfirm Callback when time is confirmed, returns hour (0-23) and minute (0-59)
 * @param initialHour Initial hour to display (0-23), defaults to current hour
 * @param initialMinute Initial minute to display (0-59), defaults to current minute
 * @param is24Hour Whether to use 24-hour format, defaults to false (12-hour format)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: (hour: Int, minute: Int) -> Unit,
    initialHour: Int = Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
    initialMinute: Int = Calendar.getInstance().get(Calendar.MINUTE),
    is24Hour: Boolean = false
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = is24Hour
    )

    TimePickerDialog(
        onDismiss = onDismiss,
        onConfirm = { onConfirm(timePickerState.hour, timePickerState.minute) }
    ) {
        TimePicker(
            state = timePickerState,
            colors = TimePickerDefaults.colors()
        )
    }
}

/**
 * Base Time Picker Dialog with custom content
 */
@Composable
fun TimePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    content: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("OK")
            }
        },
        text = {
            content()
        }
    )
}


