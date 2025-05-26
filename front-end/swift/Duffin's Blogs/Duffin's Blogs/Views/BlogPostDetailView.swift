import SwiftUI

struct BlogPostDetailView: View {
    let post: BlogPost
    @Environment(\.dismiss) private var dismiss
    @StateObject private var blogService = BlogService.shared
    @StateObject private var userPreferences = UserPreferences.shared
    @State private var showingEditPost = false
    
    // Get the most up-to-date version of the post from the service
    private var currentPost: BlogPost {
        blogService.posts.first(where: { $0.slug == post.slug }) ?? post
    }
    
    private var canEditPost: Bool {
        guard let currentUser = blogService.currentUser else { return false }
        return currentUser.id == currentPost.authorId
    }
    
    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 20) {
                // Hero Image
                if let heroBannerUrl = currentPost.absoluteHeroBannerUrl, !heroBannerUrl.isEmpty {
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
                    Text(currentPost.title)
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
                                    Text(currentPost.authorUsername)
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
                            if let postDate = currentPost.timeAgoAsDate {
                                let dateTime = userPreferences.formatDateTime(postDate)
                                
                                Text(dateTime.date)
                                    .font(.subheadline)
                                    .fontWeight(.medium)
                                
                                Text(dateTime.time)
                                    .font(.caption)
                                    .foregroundColor(.secondary)
                            } else {
                                Text(currentPost.formattedDate)
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
                    if !currentPost.tags.isEmpty {
                        VStack(alignment: .leading, spacing: 8) {
                            Text("Tags")
                                .font(.headline)
                                .fontWeight(.semibold)
                            
                            LazyVGrid(columns: [
                                GridItem(.adaptive(minimum: 80))
                            ], spacing: 8) {
                                ForEach(currentPost.tags, id: \.self) { tag in
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
                    AISummaryView(postSlug: currentPost.slug)
                    
                    Divider()
                    
                    // Content
                    VStack(alignment: .leading, spacing: 12) {
                        Text("Article")
                            .font(.headline)
                            .fontWeight(.semibold)
                        
                        HTMLTextView(htmlContent: currentPost.parsedContent)
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
                    
                    ShareLink(item: "https://duffin-blogs.yeems214.xyz/posts/\(currentPost.slug)") {
                        Image(systemName: "square.and.arrow.up")
                    }
                }
            }
        }
        .sheet(isPresented: $showingEditPost) {
            EditPostView(post: currentPost)
        }
        // Force the view to update when posts array changes
        .onReceive(blogService.$posts) { _ in
            // This will trigger a view update when posts change
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
