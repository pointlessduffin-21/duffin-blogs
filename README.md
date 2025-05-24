# Duffin's Blog

A modern, full-featured blog platform built with Flask, featuring user authentication, RESTful API support, and a beautiful glassmorphism UI design.

![Python](https://img.shields.io/badge/python-v3.8+-blue.svg)
![Flask](https://img.shields.io/badge/flask-v2.0+-green.svg)
![MongoDB](https://img.shields.io/badge/mongodb-v4.0+-brightgreen.svg)
![License](https://img.shields.io/badge/license-MIT-blue.svg)

## ✨ Features

### 🌟 Core Functionality
- **Blog Management**: Create, edit, view, and delete blog posts
- **Rich Content**: Support for text content with planned rich media support
- **Hero Images**: Upload and display banner images for posts
- **Tagging System**: Organize posts with custom tags
- **SEO-Friendly URLs**: Automatic slug generation from post titles

### 🔐 Authentication & Security
- **User Registration**: Account creation with email verification
- **Secure Login**: Password-protected user sessions with "remember me" option
- **Password Reset**: Email-based password recovery system
- **User Ownership**: Post editing/deletion restricted to authors
- **Password Encryption**: Bcrypt hashing for secure password storage
- **JWT Tokens**: Stateless authentication for API access (7-day expiration)

### 🎨 Modern UI/UX
- **Glassmorphism Design**: Translucent cards with backdrop blur effects
- **Responsive Layout**: Mobile-friendly design across all devices
- **Material Icons**: Consistent iconography throughout the application
- **Gradient Elements**: Beautiful color transitions and hover effects
- **Flash Messages**: Styled user feedback and notification system
- **Dynamic Navigation**: Context-aware navigation based on authentication status

### 📱 API Support
- **RESTful API**: Complete API for mobile app development
- **JWT Authentication**: Token-based authentication for secure API access
- **JSON Responses**: Structured data format for easy integration
- **Mobile Ready**: Prepared for React Native, Swift, and Kotlin integration
- **Error Handling**: Comprehensive error responses with proper HTTP status codes

### 📧 Email Integration
- **SMTP Support**: Configurable email service integration
- **Password Reset**: Secure token-based password recovery
- **Custom Domain**: Support for custom email domains
- **Security Features**: Time-limited tokens and secure email verification

## 🚀 Quick Start

### Prerequisites
- Python 3.8+
- MongoDB 4.0+
- SMTP Email Service (Gmail, iCloud, etc.)

### Installation

1. **Clone the repository**:
   ```bash
   git clone https://github.com/yourusername/duffin-blogs.git
   cd duffin-blogs
   ```

2. **Install dependencies**:
   ```bash
   pip install flask flask-login flask-bcrypt flask-mail pymongo python-dotenv python-slugify pyjwt
   ```

3. **Set up environment variables**:
   Create a `.env` file in the project root:
   ```env
   # Application Security
   SECRET_KEY=your-secret-key-here
   JWT_SECRET_KEY=your-jwt-secret-key-here
   
   # MongoDB Configuration
   MONGO_URI=mongodb://localhost:27017/duffins_blog
   
   # Email Configuration (for password reset)
   MAIL_SERVER=smtp.gmail.com
   MAIL_PORT=587
   MAIL_USE_TLS=True
   MAIL_USERNAME=your-email@gmail.com
   MAIL_PASSWORD=your-app-specific-password
   ```

4. **Create upload directory**:
   ```bash
   mkdir -p static/uploads
   ```

5. **Run the application**:
   ```bash
   python3 app.py
   ```

6. **Access the blog**:
   - Web Interface: `http://localhost:5000`
   - API Endpoints: `http://localhost:5000/api/`

## 🏗️ Project Structure

```
duffin-blogs/
├── app.py                      # Main application file
├── static/
│   ├── style.css              # Custom CSS with glassmorphism design
│   └── uploads/               # Uploaded images storage
├── templates/
│   ├── base.html              # Base template with navigation
│   ├── index.html             # Homepage with post listings
│   ├── create_post.html       # Post creation/editing form
│   ├── view_post.html         # Individual post view
│   └── auth/
│       ├── login.html         # User login form
│       ├── register.html      # User registration form
│       ├── forgot_password.html  # Password reset request
│       └── reset_password.html   # Password reset form
├── test_email.py              # Email configuration testing
├── API_DOCUMENTATION.md       # Complete API documentation
├── EMAIL_SETUP_GUIDE.md      # Email configuration guide
├── README_AUTHENTICATION.md  # Authentication system details
└── README.md                  # This file
```

## 🔧 Configuration

### MongoDB Setup
1. Install MongoDB locally or use MongoDB Atlas
2. Update the `MONGO_URI` in your `.env` file
3. The application will automatically create necessary collections

### Email Configuration
1. **Gmail**: Use App-Specific Passwords
2. **iCloud**: Generate App-Specific Password from Apple ID settings
3. **Custom Domain**: Configure SMTP settings accordingly

For detailed email setup instructions, see [EMAIL_SETUP_GUIDE.md](EMAIL_SETUP_GUIDE.md).

### Security Keys
Generate secure random keys for production:
```bash
# Generate SECRET_KEY
python -c "import secrets; print(secrets.token_hex(32))"

# Generate JWT_SECRET_KEY
python -c "import secrets; print(secrets.token_hex(32))"
```

## 📖 Usage

### Web Interface
1. **Register/Login**: Create an account or sign in to existing account
2. **Create Posts**: Click "Create Post" to write new blog entries
3. **Edit Posts**: Click "Edit" on your own posts to modify content
4. **Manage Content**: Delete posts, add tags, upload hero images

### API Usage
The application provides a complete RESTful API for mobile development:

#### Authentication
```bash
# Register new user
curl -X POST http://localhost:5000/api/register \
  -H "Content-Type: application/json" \
  -d '{"username":"user","email":"user@example.com","password":"password123"}'

# Login user
curl -X POST http://localhost:5000/api/login \
  -H "Content-Type: application/json" \
  -d '{"username":"user","password":"password123"}'
```

#### Blog Operations
```bash
# Get all posts (public)
curl -X GET http://localhost:5000/api/posts

# Create new post (authenticated)
curl -X POST http://localhost:5000/api/posts \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{"title":"My Post","content":"Post content","tags":["tech"]}'

# Update post (author only)
curl -X PUT http://localhost:5000/api/posts/my-post-slug \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{"title":"Updated Title","content":"Updated content"}'

# Delete post (author only)
curl -X DELETE http://localhost:5000/api/posts/my-post-slug \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

For complete API documentation, see [API_DOCUMENTATION.md](API_DOCUMENTATION.md).

## 🗄️ Database Schema

### Users Collection
```javascript
{
  _id: ObjectId,
  username: String (unique),
  email: String (unique),
  password_hash: String,
  created_at: DateTime,
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
  timestamp: DateTime,
  last_updated: DateTime,
  tags: Array,
  hero_banner_url: String
}
```

### Reset Tokens Collection
```javascript
{
  _id: ObjectId,
  user_id: ObjectId (ref: Users),
  token: String,
  created_at: DateTime,
  expires_at: DateTime (1 hour expiration)
}
```

## 🔒 Security Features

- **Password Security**: Bcrypt hashing with salt
- **JWT Tokens**: Secure API authentication with expiration
- **User Authorization**: Resource access control (users can only edit/delete their own posts)
- **Email Verification**: Secure password reset with time-limited tokens
- **Input Validation**: Server-side validation for all user inputs
- **CSRF Protection**: Built-in Flask security measures

## 🎨 Design System

The application features a modern glassmorphism design with:
- **Color Palette**: Deep blues and purples with gradient overlays
- **Typography**: Roboto font family for clean readability
- **Cards**: Translucent containers with backdrop blur
- **Buttons**: Gradient designs with hover animations
- **Icons**: Google Material Icons for consistency
- **Responsive**: Mobile-first design approach

## 🧪 Testing

### Email Testing
Test your email configuration:
```bash
python test_email.py
```

### Manual Testing
1. **Authentication Flow**: Register → Login → Password Reset
2. **Post Management**: Create → Edit → Delete
3. **API Endpoints**: Test with curl or Postman
4. **File Upload**: Test hero image functionality

## 🚀 Deployment

### Environment Variables for Production
```env
SECRET_KEY=production-secret-key
JWT_SECRET_KEY=production-jwt-secret
MONGO_URI=mongodb://production-host:27017/duffins_blog
MAIL_SERVER=your-mail-server
MAIL_USERNAME=your-production-email
MAIL_PASSWORD=your-production-password
```

### Production Considerations
1. **SSL/HTTPS**: Configure reverse proxy with SSL certificates
2. **Database**: Use MongoDB Atlas or dedicated MongoDB instance
3. **File Storage**: Consider cloud storage for uploaded images
4. **Email Service**: Use dedicated email service (SendGrid, Mailgun)
5. **Rate Limiting**: Implement API rate limiting for security
6. **Monitoring**: Add logging and monitoring solutions

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- **Flask**: The Python web framework powering the application
- **MongoDB**: Document database for flexible data storage
- **Material Icons**: Google's icon system for consistent UI
- **Glassmorphism**: Modern design trend for beautiful interfaces

## 📞 Support

For questions, issues, or feature requests:
1. Check the [API_DOCUMENTATION.md](API_DOCUMENTATION.md) for API details
2. Review [EMAIL_SETUP_GUIDE.md](EMAIL_SETUP_GUIDE.md) for email configuration
3. Open an issue on GitHub for bugs or feature requests

---

**Duffin's Blog** - A modern, secure, and API-ready blogging platform built with Flask 🚀