package com.humans.aura.core.domain.interfaces

interface WallpaperController {
    suspend fun setWorkModeWallpaper(title: String)

    suspend fun setNightModeWallpaper()
}
