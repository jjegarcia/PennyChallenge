// Quick Reference - Code Snippets for Common Tasks

// ============================================
// DEBUGGING UserDefaults
// ============================================

// In Xcode Console (LLDB):
po UserDefaults.standard.integer(forKey: "piggy_bank_balance")

// In Swift code:
let balance = UserDefaults.standard.integer(forKey: "piggy_bank_balance")
print("Balance: £\(formatCurrencyText(balance))")

// View all stored values:
po UserDefaults.standard.dictionaryRepresentation()

// Clear all data (for testing):
UserDefaults.standard.removeObject(forKey: "piggy_bank_balance")

// ============================================
// FIREBASE DEBUGGING
// ============================================

// Check if Firebase is initialized:
import FirebaseCore
print("Firebase App: \(FirebaseApp.app() != nil ? "✅ Initialized" : "❌ Not initialized")")

// Check Firestore document in console:
// 1. Go to Firebase Console
// 2. Firestore Database → piggybank-balance collection
// 3. Look for document with ID "tin"
// 4. Check "balance" and "user" fields

// Log Firebase operations:
// Add this to FirebaseManager:
print("📝 Attempting Firebase sync...")
print("   User: tin")
print("   Balance: £\(formatCurrencyText(balance))")

// ============================================
// ADDING DEBUG UI BUTTONS
// ============================================

// Add this to ContentView for debugging:
VStack {
    // ... existing UI ...
    
    #if DEBUG
    Divider()
    Text("Debug Controls")
        .font(.caption)
    
    HStack {
        Button("Clear Data") {
            UserDefaults.standard.removeObject(forKey: "piggy_bank_balance")
            viewModel.loadInitialState()
        }
        .buttonStyle(.bordered)
        
        Button("Print Balance") {
            let balance = UserDefaults.standard.integer(forKey: "piggy_bank_balance")
            print("💾 Balance: \(balance) pence = £\(formatCurrencyText(balance))")
        }
        .buttonStyle(.bordered)
    }
    #endif
}

// ============================================
// TESTING PERSISTENCE
// ============================================

// 1. Run app on simulator
// 2. Change balance to £99.99 (9999 pence)
// 3. Close app (Cmd+Q or home button in simulator)
// 4. Reopen app
// 5. Check if balance shows £99.99

// 6. Add print statement in BalancePersistenceManager:
func saveBalance(_ balance: Int) {
    defaults.set(balance, forKey: userDefaultsKey)
    print("✅ Balance saved: £\(formatCurrencyText(balance))")
}

func loadBalance() -> Int? {
    let value = defaults.integer(forKey: userDefaultsKey)
    print("📖 Balance loaded: £\(formatCurrencyText(value))")
    return value > 0 ? value : nil
}

// ============================================
// TESTING BACKGROUND TERMINATION
// ============================================

// In Xcode:
// 1. Run app on simulator
// 2. Simulate app termination:
//    - Cmd+Q (quit)
//    - Debug → Pause (if in debugger)
//    - Or let app die naturally

// 2. Check console for "Balance synced to Firebase" message

// For more detailed testing:
// Add this to PennyChallengeViewModel:
private func persistBalance() {
    print("⏱️ persistBalance() called at \(Date())")
    print("   Current balance: £\(formatCurrencyText(uiState.piggyBankBalance))")
    
    persistenceManager.saveBalance(uiState.piggyBankBalance)
    
    Task {
        await persistenceManager.syncToFirebase(balance: uiState.piggyBankBalance)
    }
}

// ============================================
// TESTING DATE CALCULATIONS
// ============================================

// Test calculateDaysSince:
let testDate = Date()  // Today
let testDateMillis = Int(testDate.timeIntervalSince1970 * 1000)
let days = calculateDaysSince(testDateMillis)
print("Days since Jan 1st: \(days)")
print("Expected savings: £\(formatCurrencyText(calculateSavingsFunctional(days)))")

// Test with specific date (Jan 1, 2026):
var calendar = Calendar.current
let jan1 = calendar.date(from: DateComponents(year: 2026, month: 1, day: 1))!
let jan1Millis = Int(jan1.timeIntervalSince1970 * 1000)
print("Days on Jan 1: \(calculateDaysSince(jan1Millis))") // Should be 1

// ============================================
// MONITORING FIREBASE SYNC
// ============================================

// Add this to FirebaseManager for detailed logging:
actor FirebaseManager {
    private func log(_ message: String) {
        let timestamp = DateFormatter.localizedString(from: Date(), dateStyle: .none, timeStyle: .medium)
        print("[\(timestamp)] \(message)")
    }
    
    func updateBalance(balance: Int, forUser user: String) async throws {
        self.log("🚀 Starting Firebase sync for user: \(user), balance: \(balance)")
        // ... existing code ...
        self.log("✅ Firebase sync completed")
    }
}

// ============================================
// SWIFT DEBUGGER COMMANDS
// ============================================

// In Xcode Debugger Console (LLDB), type:
(lldb) po viewModel.uiState.piggyBankBalance
(lldb) po UserDefaults.standard.integer(forKey: "piggy_bank_balance")
(lldb) po Date()  // Current time
(lldb) po calculateDaysSince(Int(Date().timeIntervalSince1970 * 1000))

// Set breakpoint and inspect:
(lldb) breakpoint set -n "persistBalance"
(lldb) continue

// ============================================
// UNIT TEST EXAMPLES
// ============================================

import XCTest

class PennyChallengeViewModelTests: XCTestCase {
    var viewModel: PennyChallengeViewModel!
    
    override func setUp() {
        super.setUp()
        viewModel = PennyChallengeViewModel()
    }
    
    func testCurrencyFormatting() {
        XCTAssertEqual(formatCurrencyText(10050), "100.50")
        XCTAssertEqual(formatCurrencyText(0), "0.00")
        XCTAssertEqual(formatCurrencyText(1), "0.01")
    }
    
    func testTopUpBalance() {
        let initialBalance = viewModel.uiState.piggyBankBalance
        viewModel.uiState.topUpValue = 1000  // £10.00
        viewModel.topUpBalance()
        
        XCTAssertEqual(
            viewModel.uiState.piggyBankBalance,
            initialBalance + 1000
        )
    }
    
    func testCalculateDaysSince() {
        let jan1_2026 = Calendar.current.date(from: DateComponents(year: 2026, month: 1, day: 1))!
        let jan1Millis = Int(jan1_2026.timeIntervalSince1970 * 1000)
        
        XCTAssertEqual(calculateDaysSince(jan1Millis), 1)
    }
    
    func testCalculateSavings() {
        XCTAssertEqual(calculateSavingsFunctional(1), 1)
        XCTAssertEqual(calculateSavingsFunctional(2), 3)      // 1+2
        XCTAssertEqual(calculateSavingsFunctional(10), 55)    // 1+2+...+10
        XCTAssertEqual(calculateSavingsFunctional(365), 66795) // Full year
    }
}

// Run tests:
// Cmd + U in Xcode, or
// xcodebuild test -scheme PennyChallenge

// ============================================
// COMMON XCODE SHORTCUTS
// ============================================

// Cmd+Shift+Y = Toggle console/debugger area
// Cmd+Shift+J = Show current file in navigator
// Ctrl+Cmd+E = Edit all in scope
// Cmd+Option+/ = Toggle comment
// Cmd+Ctrl+E = Re-indent code

// ============================================

