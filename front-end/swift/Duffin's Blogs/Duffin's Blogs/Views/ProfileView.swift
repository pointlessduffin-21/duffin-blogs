import SwiftUI

struct ProfileView: View {
    @StateObject private var blogService = BlogService.shared
    @StateObject private var userPreferences = UserPreferences.shared
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
                
                Section("Settings") {
                    HStack {
                        Image(systemName: "clock")
                            .foregroundColor(.blue)
                        Text("Time Format")
                        Spacer()
                        Toggle("", isOn: $userPreferences.use24HourFormat)
                            .labelsHidden()
                    }
                    
                    HStack {
                        VStack(alignment: .leading, spacing: 2) {
                            Text(userPreferences.use24HourFormat ? "24-Hour Format" : "12-Hour Format")
                                .font(.caption)
                                .foregroundColor(.secondary)
                            Text("Example: " + userPreferences.formatTime(Date()))
                                .font(.caption2)
                                .foregroundColor(.secondary)
                        }
                        Spacer()
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
                        Text("https://duffin-blogs.yeems214.xyz")
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
