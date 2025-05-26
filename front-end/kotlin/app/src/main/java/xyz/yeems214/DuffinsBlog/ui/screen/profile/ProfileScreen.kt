package xyz.yeems214.DuffinsBlog.ui.screen.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.firstOrNull
import xyz.yeems214.DuffinsBlog.data.preferences.UserPreferencesManager
import xyz.yeems214.DuffinsBlog.ui.viewmodel.AuthViewModel
import xyz.yeems214.DuffinsBlog.ui.viewmodel.BlogViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
    blogViewModel: BlogViewModel,
    userPreferencesManager: UserPreferencesManager,
    onBackClick: () -> Unit,
    onLogoutSuccess: () -> Unit
) {
    val authUiState by authViewModel.uiState.collectAsStateWithLifecycle()
    val blogUiState by blogViewModel.uiState.collectAsStateWithLifecycle()
    
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var showLogoutDialog by remember { mutableStateOf(false) }
    
    // Load user data
    LaunchedEffect(Unit) {
        username = userPreferencesManager.username.firstOrNull() ?: "Unknown"
        email = userPreferencesManager.email.firstOrNull() ?: "Unknown"
    }
    
    // Watch for logout completion
    val isLoggedIn by authViewModel.isLoggedIn.collectAsStateWithLifecycle()
    LaunchedEffect(isLoggedIn) {
        if (!isLoggedIn) {
            onLogoutSuccess()
        }
    }
    
    Column(modifier = Modifier.fillMaxSize()) {
        // Top App Bar
        TopAppBar(
            title = { Text("Profile") },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            }
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // User Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Profile Icon
                    Icon(
                        Icons.Default.AccountCircle,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Username
                    Text(
                        text = username,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    
                    // Email
                    Text(
                        text = email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            // Statistics Card
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = "Statistics",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Total Posts
                    val userPosts = blogUiState.posts.filter { 
                        it.authorUsername?.lowercase() == username.lowercase() 
                    }
                    
                    StatisticItem(
                        icon = Icons.Default.Article,
                        label = "Posts Created",
                        value = userPosts.size.toString()
                    )
                    
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    StatisticItem(
                        icon = Icons.Default.DateRange,
                        label = "Member Since",
                        value = "Recently" // This could be enhanced with actual join date
                    )
                }
            }
            
            // App Info Card
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = "App Information",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    StatisticItem(
                        icon = Icons.Default.Info,
                        label = "Version",
                        value = "1.0.0"
                    )
                    
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    StatisticItem(
                        icon = Icons.Default.Computer,
                        label = "Backend Server",
                        value = "http://localhost:5003"
                    )
                }
            }
            
            // Logout Button
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Button(
                    onClick = { showLogoutDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Icon(
                        Icons.Default.Logout,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sign Out")
                }
            }
        }
    }
    
    // Logout Confirmation Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Sign Out") },
            text = { Text("Are you sure you want to sign out?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        authViewModel.logout()
                    }
                ) {
                    Text("Sign Out")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showLogoutDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun StatisticItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
