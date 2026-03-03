package com.example.pennychallenge

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.penny.DayPickerDialog
import com.example.penny.calculateDaysSince
import com.example.penny.calculateSavingsFunctional
import com.example.penny.formatDate
import com.example.pennychallenge.ui.theme.PennyChallengeTheme

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
    var showDayPicker by remember { mutableStateOf(false) }
    val currentTime = System.currentTimeMillis()
    var selectedDateMillis by remember { mutableStateOf<Long?>(currentTime) }
    var numberOfDays by remember { mutableStateOf(calculateDaysSince(currentTime)) }
    var totalPennies by remember { mutableStateOf(calculateSavingsFunctional(calculateDaysSince(currentTime))) }

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
                text = "Days: $numberOfDays",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(8.dp)
            )

            Text(
                text = "Total Pennies: $totalPennies",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(8.dp)
            )

            Text(
                text = "Total: £${totalPennies / 100.0}",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(8.dp)
            )
        }

        Button(onClick = { showDayPicker = true }) {
            Text("Pick Day")
        }

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