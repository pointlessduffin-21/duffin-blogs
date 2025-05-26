# Duffin's Blog - API Documentation

## Overview

Duffin's Blog provides a comprehensive RESTful API for mobile app development and third-party integrations. The API supports full authentication, blog post management, and user operations with JWT token-based security.

**Base URL:** `http://localhost:5003/api`  
**Authentication:** JWT Bearer Token  
**Content-Type:** `application/json`

---

## 🔐 Authentication

### JWT Token Authentication

Most endpoints require a JWT token in the Authorization header:

```bash
Authorization: Bearer YOUR_JWT_TOKEN
```

**Token Expiration:** 7 days  
**Token Payload:**
```json
{
  "user_id": "user_id_string",
  "username": "username",
  "exp": timestamp
}
```

---

## 📋 API Endpoints

### Authentication Endpoints

#### 1. User Registration

**POST** `/api/register`

Register a new user account and receive a JWT token.

**Request Body:**
```json
{
  "username": "string (required)",
  "email": "string (required, valid email)",
  "password": "string (required, min 6 characters)"
}
```

**Success Response (201):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": "683180e41eb5184b5f3587b3",
    "username": "john_doe",
    "email": "john@example.com"
  }
}
```

**Error Responses:**
- `400` - Missing required fields
- `400` - Password too short (< 6 characters)
- `400` - Username already exists
- `400` - Email already registered
- `500` - Registration failed

**Example:**
```bash
curl -X POST http://localhost:5003/api/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "email": "john@example.com",
    "password": "securepass123"
  }'
```

---

#### 2. User Login

**POST** `/api/login`

Authenticate user and receive a JWT token.

**Request Body:**
```json
{
  "username": "string (required)",
  "password": "string (required)"
}
```

**Success Response (200):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": "683180e41eb5184b5f3587b3",
    "username": "john_doe",
    "email": "john@example.com"
  }
}
```

**Error Responses:**
- `400` - Missing username or password
- `401` - Invalid credentials
- `500` - Login failed

**Example:**
```bash
curl -X POST http://localhost:5003/api/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "password": "securepass123"
  }'
```

---

### Blog Post Endpoints

#### 3. Get All Posts

**GET** `/api/posts`

Retrieve all blog posts (public endpoint).

**Authentication:** Not required

**Success Response (200):**
```json
{
  "posts": [
    {
      "_id": "683180e41eb5184b5f3587b4",
      "title": "My First Post",
      "slug": "my-first-post",
      "content": "This is the raw content...",
      "parsed_content": "This is the <strong>parsed</strong> content...",
      "author_id": "683180e41eb5184b5f3587b3",
      "author_username": "john_doe",
      "timestamp": "2025-05-24T16:30:00.000Z",
      "last_updated": "2025-05-24T16:30:00.000Z",
      "tags": ["technology", "blog"],
      "hero_banner_url": "https://example.com/banner.jpg"
    }
  ]
}
```

**Error Response:**
- `500` - Failed to fetch posts

**Example:**
```bash
curl -X GET http://localhost:5003/api/posts
```

---

#### 4. Get Single Post

**GET** `/api/posts/{slug}`

Retrieve a specific blog post by its slug (public endpoint).

**Authentication:** Not required

**Path Parameters:**
- `slug` (string) - The post slug

**Success Response (200):**
```json
{
  "post": {
    "_id": "683180e41eb5184b5f3587b4",
    "title": "My First Post",
    "slug": "my-first-post",
    "content": "This is the raw content...",
    "parsed_content": "This is the <strong>parsed</strong> content...",
    "author_id": "683180e41eb5184b5f3587b3",
    "author_username": "john_doe",
    "timestamp": "2025-05-24T16:30:00.000Z",
    "last_updated": "2025-05-24T16:30:00.000Z",
    "tags": ["technology", "blog"],
    "hero_banner_url": "https://example.com/banner.jpg"
  }
}
```

