package com.example.ladycure.ui.theme

import DefaultColorPalette
import androidx.compose.runtime.Composable
import aquaColorPalette
import blueColorPalette
import greenColorPalette
import purpleColorPalette
import redColorPalette

// ThemeManager.kt
object ThemeManager {
    enum class AppTheme {
        DEFAULT, RED, BLUE, GREEN, AQUA, PURPLE
    }

    var currentTheme: AppTheme = AppTheme.DEFAULT
        private set

    fun setTheme(theme: AppTheme) {
        currentTheme = theme
    }

    @Composable
    fun GetCurrentColorScheme() = when (currentTheme) {
        AppTheme.DEFAULT -> DefaultColorPalette()
        AppTheme.RED -> redColorPalette()
        AppTheme.BLUE -> blueColorPalette()
        AppTheme.GREEN -> greenColorPalette()
        AppTheme.AQUA -> aquaColorPalette()
        AppTheme.PURPLE -> purpleColorPalette()
    }
}