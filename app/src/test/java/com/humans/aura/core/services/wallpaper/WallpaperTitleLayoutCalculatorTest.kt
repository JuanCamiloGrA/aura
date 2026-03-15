package com.humans.aura.core.services.wallpaper

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WallpaperTitleLayoutCalculatorTest {

    private val calculator = WallpaperTitleLayoutCalculator(::measureText)

    @Test
    fun calculate_keeps_long_multi_word_titles_visible_within_bounds() {
        val title = "Complete Payment Gateway for Classmate's landing page before Friday"

        val layout = calculator.calculate(
            title = title,
            maxWidthPx = 760f,
            maxHeightPx = 1180f,
        )

        assertEquals(title, layout.lines.joinToString(" "))
        assertTrue(layout.lines.size >= 3)
        assertTrue(layout.textSizePx < 156f)
        assertTrue(layout.lines.all { measureText(it, layout.textSizePx) <= 760f })
    }

    @Test
    fun calculate_keeps_short_titles_large_and_bottom_anchored() {
        val layout = calculator.calculate(
            title = "Deep Work",
            maxWidthPx = 760f,
            maxHeightPx = 1180f,
        )

        assertEquals(listOf("Deep Work"), layout.lines)
        assertTrue(layout.textSizePx >= 140f)
        assertTrue(layout.topOffsetPx > 650f)
    }

    @Test
    fun calculate_splits_unbroken_words_without_overflow() {
        val title = "Supercalifragilisticexpialidocious"

        val layout = calculator.calculate(
            title = title,
            maxWidthPx = 320f,
            maxHeightPx = 1180f,
        )

        assertEquals(title, layout.lines.joinToString(separator = ""))
        assertTrue(layout.lines.size > 1)
        assertTrue(layout.lines.all { measureText(it, layout.textSizePx) <= 320f })
    }

    private fun measureText(text: String, textSizePx: Float): Float {
        return text.sumOf { characterWidth(it).toDouble() }.toFloat() * textSizePx
    }

    private fun characterWidth(character: Char): Float = when {
        character == ' ' -> 0.28f
        character.isUpperCase() -> 0.68f
        character in setOf('m', 'w', 'M', 'W') -> 0.78f
        character in setOf('i', 'l', 'I', 'j', 't') -> 0.24f
        character.isLetterOrDigit() -> 0.54f
        else -> 0.32f
    }
}
