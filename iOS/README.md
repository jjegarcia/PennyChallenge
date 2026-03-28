# PennyChallenge iOS

An iOS version of the Penny Challenge app built with **SwiftUI** and **Swift 5.9+**. This is the native iOS equivalent of your Android Kotlin version.

## Project Structure

```
iOS/
├── PennyChallenge/
│   ├── PennyChallenge.swift           # App entry point
│   ├── ViewModels/
│   │   └── PennyChallengeViewModel.swift    # Main ViewModel with business logic
│   ├── Views/
│   │   ├── ContentView.swift          # Main UI view
│   │   └── DatePickerSheet.swift      # Date picker component
│   ├── Managers/
│   │   ├── BalancePersistenceManager.swift  # UserDefaults & Firebase sync
│   │   └── FirebaseManager.swift      # Firebase Firestore operations
│   └── Assets/
│       └── Colors.xcassets            # (Create as needed for theming)
└── README.md
```

## Key Features

### 1. **State Management**
- Uses `@Published` and `@State` for reactive UI updates
- MVVM architecture with `@MainActor` for thread safety
- Replaces Android's `StateFlow` with SwiftUI's `@EnvironmentObject`

### 2. **Local Persistence**
- `UserDefaults` replaces Android's `SharedPreferences`
- Stored in `BalancePersistenceManager` (Actor for thread safety)
- Auto-saves on balance changes

### 3. **Firebase Integration**
- **Important Fix**: Uses background task observers to ensure data syncs before app termination
- Listens to:
  - `UIApplication.willTerminateNotification`
  - `UIApplication.didEnterBackgroundNotification`
- Syncs balance asynchronously to avoid blocking app shutdown

### 4. **Currency Handling**
- All amounts stored in **pence** (smallest unit) to avoid floating-point errors
- `formatCurrencyText()` converts pence to GBP string format
- Matches Android implementation exactly

### 5. **Date Calculations**
- `calculateDaysSince()`: Counts days from Jan 1st of selected year
- `calculateSavingsFunctional()`: Computes total savings (1+2+3+...+n)
- Consistent with Kotlin version

## Getting Started

### Prerequisites
- Xcode 14.0+
- Swift 5.9+
- iOS 14.0+ minimum deployment target
- Firebase account (optional, for cloud sync)

### Setup Steps

1. **Create Xcode Project**
   ```bash
   mkdir -p PennyChallenge/iOS
   cd PennyChallenge/iOS
   ```

2. **Open Xcode and create new iOS App project**
   - Product Name: `PennyChallenge`
   - Interface: SwiftUI
   - Lifecycle: SwiftUI App
   - Language: Swift

3. **Copy the Swift files** into your Xcode project

