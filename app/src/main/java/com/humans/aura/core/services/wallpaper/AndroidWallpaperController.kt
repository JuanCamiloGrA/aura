package com.humans.aura.core.services.wallpaper

import com.humans.aura.core.domain.interfaces.WallpaperController

class AndroidWallpaperController : WallpaperController {
    override suspend fun setNightModeWallpaper() = Unit
}
