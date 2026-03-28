//
//  PennyChallengeViewModel.swift
//  PennyChallenge
//
//  ViewModel for managing the Penny Challenge UI state and logic

import Foundation
import Combine

// MARK: - UI State Model
struct PennyChallengeUiState: Equatable {
    var piggyBankBalance: Int = 17000 // Default: £170.00 in pence
    var piggyBankBalanceText: String = formatCurrencyText(17000)
    var topUpValue: Int = 0
    var topUpValueText: String = "0.00"
    var withdrawValue: Int = 0
    var withdrawValueText: String = "0.00"
    var showDayPicker: Bool = false
    var selectedDateMillis: Int? = Int(Date().timeIntervalSince1970 * 1000)
    var numberOfDays: Int = 0
    var totalPennies: Int = 0
}

// MARK: - Currency Formatting
func formatCurrencyText(_ pence: Int) -> String {
    let pounds = Double(pence) / 100.0
    return String(format: "%.2f", pounds)
}

// MARK: - ViewModel
@MainActor
class PennyChallengeViewModel: ObservableObject {
    @Published var uiState = PennyChallengeUiState()
    
    private let persistenceManager = BalancePersistenceManager.shared
    private var cancellables = Set<AnyCancellable>()
    
    private let defaultPiggyBankBalance = 17000 // £170.00 in pence
    private let piggyBankPrefsKey = "piggy_bank_balance"
    
    init() {
        loadInitialState()
        setupObservers()
    }
    
    // MARK: - Initialization
    private func loadInitialState() {
        let storedBalance = persistenceManager.loadBalance() ?? defaultPiggyBankBalance
        let currentTime = Int(Date().timeIntervalSince1970 * 1000)
        let days = calculateDaysSince(currentTime)
        let totalPennies = calculateSavingsFunctional(days)
        
        uiState = PennyChallengeUiState(
            piggyBankBalance: storedBalance,
            piggyBankBalanceText: formatCurrencyText(storedBalance),
            selectedDateMillis: currentTime,
            numberOfDays: days,
            totalPennies: totalPennies
        )
    }
    
    private func setupObservers() {
        // Save balance to both UserDefaults and Firebase when balance changes
        NotificationCenter.default.publisher(for: UIApplication.willTerminateNotification)
            .sink { [weak self] _ in
                self?.persistBalance()
            }
            .store(in: &cancellables)
        
        NotificationCenter.default.publisher(for: UIApplication.didEnterBackgroundNotification)
            .sink { [weak self] _ in
                self?.persistBalance()
            }
            .store(in: &cancellables)
    }
    
    // MARK: - Computed Properties
    var suggestedTopUpText: String {
        let difference = uiState.totalPennies - uiState.piggyBankBalance
        return formatCurrencyText(difference)
    }
    
    // MARK: - Top-Up Logic
    func onTopUpTextChanged(_ input: String) {
        if let parsed = Double(input) {
            let penceValue = Int(parsed * 100)
            uiState.topUpValueText = input
            uiState.topUpValue = penceValue
        } else if input.isEmpty {
            uiState.topUpValueText = input
            uiState.topUpValue = 0
        } else {
            uiState.topUpValueText = input
        }
    }
    
    func topUpBalance() {
        let newBalance = uiState.piggyBankBalance + uiState.topUpValue
        uiState.piggyBankBalance = newBalance
        uiState.piggyBankBalanceText = formatCurrencyText(newBalance)
        uiState.topUpValueText = "0.00"
        uiState.topUpValue = 0
        persistBalance()
    }
    
    // MARK: - Withdraw Logic
    func onWithdrawTextChanged(_ input: String) {
        if let parsed = Double(input) {
            let penceValue = Int(parsed * 100)
            uiState.withdrawValueText = input
            uiState.withdrawValue = penceValue
        } else if input.isEmpty {
            uiState.withdrawValueText = input
            uiState.withdrawValue = 0
        } else {
            uiState.withdrawValueText = input
        }
    }
    
    func withdrawBalance() {
        let newBalance = uiState.piggyBankBalance - uiState.withdrawValue
        uiState.piggyBankBalance = newBalance
        uiState.piggyBankBalanceText = formatCurrencyText(newBalance)
        uiState.withdrawValueText = "0.00"
        uiState.withdrawValue = 0
        persistBalance()
    }
    
    // MARK: - Balance Edit Logic
    func onBalanceTextChanged(_ input: String) {
        if let parsed = Double(input) {
            let penceValue = Int(parsed * 100)
            uiState.piggyBankBalanceText = input
            uiState.piggyBankBalance = penceValue
        } else if input.isEmpty {
            uiState.piggyBankBalanceText = input
        } else {
            uiState.piggyBankBalanceText = input
        }
    }
    
    // MARK: - Day Picker Logic
    func onShowDayPicker() {
        uiState.showDayPicker = true
    }
    
    func onDayPickerDismissed() {
        uiState.showDayPicker = false
    }
    
    func onDateSelected(_ date: Date) {
        let dateMillis = Int(date.timeIntervalSince1970 * 1000)
        let days = calculateDaysSince(dateMillis)
        let totalPennies = calculateSavingsFunctional(days)
        
        uiState.selectedDateMillis = dateMillis
        uiState.numberOfDays = days
        uiState.totalPennies = totalPennies
        uiState.showDayPicker = false
    }
    
    // MARK: - Persistence
    private func persistBalance() {
        // Save to UserDefaults (local storage)
        persistenceManager.saveBalance(uiState.piggyBankBalance)
        
        // Sync to Firebase asynchronously
        Task {
            await persistenceManager.syncToFirebase(balance: uiState.piggyBankBalance)
        }
    }
    
    deinit {
        // Ensure data is saved when ViewModel is destroyed
        persistBalance()
    }
}

// MARK: - Utility Functions
func calculateDaysSince(_ selectedDate: Int) -> Int {
    let selectedDateSeconds = TimeInterval(selectedDate) / 1000.0
    let selectedDateObj = Date(timeIntervalSince1970: selectedDateSeconds)
    
    var calendar = Calendar.current
    calendar.timeZone = TimeZone.current
    
    let components = calendar.dateComponents([.year], from: selectedDateObj)
    guard let year = components.year else { return 0 }
    
    let startOfYear = calendar.date(from: DateComponents(year: year, month: 1, day: 1)) ?? Date()
    let diffInSeconds = selectedDateObj.timeIntervalSince(startOfYear)
    let daysDiff = Int(diffInSeconds / (24 * 60 * 60))
    
    // Add 1 to make it inclusive (day 1 is January 1st itself)
    return daysDiff + 1
}

func calculateSavingsFunctional(_ days: Int) -> Int {
    return (1...days).reduce(0, +)
}

func formatDate(_ dateMillis: Int?, pattern: String = "MMM dd, yyyy") -> String {
    guard let dateMillis = dateMillis else {
        return "No date selected"
    }
    
    let dateSeconds = TimeInterval(dateMillis) / 1000.0
    let date = Date(timeIntervalSince1970: dateSeconds)
    
    let formatter = DateFormatter()
    formatter.dateFormat = pattern
    formatter.locale = Locale.current
    
    return formatter.string(from: date)
}

