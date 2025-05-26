package xyz.yeems214.DuffinsBlog.ui.screen.blog

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import xyz.yeems214.DuffinsBlog.data.model.BlogPost
import xyz.yeems214.DuffinsBlog.ui.viewmodel.BlogViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlogDetailScreen(
    postId: String,
    blogViewModel: BlogViewModel,
    onBackClick: () -> Unit,
    onTagClick: (String) -> Unit
) {
    val selectedPost by blogViewModel.selectedPost.collectAsStateWithLifecycle()
    val context = LocalContext.current
    
    // Find the post from the current posts list if selectedPost is null
    val uiState by blogViewModel.uiState.collectAsStateWithLifecycle()
    val post = selectedPost ?: uiState.posts.find { it.id == postId }
    
    LaunchedEffect(postId) {
        if (selectedPost?.id != postId) {
            // Try to find the post in the current list first
            val foundPost = uiState.posts.find { it.id == postId }
            if (foundPost != null) {
                blogViewModel.selectPost(foundPost)
            }
        }
    }
    
    Column(modifier = Modifier.fillMaxSize()) {
        // Top App Bar
        TopAppBar(
            title = { Text("Blog Post") },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                post?.let {
                    IconButton(
                        onClick = {
                            // Share functionality could be implemented here
                            // For now, we'll just show a toast-like message
                        }
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }
                }
            }
        )
        
        if (post == null) {
            // Loading or error state
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Loading post...")
                }
            }
        } else {
            // Post content
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    // Hero Image
                    post.heroImage?.let { imageUrl ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                        ) {
                            AsyncImage(
                                model = imageUrl,
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(250.dp),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
                
                item {
                    // Title
                    Text(
                        text = post.title,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                item {
                    // Author and Date Info
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Author
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Author",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = post.authorUsername ?: "Unknown",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                            
                            // Date
                            post.createdAt?.let { dateString ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.DateRange,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "Published",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        val formattedDate = try {
                                            val date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).parse(dateString)
                                            SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(date ?: Date())
                                        } catch (e: Exception) {
                                            dateString
                                        }
                                        Text(
                                            text = formattedDate,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                
                item {
                    // Tags
                    if (post.tags.isNotEmpty()) {
                        Column {
                            Text(
                                text = "Tags",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(post.tags) { tag ->
                                    AssistChip(
                                        onClick = { onTagClick(tag) },
                                        label = { Text(tag) },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Default.Tag,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
                
                item {
                    // Summary (if available)
                    post.summary?.takeIf { it.isNotBlank() }?.let { summary ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "AI Summary",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = summary,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
                
                item {
                    // Content
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Content",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = post.content,
                                style = MaterialTheme.typography.bodyLarge,
                                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.4,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
                
                // Bottom padding
                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}
