package com.example.ladycure.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class ResponsiveDimens(val screenWidth: Dp, val screenHeight: Dp) {
    fun w(fraction: Float): Dp = screenWidth * fraction
    fun h(fraction: Float): Dp = screenHeight * fraction
}

object FractionDimens {
    const val paddingSmallW = 16 / 411f
    const val paddingSmallH = 16 / 914f
    const val spacingTinyW = 12 / 411f
    const val spacingTinyH = 12 / 914f
    const val paddingMediumW = 32 / 411f
    const val paddingMediumH = 32 / 914f
    const val paddingTinyH = 6 / 914f
    const val iconSmallW = 40 / 411f
    const val iconMediumW = 48 / 411f
    const val iconLargeW = 64 / 411f
}

@Composable
fun rememberResponsiveDimens(): ResponsiveDimens {
    val config = LocalConfiguration.current
    return remember(config.screenWidthDp, config.screenHeightDp) {
        ResponsiveDimens(
            screenWidth = config.screenWidthDp.dp,
            screenHeight = config.screenHeightDp.dp
        )
    }
}
