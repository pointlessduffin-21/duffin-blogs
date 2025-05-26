import Foundation
import Combine

class BlogService: ObservableObject {
    static let shared = BlogService()
    
    private let baseURL = "https://duffin-blogs.yeems214.xyz/api"
    private let session = URLSession.shared
    
    @Published var isAuthenticated = false
    @Published var currentUser: User?
    @Published var posts: [BlogPost] = []
    @Published var isLoading = false
    @Published var errorMessage: String?
    
    private var authToken: String? {
        get {
            UserDefaults.standard.string(forKey: "auth_token")
        }
        set {
            UserDefaults.standard.set(newValue, forKey: "auth_token")
            isAuthenticated = newValue != nil
        }
    }
    
    init() {
        checkAuthStatus()
    }
    
    private func checkAuthStatus() {
        if authToken != nil {
            isAuthenticated = true
            // Load stored user information
            restoreUserFromStorage()
        }
    }
    
    private func restoreUserFromStorage() {
        if let userData = UserDefaults.standard.data(forKey: "current_user"),
           let user = try? JSONDecoder().decode(User.self, from: userData) {
            currentUser = user
        }
    }
    
    private func saveUserToStorage(_ user: User) {
        if let userData = try? JSONEncoder().encode(user) {
            UserDefaults.standard.set(userData, forKey: "current_user")
        }
    }
    
    // MARK: - Authentication Methods
    
    func login(username: String, password: String) async throws {
        let request = LoginRequest(username: username, password: password)
        let response: AuthResponse = try await performRequest(
            endpoint: "/login",
            method: "POST",
            body: request,
            requiresAuth: false
        )
        
        await MainActor.run {
            self.authToken = response.token
            self.currentUser = response.user
            self.isAuthenticated = true
            self.saveUserToStorage(response.user)
        }
    }
    
    func register(username: String, email: String, password: String) async throws {
        let request = RegisterRequest(username: username, email: email, password: password)
        let response: AuthResponse = try await performRequest(
            endpoint: "/register",
            method: "POST",
            body: request,
            requiresAuth: false
        )
        
        await MainActor.run {
            self.authToken = response.token
            self.currentUser = response.user
            self.isAuthenticated = true
            self.saveUserToStorage(response.user)
        }
    }
    
    func logout() {
        authToken = nil
        currentUser = nil
        isAuthenticated = false
        posts = []
        UserDefaults.standard.removeObject(forKey: "current_user")
    }
    
    // MARK: - Blog Post Methods
    
    func fetchPosts() async throws {
        await MainActor.run { isLoading = true }
        
        let response: BlogPostsResponse = try await performRequest(
            endpoint: "/posts",
            method: "GET",
            requiresAuth: false
        )
        
        await MainActor.run {
            self.posts = response.posts
            self.isLoading = false
        }
    }
    
    func fetchPost(slug: String) async throws -> BlogPost {
        let response: BlogPostResponse = try await performRequest(
            endpoint: "/posts/\(slug)",
            method: "GET",
            requiresAuth: false
        )
        return response.post
    }
    
    func createPost(title: String, content: String, tags: [String], heroBannerUrl: String? = nil) async throws {
        let request = CreatePostRequest(
            title: title,
            content: content,
            tags: tags,
            heroBannerUrl: heroBannerUrl
        )
        
        let _: BlogPostResponse = try await performRequest(
            endpoint: "/posts",
            method: "POST",
            body: request,
            requiresAuth: true
        )
        
        // Refresh posts after creating
        try await fetchPosts()
    }
    
    func updatePost(slug: String, title: String, content: String, tags: [String], heroBannerUrl: String? = nil) async throws {
        let request = CreatePostRequest(
            title: title,
            content: content,
            tags: tags,
            heroBannerUrl: heroBannerUrl
        )
        
        let _: BlogPostResponse = try await performRequest(
            endpoint: "/posts/\(slug)",
            method: "PUT",
            body: request,
            requiresAuth: true
        )
        
        // Refresh posts after updating
        try await fetchPosts()
    }
    
    // MARK: - AI Summary Methods
    
    func generateAISummary(for slug: String) async throws -> String {
        let response: AISummaryResponse = try await performRequest(
            endpoint: "/generate-summary/\(slug)",
            method: "GET",
            requiresAuth: false
        )
        return response.summary
    }
    
    // MARK: - Generic Request Handler
    
    private func performRequest<T: Codable, U: Codable>(
        endpoint: String,
        method: String,
        body: U? = nil,
        requiresAuth: Bool = false
    ) async throws -> T where U: Codable {
        
        guard let url = URL(string: baseURL + endpoint) else {
            throw APIError.invalidURL
        }
        
        var request = URLRequest(url: url)
        request.httpMethod = method
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        
        if requiresAuth, let token = authToken {
            request.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization")
        }
        
        if let body = body {
            do {
                request.httpBody = try JSONEncoder().encode(body)
            } catch {
                throw APIError.encodingFailed
            }
        }
        
        do {
            let (data, response) = try await session.data(for: request)
            
            guard let httpResponse = response as? HTTPURLResponse else {
                throw APIError.invalidResponse
            }
            
            if httpResponse.statusCode >= 400 {
                if let errorData = try? JSONDecoder().decode(ErrorResponse.self, from: data) {
                    throw APIError.serverError(errorData.message)
                } else {
                    throw APIError.serverError("HTTP \(httpResponse.statusCode)")
                }
            }
            
            do {
                return try JSONDecoder().decode(T.self, from: data)
            } catch {
                print("Decoding error: \(error)")
                throw APIError.decodingFailed
            }
        } catch {
            if error is APIError {
                throw error
            } else {
                throw APIError.networkError(error.localizedDescription)
            }
        }
    }
    
    private func performRequest<T: Codable>(
        endpoint: String,
        method: String,
        requiresAuth: Bool = false
    ) async throws -> T where T: Codable {
        return try await performRequest(
            endpoint: endpoint,
            method: method,
            body: EmptyBody?.none,
            requiresAuth: requiresAuth
        )
    }
}

// MARK: - Error Handling

enum APIError: LocalizedError {
    case invalidURL
    case encodingFailed
    case decodingFailed
    case invalidResponse
    case networkError(String)
    case serverError(String)
    case unauthorized
    
    var errorDescription: String? {
        switch self {
        case .invalidURL:
            return "Invalid URL"
        case .encodingFailed:
            return "Failed to encode request"
        case .decodingFailed:
            return "Failed to decode response"
        case .invalidResponse:
            return "Invalid response"
        case .networkError(let message):
            return "Network error: \(message)"
        case .serverError(let message):
            return "Server error: \(message)"
        case .unauthorized:
            return "Unauthorized access"
        }
    }
}

struct ErrorResponse: Codable {
    let message: String
}

struct EmptyBody: Codable {}