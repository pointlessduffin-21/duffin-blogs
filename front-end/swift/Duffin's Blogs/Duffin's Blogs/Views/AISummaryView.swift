import SwiftUI

struct AISummaryView: View {
    let postSlug: String
    @StateObject private var blogService = BlogService.shared
    @State private var summary: String = ""
    @State private var isLoading: Bool = false
    @State private var hasError: Bool = false
    @State private var errorMessage: String = ""
    @State private var isExpanded: Bool = false
    
    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            // Header with AI icon and title
            HStack {
                Image(systemName: "brain.head.profile")
                    .foregroundColor(.purple)
                    .font(.title2)
                
                Text("AI Summary")
                    .font(.headline)
                    .fontWeight(.semibold)
                
                Spacer()
                
                if !summary.isEmpty && !isLoading {
                    Button(action: {
                        withAnimation(.easeInOut(duration: 0.3)) {
                            isExpanded.toggle()
                        }
                    }) {
                        Image(systemName: isExpanded ? "chevron.up" : "chevron.down")
                            .foregroundColor(.secondary)
                            .font(.caption)
                    }
                }
            }
            
            if isLoading {
                HStack {
                    ProgressView()
                        .scaleEffect(0.8)
                    Text("Generating AI summary...")
                        .font(.subheadline)
                        .foregroundColor(.secondary)
                }
                .padding(.vertical, 8)
            } else if hasError {
                VStack(alignment: .leading, spacing: 8) {
                    HStack {
                        Image(systemName: "exclamationmark.triangle.fill")
                            .foregroundColor(.orange)
                        Text("Failed to generate summary")
                            .font(.subheadline)
                            .fontWeight(.medium)
                            .foregroundColor(.orange)
                    }
                    
                    Text(errorMessage)
                        .font(.caption)
                        .foregroundColor(.secondary)
                    
                    Button("Try Again") {
                        Task {
                            await generateSummary()
                        }
                    }
                    .font(.caption)
                    .foregroundColor(.blue)
                }
                .padding(.vertical, 8)
            } else if !summary.isEmpty {
                VStack(alignment: .leading, spacing: 8) {
                    Text(summary)
                        .font(.subheadline)
                        .lineLimit(isExpanded ? nil : 3)
                        .animation(.easeInOut(duration: 0.3), value: isExpanded)
                    
                    if !isExpanded && summary.count > 150 {
                        Text("Tap to read more...")
                            .font(.caption)
                            .foregroundColor(.blue)
                            .italic()
                    }
                }
                .padding(.vertical, 8)
            } else {
                Button(action: {
                    Task {
                        await generateSummary()
                    }
                }) {
                    HStack {
                        Image(systemName: "sparkles")
                            .foregroundColor(.purple)
                        Text("Generate AI Summary")
                            .font(.subheadline)
                            .fontWeight(.medium)
                    }
                    .foregroundColor(.purple)
                    .padding(.horizontal, 16)
                    .padding(.vertical, 8)
                    .background(Color.purple.opacity(0.1))
                    .cornerRadius(20)
                }
                .padding(.vertical, 8)
            }
        }
        .padding()
        .background(Color(.systemGray6))
        .cornerRadius(12)
        .onAppear {
            // Auto-generate summary when view appears
            Task {
                await generateSummary()
            }
        }
    }
    
    @MainActor
    private func generateSummary() async {
        isLoading = true
        hasError = false
        errorMessage = ""
        
        do {
            let aiSummary = try await blogService.generateAISummary(for: postSlug)
            summary = aiSummary
            isLoading = false
        } catch {
            hasError = true
            isLoading = false
            
            // Handle different error types
            if let urlError = error as? URLError {
                switch urlError.code {
                case .notConnectedToInternet:
                    errorMessage = "No internet connection"
                case .timedOut:
                    errorMessage = "Request timed out"
                default:
                    errorMessage = "Network error occurred"
                }
            } else {
                errorMessage = error.localizedDescription
            }
        }
    }
}

#Preview {
    VStack {
        AISummaryView(postSlug: "girliesss")
            .padding()
        
        Spacer()
    }
    .background(Color(.systemBackground))
}
