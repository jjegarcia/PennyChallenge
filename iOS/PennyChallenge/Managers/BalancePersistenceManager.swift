//
//  BalancePersistenceManager.swift
//  PennyChallenge
//
//  Manages local persistence of balance data using UserDefaults

import Foundation

class BalancePersistenceManager {
    static let shared = BalancePersistenceManager()

    private let userDefaultsKey = "piggy_bank_balance"
    private let defaults = UserDefaults.standard

    func saveBalance(_ balance: Int) {
        defaults.set(balance, forKey: userDefaultsKey)
    }

    func loadBalance() -> Int? {
        let value = defaults.integer(forKey: userDefaultsKey)
        return value > 0 ? value : nil
    }
}
