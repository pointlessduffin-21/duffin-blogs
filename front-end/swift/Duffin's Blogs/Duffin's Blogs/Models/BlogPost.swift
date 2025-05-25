import Foundation

struct BlogPost: Codable, Identifiable {
    let id: String
    let title: String
    let slug: String
    let content: String
    let parsedContent: String
    let authorId: String
    let authorUsername: String
    let timestamp: String
    let lastUpdated: String
    let tags: [String]
    let heroBannerUrl: String?
    
    enum CodingKeys: String, CodingKey {
        case id = "_id"
        case title
        case slug
        case content
        case parsedContent = "parsed_content"
        case authorId = "author_id"
        case authorUsername = "author_username"
        case timestamp
        case lastUpdated = "last_updated"
        case tags
        case heroBannerUrl = "hero_banner_url"
    }
    
    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        
        id = try container.decode(String.self, forKey: .id)
        title = try container.decodeIfPresent(String.self, forKey: .title) ?? "Untitled Post"
        slug = try container.decodeIfPresent(String.self, forKey: .slug) ?? "untitled-\(id)"
        content = try container.decodeIfPresent(String.self, forKey: .content) ?? ""
        parsedContent = try container.decodeIfPresent(String.self, forKey: .parsedContent) ?? content
        authorId = try container.decodeIfPresent(String.self, forKey: .authorId) ?? ""
        authorUsername = try container.decodeIfPresent(String.self, forKey: .authorUsername) ?? "Unknown Author"
        timestamp = try container.decode(String.self, forKey: .timestamp)
        lastUpdated = try container.decodeIfPresent(String.self, forKey: .lastUpdated) ?? timestamp
        tags = try container.decodeIfPresent([String].self, forKey: .tags) ?? []
        heroBannerUrl = try container.decodeIfPresent(String.self, forKey: .heroBannerUrl)
    }
    
    // MARK: - Computed Properties
    
    /// Returns the absolute URL for the hero banner image
    var absoluteHeroBannerUrl: String? {
        guard let heroBannerUrl = heroBannerUrl else { return nil }
        
        // If already absolute URL, return as is
        if heroBannerUrl.hasPrefix("http://") || heroBannerUrl.hasPrefix("https://") {
            return heroBannerUrl
        }
        
        // Convert relative URL to absolute URL
        if heroBannerUrl.hasPrefix("/") {
            return "http://localhost:5003\(heroBannerUrl)"
        }
        
        return heroBannerUrl
    }
    
    var formattedDate: String {
        let formatter = ISO8601DateFormatter()
        if let date = formatter.date(from: timestamp) {
            let displayFormatter = DateFormatter()
            
            // Check if the date is today
            if Calendar.current.isDateInToday(date) {
                displayFormatter.dateStyle = .none
                displayFormatter.timeStyle = .short
                return "Today at \(displayFormatter.string(from: date))"
            }
            
            // Check if the date is yesterday
            if Calendar.current.isDateInYesterday(date) {
                displayFormatter.dateStyle = .none
                displayFormatter.timeStyle = .short
                return "Yesterday at \(displayFormatter.string(from: date))"
            }
            
            // Check if the date is within this week
            let weekAgo = Calendar.current.date(byAdding: .day, value: -7, to: Date()) ?? Date()
            if date > weekAgo {
                let dayFormatter = DateFormatter()
                dayFormatter.dateFormat = "EEEE" // Day of week
                displayFormatter.dateStyle = .none
                displayFormatter.timeStyle = .short
                return "\(dayFormatter.string(from: date)) at \(displayFormatter.string(from: date))"
            }
            
            // For older dates, show a more friendly format
            displayFormatter.dateStyle = .medium
            displayFormatter.timeStyle = .none
            return displayFormatter.string(from: date)
        }
        return timestamp
    }
    
    var timeAgo: String {
        let formatter = ISO8601DateFormatter()
        if let date = formatter.date(from: timestamp) {
            let now = Date()
            let components = Calendar.current.dateComponents([.day, .hour, .minute], from: date, to: now)
            
            if let days = components.day, days > 0 {
                return "\(days) day\(days == 1 ? "" : "s") ago"
            } else if let hours = components.hour, hours > 0 {
                return "\(hours) hour\(hours == 1 ? "" : "s") ago"
            } else if let minutes = components.minute, minutes > 0 {
                return "\(minutes) minute\(minutes == 1 ? "" : "s") ago"
            } else {
                return "Just now"
            }
        }
        return "Unknown"
    }
    
    // Static method for creating sample instances (for previews and testing)
    static func sample(
        id: String = "1",
        title: String = "Sample Blog Post",
        slug: String = "sample-blog-post",
        content: String = "This is a sample blog post content...",
        parsedContent: String? = nil,
        authorId: String = "author1",
        authorUsername: String = "john_doe",
        timestamp: String = "2025-05-25T10:00:00.000Z",
        lastUpdated: String? = nil,
        tags: [String] = ["swift", "ios", "technology"],
        heroBannerUrl: String? = "https://picsum.photos/400/250"
    ) -> BlogPost {
        let tagsData = try! JSONSerialization.data(withJSONObject: tags, options: [])
        let tagsString = String(data: tagsData, encoding: .utf8)!
        
        let heroBannerPart = heroBannerUrl != nil ? "\"\(heroBannerUrl!)\"" : "null"
        
        let sampleJSON = """
        {
            "_id": "\(id)",
            "title": "\(title)",
            "slug": "\(slug)",
            "content": "\(content)",
            "parsed_content": "\(parsedContent ?? content)",
            "author_id": "\(authorId)",
            "author_username": "\(authorUsername)",
            "timestamp": "\(timestamp)",
            "last_updated": "\(lastUpdated ?? timestamp)",
            "tags": \(tagsString),
            "hero_banner_url": \(heroBannerPart)
        }
        """.data(using: .utf8)!
        
        return try! JSONDecoder().decode(BlogPost.self, from: sampleJSON)
    }
}

struct BlogPostsResponse: Codable {
    let posts: [BlogPost]
}

struct BlogPostResponse: Codable {
    let post: BlogPost
}

struct CreatePostRequest: Codable {
    let title: String
    let content: String
    let tags: [String]
    let heroBannerUrl: String?
    
    enum CodingKeys: String, CodingKey {
        case title
        case content
        case tags
        case heroBannerUrl = "hero_banner_url"
    }
}

// MARK: - AI Summary Models
struct AISummaryResponse: Codable {
    let summary: String
}

struct AISummaryErrorResponse: Codable {
    let error: String
}