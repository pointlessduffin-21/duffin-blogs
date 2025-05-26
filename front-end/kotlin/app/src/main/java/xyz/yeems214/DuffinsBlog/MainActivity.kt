package xyz.yeems214.DuffinsBlog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import xyz.yeems214.DuffinsBlog.data.api.ApiClient
import xyz.yeems214.DuffinsBlog.data.preferences.UserPreferencesManager
import xyz.yeems214.DuffinsBlog.data.repository.AuthRepository
import xyz.yeems214.DuffinsBlog.data.repository.BlogRepository
import xyz.yeems214.DuffinsBlog.ui.navigation.NavigationArgs
import xyz.yeems214.DuffinsBlog.ui.navigation.Screen
import xyz.yeems214.DuffinsBlog.ui.screen.auth.LoginScreen
import xyz.yeems214.DuffinsBlog.ui.screen.auth.RegisterScreen
import xyz.yeems214.DuffinsBlog.ui.screen.blog.BlogDetailScreen
import xyz.yeems214.DuffinsBlog.ui.screen.blog.BlogListScreen
import xyz.yeems214.DuffinsBlog.ui.screen.blog.CreatePostScreen
import xyz.yeems214.DuffinsBlog.ui.screen.profile.ProfileScreen
import xyz.yeems214.DuffinsBlog.ui.theme.DuffinsBlogTheme
import xyz.yeems214.DuffinsBlog.ui.viewmodel.AuthViewModel
import xyz.yeems214.DuffinsBlog.ui.viewmodel.BlogViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            DuffinsBlogTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DuffinsBlogApp()
                }
            }
        }
    }
}

@Composable
fun DuffinsBlogApp() {
    val navController = rememberNavController()
    
    // Initialize repositories and dependencies
    val userPreferencesManager = UserPreferencesManager(navController.context)
    val authRepository = AuthRepository(ApiClient.blogApiService, userPreferencesManager)
    val blogRepository = BlogRepository(ApiClient.blogApiService)
    
    // Initialize ViewModels
    val authViewModel: AuthViewModel = viewModel {
        AuthViewModel(authRepository)
    }
    val blogViewModel: BlogViewModel = viewModel {
        BlogViewModel(blogRepository, authRepository)
    }
    
    // Observe login state
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
    
    NavHost(
        navController = navController,
        startDestination = if (isLoggedIn) Screen.BlogList.route else Screen.Login.route
    ) {
        // Authentication Screens
        composable(Screen.Login.route) {
            LoginScreen(
                authViewModel = authViewModel,
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onLoginSuccess = {
                    navController.navigate(Screen.BlogList.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Register.route) {
            RegisterScreen(
                authViewModel = authViewModel,
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                onRegisterSuccess = {
                    navController.navigate(Screen.BlogList.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                }
            )
        }
        
        // Main App Screens
        composable(Screen.BlogList.route) {
            BlogListScreen(
                blogViewModel = blogViewModel,
                onPostClick = { post ->
                    blogViewModel.selectPost(post)
                    navController.navigate(Screen.BlogDetail.createRoute(post.id ?: ""))
                },
                onCreatePostClick = {
                    navController.navigate(Screen.CreatePost.route)
                },
                onProfileClick = {
                    navController.navigate(Screen.Profile.route)
                }
            )
        }
        
        composable(Screen.BlogDetail.route) { backStackEntry ->
            val postId = backStackEntry.arguments?.getString(NavigationArgs.POST_ID) ?: ""
            BlogDetailScreen(
                postId = postId,
                blogViewModel = blogViewModel,
                onBackClick = {
                    navController.popBackStack()
                },
                onTagClick = { tag ->
                    blogViewModel.filterByTag(tag)
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.CreatePost.route) {
            CreatePostScreen(
                blogViewModel = blogViewModel,
                onBackClick = {
                    navController.popBackStack()
                },
                onPostCreated = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.Profile.route) {
            ProfileScreen(
                authViewModel = authViewModel,
                blogViewModel = blogViewModel,
                userPreferencesManager = userPreferencesManager,
                onBackClick = {
                    navController.popBackStack()
                },
                onLogoutSuccess = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}