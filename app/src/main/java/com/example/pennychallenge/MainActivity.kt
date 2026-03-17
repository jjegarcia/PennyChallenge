package com.example.pennychallenge

import android.content.Context
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import com.example.penny.DayPickerDialog
import com.example.penny.calculateDaysSince
import com.example.penny.calculateSavingsFunctional
import com.example.penny.formatDate
import com.example.pennychallenge.ui.theme.PennyChallengeTheme
import java.util.Locale
import kotlin.math.roundToLong

private const val PIGGY_BANK_PREFS = "piggy_bank_prefs"
private const val PIGGY_BANK_BALANCE_KEY = "piggy_bank_balance"
private const val DEFAULT_PIGGY_BANK_BALANCE = 170L

private fun formatCurrencyText(pence: Long): String = String.format(Locale.UK, "%.2f", pence / 100.0)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PennyChallengeTheme() {
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
fun PennyChallengePage(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val sharedPreferences = remember(context) {
        context.getSharedPreferences(PIGGY_BANK_PREFS, Context.MODE_PRIVATE)
    }
    val storedPiggyBankBalance = remember(sharedPreferences) {
        sharedPreferences.getLong(PIGGY_BANK_BALANCE_KEY, DEFAULT_PIGGY_BANK_BALANCE)
    }
    var piggyBankBalance by remember { mutableStateOf(storedPiggyBankBalance) }
    var piggyBankBalanceText by remember {
        mutableStateOf(formatCurrencyText(storedPiggyBankBalance))
    }
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
    val formatCurrencyText = formatCurrencyText(totalPennies - piggyBankBalance)
    fun updateBalance() {
        sharedPreferences.edit {
            putLong(PIGGY_BANK_BALANCE_KEY, piggyBankBalance)
        }
        piggyBankBalanceText = formatCurrencyText(piggyBankBalance)
    }
    fun topUpBalance() {
        piggyBankBalance += topUpValue
        updateBalance()
    }
 fun withdrawBalance() {
        piggyBankBalance -= withdrawValue
        updateBalance()
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
                text = "add today: £${formatCurrencyText(numberOfDays.toLong())}",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(8.dp)
            )
            Text(
                text = "expected to date : £${formatCurrencyText(totalPennies)}",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(8.dp)
            )
        }

        Button(onClick = { showDayPicker = true }) {
            Text("Pick Day")
        }
        Text(
            text = "suggested top-up: £$formatCurrencyText",
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

        Button(onClick = ::topUpBalance) {
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

        Button(onClick = ::withdrawBalance) {
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

        Text(
            text = "total balance : £${formatCurrencyText(piggyBankBalance)}",
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

fun PennyChallengePagePreview() {
    PennyChallengeTheme() {
        PennyChallengePage()
    }
}