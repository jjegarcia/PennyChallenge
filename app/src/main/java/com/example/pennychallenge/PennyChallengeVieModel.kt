package com.example.pennychallenge

import android.app.Application
import android.content.Context
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.penny.calculateDaysSince
import com.example.penny.calculateSavingsFunctional
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.roundToLong

private const val PIGGY_BANK_PREFS = "piggy_bank_prefs"
private const val PIGGY_BANK_BALANCE_KEY = "piggy_bank_balance"
private const val DEFAULT_PIGGY_BANK_BALANCE = 170L
private const val FIRESTORE_BALANCE_USER = "tin"

// Represents the full UI state as a single immutable snapshot
data class PennyChallengeUiState(
    val piggyBankBalance: Long = DEFAULT_PIGGY_BANK_BALANCE,
    val piggyBankBalanceText: String = formatCurrencyText(DEFAULT_PIGGY_BANK_BALANCE),
    val topUpValue: Long = 0L,
    val topUpValueText: String = "0.00",
    val withdrawValue: Long = 0L,
    val withdrawValueText: String = "0.00",
    val showDayPicker: Boolean = false,
    val selectedDateMillis: Long? = System.currentTimeMillis(),
    val numberOfDays: Int = 0,
    val totalPennies: Long = 0L,
)

// Top-level so it can be used both in the ViewModel and UiState defaults
fun formatCurrencyText(pence: Long): String =
    String.format(Locale.UK, "%.2f", pence / 100.0)

class PennyChallengeViewModel(application: Application) : AndroidViewModel(application) {

    private val sharedPreferences = application.getSharedPreferences(
        PIGGY_BANK_PREFS,
        Context.MODE_PRIVATE
    )

    private val _uiState = MutableStateFlow(buildInitialState())
    val uiState: StateFlow<PennyChallengeUiState> = _uiState.asStateFlow()
    private val firestore = FirebaseFirestore.getInstance()

    init {
        syncBalanceFromFirestore()
    }

    // ------------------------------------------------------------------
    // Initialisation
    // ------------------------------------------------------------------

    private fun buildInitialState(): PennyChallengeUiState {
        val storedBalance = sharedPreferences.getLong(
            PIGGY_BANK_BALANCE_KEY,
            DEFAULT_PIGGY_BANK_BALANCE
        )
        val currentTime = System.currentTimeMillis()
        val days = calculateDaysSince(currentTime)
        val totalPennies = calculateSavingsFunctional(days)

        return PennyChallengeUiState(
            piggyBankBalance = storedBalance,
            piggyBankBalanceText = formatCurrencyText(storedBalance),
            selectedDateMillis = currentTime,
            numberOfDays = days,
            totalPennies = totalPennies,
        )
    }

    private fun syncBalanceFromFirestore() {
        FirestoreBalanceHelper.fetchBalanceForUser(
            firestore = firestore,
            user = FIRESTORE_BALANCE_USER,
            onSuccess = { remoteBalance ->
                if (remoteBalance == null) return@fetchBalanceForUser
                persistBalance(remoteBalance)
                _uiState.update { state ->
                    state.copy(
                        piggyBankBalance = remoteBalance,
                        piggyBankBalanceText = formatCurrencyText(remoteBalance)
                    )
                }
            },
            onFailure = {
                // Keep local/default balance if Firestore is unavailable.
            }
        )
    }

    fun syncStoredBalanceToFirestore() {
        val storedBalance = sharedPreferences.getLong(
            PIGGY_BANK_BALANCE_KEY,
            DEFAULT_PIGGY_BANK_BALANCE
        )

        FirestoreBalanceHelper.upsertBalanceForUser(
            firestore = firestore,
            user = FIRESTORE_BALANCE_USER,
            balance = storedBalance,
            onSuccess = {},
            onFailure = {
                // Keep local data; a failed exit sync should not affect app flow.
            }
        )
    }

    // ------------------------------------------------------------------
    // Derived value — exposed as a convenience property
    // ------------------------------------------------------------------

    val suggestedTopUpText: String
        get() = formatCurrencyText(
            _uiState.value.totalPennies - _uiState.value.piggyBankBalance
        )

    // ------------------------------------------------------------------
    // Top-Up
    // ------------------------------------------------------------------

    fun onTopUpTextChanged(input: String) {
        val parsed = input.toDoubleOrNull()
        _uiState.update { state ->
            state.copy(
                topUpValueText = input,
                topUpValue = if (parsed != null) (parsed * 100).roundToLong()
                else state.topUpValue
            )
        }
    }

    fun topUpBalance() {
        viewModelScope.launch {
            _uiState.update { state ->
                val newBalance = state.piggyBankBalance + state.topUpValue
                persistBalance(newBalance)
                state.copy(
                    piggyBankBalance = newBalance,
                    piggyBankBalanceText = formatCurrencyText(newBalance)
                )
            }
        }
    }

    // ------------------------------------------------------------------
    // Withdraw
    // ------------------------------------------------------------------

    fun onWithdrawTextChanged(input: String) {
        val parsed = input.toDoubleOrNull()
        _uiState.update { state ->
            state.copy(
                withdrawValueText = input,
                withdrawValue = if (parsed != null) (parsed * 100).roundToLong()
                else state.withdrawValue
            )
        }
    }

    fun withdrawBalance() {
        viewModelScope.launch {
            _uiState.update { state ->
                val newBalance = state.piggyBankBalance - state.withdrawValue
                persistBalance(newBalance)
                state.copy(
                    piggyBankBalance = newBalance,
                    piggyBankBalanceText = formatCurrencyText(newBalance)
                )
            }
        }
    }

    // ------------------------------------------------------------------
    // Manual balance edit
    // ------------------------------------------------------------------

    fun onBalanceTextChanged(input: String) {
        val parsed = input.toDoubleOrNull()
        _uiState.update { state ->
            state.copy(
                piggyBankBalanceText = input,
                piggyBankBalance = if (parsed != null) (parsed * 100).roundToLong()
                else state.piggyBankBalance
            )
        }
    }

    // ------------------------------------------------------------------
    // Day Picker
    // ------------------------------------------------------------------

    fun onShowDayPicker() {
        _uiState.update { it.copy(showDayPicker = true) }
    }

    fun onDayPickerDismissed() {
        _uiState.update { it.copy(showDayPicker = false) }
    }

    fun onDateSelected(dateMillis: Long) {
        val days = calculateDaysSince(dateMillis)
        val totalPennies = calculateSavingsFunctional(days)
        _uiState.update { state ->
            state.copy(
                selectedDateMillis = dateMillis,
                numberOfDays = days,
                totalPennies = totalPennies,
                showDayPicker = false
            )
        }
    }

    // ------------------------------------------------------------------
    // Persistence
    // ------------------------------------------------------------------

    private fun persistBalance(balance: Long) {
        sharedPreferences.edit {
            putLong(PIGGY_BANK_BALANCE_KEY, balance)
        }
    }

    override fun onCleared() {
        syncStoredBalanceToFirestore()
        super.onCleared()
    }
}
