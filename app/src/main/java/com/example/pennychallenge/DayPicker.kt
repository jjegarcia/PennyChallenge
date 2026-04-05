package com.example.penny

import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.div
import kotlin.text.toInt

/**
 * A reusable Day Picker Dialog component using Material3 DatePicker
 *
 * @param onDismiss Callback when dialog is dismissed
 * @param onConfirm Callback when date is confirmed, returns the selected date in milliseconds (epoch time)
 * @param initialSelectedDateMillis Initial date to display in milliseconds, defaults to today
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayPickerDialog(
    onDismiss: () -> Unit,
    onConfirm: (dateMillis: Long?) -> Unit,
    initialSelectedDateMillis: Long? = System.currentTimeMillis()
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialSelectedDateMillis
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = { onConfirm(datePickerState.selectedDateMillis) }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

/**
 * Utility function to format date from milliseconds
 *
 * @param dateMillis Date in milliseconds (epoch time)
 * @param pattern Date format pattern (e.g., "MMM dd, yyyy", "dd/MM/yyyy")
 * @return Formatted date string
 */
fun formatDate(dateMillis: Long?, pattern: String = "MMM dd, yyyy"): String {
    return if (dateMillis != null) {
        val date = Date(dateMillis)
        SimpleDateFormat(pattern, Locale.getDefault()).format(date)
    } else {
        "No date selected"
    }
}

/**
 * Calculate the number of pennies saved based on the penny challenge
 * Day 1: 1 penny, Day 2: 2 pennies, Day 3: 3 pennies, etc.
 *
 * @param days Number of days in the challenge
 * @return Total number of pennies saved
 */
fun calculateSavingsFunctional(days: Int): Long {
    return (1..days.toLong()).sum()
}

/**
 * Calculate the number of days from January 1st of the current year to the selected date
 *
 * @param selectedDate Selected date in milliseconds (epoch time)
 * @return Number of days from January 1st of current year to selected date (inclusive)
 */
fun calculateDaysSince(selectedDate: Long?): Int {
    val dateMillis = selectedDate ?: return 0

    val calendar = java.util.Calendar.getInstance()
    calendar.timeInMillis = dateMillis

// Get January 1st of the same year as the selected date
    val startOfYear = java.util.Calendar.getInstance()
    startOfYear.set(calendar.get(java.util.Calendar.YEAR), 0, 1, 0, 0, 0)
    startOfYear.set(java.util.Calendar.MILLISECOND, 0)

    val diffInMillis = dateMillis - startOfYear.timeInMillis
    val daysDiff = (diffInMillis / (1000 * 60 * 60 * 24)).toInt()

// Add 1 to make it inclusive (day 1 is January 1st itself)
    return daysDiff + 1
}

