import SwiftUI

struct ProfileView: View {
    @StateObject private var blogService = BlogService.shared
    @Environment(\.dismiss) private var dismiss
    
    var body: some View {
        NavigationView {
            Form {
                if let user = blogService.currentUser {
                    Section("User Information") {
                        HStack {
                            Image(systemName: "person.circle.fill")
                                .font(.largeTitle)
                                .foregroundColor(.blue)
                            
                            VStack(alignment: .leading, spacing: 4) {
                                Text(user.username)
                                    .font(.headline)
                                    .fontWeight(.semibold)
                                
                                Text(user.email)
                                    .font(.subheadline)
                                    .foregroundColor(.secondary)
                            }
                            
                            Spacer()
                        }
                        .padding(.vertical, 8)
                    }
                    
                    Section("Statistics") {
                        HStack {
                            VStack(alignment: .leading) {
                                Text("Total Posts")
                                    .font(.caption)
                                    .foregroundColor(.secondary)
                                Text("\(userPostsCount)")
                                    .font(.title2)
                                    .fontWeight(.bold)
                            }
                            
                            Spacer()
                            
                            VStack(alignment: .trailing) {
                                Text("Member Since")
                                    .font(.caption)
                                    .foregroundColor(.secondary)
                                Text("2025")
                                    .font(.title2)
                                    .fontWeight(.bold)
                            }
                        }
                        .padding(.vertical, 4)
                    }
                }
                
                Section("App Information") {
                    HStack {
                        Image(systemName: "info.circle")
                            .foregroundColor(.blue)
                        Text("Version")
                        Spacer()
                        Text("1.0.0")
                            .foregroundColor(.secondary)
                    }
                    
                    HStack {
                        Image(systemName: "globe")
                            .foregroundColor(.blue)
                        Text("Server")
                        Spacer()
                        Text("localhost:5003")
                            .foregroundColor(.secondary)
                    }
                }
                
                Section {
                    Button(action: {
                        blogService.logout()
                        dismiss()
                    }) {
                        HStack {
                            Image(systemName: "rectangle.portrait.and.arrow.right")
                                .foregroundColor(.red)
                            Text("Sign Out")
                                .foregroundColor(.red)
                        }
                    }
                }
            }
            .navigationTitle("Profile")
            .navigationBarTitleDisplayMode(.large)
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("Done") {
                        dismiss()
                    }
                }
            }
        }
    }
    
    private var userPostsCount: Int {
        guard let user = blogService.currentUser else { return 0 }
        return blogService.posts.filter { $0.authorId == user.id }.count
    }
}

#Preview {
    ProfileView()
}
