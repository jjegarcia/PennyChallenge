//
//  DatePickerSheet.swift
//  PennyChallenge
//
//  Date picker dialog component

import SwiftUI

struct DatePickerSheet: View {
    @Binding var isPresented: Bool
    var onDateSelected: (Date) -> Void
    
    @State private var selectedDate = Date()
    
    var body: some View {
        VStack(spacing: 16) {
            HStack {
                Text("Select Date")
                    .font(.headline)
                Spacer()
                Button("Cancel") {
                    isPresented = false
                }
            }
            .padding()
            
            DatePicker(
                "Select a date",
                selection: $selectedDate,
                displayedComponents: .date
            )
            .datePickerStyle(.graphical)
            .padding()
            
            HStack(spacing: 12) {
                Button("Cancel") {
                    isPresented = false
                }
                .frame(maxWidth: .infinity)
                .padding()
                .background(Color.gray)
                .foregroundColor(.white)
                .cornerRadius(8)
                
                Button("OK") {
                    onDateSelected(selectedDate)
                    isPresented = false
                }
                .frame(maxWidth: .infinity)
                .padding()
                .background(Color.blue)
                .foregroundColor(.white)
                .cornerRadius(8)
            }
            .padding()
            
            Spacer()
        }
        .background(Color(UIColor.systemBackground))
        .cornerRadius(16, corners: [.topLeft, .topRight])
        .ignoresSafeArea()
    }
}

// Custom corner radius modifier
extension View {
    func cornerRadius(_ radius: CGFloat, corners: UIRectCorner) -> some View {
        clipShape(RoundedCorner(radius: radius, corners: corners))
    }
}

struct RoundedCorner: Shape {
    var radius: CGFloat = .infinity
    var corners: UIRectCorner = .allCorners

    func path(in rect: CGRect) -> Path {
        let path = UIBezierPath(
            roundedRect: rect,
            byRoundingCorners: corners,
            cornerRadii: CGSize(width: radius, height: radius)
        )
        return Path(path.cgPath)
    }
}

#Preview {
    DatePickerSheet(
        isPresented: .constant(true),
        onDateSelected: { _ in }
    )
}