4. **Configure Firebase (Optional)**

   If you want Firebase cloud sync:
   
   a. **Install Firebase SDK via CocoaPods**:
   ```bash
   # In iOS project directory
   pod init
   ```
   
   Edit `Podfile`:
   ```ruby
   target 'PennyChallenge' do
     pod 'Firebase/Firestore'
     pod 'Firebase/Analytics'
   end
   ```
   
   ```bash
   pod install
   ```
   
   b. **Download GoogleService-Info.plist**
   - Go to [Firebase Console](https://console.firebase.google.com)
   - Select your project
   - Download `GoogleService-Info.plist`
   - Add to Xcode project (check "Copy items if needed")

   c. **Enable Firebase SDK in code**
   
   Replace the `FirebaseManager.swift` REST API implementation with the SDK version (see commented code at bottom of file):
   
   ```swift
   import FirebaseFirestore
   
   actor FirebaseManager {
       static let shared = FirebaseManager()
       
       private let db = Firestore.firestore()
       
       func updateBalance(balance: Int, forUser user: String) async throws {
           try await db.collection("piggybank-balance").document(user).setData([
               "user": user,
               "balance": balance
           ], merge: true)
       }
   }
   ```

5. **Run the app**
   ```bash
   open PennyChallenge.xcworkspace
   ```

## Debugging Shared Preferences / UserDefaults

### View UserDefaults in Xcode Console

```swift
// Add this to your app at launch for debugging
let defaults = UserDefaults.standard
if let balance = defaults.object(forKey: "piggy_bank_balance") {
    print("💾 Stored Balance: \(balance)")
}

// Or use this command in Xcode console:
po UserDefaults.standard.dictionary(forKey: "piggy_bank_balance")
```

### Terminal Command
```bash
# View all UserDefaults for the app (macOS only)
defaults read com.example.pennychallenge

# On actual iOS device, use Xcode's debug navigator:
# Debug Navigator > View Memory Graph > Check UserDefaults container
```

### Xcode Debug Tips
1. Go to **View** → **Debug Area** → **Activate Console**
2. Add breakpoint in `BalancePersistenceManager.saveBalance()`
3. Use **po** command to inspect values:
   ```
   po UserDefaults.standard.integer(forKey: "piggy_bank_balance")
   ```

## Fixing the Termination Issue

### Android Problem
Your Android app had `onDestroy()` running too quickly, not completing the Firebase sync.

### iOS Solution
This is **fixed** in the Swift version:

```swift
private func setupObservers() {
    // These ensure data persists BEFORE app terminates
    NotificationCenter.default.publisher(for: UIApplication.willTerminateNotification)
        .sink { [weak self] _ in
            self?.persistBalance()  // This runs synchronously before app closes
        }
        .store(in: &cancellables)
    
    NotificationCenter.default.publisher(for: UIApplication.didEnterBackgroundNotification)
        .sink { [weak self] _ in
            self?.persistBalance()  // Also save when backgrounding
        }
        .store(in: &cancellables)
}
```

**Additional safeguard in deinit:**
```swift
deinit {
    // Ensures data is saved when ViewModel is destroyed
    persistBalance()
}
```

### For Critical Data Persistence
For guaranteed persistence before app termination:

```swift
// In BalancePersistenceManager
func syncToFirebase(balance: Int) async {
    // Use a background task
    let backgroundTask = UIApplication.shared.beginBackgroundTask { }
    
    defer {
        UIApplication.shared.endBackgroundTask(backgroundTask)
    }
    
    do {
        try await firebaseManager.updateBalance(balance: balance, forUser: "tin")
    } catch {
        print("❌ Firebase sync failed: \(error)")
    }
}
```

## API Endpoints

### Firestore REST API
Your app syncs to:
```
https://firestore.googleapis.com/v1/projects/piggybank-19219/databases/(default)/documents/piggybank-balance/{user}
```

**Document Structure:**
```json
{
  "fields": {
    "user": { "stringValue": "tin" },
    "balance": { "integerValue": "17000" }  // in pence
  }
}
```

## Comparison: Android vs iOS

| Feature | Android (Kotlin) | iOS (Swift) |
|---------|------------------|------------|
| State Management | `StateFlow` | `@Published` |
| Local Storage | `SharedPreferences` | `UserDefaults` |
| ViewModel | `AndroidViewModel` | `ObservableObject` |
| UI Framework | Jetpack Compose | SwiftUI |
| Threading | `viewModelScope` | `@MainActor` + `async/await` |
| Date Picker | Material3 DatePicker | DatePickerStyle(.graphical) |
| Persistence | Activity lifecycle | App lifecycle notifications |

## Known Limitations

1. **Firebase REST API** - Current implementation uses REST API. For production, use Firebase SDK:
   - Better security rules support
   - Offline persistence
   - Real-time updates

2. **Authentication** - This version assumes unauthenticated access. Add proper Firebase authentication for production.

3. **Error Handling** - Add retry logic and user-facing error alerts in production.

## Next Steps

1. ✅ Test on iOS simulator
2. ✅ Add proper Firebase authentication
3. ✅ Add error alert dialogs for Firebase failures
4. ✅ Implement proper logging system
5. ✅ Add unit tests for ViewModels
6. ✅ Add UI tests
7. ✅ Configure app signing and provisioning profiles for TestFlight/App Store

## Support

For questions about the architecture differences between Android and iOS versions, refer to:
- [SwiftUI Documentation](https://developer.apple.com/documentation/swiftui/)
- [Combine Framework](https://developer.apple.com/documentation/combine)
- [Firebase iOS SDK](https://firebase.google.com/docs/ios/setup)

---

**Created**: March 2026  
**Swift Version**: 5.9+  
**Minimum iOS**: 14.0

