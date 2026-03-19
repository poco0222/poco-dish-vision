/**
 * @file ColorTokens.kt
 * @author PopoY
 * @date 2026-03-19
 * @description 汇总 Phase 1 共享颜色令牌（Color Tokens）。
 */
package com.poco.dishvision.core.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * 应用共享颜色令牌。
 */
object ColorTokens {

    val HomeBackgroundGradient = listOf(
        Color(0xFF10151F),
        Color(0xFF182235),
        Color(0xFF0C1118),
    )

    val BrowseBackgroundGradient = listOf(
        Color(0xFF0F1723),
        Color(0xFF162336),
        Color(0xFF0B1018),
    )

    val SettingsBackgroundGradient = listOf(
        Color(0xFF0E1520),
        Color(0xFF182437),
        Color(0xFF0A1018),
    )

    val TextPrimary = Color.White
    val TextSecondary = Color(0xFFE4EAF4)
    val TextMuted = Color(0xCCF4F7FB)
    val Accent = Color(0xFFFFD166)
    val GlassSurface = Color(0xCC152131)
    val GlassSurfaceSoft = Color(0x331D2635)
    val GlassSurfaceStrong = Color(0xF2253550)
    val GlassBorderSubtle = Color(0x26FFFFFF)
    val GlassBorderFocused = Color(0xFFFFD166)
    val CardFocusedSurface = Color(0xFF263750)
    val CategorySelectedSurface = Color(0xFF253552)
    val BadgeSurface = Color(0x332B3E58)
}
