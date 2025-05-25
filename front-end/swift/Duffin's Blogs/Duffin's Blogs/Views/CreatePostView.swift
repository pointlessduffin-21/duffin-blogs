import SwiftUI

struct CreatePostView: View {
    @StateObject private var blogService = BlogService.shared
    @Environment(\.dismiss) private var dismiss
    
    @State private var title = ""
    @State private var content = ""
    @State private var tags = ""
    @State private var heroBannerUrl = ""
    @State private var isLoading = false
    @State private var showError = false
    @State private var errorMessage = ""
    
    var body: some View {
        NavigationView {
            ScrollView {
                VStack(spacing: 20) {
                    VStack(alignment: .leading, spacing: 8) {
                        Text("Title")
                            .font(.headline)
                            .fontWeight(.semibold)
                        
                        TextField("Enter post title", text: $title)
                            .textFieldStyle(RoundedBorderTextFieldStyle())
                            .font(.body)
                    }
                    
                    VStack(alignment: .leading, spacing: 8) {
                        Text("Hero Image URL (Optional)")
                            .font(.headline)
                            .fontWeight(.semibold)
                        
                        TextField("https://example.com/image.jpg", text: $heroBannerUrl)
                            .textFieldStyle(RoundedBorderTextFieldStyle())
                            .font(.body)
                            .autocapitalization(.none)
                            .keyboardType(.URL)
                        
                        if !heroBannerUrl.isEmpty, let url = URL(string: heroBannerUrl) {
                            AsyncImage(url: url) { image in
                                image
                                    .resizable()
                                    .aspectRatio(contentMode: .fill)
                            } placeholder: {
                                Rectangle()
                                    .fill(Color(.systemGray5))
                                    .overlay(
                                        ProgressView()
                                    )
                            }
                            .frame(height: 150)
                            .cornerRadius(12)
                            .clipped()
                        }
                    }
                    
                    VStack(alignment: .leading, spacing: 8) {
                        Text("Tags")
                            .font(.headline)
                            .fontWeight(.semibold)
                        
                        TextField("technology, ios, swift (comma separated)", text: $tags)
                            .textFieldStyle(RoundedBorderTextFieldStyle())
                            .font(.body)
                            .autocapitalization(.none)
                        
                        Text("Separate tags with commas")
                            .font(.caption)
                            .foregroundColor(.secondary)
                    }
                    
                    VStack(alignment: .leading, spacing: 8) {
                        Text("Content")
                            .font(.headline)
                            .fontWeight(.semibold)
                        
                        TextEditor(text: $content)
                            .frame(minHeight: 200)
                            .padding(8)
                            .background(Color(.systemGray6))
                            .cornerRadius(8)
                            .font(.body)
                    }
                    
                    Button(action: createPost) {
                        HStack {
                            if isLoading {
                                ProgressView()
                                    .progressViewStyle(CircularProgressViewStyle(tint: .white))
                                    .scaleEffect(0.8)
                            } else {
                                Image(systemName: "plus.circle.fill")
                                Text("Create Post")
                                    .fontWeight(.semibold)
                            }
                        }
                        .frame(maxWidth: .infinity)
                        .padding()
                        .background(
                            LinearGradient(
                                colors: [.blue, .blue.opacity(0.8)],
                                startPoint: .leading,
                                endPoint: .trailing
                            )
                        )
                        .foregroundColor(.white)
                        .cornerRadius(12)
                    }
                    .disabled(isLoading || !isFormValid)
                    
                    Spacer(minLength: 50)
                }
                .padding(.horizontal, 20)
            }
            .navigationTitle("New Post")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("Cancel") {
                        dismiss()
                    }
                }
            }
        }
        .alert("Error", isPresented: $showError) {
            Button("OK") { }
        } message: {
            Text(errorMessage)
        }
    }
    
    private var isFormValid: Bool {
        !title.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty &&
        !content.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty
    }
    
    private var parsedTags: [String] {
        tags.components(separatedBy: ",")
            .map { $0.trimmingCharacters(in: .whitespacesAndNewlines) }
            .filter { !$0.isEmpty }
    }
    
    private func createPost() {
        isLoading = true
        
        let trimmedTitle = title.trimmingCharacters(in: .whitespacesAndNewlines)
        let trimmedContent = content.trimmingCharacters(in: .whitespacesAndNewlines)
        let trimmedBannerUrl = heroBannerUrl.trimmingCharacters(in: .whitespacesAndNewlines)
        
        Task {
            do {
                try await blogService.createPost(
                    title: trimmedTitle,
                    content: trimmedContent,
                    tags: parsedTags,
                    heroBannerUrl: trimmedBannerUrl.isEmpty ? nil : trimmedBannerUrl
                )
                
                await MainActor.run {
                    isLoading = false
                    dismiss()
                }
            } catch {
                await MainActor.run {
                    isLoading = false
                    errorMessage = error.localizedDescription
                    showError = true
                }
            }
        }
    }
}

#Preview {
    CreatePostView()
}
