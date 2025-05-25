import Foundation

struct User: Codable, Identifiable {
    let id: String
    let username: String
    let email: String
    
    enum CodingKeys: String, CodingKey {
        case id = "id"
        case _id = "_id"
        case username
        case email
    }
    
    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        
        // Try to decode "id" first, then fall back to "_id"
        if let idValue = try? container.decode(String.self, forKey: .id) {
            self.id = idValue
        } else if let idValue = try? container.decode(String.self, forKey: ._id) {
            self.id = idValue
        } else {
            throw DecodingError.keyNotFound(CodingKeys.id, DecodingError.Context(
                codingPath: decoder.codingPath,
                debugDescription: "Neither 'id' nor '_id' key found"
            ))
        }
        
        self.username = try container.decode(String.self, forKey: .username)
        self.email = try container.decode(String.self, forKey: .email)
    }
    
    func encode(to encoder: Encoder) throws {
        var container = encoder.container(keyedBy: CodingKeys.self)
        try container.encode(id, forKey: .id)
        try container.encode(username, forKey: .username)
        try container.encode(email, forKey: .email)
    }
}

struct AuthResponse: Codable {
    let token: String
    let user: User
}

struct LoginRequest: Codable {
    let username: String
    let password: String
}

struct RegisterRequest: Codable {
    let username: String
    let email: String
    let password: String
}