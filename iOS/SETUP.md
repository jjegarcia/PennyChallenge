# Swift Files Setup Instructions

## File Organization

Copy these Swift files into your Xcode project in the following structure:

```
PennyChallenge/
├── PennyChallenge.swift
├── Views/
│   ├── ContentView.swift
│   └── DatePickerSheet.swift
├── ViewModels/
│   └── PennyChallengeViewModel.swift
└── Managers/
    ├── BalancePersistenceManager.swift
    └── FirebaseManager.swift
```

## Step-by-Step Setup in Xcode

### 1. Create Folder Structure
```
In Xcode:
- Right-click on project
- New Group > "Views"
- New Group > "ViewModels"
- New Group > "Managers"
```

### 2. Add Files
```
- File > New > File > Swift File
- Name each file appropriately
- Make sure "Add to target" is checked
```

### 3. Copy Code
Copy the content from each `.swift` file into Xcode

### 4. Verify Imports
No external dependencies required for basic version!

- SwiftUI ✅ (built-in)
- Combine ✅ (built-in)
- Foundation ✅ (built-in)

### 5. Update PennyChallenge.swift (Entry Point)
Make sure this is your App struct (delete default SceneDelegate if present)

### 6. Build & Run
```
Cmd + B  // Build
Cmd + R  // Run
```

## Debugging UserDefaults

Add this code to `PennyChallengeViewModel.swift` for debugging:

```swift
func debugPrintStoredBalance() {
    if let balance = persistenceManager.loadBalance() {
        print("🔍 DEBUG: UserDefaults Balance = £\(formatCurrencyText(balance))")
    } else {
        print("🔍 DEBUG: No balance stored in UserDefaults")
    }
}
```

Then call it in your View:

```swift
.onAppear {
    viewModel.debugPrintStoredBalance()
}
```

Or use Xcode's LLDB console:

```
(lldb) po UserDefaults.standard.integer(forKey: "piggy_bank_balance")
17000
```

## Firebase Configuration (Optional)

If you want Firebase cloud sync:

### Option A: Using REST API (Current Implementation)
- No setup needed! Uses direct HTTP requests
- Works with proper Firestore security rules

### Option B: Using Firebase SDK (Recommended for Production)

1. **Install Firebase via CocoaPods**
   ```bash
   cd /path/to/iOS
   pod init
   # Edit Podfile to add Firebase/Firestore
   pod install
   ```

2. **Download GoogleService-Info.plist**
   - Firebase Console → Project Settings
   - Add to Xcode project

3. **Update FirebaseManager.swift**
   - Uncomment the Firebase SDK implementation at the bottom
   - Remove REST API implementation

4. **Initialize Firebase in your app**
   ```swift
   import FirebaseCore
   
   @main
   struct PennyChallengeApp: App {
       init() {
           FirebaseApp.configure()
       }
       
       var body: some Scene {
           WindowGroup {
               ContentView()
                   .environmentObject(PennyChallengeViewModel())
           }
       }
   }
   ```

## Common Issues & Solutions

### Issue: "Cannot find 'viewModel' in scope"
**Solution**: Make sure ContentView has `@EnvironmentObject var viewModel: PennyChallengeViewModel`

### Issue: UserDefaults not saving
**Solution**: 
1. Check that `BalancePersistenceManager.saveBalance()` is being called
2. Add debug prints in `persistBalance()`
3. Use Xcode console to verify: `po UserDefaults.standard.dictionaryRepresentation()`

### Issue: Date picker showing wrong date
**Solution**: Check timezone handling in `calculateDaysSince()` - may need to adjust for local timezone

### Issue: Firebase sync failing silently
**Solution**:
1. Check Firestore rules allow public write to `piggybank-balance` collection
2. Use Firestore REST API explorer in Firebase Console
3. Add debug prints in `FirebaseManager`

## Testing Balance Persistence

Add this SwiftUI code to test persistence:

```swift
// In ContentView
Button("Debug: Clear All Data") {
    UserDefaults.standard.removeObject(forKey: "piggy_bank_balance")
    viewModel.loadInitialState() // Reload from defaults
}

Button("Debug: Print Balance") {
    let balance = UserDefaults.standard.integer(forKey: "piggy_bank_balance")
    print("Current balance in UserDefaults: \(balance) pence")
}
```

## Next Steps

1. Build and run on simulator
2. Change the balance
3. Close the app
4. Reopen - balance should persist
5. Check Xcode console output for Firebase sync messages
6. Add error alerts in production

---

Questions? Check README.md for detailed documentation!

