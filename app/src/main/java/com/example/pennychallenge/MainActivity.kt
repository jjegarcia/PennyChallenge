package com.example.pennychallenge

import android.R
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.penny.DayPickerDialog
import com.example.penny.calculateDaysSince
import com.example.penny.calculateSavingsFunctional
import com.example.penny.formatDate
import com.example.pennychallenge.ui.theme.PennyChallengeTheme
import kotlin.math.roundToLong

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PennyChallengeTheme() {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    DayPickerDemo(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun DayPickerDemo(modifier: Modifier = Modifier) {
    var piggyBankBalance by remember { mutableStateOf(0L) }
    var piggyBankBalanceText by remember { mutableStateOf("0.00") }
    var topUpValue by remember { mutableStateOf(0L) }
    var topUpValueText by remember { mutableStateOf("0.00") }
    var withdrawValue by remember { mutableStateOf(0L) }
    var withdrawValueText by remember { mutableStateOf("0.00") }
    var showDayPicker by remember { mutableStateOf(false) }
    val currentTime = System.currentTimeMillis()
    var selectedDateMillis by remember { mutableStateOf<Long?>(currentTime) }
    var numberOfDays by remember { mutableStateOf(calculateDaysSince(currentTime)) }
    var totalPennies by remember {
        mutableStateOf(
            calculateSavingsFunctional(
                calculateDaysSince(
                    currentTime
                )
            )
        )
    }

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = formatDate(selectedDateMillis),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(16.dp)
        )

        if (selectedDateMillis != null) {
            Text(
                text = "add today: £${String.format("%.2f", numberOfDays / 100.0)}",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(8.dp)
            )
            Text(
                text = "expected to date : £${String.format("%.2f", totalPennies / 100.0)}",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(8.dp)
            )
        }

        Button(onClick = { showDayPicker = true }) {
            Text("Pick Day")
        }
        Text(
            text = "suggested top-up: £${String.format("%.2f", (piggyBankBalance-totalPennies) / 100.0)}",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(8.dp)
        )
             TextField(
            value = topUpValueText,
            onValueChange = { input ->
                topUpValueText = input
                val parsed = input.toDoubleOrNull()
                if (parsed != null) {
                    topUpValue = (parsed * 100).roundToLong()
                }
            },
            label = { Text("Top-Up (GBP)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.padding(8.dp)
        )

        Button(onClick = { }) {
            Text("Top-Up")
        }
             TextField(
            value = withdrawValueText,
            onValueChange = { input ->
                withdrawValueText = input
                val parsed = input.toDoubleOrNull()
                if (parsed != null) {
                    withdrawValue = (parsed * 100).roundToLong()
                }
            },
            label = { Text("Withdraw (GBP)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.padding(8.dp)
        )

        Button(onClick = { }) {
            Text("Withdraw")
        }
             TextField(
            value = piggyBankBalanceText,
            onValueChange = { input ->
                piggyBankBalanceText = input
                val parsed = input.toDoubleOrNull()
                if (parsed != null) {
                    piggyBankBalance = (parsed * 100).roundToLong()
                }
            },
            label = { Text("Update (GBP)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.padding(8.dp)
        )

        Button(onClick = { }) {
            Text("Update")
        }

        Text(
            text = "total balance : £${String.format("%.2f", piggyBankBalance / 100.0)}",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(8.dp)
        )


        if (showDayPicker) {
            DayPickerDialog(
                onDismiss = { showDayPicker = false },
                onConfirm = { dateMillis ->
                    selectedDateMillis = dateMillis
                    numberOfDays = calculateDaysSince(dateMillis)
                    totalPennies = calculateSavingsFunctional(numberOfDays)
                    showDayPicker = false
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable

fun DayPickerDemoPreview() {
    PennyChallengeTheme() {
        DayPickerDemo()
    }
}