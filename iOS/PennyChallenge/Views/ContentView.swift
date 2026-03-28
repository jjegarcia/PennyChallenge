//
//  ContentView.swift
//  PennyChallenge
//
//  Main UI view for the Penny Challenge app

import SwiftUI

struct ContentView: View {
    @EnvironmentObject var viewModel: PennyChallengeViewModel
    
    var body: some View {
        ZStack {
            Color(UIColor.systemBackground)
                .ignoresSafeArea()
            
            ScrollView {
                VStack(spacing: 16) {
                    // --- Date Display ---
                    Text(formatDate(viewModel.uiState.selectedDateMillis))
                        .font(.headline)
                        .padding()
                    
                    if viewModel.uiState.selectedDateMillis != nil {
                        VStack(spacing: 8) {
                            Text("add today: £\(formatCurrencyText(viewModel.uiState.numberOfDays))")
                                .font(.body)
                            
                            Text("expected to date: £\(formatCurrencyText(viewModel.uiState.totalPennies))")
                                .font(.title2)
                                .fontWeight(.semibold)
                                .foregroundColor(.blue)
                        }
                        .padding()
                    }
                    
                    // --- Day Picker Button ---
                    Button(action: viewModel.onShowDayPicker) {
                        Text("Pick Day")
                            .frame(maxWidth: .infinity)
                            .padding()
                            .background(Color.blue)
                            .foregroundColor(.white)
                            .cornerRadius(8)
                    }
                    .padding(.horizontal)
                    
                    Divider()
                        .padding()
                    
                    // --- Suggested Top-Up ---
                    Text("suggested top-up: £\(viewModel.suggestedTopUpText)")
                        .font(.body)
                        .padding()
                    
                    // --- Top-Up Section ---
                    VStack(spacing: 8) {
                        TextField("Top-Up (GBP)", text: $viewModel.uiState.topUpValueText)
                            .onChange(of: viewModel.uiState.topUpValueText) { newValue in
                                viewModel.onTopUpTextChanged(newValue)
                            }
                            .textFieldStyle(.roundedBorder)
                            .keyboardType(.decimalPad)
                            .padding(.horizontal)
                        
                        Button(action: viewModel.topUpBalance) {
                            Text("Top-Up")
                                .frame(maxWidth: .infinity)
                                .padding()
                                .background(Color.green)
                                .foregroundColor(.white)
                                .cornerRadius(8)
                        }
                        .padding(.horizontal)
                    }
                    
                    Divider()
                        .padding()
                    
                    // --- Withdraw Section ---
                    VStack(spacing: 8) {
                        TextField("Withdraw (GBP)", text: $viewModel.uiState.withdrawValueText)
                            .onChange(of: viewModel.uiState.withdrawValueText) { newValue in
                                viewModel.onWithdrawTextChanged(newValue)
                            }
                            .textFieldStyle(.roundedBorder)
                            .keyboardType(.decimalPad)
                            .padding(.horizontal)
                        
                        Button(action: viewModel.withdrawBalance) {
                            Text("Withdraw")
                                .frame(maxWidth: .infinity)
                                .padding()
                                .background(Color.red)
                                .foregroundColor(.white)
                                .cornerRadius(8)
                        }
                        .padding(.horizontal)
                    }
                    
                    Divider()
                        .padding()
                    
                    // --- Manual Balance Edit ---
                    VStack(spacing: 8) {
                        TextField("Update (GBP)", text: $viewModel.uiState.piggyBankBalanceText)
                            .onChange(of: viewModel.uiState.piggyBankBalanceText) { newValue in
                                viewModel.onBalanceTextChanged(newValue)
                            }
                            .textFieldStyle(.roundedBorder)
                            .keyboardType(.decimalPad)
                            .padding(.horizontal)
                        
                        Text("total balance: £\(formatCurrencyText(viewModel.uiState.piggyBankBalance))")
                            .font(.title2)
                            .fontWeight(.semibold)
                            .foregroundColor(.blue)
                            .padding()
                    }
                    
                    Spacer()
                }
                .padding()
            }
            
            // --- Date Picker Sheet ---
            if viewModel.uiState.showDayPicker {
                DatePickerSheet(
                    isPresented: $viewModel.uiState.showDayPicker,
                    onDateSelected: viewModel.onDateSelected
                )
                .transition(.move(edge: .bottom))
            }
        }
    }
}

#Preview {
    ContentView()
        .environmentObject(PennyChallengeViewModel())
}

