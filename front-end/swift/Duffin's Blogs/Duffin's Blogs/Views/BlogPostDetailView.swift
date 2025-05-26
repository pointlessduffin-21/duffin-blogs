import SwiftUI

struct BlogPostDetailView: View {
    let post: BlogPost
    @Environment(\.dismiss) private var dismiss
    @StateObject private var blogService = BlogService.shared
    @StateObject private var userPreferences = UserPreferences.shared
    @State private var showingEditPost = false
    
    private var canEditPost: Bool {
        guard let currentUser = blogService.currentUser else { return false }
        return currentUser.id == post.authorId
    }
    
    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 20) {
                // Hero Image
                if let heroBannerUrl = post.absoluteHeroBannerUrl, !heroBannerUrl.isEmpty {
                    AsyncImage(url: URL(string: heroBannerUrl)) { image in
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
                    .frame(height: 250)
                    .cornerRadius(16)
                    .clipped()
                }
                
                VStack(alignment: .leading, spacing: 16) {
                    // Title
                    Text(post.title)
                        .font(.largeTitle)
                        .fontWeight(.bold)
                        .fixedSize(horizontal: false, vertical: true)
                    
                    // Author and Date Info
                    HStack {
                        VStack(alignment: .leading, spacing: 4) {
                            HStack(spacing: 8) {
                                Image(systemName: "person.circle.fill")
                                    .foregroundColor(.blue)
                                    .font(.title3)
                                
                                VStack(alignment: .leading, spacing: 2) {
                                    Text(post.authorUsername)
                                        .font(.headline)
                                        .fontWeight(.semibold)
                                    
                                    Text("Author")
                                        .font(.caption)
                                        .foregroundColor(.secondary)
                                }
                            }
                        }
                        
                        Spacer()
                        
                        VStack(alignment: .trailing, spacing: 4) {
                            if let postDate = post.timeAgoAsDate {
                                let dateTime = userPreferences.formatDateTime(postDate)
                                
                                Text(dateTime.date)
                                    .font(.subheadline)
                                    .fontWeight(.medium)
                                
                                Text(dateTime.time)
                                    .font(.caption)
                                    .foregroundColor(.secondary)
                            } else {
                                Text(post.formattedDate)
                                    .font(.subheadline)
                                    .fontWeight(.medium)
                                
                                Text("Time unavailable")
                                    .font(.caption)
                                    .foregroundColor(.secondary)
                            }
                        }
                    }
                    .padding()
                    .background(Color(.systemGray6))
                    .cornerRadius(12)
                    
                    // Tags
                    if !post.tags.isEmpty {
                        VStack(alignment: .leading, spacing: 8) {
                            Text("Tags")
                                .font(.headline)
                                .fontWeight(.semibold)
                            
                            LazyVGrid(columns: [
                                GridItem(.adaptive(minimum: 80))
                            ], spacing: 8) {
                                ForEach(post.tags, id: \.self) { tag in
                                    Text("#\(tag)")
                                        .font(.caption)
                                        .fontWeight(.medium)
                                        .foregroundColor(.blue)
                                        .padding(.horizontal, 12)
                                        .padding(.vertical, 6)
                                        .background(Color.blue.opacity(0.1))
                                        .cornerRadius(16)
                                }
                            }
                        }
                    }
                    
                    Divider()
                    
                    // AI Summary
                    AISummaryView(postSlug: post.slug)
                    
                    Divider()
                    
                    // Content
                    VStack(alignment: .leading, spacing: 12) {
                        Text("Article")
                            .font(.headline)
                            .fontWeight(.semibold)
                        
                        HTMLTextView(htmlContent: post.parsedContent)
                            .frame(minHeight: 200)
                    }
                }
                .padding(.horizontal, 20)
            }
            .padding(.bottom, 40)
        }
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .navigationBarTrailing) {
                HStack {
                    if canEditPost {
                        Button(action: { showingEditPost = true }) {
                            Image(systemName: "pencil")
                        }
                    }
                    
                    ShareLink(item: "Check out this blog post: \(post.title)") {
                        Image(systemName: "square.and.arrow.up")
                    }
                }
            }
        }
        .sheet(isPresented: $showingEditPost) {
            EditPostView(post: post)
        }
    }
}

#Preview {
    NavigationView {
        BlogPostDetailView(post: BlogPost.sample(
            title: "Sample Blog Post",
            content: "This is a sample blog post content. It contains multiple paragraphs and demonstrates how the content will be displayed in the detail view. The content can be quite long and will scroll properly.",
            tags: ["swift", "ios", "technology"]
        ))
    }
}
