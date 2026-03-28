# iOS vs Android Architecture Comparison

## Overview
This document explains how the Android Kotlin implementation maps to the iOS Swift version.

## Core Architecture

### State Management

**Android (Kotlin)**
```kotlin
// Uses Jetpack Compose + StateFlow
private val _uiState = MutableStateFlow(buildInitialState())
val uiState: StateFlow<PennyChallengeUiState> = _uiState.asStateFlow()

_uiState.update { state ->
    state.copy(
        piggyBankBalance = newBalance,
        piggyBankBalanceText = formatCurrencyText(newBalance)
    )
}
```

**iOS (Swift)**
```swift
// Uses SwiftUI + @Published
@Published var uiState = PennyChallengeUiState()

// Direct mutation (Swiftui detects changes automatically)
uiState.piggyBankBalance = newBalance
uiState.piggyBankBalanceText = formatCurrencyText(newBalance)
```

### Local Persistence

**Android**
```kotlin
// SharedPreferences
private val sharedPreferences = application.getSharedPreferences(
    PIGGY_BANK_PREFS,
    Context.MODE_PRIVATE
)

private fun persistBalance(balance: Long) {
    sharedPreferences.edit {
        putLong(PIGGY_BANK_BALANCE_KEY, balance)
    }
}
```

**iOS**
```swift
// UserDefaults
private let defaults = UserDefaults.standard

func saveBalance(_ balance: Int) {
    defaults.set(balance, forKey: userDefaultsKey)
}
```

### Threading & Async

**Android**
```kotlin
// Uses viewModelScope (Coroutines)
fun topUpBalance() {
    viewModelScope.launch {
        _uiState.update { state ->
            val newBalance = state.piggyBankBalance + state.topUpValue
            persistBalance(newBalance)
            // ...
        }
    }
}
```

**iOS**
```swift
// Uses @MainActor + async/await
@MainActor
class PennyChallengeViewModel: ObservableObject {
    func topUpBalance() {
        let newBalance = uiState.piggyBankBalance + uiState.topUpValue
        uiState.piggyBankBalance = newBalance
        uiState.piggyBankBalanceText = formatCurrencyText(newBalance)
        persistBalance()
    }
    
    // Async operations use Task or async functions
    Task {
        await persistenceManager.syncToFirebase(balance: uiState.piggyBankBalance)
    }
}
```

## Data Models

### Android
```kotlin
data class PennyChallengeUiState(
    val piggyBankBalance: Long = DEFAULT_PIGGY_BANK_BALANCE,
    val piggyBankBalanceText: String = formatCurrencyText(DEFAULT_PIGGY_BANK_BALANCE),
    // ...
)
```

### iOS
```swift
struct PennyChallengeUiState: Equatable {
    var piggyBankBalance: Int = 17000  // Default: £170.00
    var piggyBankBalanceText: String = formatCurrencyText(17000)
    // ...
}
```

**Key Difference**: 
- Android uses `Long` for large numbers
- iOS uses `Int` (64-bit by default in modern Swift)
- Both store amounts in pence to avoid floating-point errors

## Currency Handling

Both versions use identical logic:

```
1 Pound (£) = 100 Pence
Stored internally as: pence (Int)
Displayed as: "£XX.XX" formatted string
```

Example: £170.50
- Stored: `17050` (pence)
- Displayed: "170.50" (formatted)

## Date Calculations

### calculateDaysSince()

**Concept**: Count days from January 1st of the selected year to the selected date (inclusive)

**Android**
```kotlin
fun calculateDaysSince(selectedDate: Long?): Int {
    if (selectedDate == null) return 0
    
    val calendar = java.util.Calendar.getInstance()
    calendar.timeInMillis = selectedDate
    
    val startOfYear = java.util.Calendar.getInstance()
    startOfYear.set(calendar.get(java.util.Calendar.YEAR), 0, 1, 0, 0, 0)
    
    val diffInMillis = selectedDate - startOfYear.timeInMillis
    val daysDiff = (diffInMillis / (1000 * 60 * 60 * 24)).toInt()
    
    return daysDiff + 1  // Inclusive
}
```

**iOS**
```swift
func calculateDaysSince(_ selectedDate: Int) -> Int {
    let selectedDateSeconds = TimeInterval(selectedDate) / 1000.0
    let selectedDateObj = Date(timeIntervalSince1970: selectedDateSeconds)
    
    var calendar = Calendar.current
    let components = calendar.dateComponents([.year], from: selectedDateObj)
    guard let year = components.year else { return 0 }
    
    let startOfYear = calendar.date(from: DateComponents(year: year, month: 1, day: 1)) ?? Date()
    let diffInSeconds = selectedDateObj.timeIntervalSince(startOfYear)
    let daysDiff = Int(diffInSeconds / (24 * 60 * 60))
    
    return daysDiff + 1  // Inclusive
}
```

**Note**: 
- Android receives milliseconds (Long)
- iOS receives milliseconds (Int) and converts to TimeInterval
- Math is identical, just different time units

### calculateSavingsFunctional()

Both versions compute: 1 + 2 + 3 + ... + n

**Android**
```kotlin
fun calculateSavingsFunctional(days: Int): Long {
    return (1..days.toLong()).sum()
}
```

