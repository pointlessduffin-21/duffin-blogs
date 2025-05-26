package xyz.yeems214.DuffinsBlog.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

data class ArticleElement(
    val type: ElementType,
    val content: String,
    val level: Int = 0
)

enum class ElementType {
    TEXT, HEADING, IMAGE, CODE_BLOCK
}

@Composable
fun ArticleRenderer(
    content: String,
    modifier: Modifier = Modifier
) {
    val elements = remember(content) { parseArticleContent(content) }
    
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(elements) { element ->
            when (element.type) {
                ElementType.TEXT -> {
                    Text(
                        text = element.content,
                        style = MaterialTheme.typography.bodyLarge,
                        lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.4,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                ElementType.HEADING -> {
                    Text(
                        text = element.content,
                        style = when (element.level) {
                            1 -> MaterialTheme.typography.headlineLarge
                            2 -> MaterialTheme.typography.headlineMedium
                            3 -> MaterialTheme.typography.headlineSmall
                            4 -> MaterialTheme.typography.titleLarge
                            5 -> MaterialTheme.typography.titleMedium
                            else -> MaterialTheme.typography.titleSmall
                        },
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                ElementType.IMAGE -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        AsyncImage(
                            model = element.content,
                            contentDescription = "Article image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 200.dp, max = 400.dp),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
                ElementType.CODE_BLOCK -> {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Text(
                            text = element.content,
                            style = MaterialTheme.typography.bodyMedium,
                            fontStyle = FontStyle.Italic,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun parseArticleContent(content: String): List<ArticleElement> {
    val elements = mutableListOf<ArticleElement>()
    val lines = content.split("\n")
    
    var i = 0
    while (i < lines.size) {
        val line = lines[i].trim()
        
        when {
            // Markdown headings
            line.startsWith("#") -> {
                val level = line.takeWhile { it == '#' }.length
                val text = line.dropWhile { it == '#' }.trim()
                elements.add(ArticleElement(ElementType.HEADING, text, level))
            }
            
            // Markdown images
            line.contains("![") && line.contains("](") -> {
                val imageRegex = """!\[.*?\]\((.*?)\)""".toRegex()
                val matches = imageRegex.findAll(line)
                for (match in matches) {
                    val imageUrl = match.groupValues[1]
                    if (imageUrl.isNotBlank()) {
                        elements.add(ArticleElement(ElementType.IMAGE, imageUrl))
                    }
                }
                // Add remaining text
                val textWithoutImages = line.replace(imageRegex, "").trim()
                if (textWithoutImages.isNotBlank()) {
                    elements.add(ArticleElement(ElementType.TEXT, textWithoutImages))
                }
            }
            
            // HTML images
            line.contains("<img") -> {
                val imgRegex = """<img[^>]+src=["']([^"']+)["'][^>]*>""".toRegex()
                val matches = imgRegex.findAll(line)
                for (match in matches) {
                    val imageUrl = match.groupValues[1]
                    if (imageUrl.isNotBlank()) {
                        elements.add(ArticleElement(ElementType.IMAGE, imageUrl))
                    }
                }
                // Add remaining text
                val textWithoutImages = line.replace(imgRegex, "").trim()
                if (textWithoutImages.isNotBlank()) {
                    elements.add(ArticleElement(ElementType.TEXT, textWithoutImages))
                }
            }
            
            // Code blocks
            line.startsWith("```") -> {
                val codeLines = mutableListOf<String>()
                i++ // Skip opening ```
                while (i < lines.size && !lines[i].trim().startsWith("```")) {
                    codeLines.add(lines[i])
                    i++
                }
                if (codeLines.isNotEmpty()) {
                    elements.add(ArticleElement(ElementType.CODE_BLOCK, codeLines.joinToString("\n")))
                }
            }
            
            // Regular text
            line.isNotBlank() -> {
                // Combine consecutive text lines into paragraphs
                val paragraph = mutableListOf(line)
                var j = i + 1
                while (j < lines.size && 
                       lines[j].trim().isNotBlank() && 
                       !lines[j].trim().startsWith("#") &&
                       !lines[j].trim().startsWith("```") &&
                       !lines[j].contains("![") &&
                       !lines[j].contains("<img")) {
                    paragraph.add(lines[j].trim())
                    j++
                }
                elements.add(ArticleElement(ElementType.TEXT, paragraph.joinToString(" ")))
                i = j - 1
            }
        }
        i++
    }
    
    return elements
}
