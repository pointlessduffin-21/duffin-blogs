package xyz.yeems214.DuffinsBlog.ui.screen.blog

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import xyz.yeems214.DuffinsBlog.ui.viewmodel.BlogViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    blogViewModel: BlogViewModel,
    onBackClick: () -> Unit,
    onPostCreated: () -> Unit
) {
    val uiState by blogViewModel.uiState.collectAsStateWithLifecycle()
    
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var heroImageUrl by remember { mutableStateOf("") }
    var tagInput by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf(mutableListOf<String>()) }
    var showImageUrlField by remember { mutableStateOf(false) }
    
    LaunchedEffect(uiState.isCreating) {
        if (!uiState.isCreating && uiState.error == null && title.isNotEmpty()) {
            // Post was successfully created
            onPostCreated()
        }
    }
    
    Column(modifier = Modifier.fillMaxSize()) {
        // Top App Bar
        TopAppBar(
            title = { Text("Create Post") },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                TextButton(
                    onClick = {
                        blogViewModel.clearError()
                        blogViewModel.createPost(
                            title = title.trim(),
                            content = content.trim(),
                            tags = tags.toList(),
                            heroImage = heroImageUrl.takeIf { it.isNotBlank() }
                        )
                    },
                    enabled = !uiState.isCreating && title.isNotBlank() && content.isNotBlank()
                ) {
                    if (uiState.isCreating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Publish")
                    }
                }
            }
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title Field
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                placeholder = { Text("Enter your blog post title...") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isCreating,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                ),
                singleLine = true
            )
            
            // Hero Image Section
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Hero Image (Optional)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )
                        IconButton(
                            onClick = { showImageUrlField = !showImageUrlField }
                        ) {
                            Icon(
                                if (showImageUrlField) Icons.Default.ExpandLess 
                                else Icons.Default.ExpandMore,
                                contentDescription = if (showImageUrlField) "Hide" else "Show"
                            )
                        }
                    }
                    
                    if (showImageUrlField) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = heroImageUrl,
                            onValueChange = { heroImageUrl = it },
                            label = { Text("Image URL") },
                            placeholder = { Text("https://example.com/image.jpg") },
                            leadingIcon = {
                                Icon(Icons.Default.Image, contentDescription = null)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !uiState.isCreating,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            singleLine = true
                        )
                    }
                }
            }
            
            // Tags Section
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Tags",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Tag Input
                    OutlinedTextField(
                        value = tagInput,
                        onValueChange = { tagInput = it },
                        label = { Text("Add tag") },
                        placeholder = { Text("Type a tag and press Enter") },
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    val newTag = tagInput.trim()
                                    if (newTag.isNotEmpty() && !tags.contains(newTag)) {
                                        tags.add(newTag)
                                        tagInput = ""
                                    }
                                },
                                enabled = tagInput.trim().isNotEmpty() && !tags.contains(tagInput.trim())
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add tag")
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isCreating,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        singleLine = true
                    )
                    
                    // Display Tags
                    if (tags.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(tags) { tag ->
                                InputChip(
                                    onClick = { },
                                    label = { Text(tag) },
                                    selected = false,
                                    trailingIcon = {
                                        IconButton(
                                            onClick = { tags.remove(tag) },
                                            modifier = Modifier.size(18.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Close,
                                                contentDescription = "Remove tag",
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
            
            // Content Field
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Content") },
                placeholder = { Text("Write your blog post content here...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                enabled = !uiState.isCreating,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences
                ),
                minLines = 10
            )
            
            // Character Count
            Text(
                text = "${content.length} characters",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
    
    // Error Message
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            // You could show a snackbar here
        }
    }
}
