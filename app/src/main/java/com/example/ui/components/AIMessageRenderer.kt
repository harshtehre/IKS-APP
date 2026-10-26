package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

/**
 * Reusable AI Response and Markdown Renderer.
 * Parses Markdown blocks (headings, code blocks, lists, blockquotes, horizontal rules)
 * and inline styles (bold, italic, code, links), completely eliminating raw Markdown asterisks
 * and providing interactive action buttons below AI responses.
 */
@Composable
fun AIMessageRenderer(
    content: String,
    modifier: Modifier = Modifier,
    isAiResponse: Boolean = true,
    baseTextStyle: TextStyle = MaterialTheme.typography.bodyMedium.copy(
        color = TextPrimaryDark,
        lineHeight = 22.sp,
        fontSize = 14.sp
    ),
    onActionClick: ((String) -> Unit)? = null
) {
    val sanitizedContent = remember(content) { sanitizeRawContent(content) }
    val blocks = remember(sanitizedContent) { parseMarkdownBlocks(sanitizedContent) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        blocks.forEach { block ->
            when (block) {
                is MarkdownBlock.Heading -> {
                    HeadingItem(block)
                }
                is MarkdownBlock.Paragraph -> {
                    val annotated = remember(block.text) {
                        parseInlineMarkdown(block.text, baseTextStyle)
                    }
                    Text(
                        text = annotated,
                        style = baseTextStyle,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                is MarkdownBlock.BulletItem -> {
                    BulletListItem(block, baseTextStyle)
                }
                is MarkdownBlock.NumberedItem -> {
                    NumberedListItem(block, baseTextStyle)
                }
                is MarkdownBlock.BlockQuote -> {
                    BlockQuoteItem(block, baseTextStyle)
                }
                is MarkdownBlock.CodeBlock -> {
                    CodeBlockItem(block)
                }
                is MarkdownBlock.Divider -> {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        thickness = 1.dp,
                        color = SurfaceCardBorder.copy(alpha = 0.7f)
                    )
                }
            }
        }

        // Subtly rendered action buttons below AI responses
        if (isAiResponse && onActionClick != null) {
            Spacer(modifier = Modifier.height(6.dp))
            AIActionButtonsBar(onActionClick = onActionClick)
        }
    }
}

/**
 * Lightweight inline markdown renderer for cards, lesson readers, and quiz explanations.
 */
@Composable
fun IksMarkdownText(
    markdown: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    color: Color = TextPrimaryDark
) {
    AIMessageRenderer(
        content = markdown,
        modifier = modifier,
        isAiResponse = false,
        baseTextStyle = style.copy(color = color, lineHeight = (style.fontSize.value * 1.5).sp),
        onActionClick = null
    )
}

@Composable
private fun HeadingItem(block: MarkdownBlock.Heading) {
    val style = when (block.level) {
        1 -> MaterialTheme.typography.titleLarge.copy(
            color = RadiantGold,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 20.sp,
            lineHeight = 26.sp
        )
        2 -> MaterialTheme.typography.titleMedium.copy(
            color = SaffronLight,
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp,
            lineHeight = 23.sp
        )
        3 -> MaterialTheme.typography.titleSmall.copy(
            color = RadiantGold,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp,
            lineHeight = 21.sp
        )
        else -> MaterialTheme.typography.bodyMedium.copy(
            color = TextPrimaryDark,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            lineHeight = 20.sp
        )
    }

    val annotated = remember(block.text) {
        parseInlineMarkdown(block.text, style)
    }

    Text(
        text = annotated,
        style = style,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = if (block.level <= 2) 4.dp else 2.dp)
    )
}

