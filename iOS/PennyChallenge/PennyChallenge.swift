//
//  PennyChallenge.swift
//  PennyChallenge
//
//  Entry point for the iOS app

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