**iOS**
```swift
func calculateSavingsFunctional(_ days: Int) -> Int {
    return (1...days).reduce(0, +)
}
```

## UI Components

### Main Screen

**Android** (Jetpack Compose)
```kotlin
@Composable
fun PennyChallengePage(
    modifier: Modifier = Modifier,
    viewModel: PennyChallengeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Column {
        Text(formatDate(uiState.selectedDateMillis))
        Button(onClick = viewModel::onShowDayPicker) {
            Text("Pick Day")
        }
        // ...
    }
}
```

**iOS** (SwiftUI)
```swift
struct ContentView: View {
    @EnvironmentObject var viewModel: PennyChallengeViewModel
    
    var body: some View {
        ScrollView {
            VStack {
                Text(formatDate(viewModel.uiState.selectedDateMillis))
                Button(action: viewModel.onShowDayPicker) {
                    Text("Pick Day")
                }
                // ...
            }
        }
    }
}
```

### Date Picker

**Android** (Material3)
```kotlin
@Composable
fun DayPickerDialog(
    onDismiss: () -> Unit,
    onConfirm: (dateMillis: Long?) -> Unit
) {
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = { /* ... */ }
    ) {
        DatePicker(state = datePickerState)
    }
}
```

**iOS** (SwiftUI)
```swift
struct DatePickerSheet: View {
    var body: some View {
        VStack {
            DatePicker(
                "Select a date",
                selection: $selectedDate,
                displayedComponents: .date
            )
            .datePickerStyle(.graphical)
        }
    }
}
```

## Lifecycle & Persistence

### The Termination Bug (Android)

**Problem**: `onDestroy()` was being called too quickly, preventing Firebase sync.

```kotlin
// WRONG - Data might not sync
override fun onDestroy() {
    super.onDestroy()
    syncToFirebase() // Too late! App is already closing
}
```

### The Solution (iOS)

**Fixed** by explicitly listening to lifecycle events:

```swift
private func setupObservers() {
    // These run BEFORE app termination
    NotificationCenter.default.publisher(for: UIApplication.willTerminateNotification)
        .sink { [weak self] _ in
            self?.persistBalance()  // Synchronous save
        }
        .store(in: &cancellables)
}
```

**Additional safety** in `deinit`:

```swift
deinit {
    persistBalance()  // Save when ViewModel is destroyed
}
```

## Firebase Integration

### Data Structure

Both versions sync to Firestore with identical structure:

```json
Collection: "piggybank-balance"
Document ID: "tin"
Fields:
{
    "user": "tin",
    "balance": 17050  // in pence
}
```

### Android Implementation
Uses Firebase SDK (implicit in your setup)

### iOS Implementation
Provides two options:

1. **REST API** (No dependencies needed)
   ```swift
   func updateBalance(balance: Int, forUser user: String) async throws {
       let endpoint = "\(firebaseURL)/\(collectionName)/\(user)"
       var request = URLRequest(url: URL(string: endpoint)!)
       request.httpMethod = "PATCH"
       // ...
   }
   ```

2. **Firebase SDK** (Recommended for production)
   ```swift
   import FirebaseFirestore
   
   func updateBalance(balance: Int, forUser user: String) async throws {
       try await db.collection("piggybank-balance").document(user).setData([
           "user": user,
           "balance": balance
       ])
   }
   ```

## Summary Table

| Aspect | Android | iOS |
|--------|---------|-----|
| **Language** | Kotlin | Swift |
| **UI Framework** | Jetpack Compose | SwiftUI |
| **State Management** | StateFlow | @Published |
| **ViewModel** | AndroidViewModel | ObservableObject |
| **Local Storage** | SharedPreferences | UserDefaults |
| **Threading** | viewModelScope / Coroutines | @MainActor / async-await |
| **Date/Time** | java.util.Calendar | Foundation.Calendar |
| **Networking** | Retrofit/OkHttp/Firebase | URLSession/Firebase |
| **Package Manager** | Gradle | CocoaPods/SPM |
| **Type Safety** | Nullable types (?) | Optional types (?) |
| **Numbers** | Long for large ints | Int (64-bit) |
| **Reactive Binding** | collectAsState | @Published + @State |

## Migration Tips

### For Android developers learning iOS:

1. **StateFlow → @Published**: Similar reactive patterns, slightly different syntax
2. **SharedPreferences → UserDefaults**: Drop-in replacements for local storage
3. **AndroidViewModel → ObservableObject**: Same lifecycle responsibilities
4. **Jetpack Compose → SwiftUI**: Both declarative, similar composition model
5. **Coroutines → async/await**: Both modern async patterns, syntax differs
6. **Lifecycle Events**: Android explicit callbacks → iOS Notifications + deinit

### Memory Management:
- **Android**: Garbage collection (mostly automatic)
- **iOS**: ARC (Automatic Reference Counting) - watch for retain cycles

### Threading:
- **Android**: Use viewModelScope to stay on main thread
- **iOS**: Use @MainActor to ensure UI updates on main thread

---

Both implementations are **functionally equivalent** and maintain the same business logic, data models, and user experience. Choose the appropriate framework for your target platform.

