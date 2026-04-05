package com.example.pennychallenge

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.penny.DayPickerDialog
import com.example.penny.formatDate
import com.example.pennychallenge.ui.theme.PennyChallengeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PennyChallengeTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    PennyChallengePage(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun PennyChallengePage(
    modifier: Modifier = Modifier,
    viewModel: PennyChallengeViewModel = viewModel()
) {
    // Collect the single UI state snapshot from the ViewModel
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // --- Date display ---
        Text(
            text = formatDate(uiState.selectedDateMillis),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(16.dp)
        )

        if (uiState.selectedDateMillis != null) {
            Text(
                text = "add today: £${formatCurrencyText(uiState.numberOfDays.toLong())}",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(8.dp)
            )
            Text(
                text = "expected to date : £${formatCurrencyText(uiState.totalPennies)}",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(8.dp)
            )
        }

        // --- Day picker ---
        Button(onClick = { viewModel.onShowDayPicker() }) {
            Text("Pick Day")
        }

        // --- Suggested top-up ---
        Text(
            text = "suggested top-up: £${viewModel.suggestedTopUpText}",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(8.dp)
        )

        // --- Top-Up ---
        TextField(
            value = uiState.topUpValueText,
            onValueChange = { value -> viewModel.onTopUpTextChanged(value) },
            label = { Text("Top-Up (GBP)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.padding(8.dp)
        )
        Button(onClick = { viewModel.topUpBalance() }) {
            Text("Top-Up")
        }

        // --- Withdraw ---
        TextField(
            value = uiState.withdrawValueText,
            onValueChange = { value -> viewModel.onWithdrawTextChanged(value) },
            label = { Text("Withdraw (GBP)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.padding(8.dp)
        )
        Button(onClick = { viewModel.withdrawBalance() }) {
            Text("Withdraw")
        }

        // --- Manual balance edit ---
        TextField(
            value = uiState.piggyBankBalanceText,
            onValueChange = { value -> viewModel.onBalanceTextChanged(value) },
            label = { Text("Update (GBP)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.padding(8.dp)
        )

        Text(
            text = "total balance : £${formatCurrencyText(uiState.piggyBankBalance)}",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(8.dp)
        )

        // --- Day Picker Dialog ---
        if (uiState.showDayPicker) {
            DayPickerDialog(
                onDismiss = { viewModel.onDayPickerDismissed() },
                onConfirm = { dateMillis ->
                    dateMillis?.let { viewModel.onDateSelected(it) }
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PennyChallengePagePreview() {
    PennyChallengeTheme {
        PennyChallengePage()
    }
}
