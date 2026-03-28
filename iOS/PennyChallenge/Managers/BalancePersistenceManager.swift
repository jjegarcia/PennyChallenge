//
//  BalancePersistenceManager.swift
//  PennyChallenge
//
//  Manages persistence of balance data to UserDefaults and Firebase

import Foundation

actor BalancePersistenceManager {
    static let shared = BalancePersistenceManager()
    
    private let userDefaultsKey = "piggy_bank_balance"
    private let defaults = UserDefaults.standard
    private let firebaseManager = FirebaseManager.shared
    
    // MARK: - UserDefaults Operations
    func saveBalance(_ balance: Int) {
        defaults.set(balance, forKey: userDefaultsKey)
    }
    
    func loadBalance() -> Int? {
        let value = defaults.integer(forKey: userDefaultsKey)
        return value > 0 ? value : nil // Return nil if not set or 0
    }
    
    // MARK: - Firebase Sync
    func syncToFirebase(balance: Int) async {
        do {
            try await firebaseManager.updateBalance(balance: balance, forUser: "tin")
            print("✅ Balance synced to Firebase: £\(formatCurrencyText(balance))")
        } catch {
            print("❌ Failed to sync balance to Firebase: \(error)")
        }
    }
}

