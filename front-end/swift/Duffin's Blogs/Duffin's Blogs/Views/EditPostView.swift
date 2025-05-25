import SwiftUI
import UIKit
import PhotosUI
import UniformTypeIdentifiers

struct EditPostView: View {
    let post: BlogPost
    @StateObject private var blogService = BlogService.shared
    @Environment(\.dismiss) private var dismiss
    
    @State private var title: String
    @State private var content: String
    @State private var tags: String
    @State private var selectedImage: UIImage?
    @State private var selectedPhotoItem: PhotosPickerItem?
    @State private var showImagePicker = false
    @State private var showImageSourceActionSheet = false
    @State private var showCamera = false
    @State private var showDocumentPicker = false
    @State private var isLoading = false
    @State private var showError = false
    @State private var errorMessage = ""
    @State private var removeCurrentImage = false
    
    init(post: BlogPost) {
        self.post = post
        _title = State(initialValue: post.title)
        _content = State(initialValue: post.content)
        _tags = State(initialValue: post.tags.joined(separator: ", "))
    }
    
    var body: some View {
        NavigationView {
            ScrollView {
                VStack(spacing: 20) {
                    titleSection
                    imageSection
                    tagsSection
                    contentSection
                    updateButton
                    Spacer(minLength: 50)
                }
                .padding(.horizontal, 20)
            }
            .navigationTitle("Edit Post")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("Cancel") {
                        dismiss()
                    }
                }
            }
        }
        .photosPicker(isPresented: $showImagePicker, selection: $selectedPhotoItem, matching: .images)
        .sheet(isPresented: $showCamera) {
            ImagePicker(selectedImage: $selectedImage, sourceType: .camera)
        }
        .sheet(isPresented: $showDocumentPicker) {
            DocumentPicker(selectedImage: $selectedImage)
        }
        .actionSheet(isPresented: $showImageSourceActionSheet) {
            ActionSheet(
                title: Text("Select Image Source"),
                message: Text("Choose how you'd like to update your hero image"),
                buttons: [
                    .default(Text("Camera")) {
                        showCamera = true
                    },
                    .default(Text("Photo Library")) {
                        showImagePicker = true
                    },
                    .default(Text("Files")) {
                        showDocumentPicker = true
                    },
                    .cancel()
                ]
            )
        }
        .onChange(of: selectedPhotoItem) { _, newItem in
            Task {
                if let newItem = newItem,
                   let data = try? await newItem.loadTransferable(type: Data.self),
                   let image = UIImage(data: data) {
                    await MainActor.run {
                        selectedImage = image
                        removeCurrentImage = false
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
    
    // MARK: - View Components
    
    private var titleSection: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("Title")
                .font(.headline)
                .fontWeight(.semibold)
            
            TextField("Enter post title", text: $title)
                .textFieldStyle(RoundedBorderTextFieldStyle())
                .font(.body)
        }
    }
    
    private var imageSection: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("Hero Image (Optional)")
                .font(.headline)
                .fontWeight(.semibold)
            
            if let currentSelectedImage = selectedImage {
                // Show newly selected image
                VStack(spacing: 12) {
                    Image(uiImage: currentSelectedImage)
                        .resizable()
                        .aspectRatio(contentMode: .fill)
                        .frame(height: 200)
                        .cornerRadius(12)
                        .clipped()
                    
                    HStack(spacing: 16) {
                        Button("Change Image") {
                            showImageSourceActionSheet = true
                        }
                        .foregroundColor(.blue)
                        
                        Button("Remove Image") {
                            selectedImage = nil
                            removeCurrentImage = true
                        }
                        .foregroundColor(.red)
                    }
                    .font(.subheadline)
                }
            } else if let currentImageUrl = post.absoluteHeroBannerUrl, !currentImageUrl.isEmpty, !removeCurrentImage {
                // Show current image from post
                VStack(spacing: 12) {
                    AsyncImage(url: URL(string: currentImageUrl)) { image in
                        image
                            .resizable()
                            .aspectRatio(contentMode: .fill)
                    } placeholder: {
                        Rectangle()
                            .fill(Color(.systemGray5))
                            .overlay(ProgressView())
                    }
                    .frame(height: 200)
                    .cornerRadius(12)
                    .clipped()
                    
                    HStack(spacing: 16) {
                        Button("Change Image") {
                            showImageSourceActionSheet = true
                        }
                        .foregroundColor(.blue)
                        
                        Button("Remove Image") {
                            removeCurrentImage = true
                        }
                        .foregroundColor(.red)
                    }
                    .font(.subheadline)
                }
            } else {
                // Show add image button
                addImageButton
            }
        }
    }
    
    private var addImageButton: some View {
        Button(action: { showImageSourceActionSheet = true }) {
            VStack(spacing: 12) {
                Image(systemName: "photo.badge.plus")
                    .font(.system(size: 40))
                    .foregroundColor(.blue)
                
                Text("Add Hero Image")
                    .font(.headline)
                    .foregroundColor(.blue)
                
                Text("Camera • Photos • Files")
                    .font(.caption)
                    .foregroundColor(.secondary)
            }
            .frame(maxWidth: .infinity)
            .frame(height: 120)
            .background(Color(.systemGray6))
            .cornerRadius(12)
            .overlay(
                RoundedRectangle(cornerRadius: 12)
                    .stroke(Color.blue.opacity(0.3), lineWidth: 2)
                    .background(RoundedRectangle(cornerRadius: 12).fill(Color.blue.opacity(0.05)))
            )
        }
        .buttonStyle(PlainButtonStyle())
    }
    
    private var tagsSection: some View {
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
    }
    
    private var contentSection: some View {
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
    }
    
    private var updateButton: some View {
        Button(action: updatePost) {
            HStack {
                if isLoading {
                    ProgressView()
                        .progressViewStyle(CircularProgressViewStyle(tint: .white))
                        .scaleEffect(0.8)
                } else {
                    Image(systemName: "checkmark.circle.fill")
                    Text("Update Post")
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
    }
    
    // MARK: - Helper Methods
    
    private var isFormValid: Bool {
        !title.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty &&
        !content.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty
    }
    
    private var parsedTags: [String] {
        tags.components(separatedBy: ",")
            .map { $0.trimmingCharacters(in: .whitespacesAndNewlines) }
            .filter { !$0.isEmpty }
    }
    
    private func updatePost() {
        isLoading = true
        
        let trimmedTitle = title.trimmingCharacters(in: .whitespacesAndNewlines)
        let trimmedContent = content.trimmingCharacters(in: .whitespacesAndNewlines)
        
        // Determine final banner URL
        var finalBannerUrl: String? = nil
        
        if removeCurrentImage {
            // User wants to remove image
            finalBannerUrl = nil
        } else if let selectedImage = selectedImage {
            // User selected a new image
            if let imageData = selectedImage.jpegData(compressionQuality: 0.8) {
                let base64String = imageData.base64EncodedString()
                finalBannerUrl = "data:image/jpeg;base64,\(base64String)"
            }
        } else {
            // Keep existing image
            finalBannerUrl = post.heroBannerUrl
        }
        
        Task {
            do {
                try await blogService.updatePost(
                    slug: post.slug,
                    title: trimmedTitle,
                    content: trimmedContent,
                    tags: parsedTags,
                    heroBannerUrl: finalBannerUrl
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
    EditPostView(post: BlogPost.sample(
        title: "Sample Blog Post",
        content: "This is a sample blog post content for editing...",
        tags: ["swift", "ios", "technology"]
    ))
}
