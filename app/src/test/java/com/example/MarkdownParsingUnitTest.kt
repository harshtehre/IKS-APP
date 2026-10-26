package com.example

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.ui.components.MarkdownBlock
import com.example.ui.components.parseInlineMarkdown
import com.example.ui.components.parseMarkdownBlocks
import org.junit.Assert.*
import org.junit.Test

class MarkdownParsingUnitTest {

    private val baseStyle = TextStyle(fontSize = 14.sp)

    @Test
    fun testBoldParsingEliminatesAsterisks() {
        val input = "**Important Concept**"
        val annotated = parseInlineMarkdown(input, baseStyle)
        
        // Assert raw asterisks are completely removed
        assertFalse("Raw asterisks must not be present in output text", annotated.text.contains("*"))
        assertEquals("Important Concept", annotated.text)
        
        // Assert bold style applied
        val spans = annotated.spanStyles
        assertTrue("Should have at least one span style", spans.isNotEmpty())
        assertEquals(FontWeight.Bold, spans[0].item.fontWeight)
    }

    @Test
    fun testItalicParsingEliminatesAsterisks() {
        val input = "*Aryabhata*"
        val annotated = parseInlineMarkdown(input, baseStyle)
        
        assertFalse("Raw asterisks must not be present", annotated.text.contains("*"))
        assertEquals("Aryabhata", annotated.text)
        
        val spans = annotated.spanStyles
        assertTrue(spans.isNotEmpty())
        assertEquals(FontStyle.Italic, spans[0].item.fontStyle)
    }

    @Test
    fun testMathematicalAsteriskPreserved() {
        val mathInput = "Area = 5 * 4 = 20"
        val annotated = parseInlineMarkdown(mathInput, baseStyle)
        
        // Legitimate math multiplication asterisk should NOT be stripped
        assertEquals("Area = 5 * 4 = 20", annotated.text)
    }

    @Test
    fun testHeadingsParsing() {
        val markdown = """
            ## Summary
            This is the body.
        """.trimIndent()
        val blocks = parseMarkdownBlocks(markdown)
        
        assertTrue("Should have 2 blocks", blocks.size >= 2)
        val heading = blocks[0] as MarkdownBlock.Heading
        assertEquals(2, heading.level)
        assertEquals("Summary", heading.text)
    }

    @Test
    fun testBulletListParsing() {
        val markdown = """
            - Point 1
            - Point 2
        """.trimIndent()
        val blocks = parseMarkdownBlocks(markdown)
        
        assertEquals(2, blocks.size)
        assertTrue(blocks[0] is MarkdownBlock.BulletItem)
        assertEquals("Point 1", (blocks[0] as MarkdownBlock.BulletItem).text)
        assertEquals("Point 2", (blocks[1] as MarkdownBlock.BulletItem).text)
    }

    @Test
    fun testNumberedListParsing() {
        val markdown = """
            1. Step 1
            2. Step 2
        """.trimIndent()
        val blocks = parseMarkdownBlocks(markdown)
        
        assertEquals(2, blocks.size)
        assertTrue(blocks[0] is MarkdownBlock.NumberedItem)
        assertEquals(1, (blocks[0] as MarkdownBlock.NumberedItem).number)
        assertEquals("Step 1", (blocks[0] as MarkdownBlock.NumberedItem).text)
        assertEquals(2, (blocks[1] as MarkdownBlock.NumberedItem).number)
        assertEquals("Step 2", (blocks[1] as MarkdownBlock.NumberedItem).text)
    }

    @Test
    fun testInlineBoldInsideBullet() {
        val input = "**Baudhayana Theorem**: The diagonal of a rectangle produces area equal to sum of areas."
        val annotated = parseInlineMarkdown(input, baseStyle)
        
        assertFalse(annotated.text.contains("**"))
        assertTrue(annotated.text.startsWith("Baudhayana Theorem: The diagonal"))
        val spans = annotated.spanStyles
        assertTrue(spans.isNotEmpty())
        assertEquals(FontWeight.Bold, spans[0].item.fontWeight)
    }
}
