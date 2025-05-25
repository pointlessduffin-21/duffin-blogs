import SwiftUI

struct BlogListView: View {
    @StateObject private var blogService = BlogService.shared
    @State private var showingCreatePost = false
    @State private var showingProfile = false
    @State private var searchText = ""
    @State private var selectedTag: String?
    
    var filteredPosts: [BlogPost] {
        var posts = blogService.posts
        
        if !searchText.isEmpty {
            posts = posts.filter { post in
                post.title.localizedCaseInsensitiveContains(searchText) ||
                post.content.localizedCaseInsensitiveContains(searchText) ||
                post.authorUsername.localizedCaseInsensitiveContains(searchText)
            }
        }
        
        if let tag = selectedTag {
            posts = posts.filter { $0.tags.contains(tag) }
        }
        
        return posts
    }
    
    var allTags: [String] {
        let tags = blogService.posts.flatMap { $0.tags }
        return Array(Set(tags)).sorted()
    }
    
    var body: some View {
        NavigationView {
            VStack {
                // Search bar
                HStack {
                    Image(systemName: "magnifyingglass")
                        .foregroundColor(.gray)
                    
                    TextField("Search posts...", text: $searchText)
                        .textFieldStyle(RoundedBorderTextFieldStyle())
                }
                .padding(.horizontal)
                
                // Tag filter
                if !allTags.isEmpty {
                    ScrollView(.horizontal, showsIndicators: false) {
                        HStack {
                            TagButton(title: "All", isSelected: selectedTag == nil) {
                                selectedTag = nil
                            }
                            
                            ForEach(allTags, id: \.self) { tag in
                                TagButton(title: tag, isSelected: selectedTag == tag) {
                                    selectedTag = selectedTag == tag ? nil : tag
                                }
                            }
                        }
                        .padding(.horizontal)
                    }
                }
                
                // Blog posts list
                if blogService.isLoading && blogService.posts.isEmpty {
                    ProgressView("Loading posts...")
                        .frame(maxWidth: .infinity, maxHeight: .infinity)
                } else if blogService.posts.isEmpty {
                    VStack(spacing: 20) {
                        Image(systemName: "doc.text")
                            .font(.system(size: 60))
                            .foregroundColor(.gray)
                        
                        Text("No blog posts yet")
                            .font(.title2)
                            .fontWeight(.semibold)
                        
                        Text("Be the first to share your thoughts!")
                            .foregroundColor(.secondary)
                        
                        Button("Create Post") {
                            showingCreatePost = true
                        }
                        .buttonStyle(.borderedProminent)
                    }
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                } else {
                    List(filteredPosts) { post in
                        NavigationLink(destination: BlogPostDetailView(post: post)) {
                            BlogPostRowView(post: post)
                        }
                        .listRowSeparator(.hidden)
                        .contextMenu {
                            Button(action: {
                                // Open post action
                            }) {
                                Label("Open Post", systemImage: "doc.text")
                            }
                            
                            Button(action: {
                                // Share post action
                                let shareText = "Check out this blog post: \(post.title)"
                                if let windowScene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
                                   let window = windowScene.windows.first {
                                    let activityController = UIActivityViewController(
                                        activityItems: [shareText],
                                        applicationActivities: nil
                                    )
                                    window.rootViewController?.present(activityController, animated: true)
                                }
                            }) {
                                Label("Share", systemImage: "square.and.arrow.up")
                            }
                            
                            Button(action: {
                                // Copy link action
                                UIPasteboard.general.string = "Check out this blog post: \(post.title)"
                            }) {
                                Label("Copy Link", systemImage: "link")
                            }
                        } preview: {
                            BlogPostDetailView(post: post)
                                .frame(width: 300, height: 500)
                                .cornerRadius(16)
                        }
                    }
                    .listStyle(PlainListStyle())
                    .refreshable {
                        Task {
                            do {
                                try await blogService.fetchPosts()
                            } catch {
                                // Handle error silently or show alert
                            }
                        }
                    }
                }
            }
            .navigationTitle("Duffin's Blogs")
            .navigationBarTitleDisplayMode(.large)
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    HStack {
                        if blogService.isAuthenticated {
                            Button(action: { showingCreatePost = true }) {
                                Image(systemName: "plus")
                            }
                            
                            Button(action: { showingProfile = true }) {
                                Image(systemName: "person.circle")
                            }
                        } else {
                            Button(action: { showingProfile = true }) {
                                Image(systemName: "person")
                            }
                        }
                    }
                }
            }
            .onAppear {
                Task {
                    do {
                        try await blogService.fetchPosts()
                    } catch {
                        // Handle error silently
                    }
                }
            }
            .sheet(isPresented: $showingCreatePost) {
                CreatePostView()
            }
            .sheet(isPresented: $showingProfile) {
                if blogService.isAuthenticated {
                    ProfileView()
                } else {
                    LoginView()
                }
            }
        }
    }
}

struct TagButton: View {
    let title: String
    let isSelected: Bool
    let action: () -> Void
    
    var body: some View {
        Button(action: action) {
            Text(title)
                .font(.caption)
                .fontWeight(.medium)
                .padding(.horizontal, 12)
                .padding(.vertical, 6)
                .background(
                    isSelected ? Color.blue : Color(.systemGray5)
                )
                .foregroundColor(isSelected ? .white : .primary)
                .cornerRadius(16)
        }
    }
}

struct BlogPostRowView: View {
    let post: BlogPost
    
    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
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
                .frame(height: 180)
                .cornerRadius(12)
                .clipped()
            }
            
            VStack(alignment: .leading, spacing: 8) {
                // Title
                Text(post.title)
                    .font(.headline)
                    .fontWeight(.bold)
                    .lineLimit(2)
                
                // Content Preview
                Text(post.content)
                    .font(.subheadline)
                    .foregroundColor(.secondary)
                    .lineLimit(3)
                
                // Tags
                if !post.tags.isEmpty {
                    ScrollView(.horizontal, showsIndicators: false) {
                        HStack(spacing: 6) {
                            ForEach(post.tags, id: \.self) { tag in
                                Text("#\(tag)")
                                    .font(.caption)
                                    .foregroundColor(.blue)
                                    .padding(.horizontal, 8)
                                    .padding(.vertical, 4)
                                    .background(Color.blue.opacity(0.1))
                                    .cornerRadius(8)
                            }
                        }
                        .padding(.horizontal, 1)
                    }
                }
                
                // Author and Date
                HStack {
                    HStack(spacing: 4) {
                        Image(systemName: "person.circle.fill")
                            .foregroundColor(.blue)
                        Text(post.authorUsername)
                            .font(.caption)
                            .fontWeight(.medium)
                    }
                    
                    Spacer()
                    
                    Text(post.timeAgo)
                        .font(.caption)
                        .foregroundColor(.secondary)
                }
            }
            .padding(.horizontal, 4)
        }
        .padding()
        .background(Color(.systemBackground))
        .cornerRadius(16)
        .shadow(color: .black.opacity(0.1), radius: 4, x: 0, y: 2)
    }
}

#Preview {
    BlogListView()
}
