package com.humans.aura.core.presentation.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.TextDecoration
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer

private val markdownParser: Parser = Parser.builder().build()
private val markdownRenderer: HtmlRenderer = HtmlRenderer.builder()
    .escapeHtml(true)
    .sanitizeUrls(true)
    .build()

@Composable
fun MarkdownMessageText(
    markdown: String,
    modifier: Modifier = Modifier,
    style: TextStyle = TextStyle.Default,
    color: Color = Color.Unspecified,
) {
    val annotated = remember(markdown) {
        markdownToAnnotatedString(markdown, color)
    }

    Text(
        text = annotated,
        modifier = modifier,
        style = style,
        color = color,
    )
}

internal fun markdownToAnnotatedString(markdown: String, color: Color = Color.Unspecified): AnnotatedString {
    return runCatching {
        val html = markdownRenderer.render(markdownParser.parse(markdown))
        AnnotatedString.fromHtml(
            html,
            linkStyles = TextLinkStyles(
                style = SpanStyle(
                    color = if (color == Color.Unspecified) Color.Unspecified else color,
                    textDecoration = TextDecoration.Underline,
                ),
            ),
        )
    }.getOrElse {
        AnnotatedString(markdown)
    }
}
