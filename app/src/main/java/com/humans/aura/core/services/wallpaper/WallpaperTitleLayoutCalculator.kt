package com.humans.aura.core.services.wallpaper

internal data class WallpaperTitleLayout(
    val lines: List<String>,
    val textSizePx: Float,
    val lineHeightPx: Float,
    val topOffsetPx: Float,
)

internal class WallpaperTitleLayoutCalculator(
    private val measureText: (text: String, textSizePx: Float) -> Float,
) {

    fun calculate(
        title: String,
        maxWidthPx: Float,
        maxHeightPx: Float,
    ): WallpaperTitleLayout {
        val normalizedTitle = title.trim().split(Regex("\\s+")).filter(String::isNotBlank).joinToString(" ")
        val words = normalizedTitle.split(" ").filter(String::isNotBlank).ifEmpty { listOf("Focus") }
        val preferredMaxLines = preferredMaxLinesFor(normalizedTitle)
        val maxLines = maxLinesFor(normalizedTitle)

        var textSizePx = MAX_TEXT_SIZE_PX
        while (textSizePx >= MIN_TEXT_SIZE_PX) {
            val lines = breakIntoLines(words = words, maxWidthPx = maxWidthPx, textSizePx = textSizePx)
            val layout = createLayout(lines = lines, textSizePx = textSizePx, maxHeightPx = maxHeightPx)
            if (lines.size <= preferredMaxLines && layout.lineHeightPx * lines.size <= maxHeightPx) {
                return layout
            }
            textSizePx -= TEXT_SIZE_STEP_PX
        }

        textSizePx = MAX_TEXT_SIZE_PX
        while (textSizePx >= MIN_TEXT_SIZE_PX) {
            val lines = breakIntoLines(words = words, maxWidthPx = maxWidthPx, textSizePx = textSizePx)
            val layout = createLayout(lines = lines, textSizePx = textSizePx, maxHeightPx = maxHeightPx)
            if (lines.size <= maxLines && layout.lineHeightPx * lines.size <= maxHeightPx) {
                return layout
            }
            textSizePx -= TEXT_SIZE_STEP_PX
        }

        val fallbackLines = breakIntoLines(words = words, maxWidthPx = maxWidthPx, textSizePx = MIN_TEXT_SIZE_PX)
        return createLayout(lines = fallbackLines, textSizePx = MIN_TEXT_SIZE_PX, maxHeightPx = maxHeightPx)
    }

    private fun createLayout(
        lines: List<String>,
        textSizePx: Float,
        maxHeightPx: Float,
    ): WallpaperTitleLayout {
        val lineHeightPx = textSizePx * LINE_HEIGHT_MULTIPLIER
        val contentHeightPx = lineHeightPx * lines.size
        val remainingHeightPx = (maxHeightPx - contentHeightPx).coerceAtLeast(0f)
        val topOffsetPx = remainingHeightPx * topBiasFor(lines.size)
        return WallpaperTitleLayout(
            lines = lines,
            textSizePx = textSizePx,
            lineHeightPx = lineHeightPx,
            topOffsetPx = topOffsetPx,
        )
    }

    private fun breakIntoLines(
        words: List<String>,
        maxWidthPx: Float,
        textSizePx: Float,
    ): List<String> {
        val lines = mutableListOf<String>()
        var currentLine = ""

        words.forEach { word ->
            if (currentLine.isEmpty()) {
                if (fits(word, maxWidthPx, textSizePx)) {
                    currentLine = word
                } else {
                    val segments = splitLongWord(word = word, maxWidthPx = maxWidthPx, textSizePx = textSizePx)
                    lines += segments.dropLast(1)
                    currentLine = segments.last()
                }
            } else {
                val candidate = "$currentLine $word"
                if (fits(candidate, maxWidthPx, textSizePx)) {
                    currentLine = candidate
                } else {
                    lines += currentLine
                    currentLine = ""
                    if (fits(word, maxWidthPx, textSizePx)) {
                        currentLine = word
                    } else {
                        val segments = splitLongWord(word = word, maxWidthPx = maxWidthPx, textSizePx = textSizePx)
                        lines += segments.dropLast(1)
                        currentLine = segments.last()
                    }
                }
            }
        }

        if (currentLine.isNotEmpty()) {
            lines += currentLine
        }

        return lines
    }

    private fun splitLongWord(
        word: String,
        maxWidthPx: Float,
        textSizePx: Float,
    ): List<String> {
        val segments = mutableListOf<String>()
        var startIndex = 0

        while (startIndex < word.length) {
            var endIndex = startIndex + 1
            while (endIndex <= word.length && fits(word.substring(startIndex, endIndex), maxWidthPx, textSizePx)) {
                endIndex += 1
            }
            val resolvedEndIndex = (endIndex - 1).coerceAtLeast(startIndex + 1)
            segments += word.substring(startIndex, resolvedEndIndex)
            startIndex = resolvedEndIndex
        }

        return segments
    }

    private fun fits(
        text: String,
        maxWidthPx: Float,
        textSizePx: Float,
    ): Boolean = measureText(text, textSizePx) <= maxWidthPx

    private fun maxLinesFor(title: String): Int = when (title.length) {
        in 0..24 -> 3
        in 25..56 -> 4
        in 57..96 -> 5
        else -> 6
    }

    private fun preferredMaxLinesFor(title: String): Int = when (title.length) {
        in 0..18 -> 1
        in 19..42 -> 2
        in 43..72 -> 3
        else -> 4
    }

    private fun topBiasFor(lineCount: Int): Float = when {
        lineCount <= 1 -> 0.78f
        lineCount == 2 -> 0.72f
        lineCount == 3 -> 0.64f
        else -> 0.56f
    }

    private companion object {
        const val MAX_TEXT_SIZE_PX = 156f
        const val MIN_TEXT_SIZE_PX = 72f
        const val TEXT_SIZE_STEP_PX = 4f
        const val LINE_HEIGHT_MULTIPLIER = 1.08f
    }
}
