package com.humans.aura.core.services.wallpaper

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import com.humans.aura.core.domain.interfaces.WallpaperController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AndroidWallpaperController(
    private val context: Context,
    private val wallpaperManagerProvider: (Context) -> WallpaperManager = WallpaperManager::getInstance,
) : WallpaperController {

    override suspend fun setWorkModeWallpaper(title: String) {
        applyBitmapWallpaper(
            backgroundColor = Color.rgb(244, 244, 242),
            accentColor = Color.rgb(28, 28, 28),
            title = title.ifBlank { "Focus" },
            subtitle = "Active goal",
        )
    }

    override suspend fun setNightModeWallpaper() {
        applyLockScreenWallpaper(renderNightWallpaperBitmap())
    }

    private suspend fun applyBitmapWallpaper(
        backgroundColor: Int,
        accentColor: Int,
        title: String,
        subtitle: String,
    ) = withContext(Dispatchers.IO) {
        val bitmap = renderWallpaperBitmap(
            backgroundColor = backgroundColor,
            accentColor = accentColor,
            title = title,
            subtitle = subtitle,
        )

        applyLockScreenWallpaper(bitmap)
    }

    private fun applyLockScreenWallpaper(bitmap: Bitmap) {
        val wallpaperManager = wallpaperManagerProvider(context)
        if (!wallpaperManager.isWallpaperSupported || !wallpaperManager.isSetWallpaperAllowed) {
            return
        }
        wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK)
    }

    private fun renderWallpaperBitmap(
        backgroundColor: Int,
        accentColor: Int,
        title: String,
        subtitle: String,
    ): Bitmap {
        val width = 1080
        val height = 2400
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        canvas.drawColor(backgroundColor)

        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = accentColor
            style = Paint.Style.STROKE
            strokeWidth = 4f
            alpha = if (backgroundColor == Color.BLACK) 70 else 30
        }
        canvas.drawRoundRect(
            RectF(64f, 180f, width - 64f, height - 180f),
            72f,
            72f,
            strokePaint,
        )

        val panelTop = height - 1020f
        val panelBottom = height - 340f
        val panelLeft = 96f
        val panelRight = width - 96f
        val panelFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = accentColor
            style = Paint.Style.FILL
            alpha = if (backgroundColor == Color.BLACK) 24 else 12
        }
        canvas.drawRoundRect(
            RectF(panelLeft, panelTop, panelRight, panelBottom),
            56f,
            56f,
            panelFillPaint,
        )

        val contentLeft = 112f
        val contentRight = width - 112f
        val subtitleBaseline = panelTop + 116f
        val titleAreaTop = panelTop + 200f
        val titleAreaBottom = panelBottom - 96f

        val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = accentColor
            textSize = 38f
            alpha = if (backgroundColor == Color.BLACK) 180 else 150
            letterSpacing = 0.18f
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
        }
        canvas.drawText(subtitle.uppercase(), contentLeft, subtitleBaseline, subtitlePaint)

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = accentColor
            isFakeBoldText = true
            isSubpixelText = true
            letterSpacing = -0.02f
            typeface = Typeface.create("sans-serif", Typeface.BOLD)
        }

        val titleLayout = WallpaperTitleLayoutCalculator { text, textSizePx ->
            titlePaint.textSize = textSizePx
            titlePaint.measureText(text)
        }.calculate(
            title = title,
            maxWidthPx = contentRight - contentLeft,
            maxHeightPx = titleAreaBottom - titleAreaTop,
        )

        titlePaint.textSize = titleLayout.textSizePx
        val fontMetrics = titlePaint.fontMetrics
        var baseline = titleAreaTop + titleLayout.topOffsetPx - fontMetrics.ascent
        titleLayout.lines.forEach { line ->
            canvas.drawText(line, contentLeft, baseline, titlePaint)
            baseline += titleLayout.lineHeightPx
        }

        return bitmap
    }

    private fun renderNightWallpaperBitmap(): Bitmap {
        val width = 1080
        val height = 2400
        return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).also { bitmap ->
            Canvas(bitmap).drawColor(Color.BLACK)
        }
    }
}
