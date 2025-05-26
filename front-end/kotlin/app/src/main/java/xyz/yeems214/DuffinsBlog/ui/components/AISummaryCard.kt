package xyz.yeems214.DuffinsBlog.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import xyz.yeems214.DuffinsBlog.data.model.BlogPost

@Composable
fun AISummaryCard(
    post: BlogPost,
    onGenerateSummary: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    var isGenerating by remember { mutableStateOf(false) }
    var hasError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    
    val hasExistingSummary = post.displaySummary?.isNotBlank() == true
    val shouldShowCard = hasExistingSummary || onGenerateSummary != null
    
    if (shouldShowCard) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f)
            ),
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 2.dp
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Clickable header that expands/collapses the AI summary
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()                        .clickable { 
                            if (hasExistingSummary) {
                                isExpanded = !isExpanded
                            } else if (onGenerateSummary != null && !isGenerating) {
                                isGenerating = true
                                hasError = false
                                errorMessage = ""
                                post.slug?.let { postSlug ->
                                    onGenerateSummary(postSlug)
                                }
                            }
                        }
                        .padding(vertical = 8.dp)
                ) {
                    Icon(
                        Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = if (hasExistingSummary) {
                            "Click to expand AI insights"
                        } else {
                            "Generate AI Summary"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    if (isGenerating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else if (hasExistingSummary) {
                        Icon(
                            if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (isExpanded) "Collapse" else "Expand",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )                    } else {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = "Generate",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                // Loading state
                if (isGenerating) {
                    AnimatedVisibility(
                        visible = true,
                        enter = expandVertically(animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)),
                        exit = shrinkVertically(animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp)
                        ) {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f),
                                thickness = 1.dp,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 8.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "AI is analyzing your content...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
                
                // Error state
                if (hasError) {
                    AnimatedVisibility(
                        visible = true,
                        enter = expandVertically(animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)),
                        exit = shrinkVertically(animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp)
                        ) {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f),
                                thickness = 1.dp,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Error,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Failed to generate AI summary",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                    if (errorMessage.isNotBlank()) {
                                        Text(
                                            text = errorMessage,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.weight(1f))
                                TextButton(
                                    onClick = {
                                        isGenerating = true
                                        hasError = false
                                        errorMessage = ""
                                        post.slug?.let { postSlug ->
                                            onGenerateSummary?.invoke(postSlug)
                                        }
                                    }
                                ) {
                                    Text("Try Again")
                                }
                            }
                        }
                    }
                }
                
                // Content area that shows/hides based on expanded state
                if (hasExistingSummary) {
                    AnimatedVisibility(
                        visible = isExpanded,
                        enter = expandVertically(animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)),
                        exit = shrinkVertically(animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp)
                        ) {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f),
                                thickness = 1.dp,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "AI Summary",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    Icons.Default.Psychology,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = post.displaySummary ?: "",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.2
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // AI disclaimer
                            Text(
                                text = "This summary was generated by AI and may not capture all nuances of the original content.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )
                        }
                    }                }
            }
        }
    }
    
    // Handle external state updates for generation completion
    LaunchedEffect(post.displaySummary, post.slug) {
        if (isGenerating && post.displaySummary?.isNotBlank() == true) {
            isGenerating = false
            hasError = false
            errorMessage = ""
            isExpanded = true // Auto-expand when summary is generated
        }
    }
    
    // Handle generation failures by watching for timeout or other issues
    LaunchedEffect(isGenerating) {
        if (isGenerating) {
            kotlinx.coroutines.delay(30000) // 30 second timeout
            if (isGenerating) {
                isGenerating = false
                hasError = true
                errorMessage = "Request timed out. Please try again."
            }
        }
    }
}
