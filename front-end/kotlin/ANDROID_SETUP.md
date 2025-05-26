# Duffin's Blog Android App

A Material 3 Android front-end for the Duffin's Blog platform, built with Kotlin and Jetpack Compose.

## Features

- **Material 3 Design**: Modern Material Design 3 UI with custom color scheme
- **Authentication**: JWT-based login and registration system
- **Blog Management**: Create, read, search, and filter blog posts
- **Tag System**: Interactive tag filtering and navigation
- **User Profiles**: View user statistics and manage account
- **Image Support**: Hero image support for blog posts with Coil image loading
- **Offline Ready**: DataStore preferences for secure token storage
- **Modern Architecture**: MVVM with Repository pattern, Coroutines, and StateFlow

## Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM + Repository Pattern
- **Networking**: Retrofit + OkHttp + Gson
- **Navigation**: Navigation Compose
- **Image Loading**: Coil
- **Local Storage**: DataStore Preferences
- **Async**: Kotlin Coroutines + StateFlow
- **Material Design**: Material 3 Components

## API Integration

The app connects to the Flask backend at:
- **Production**: `https://duffin-blogs.yeems214.xyz/api/`

The app uses secure HTTPS communication for all API requests.

## Building the App

### Prerequisites
- Android Studio or VS Code with Android development setup
- JDK 17 or higher
- Android SDK API 34+

### Build Commands

```bash
# Clean project
./gradlew clean

# Build debug APK
./gradlew assembleDebug

# Install on connected device/emulator
./gradlew installDebug

# Run all tests
./gradlew test
```

### VS Code Tasks

Use the pre-configured VS Code task:
- `Ctrl+Shift+P` → "Tasks: Run Task" → "Build Android App"

## Project Structure

```
app/src/main/java/xyz/yeems214/DuffinsBlog/
├── MainActivity.kt                    # Main entry point
├── data/
│   ├── api/                          # Network layer
│   │   ├── ApiClient.kt              # Retrofit configuration
│   │   └── BlogApiService.kt         # API endpoints
│   ├── model/                        # Data models
│   │   ├── User.kt                   # User-related models
│   │   └── BlogPost.kt               # Blog-related models
│   ├── preferences/                   # Local storage
│   │   └── UserPreferencesManager.kt # DataStore preferences
│   └── repository/                    # Data repositories
│       ├── AuthRepository.kt         # Authentication data
│       └── BlogRepository.kt         # Blog data
├── ui/
│   ├── theme/                        # Material 3 theming
│   │   ├── Color.kt                  # Custom color scheme
│   │   ├── Theme.kt                  # Theme configuration
│   │   └── Type.kt                   # Typography
│   ├── navigation/                   # Navigation setup
│   │   └── Screen.kt                 # Screen definitions
│   ├── viewmodel/                    # ViewModels
│   │   ├── AuthViewModel.kt          # Authentication logic
│   │   └── BlogViewModel.kt          # Blog operations
│   └── screen/                       # UI screens
│       ├── auth/                     # Authentication screens
│       ├── blog/                     # Blog-related screens
│       └── profile/                  # User profile screens
```

## API Endpoints

The app integrates with the following Flask backend endpoints:

- `POST /api/auth/register` - User registration
- `POST /api/auth/login` - User login
- `GET /api/auth/user` - Get current user info
- `GET /api/posts` - Get all blog posts
- `GET /api/posts/{id}` - Get specific blog post
- `POST /api/posts` - Create new blog post
- `GET /api/posts/search` - Search blog posts
- `GET /api/posts/tag/{tag}` - Filter posts by tag
- `GET /api/posts/user/{userId}` - Get user's posts

## App Flow

1. **Launch**: Check authentication status
2. **Authentication**: Login/Register screens with validation
3. **Blog List**: View, search, and filter blog posts
4. **Post Detail**: Read full posts with hero images
5. **Create Post**: Rich form for creating new blog posts
6. **Profile**: View user stats and manage account

## Development Notes

- The app uses Material 3 dynamic color theming
- All network calls are handled with proper error handling
- Images are loaded with Coil for efficient caching
- Authentication tokens are securely stored with DataStore
- The app follows modern Android development best practices

## Known Issues

- Some Material Icons show deprecation warnings (non-blocking)
- Requires backend server running on `http://127.0.0.1:5003`
- Image uploads not yet implemented (form ready)

## Testing

1. Start the Flask backend server
2. Build and install the Android app
3. Register a new account or login
4. Test blog reading, creation, and user profile features

The app is ready for testing and further development!
