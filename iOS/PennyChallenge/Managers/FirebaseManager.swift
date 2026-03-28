//
//  FirebaseManager.swift
//  PennyChallenge
//
//  Manages Firebase Firestore operations

import Foundation

// MARK: - Firebase Error
enum FirebaseError: LocalizedError {
    case invalidURL
    case networkError(String)
    case decodingError(String)
    case unknownError(String)
    
    var errorDescription: String? {
        switch self {
        case .invalidURL:
            return "Invalid Firebase URL"
        case .networkError(let message):
            return "Network error: \(message)"
        case .decodingError(let message):
            return "Decoding error: \(message)"
        case .unknownError(let message):
            return "Unknown error: \(message)"
        }
    }
}

// MARK: - Firebase Manager
actor FirebaseManager {
    static let shared = FirebaseManager()
    
    private let projectId = "piggybank-19219"
    private let collectionName = "piggybank-balance"
    private let firebaseURL = "https://firestore.googleapis.com/v1/projects/piggybank-19219/databases/(default)/documents"
    
    // For production, you should use:
    // 1. Firebase SDK (FirebaseFirestore)
    // 2. Proper authentication with service account or user credentials
    // 3. Better error handling and retry logic
    
    // MARK: - Update Balance in Firestore
    func updateBalance(balance: Int, forUser user: String) async throws {
        // Note: Direct REST API access to Firestore requires proper authentication
        // This is a simplified example. In production, use Firebase SDK:
        // import FirebaseFirestore
        
        let documentId = user
        let endpoint = "\(firebaseURL)/\(collectionName)/\(documentId)"
        
        guard let url = URL(string: endpoint) else {
            throw FirebaseError.invalidURL
        }
        
        // Create document data
        let documentData: [String: Any] = [
            "user": ["stringValue": user],
            "balance": ["integerValue": String(balance)]
        ]
        
        let requestBody: [String: Any] = [
            "fields": documentData
        ]
        
        var request = URLRequest(url: url)
        request.httpMethod = "PATCH"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        
        do {
            request.httpBody = try JSONSerialization.data(withJSONObject: requestBody)
        } catch {
            throw FirebaseError.decodingError("Failed to encode request: \(error)")
        }
        
        do {
            let (data, response) = try await URLSession.shared.data(for: request)
            
            guard let httpResponse = response as? HTTPURLResponse else {
                throw FirebaseError.networkError("Invalid response")
            }
            
            if !(200...299).contains(httpResponse.statusCode) {
                let errorMessage = String(data: data, encoding: .utf8) ?? "Unknown error"
                throw FirebaseError.networkError("HTTP \(httpResponse.statusCode): \(errorMessage)")
            }
        } catch let error as FirebaseError {
            throw error
        } catch {
            throw FirebaseError.networkError(error.localizedDescription)
        }
    }
    
    // MARK: - Fetch Balance from Firestore
    func fetchBalance(forUser user: String) async throws -> Int? {
        let documentId = user
        let endpoint = "\(firebaseURL)/\(collectionName)/\(documentId)"
        
        guard let url = URL(string: endpoint) else {
            throw FirebaseError.invalidURL
        }
        
        var request = URLRequest(url: url)
        request.httpMethod = "GET"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        
        do {
            let (data, response) = try await URLSession.shared.data(for: request)
            
            guard let httpResponse = response as? HTTPURLResponse else {
                throw FirebaseError.networkError("Invalid response")
            }
            
            if !(200...299).contains(httpResponse.statusCode) {
                let errorMessage = String(data: data, encoding: .utf8) ?? "Unknown error"
                throw FirebaseError.networkError("HTTP \(httpResponse.statusCode): \(errorMessage)")
            }
            
            if let responseDict = try JSONSerialization.jsonObject(with: data) as? [String: Any],
               let fields = responseDict["fields"] as? [String: Any],
               let balanceField = fields["balance"] as? [String: Any],
               let balanceString = balanceField["integerValue"] as? String,
               let balance = Int(balanceString) {
                return balance
            }
            
            return nil
        } catch let error as FirebaseError {
            throw error
        } catch {
            throw FirebaseError.networkError(error.localizedDescription)
        }
    }
}

// MARK: - Alternative: Firebase SDK Implementation
// For production use, replace the above with the official Firebase SDK:
/*
import FirebaseFirestore

actor FirebaseManager {
    static let shared = FirebaseManager()
    
    private let db = Firestore.firestore()
    private let collectionName = "piggybank-balance"
    
    func updateBalance(balance: Int, forUser user: String) async throws {
        try await db.collection(collectionName).document(user).setData([
            "user": user,
            "balance": balance
        ], merge: true)
    }
    
    func fetchBalance(forUser user: String) async throws -> Int? {
        let document = try await db.collection(collectionName).document(user).getDocument()
        return document.data()?["balance"] as? Int
    }
}
*/

