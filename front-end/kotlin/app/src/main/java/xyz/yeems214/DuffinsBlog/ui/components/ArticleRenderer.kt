package xyz.yeems214.DuffinsBlog.ui.components

import android.graphics.Typeface
import android.text.util.Linkify
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import io.noties.markwon.Markwon
import io.noties.markwon.html.HtmlPlugin
import io.noties.markwon.image.coil.CoilImagesPlugin
import io.noties.markwon.linkify.LinkifyPlugin

@Composable
fun ArticleRenderer(
    content: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val textColor = MaterialTheme.colorScheme.onSurface.toArgb()
    
    val markwon = remember(context) {
        Markwon.builder(context)
            .usePlugin(HtmlPlugin.create())
            .usePlugin(CoilImagesPlugin.create(context))
            .usePlugin(LinkifyPlugin.create())
            .build()
    }
    
    AndroidView(
        factory = { ctx ->
            android.widget.TextView(ctx).apply {
                setTextColor(textColor)
                textSize = 16f
                typeface = Typeface.DEFAULT
                setPadding(16, 16, 16, 16)
                autoLinkMask = Linkify.WEB_URLS
                setLineSpacing(1.2f, 1.0f)
            }
        },
        update = { textView ->
            textView.setTextColor(textColor)
            markwon.setMarkdown(textView, content)
        },
        modifier = modifier.fillMaxWidth()
    )
}
