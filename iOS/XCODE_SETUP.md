# Creating the Xcode Project from Scratch

## Method 1: Using Xcode GUI (Recommended for Beginners)

### Step 1: Create New Project
1. Open **Xcode**
2. Go to **File > New > Project**
3. Select **iOS** tab
4. Choose **App** template
5. Click **Next**

### Step 2: Configure Project
- **Product Name**: `PennyChallenge`
- **Organization Identifier**: `com.example` (or your domain)
- **Bundle Identifier**: `com.example.pennychallenge` (auto-filled)
- **Language**: Swift
- **Interface**: SwiftUI
- **Lifecycle**: SwiftUI App (not SceneDelegate)
- **Include Tests**: Optional
- Click **Next**

### Step 3: Choose Location
- Select folder: `/Users/jgarc609/github/PennyChallenge/iOS`
- Check "Create Git repository on my Mac" (optional)
- Click **Create**

### Step 4: Create Folder Structure

In Xcode Project Navigator (left panel):

1. Right-click on project folder
2. **New Group** → Name: `Views`
3. **New Group** → Name: `ViewModels`
4. **New Group** → Name: `Managers`

### Step 5: Add Swift Files

For each Swift file:
1. **File > New > File** (or Cmd+N)
2. Select **Swift File**
3. Name it (e.g., `ContentView.swift`)
4. **Create**
5. Paste the code from the generated files

**Files to create:**
- `PennyChallenge.swift` (root)
- `Views/ContentView.swift`
- `Views/DatePickerSheet.swift`
- `ViewModels/PennyChallengeViewModel.swift`
- `Managers/BalancePersistenceManager.swift`
- `Managers/FirebaseManager.swift`

### Step 6: Update Main App File

Replace the default `PennyChallenge.swift`:
```swift
import SwiftUI

@main
struct PennyChallengeApp: App {
    var body: some Scene {
        WindowGroup {
            ContentView()
                .environmentObject(PennyChallengeViewModel())
        }
    }
}
```

### Step 7: Verify Build

```bash
Cmd + B  # Build
```

You should see "Build successful" in Xcode.

### Step 8: Run on Simulator

```bash
Cmd + R  # Run
```

## Method 2: Using Command Line

### Step 1: Create Project Structure
```bash
cd /Users/jgarc609/github/PennyChallenge/iOS

# Create directory
mkdir PennyChallenge
cd PennyChallenge

# Create subdirectories
mkdir -p Sources/{Views,ViewModels,Managers}
mkdir Tests
```

### Step 2: Create Swift Package (Alternative to Xcode Project)
```bash
swift package init --type executable
```

### Step 3: Create Files
Copy Swift code into appropriate directories:
```
Sources/
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

### Step 4: Edit Package.swift
```swift
// swift-tools-version:5.9
import PackageDescription

let package = Package(
    name: "PennyChallenge",
    platforms: [
        .iOS(.v14)
    ],
    products: [
        .library(name: "PennyChallenge", targets: ["PennyChallenge"])
    ],
    targets: [
        .target(
            name: "PennyChallenge",
            dependencies: []
        )
    ]
)
```

### Step 5: Build
```bash
swift build
```

---

## Project Structure Explanation

```
PennyChallenge/
├── PennyChallenge.swift              # App entry point (@main)
│
├── Views/                             # UI Components
│   ├── ContentView.swift             # Main screen
│   └── DatePickerSheet.swift         # Date picker dialog
│
├── ViewModels/                        # Business Logic
│   └── PennyChallengeViewModel.swift  # State management
│
├── Managers/                          # Services
│   ├── BalancePersistenceManager.swift # Local storage + Firebase sync
│   └── FirebaseManager.swift          # Firebase operations
│
├── Assets.xcassets/                  # Images, colors, icons
├── Info.plist                        # App configuration
├── Preview Content/                  # Preview assets for SwiftUI
│   └── Preview Assets.xcassets/
│
└── Tests/                            # Unit tests (optional)
```

---

## Build Settings Configuration

### In Xcode:

1. Select **PennyChallenge** project in navigator
2. Select **PennyChallenge** target
3. Go to **Build Settings** tab

### Key Settings to Verify:

```
Swift Compiler
├── Language
│   └── Swift Language Version: Swift 5.9
├── Code Generation
│   └── Optimization Level: Optimize for Speed