**Error Responses:**
- `404` - Post not found
- `500` - Failed to fetch post

**Example:**
```bash
curl -X GET http://localhost:5003/api/posts/my-first-post
```

---

#### 5. Create New Post

**POST** `/api/posts`

Create a new blog post (authenticated users only).

**Authentication:** Required (JWT Token)

**Request Body:**
```json
{
  "title": "string (required)",
  "content": "string (optional, default: '')",
  "tags": ["string"] // optional array
}
```

**Success Response (201):**
```json
{
  "post": {
    "_id": "683180e41eb5184b5f3587b5",
    "title": "New Post",
    "slug": "new-post",
    "content": "Post content here...",
    "author_id": "683180e41eb5184b5f3587b3",
    "author_username": "john_doe",
    "timestamp": "2025-05-24T16:30:00.000Z",
    "last_updated": "2025-05-24T16:30:00.000Z",
    "tags": ["technology"]
  }
}
```

**Error Responses:**
- `400` - Title is required
- `401` - Unauthorized (invalid/missing token)
- `500` - Failed to create post

**Example:**
```bash
curl -X POST http://localhost:5003/api/posts \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "title": "My New Post",
    "content": "This is the content of my new post.",
    "tags": ["technology", "programming"]
  }'
```

---

#### 6. Update Post

**PUT** `/api/posts/{slug}`

Update an existing blog post (author only).

**Authentication:** Required (JWT Token)  
**Authorization:** Only the post author can update

**Path Parameters:**
- `slug` (string) - The post slug

**Request Body:**
```json
{
  "title": "string (required)",
  "content": "string (optional)",
  "tags": ["string"] // optional array
}
```

**Success Response (200):**
```json
{
  "post": {
    "_id": "683180e41eb5184b5f3587b4",
    "title": "Updated Post Title",
    "slug": "updated-post-title",
    "content": "Updated content...",
    "author_id": "683180e41eb5184b5f3587b3",
    "author_username": "john_doe",
    "timestamp": "2025-05-24T16:30:00.000Z",
    "last_updated": "2025-05-24T16:35:00.000Z",
    "tags": ["updated", "post"]
  }
}
```

**Error Responses:**
- `400` - Title is required
- `400` - A post with this title already exists
- `401` - Unauthorized (invalid/missing token)
- `403` - You can only edit your own posts
- `404` - Post not found
- `500` - Failed to update post

**Example:**
```bash
curl -X PUT http://localhost:5003/api/posts/my-first-post \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "title": "Updated Post Title",
    "content": "This is the updated content.",
    "tags": ["updated", "technology"]
  }'
```

---

#### 7. Delete Post

**DELETE** `/api/posts/{slug}`

Delete a blog post (author only).

**Authentication:** Required (JWT Token)  
**Authorization:** Only the post author can delete

**Path Parameters:**
- `slug` (string) - The post slug

**Success Response (200):**
```json
{
  "message": "Post deleted successfully"
}
```

**Error Responses:**
- `401` - Unauthorized (invalid/missing token)
- `403` - You can only delete your own posts
- `404` - Post not found
- `500` - Failed to delete post

