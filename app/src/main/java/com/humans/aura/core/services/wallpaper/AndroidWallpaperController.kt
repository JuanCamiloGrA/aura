package com.humans.aura.core.services.wallpaper

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
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

        val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = accentColor
            textSize = 40f
            alpha = if (backgroundColor == Color.BLACK) 180 else 150
            letterSpacing = 0.2f
        }
        canvas.drawText(subtitle.uppercase(), 112f, 360f, subtitlePaint)

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = accentColor
            textSize = 124f
            isFakeBoldText = true
        }
        val lineSpacing = 138f
        title.trim().split(" ").chunked(2).map { it.joinToString(" ") }
            .take(3)
            .forEachIndexed { index, line ->
                canvas.drawText(line, 112f, 620f + (index * lineSpacing), titlePaint)
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