Linking
├── Mach-O Type: Executable
├── Prefer Module Search
│   └── Enabled

Deployment
├── iOS Deployment Target: iOS 14.0 or higher
├── Supports iPad: Yes
└── Requires full screen: No
```

### Product Settings:

1. Select **PennyChallenge** target
2. **General** tab:
   - **Bundle ID**: `com.example.pennychallenge`
   - **Minimum Deployment**: iOS 14.0
   - **Supported Orientations**: Portrait, Landscape

---

## Running on Different Simulators

### Via Xcode:
1. Select simulator from top toolbar (next to Play button)
2. Choose device (iPhone 15 Pro, iPhone SE, etc.)
3. Press **Cmd + R** to run

### Via Terminal:
```bash
# List available simulators
xcrun simctl list devices

# Run on specific simulator
xcodebuild -scheme PennyChallenge -destination 'generic/platform=iOS Simulator,name=iPhone 15 Pro' test
```

### Popular Test Devices:
- iPhone 15 Pro
- iPhone SE (2nd gen)
- iPhone 14

---

## Common Build Issues & Solutions

### Issue: "Cannot find 'viewModel' in scope"
```swift
// Make sure ContentView has:
@EnvironmentObject var viewModel: PennyChallengeViewModel

// And app passes it:
ContentView()
    .environmentObject(PennyChallengeViewModel())
```

### Issue: "No such module 'FirebaseCore'"
```bash
# Install Firebase via CocoaPods (see SETUP.md)
# Or use REST API version (no dependencies needed)
```

### Issue: Build fails with "Cannot find 'formatCurrencyText'"
- Make sure all utility functions are in `PennyChallengeViewModel.swift`
- Check they're not inside a function or class scope

### Issue: Preview not rendering
```swift
// Make sure preview includes environment object:
#Preview {
    ContentView()
        .environmentObject(PennyChallengeViewModel())
}
```

---

## Testing the App

### Manual Testing Checklist:

- [ ] App launches successfully
- [ ] Initial balance loads (£170.00)
- [ ] Can input top-up amount
- [ ] Top-up button updates balance
- [ ] Can input withdrawal amount
- [ ] Withdrawal button updates balance
- [ ] Can manually edit balance
- [ ] Date picker opens and closes
- [ ] Date selection updates calculations
- [ ] Balance persists after app close/reopen
- [ ] No crashes on rapid input

### Automated Testing:

Create a test file `PennyChallengeViewModelTests.swift`:

```swift
import XCTest
@testable import PennyChallenge

final class PennyChallengeViewModelTests: XCTestCase {
    
    var viewModel: PennyChallengeViewModel!
    
    override func setUp() {
        super.setUp()
        viewModel = PennyChallengeViewModel()
    }
    
    func testInitialBalance() {
        XCTAssertGreater(viewModel.uiState.piggyBankBalance, 0)
    }
    
    func testTopUpCalculation() {
        let initial = viewModel.uiState.piggyBankBalance
        viewModel.onTopUpTextChanged("10.00")
        viewModel.topUpBalance()
        
        XCTAssertEqual(
            viewModel.uiState.piggyBankBalance,
            initial + 1000
        )
    }
}
```

Run tests: **Cmd + U**

---

## Preparing for App Store

When ready for distribution:

1. **Code Signing**
   - Team ID setup
   - Provisioning profiles
   - Certificates

2. **App Info**
   - App Icon
   - App Description
   - Screenshots
   - Version number

3. **Configuration**
   - Privacy settings (Info.plist)
   - Localization
   - Support URL

4. **Testing**
   - TestFlight (beta testing)
   - Full app review

---

## Resources

- [Apple SwiftUI Documentation](https://developer.apple.com/documentation/swiftui)
- [Xcode Help](https://help.apple.com/xcode/)
- [Swift by Example](https://www.swiftbysundell.com/)
- [Combine Framework Docs](https://developer.apple.com/documentation/combine)

---

**Project created**: March 28, 2026  
**Swift version**: 5.9+  
**Xcode minimum**: 14.0  
**iOS minimum**: 14.0