@Composable
private fun BulletListItem(block: MarkdownBlock.BulletItem, baseStyle: TextStyle) {
    val annotated = remember(block.text) {
        parseInlineMarkdown(block.text, baseStyle)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 6.dp, top = 2.dp, bottom = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "✦",
            color = SaffronPrimary,
            fontSize = 11.sp,
            modifier = Modifier.padding(top = 2.dp)
        )
        Text(
            text = annotated,
            style = baseStyle,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun NumberedListItem(block: MarkdownBlock.NumberedItem, baseStyle: TextStyle) {
    val annotated = remember(block.text) {
        parseInlineMarkdown(block.text, baseStyle)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, top = 2.dp, bottom = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            color = SurfaceNavy,
            shape = RoundedCornerShape(4.dp),
            border = BorderStroke(1.dp, SaffronPrimary.copy(alpha = 0.4f)),
            modifier = Modifier.padding(top = 1.dp)
        ) {
            Text(
                text = "${block.number}.",
                color = SaffronLight,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
            )
        }
        Text(
            text = annotated,
            style = baseStyle,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun BlockQuoteItem(block: MarkdownBlock.BlockQuote, baseStyle: TextStyle) {
    val annotated = remember(block.text) {
        parseInlineMarkdown(block.text, baseStyle.copy(fontStyle = FontStyle.Italic, color = SaffronLight))
    }

    Surface(
        color = SurfaceNavy.copy(alpha = 0.7f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, SaffronPrimary.copy(alpha = 0.4f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight()
                    .background(SaffronPrimary, RoundedCornerShape(2.dp))
            )
            Text(
                text = annotated,
                style = baseStyle.copy(
                    fontStyle = FontStyle.Italic,
                    color = SaffronLight
                ),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun CodeBlockItem(block: MarkdownBlock.CodeBlock) {
    val clipboardManager = LocalClipboardManager.current

    Surface(
        color = DeepestVoid,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, SurfaceCardBorder),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceNavy)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = block.language.ifBlank { "Text / Sanskrit Code" }.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextSecondaryDark,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                )

                IconButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(block.code))
                    },
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ContentCopy,
                        contentDescription = "Copy code",
                        tint = TextSecondaryDark,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }

            Text(
                text = block.code,
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    color = TextPrimaryDark,
                    lineHeight = 18.sp
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(12.dp)
            )
        }
    }
}

@Composable
private fun AIActionButtonsBar(onActionClick: (String) -> Unit) {
    val actions = listOf(
        "Explain Again" to "Can you explain this again in a different way?",
        "Summarize" to "Summarize this in 3 concise bullet points.",
        "Quiz Me" to "Quiz me with a multiple-choice question on this topic.",
        "Make Flashcards" to "Generate 3 key flashcards from this answer.",
        "Ask Follow-up" to "What are the modern scientific parallels to this concept?"
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Suggested actions:",
            style = MaterialTheme.typography.labelSmall.copy(
                color = TextSecondaryDark,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold
            ),
            modifier = Modifier.padding(bottom = 6.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            actions.forEach { (label, prompt) ->
                Surface(
                    shape = RoundedCornerShape(100.dp),
                    color = SurfaceNavy,
                    border = BorderStroke(1.dp, SaffronPrimary.copy(alpha = 0.45f)),
                    modifier = Modifier
                        .clip(RoundedCornerShape(100.dp))
                        .clickable { onActionClick(prompt) }
                        .testTag("ai_action_${label.lowercase().replace(" ", "_")}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = when (label) {
                                "Explain Again" -> "🔄"
                                "Summarize" -> "📝"
                                "Quiz Me" -> "❓"
                                "Make Flashcards" -> "🗂️"
                                else -> "💬"
                            },
                            fontSize = 10.sp
                        )
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = SaffronLight,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Markdown AST and Parsing Engine
// -------------------------------------------------------------

sealed interface MarkdownBlock {
    data class Heading(val level: Int, val text: String) : MarkdownBlock
    data class Paragraph(val text: String) : MarkdownBlock
    data class BulletItem(val text: String) : MarkdownBlock
    data class NumberedItem(val number: Int, val text: String) : MarkdownBlock
    data class BlockQuote(val text: String) : MarkdownBlock
    data class CodeBlock(val language: String, val code: String) : MarkdownBlock
    object Divider : MarkdownBlock
}

internal fun sanitizeRawContent(content: String): String {
    return content
        .replace("\r\n", "\n")
        .replace("\r", "\n")
        // Remove accidental raw html tags
        .replace("<br>", "\n")
        .replace("<br/>", "\n")
        .replace("<p>", "")
        .replace("</p>", "\n")
        // Normalize LaTeX delimiters for simple rendering: $formula$ -> formula
        .replace(Regex("""\$(.*?)\$""")) { matchResult ->
            matchResult.groupValues[1]
                .replace("\\text{", "")
                .replace("}", "")
                .replace("\\delta", "δ")
                .replace("\\pi", "π")
                .replace("\\times", "×")
        }
}

internal fun parseMarkdownBlocks(rawText: String): List<MarkdownBlock> {
    val blocks = mutableListOf<MarkdownBlock>()
    val lines = rawText.lines()
    var i = 0

    while (i < lines.size) {
        val line = lines[i]
        val trimmed = line.trim()

        if (trimmed.isEmpty()) {
            i++
            continue
        }

        // Code block start
        if (trimmed.startsWith("```")) {
            val language = trimmed.removePrefix("```").trim()
            val codeLines = mutableListOf<String>()
            i++
            while (i < lines.size && !lines[i].trim().startsWith("```")) {
                codeLines.add(lines[i])
                i++
            }
            if (i < lines.size && lines[i].trim().startsWith("```")) {
                i++
            }
            blocks.add(MarkdownBlock.CodeBlock(language, codeLines.joinToString("\n")))
            continue
        }

        // Horizontal divider
        if (trimmed == "---" || trimmed == "***" || trimmed == "___") {
            blocks.add(MarkdownBlock.Divider)
            i++
            continue
        }

        // Headings (#, ##, ###, ####)
        if (trimmed.startsWith("#")) {
            var level = 0
            while (level < trimmed.length && trimmed[level] == '#') {
                level++
            }
            if (level in 1..6 && trimmed.length > level && trimmed[level] == ' ') {
                val headingText = trimmed.substring(level + 1).trim()
                blocks.add(MarkdownBlock.Heading(level, headingText))
                i++
                continue
            }
        }

        // Blockquote (> )
        if (trimmed.startsWith(">")) {
            val quoteLines = mutableListOf<String>()
            quoteLines.add(trimmed.removePrefix(">").trim())
            i++
            while (i < lines.size && lines[i].trim().startsWith(">")) {
                quoteLines.add(lines[i].trim().removePrefix(">").trim())
                i++
            }
            blocks.add(MarkdownBlock.BlockQuote(quoteLines.joinToString(" ")))
            continue
        }

        // Numbered list item (e.g. 1. text)
        val numMatch = Regex("""^(\d+)\.\s+(.*)""").find(trimmed)
        if (numMatch != null) {
            val num = numMatch.groupValues[1].toIntOrNull() ?: 1
            val itemText = numMatch.groupValues[2]
            blocks.add(MarkdownBlock.NumberedItem(num, itemText))
            i++
            continue
        }

        // Bullet list item (* text, - text, • text)
        if (trimmed.startsWith("* ") || trimmed.startsWith("- ") || trimmed.startsWith("• ")) {
            val bulletText = trimmed.substring(2).trim()
            blocks.add(MarkdownBlock.BulletItem(bulletText))
            i++
            continue
        }

        // Default: Regular Paragraph (accumulate continuous text lines)
        val paraLines = mutableListOf<String>()
        paraLines.add(trimmed)
        i++
        while (i < lines.size) {
            val nextLine = lines[i].trim()
            if (nextLine.isEmpty() ||
                nextLine.startsWith("#") ||
                nextLine.startsWith("```") ||
                nextLine.startsWith(">") ||
                nextLine.startsWith("* ") ||
                nextLine.startsWith("- ") ||
                nextLine.startsWith("• ") ||
                Regex("""^(\d+)\.\s+""").containsMatchIn(nextLine) ||
                nextLine == "---"
            ) {
                break
            }
            paraLines.add(nextLine)
            i++
        }
        blocks.add(MarkdownBlock.Paragraph(paraLines.joinToString(" ")))
    }

    return blocks
}

/**
 * Parses inline Markdown tags:
 * - ***bold and italic***
 * - **bold** or __bold__
 * - *italic* or _italic_
 * - `code`
 * - [label](url)
 * Completely eliminates raw asterisks from markdown formatting while safely preserving legitimate math notation (e.g. 5 * 4 = 20)!
 */
fun parseInlineMarkdown(text: String, baseStyle: TextStyle): AnnotatedString {
    return buildAnnotatedString {
        var cursor = 0
        val length = text.length

        while (cursor < length) {
            // 1. Check for bold+italic (***text***)
            if (cursor + 2 < length && text[cursor] == '*' && text[cursor + 1] == '*' && text[cursor + 2] == '*') {
                val endIdx = text.indexOf("***", cursor + 3)
                if (endIdx != -1) {
                    val content = text.substring(cursor + 3, endIdx)
                    pushStyle(SpanStyle(fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic, color = RadiantGold))
                    append(content)
                    pop()
                    cursor = endIdx + 3
                    continue
                }
            }

            // 2. Check for bold (**text** or __text__)
            if (cursor + 1 < length &&
                ((text[cursor] == '*' && text[cursor + 1] == '*') ||
                 (text[cursor] == '_' && text[cursor + 1] == '_'))
            ) {
                val delim = text.substring(cursor, cursor + 2)
                val endIdx = text.indexOf(delim, cursor + 2)
                if (endIdx != -1 && endIdx > cursor + 2) {
                    val boldContent = text.substring(cursor + 2, endIdx)
                    pushStyle(
                        SpanStyle(
                            fontWeight = FontWeight.Bold,
                            color = RadiantGold
                        )
                    )
                    // Check if boldContent has nested italic
                    if (boldContent.startsWith("*") && boldContent.endsWith("*") && boldContent.length > 2) {
                        pushStyle(SpanStyle(fontStyle = FontStyle.Italic))
                        append(boldContent.substring(1, boldContent.length - 1))
                        pop()
                    } else {
                        append(boldContent)
                    }
                    pop()
                    cursor = endIdx + 2
                    continue
                }
            }

            // 3. Check for inline code (`code`)
            if (text[cursor] == '`') {
                val endIdx = text.indexOf('`', cursor + 1)
                if (endIdx != -1) {
                    val codeContent = text.substring(cursor + 1, endIdx)
                    pushStyle(
                        SpanStyle(
                            fontFamily = FontFamily.Monospace,
                            background = SurfaceNavy,
                            color = SaffronLight,
                            fontSize = (baseStyle.fontSize.value * 0.95).sp
                        )
                    )
                    append(" $codeContent ")
                    pop()
                    cursor = endIdx + 1
                    continue
                }
            }

            // 4. Check for links [label](url)
            if (text[cursor] == '[') {
                val closeBracket = text.indexOf(']', cursor + 1)
                if (closeBracket != -1 && closeBracket + 1 < length && text[closeBracket + 1] == '(') {
                    val closeParen = text.indexOf(')', closeBracket + 2)
                    if (closeParen != -1) {
                        val linkLabel = text.substring(cursor + 1, closeBracket)
                        pushStyle(
                            SpanStyle(
                                color = TealAccent,
                                textDecoration = TextDecoration.Underline,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        append(linkLabel)
                        pop()
                        cursor = closeParen + 1
                        continue
                    }
                }
            }

            // 5. Check for italic (*text* or _text_)
            // Note: In markdown, delimiter must not be followed by space (e.g. *italic*, NOT 5 * 4)
            if ((text[cursor] == '*' || text[cursor] == '_') &&
                cursor + 1 < length && text[cursor + 1] != ' '
            ) {
                val delim = text[cursor]
                val endIdx = text.indexOf(delim, cursor + 1)
                // Ensure endIdx does not have leading space (e.g. valid markdown token)
                if (endIdx != -1 && endIdx > cursor + 1 && text[endIdx - 1] != ' ') {
                    val italicContent = text.substring(cursor + 1, endIdx)
                    pushStyle(SpanStyle(fontStyle = FontStyle.Italic, color = SaffronLight))
                    append(italicContent)
                    pop()
                    cursor = endIdx + 1
                    continue
                }
            }

            // Normal character (including legitimate math symbols like * or _)
            append(text[cursor])
            cursor++
        }
    }
}