**Example:**
```bash
curl -X DELETE http://localhost:5003/api/posts/my-first-post \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

## 🔒 Security Features

### Authentication & Authorization

1. **Password Security:**
   - Passwords hashed using bcrypt
   - Minimum 6 characters required
   - No passwords stored in plaintext

2. **JWT Tokens:**
   - 7-day expiration
   - Signed with secret key
   - Required for protected endpoints

3. **User Ownership:**
   - Users can only edit/delete their own posts
   - Author validation on all modifications
   - Proper error messages without information disclosure

4. **API Rate Limiting:**
   - Production deployment should implement rate limiting
   - Consider using tools like Flask-Limiter

---

## 📱 Mobile App Integration

### Authentication Flow

1. **User Registration/Login:**
   ```javascript
   // Register new user
   const response = await fetch('/api/register', {
     method: 'POST',
     headers: { 'Content-Type': 'application/json' },
     body: JSON.stringify({
       username: 'user123',
       email: 'user@example.com',
       password: 'securepass'
     })
   });
   const { token, user } = await response.json();
   
   // Store token securely
   localStorage.setItem('authToken', token);
   ```

2. **Authenticated Requests:**
   ```javascript
   const token = localStorage.getItem('authToken');
   const response = await fetch('/api/posts', {
     method: 'POST',
     headers: {
       'Content-Type': 'application/json',
       'Authorization': `Bearer ${token}`
     },
     body: JSON.stringify({
       title: 'New Post',
       content: 'Post content...'
     })
   });
   ```

### Data Models

#### User Object
```typescript
interface User {
  id: string;
  username: string;
  email: string;
}
```

#### Post Object
```typescript
interface Post {
  _id: string;
  title: string;
  slug: string;
  content: string;
  parsed_content: string;
  author_id: string;
  author_username: string;
  timestamp: string; // ISO format
  last_updated: string; // ISO format
  tags: string[];
  hero_banner_url?: string;
}
```

#### API Response
```typescript
interface ApiResponse<T> {
  data?: T;
  message?: string;
  error?: string;
}
```

---

## 🚀 Getting Started

### 1. Start the Server
```bash
python3 app.py
# Server runs on http://localhost:5003
```

### 2. Test the API
```bash
# Health check
curl http://localhost:5003/api/posts

# Register a user
curl -X POST http://localhost:5003/api/register \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","email":"test@example.com","password":"password123"}'

# Login and get token
curl -X POST http://localhost:5003/api/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"password123"}'
```

### 3. Environment Setup

Required environment variables in `.env`:
```bash
MONGO_URI=mongodb://root:password@localhost:27017/duffins_blog?authSource=admin
SECRET_KEY=your-secret-key
JWT_SECRET_KEY=your-jwt-secret
MAIL_SERVER=smtp.gmail.com
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
```

---

## 📊 Database Schema

### Users Collection
```javascript
{
  _id: ObjectId,
  username: String (unique),
  email: String (unique),
  password_hash: String,
  created_at: Date,
  is_active: Boolean
}
```

### Posts Collection
```javascript
{
  _id: ObjectId,
  title: String,
  slug: String (unique),
  content: String,
  author_id: ObjectId (ref: Users),
  author_username: String,
  timestamp: Date,
  last_updated: Date,
  tags: [String],
  hero_banner_url: String
}
```

### Reset Tokens Collection
```javascript
{
  _id: ObjectId,
  user_id: ObjectId (ref: Users),
  token: String,
  created_at: Date,
  expires_at: Date
}
```

---

## ⚠️ Error Handling

### Standard Error Response Format
```json
{
  "message": "Error description",
  "error": "Detailed error message (in development)"
}
```

### HTTP Status Codes
- `200` - Success
- `201` - Created
- `400` - Bad Request (validation errors)
- `401` - Unauthorized (authentication required)
- `403` - Forbidden (authorization failed)
- `404` - Not Found
- `500` - Internal Server Error

---

## 🔧 Development Notes

### Content Parsing
- The API returns both `content` (raw) and `parsed_content` (processed)
- `parsed_content` includes HTML formatting for display
- Mobile apps can choose which version to use

### Slug Generation
- Post slugs are auto-generated from titles
- Duplicate slugs get numbered suffixes (e.g., `title-1`, `title-2`)
- Updating a post title updates the slug

### Token Management
- JWT tokens expire after 7 days
- Mobile apps should handle token refresh
- Invalid tokens return 401 status

---

## 📞 Support

For API support and questions:
- Check the server logs for detailed error messages
- Ensure proper JSON formatting in requests
- Verify authentication headers for protected endpoints
- Test endpoints individually before integration

---

*Last Updated: May 24, 2025*  
*API Version: 1.0*
