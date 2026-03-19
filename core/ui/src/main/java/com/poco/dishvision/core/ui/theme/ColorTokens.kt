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

    // 来源：design/pencil-new.pen `R3XHK` 及变量区，统一承接湘味主题主背景渐变。
    val HomeBackgroundGradient = listOf(
        Color(0xFF120B09),
        Color(0xFF24110E),
        Color(0xFF3B1612),
    )

    val BrowseBackgroundGradient = listOf(
        Color(0xFF140C09),
        Color(0xFF2A140F),
        Color(0xFF411713),
    )

    val SettingsBackgroundGradient = listOf(
        Color(0xFF140C09),
        Color(0xFF2A140F),
        Color(0xFF411713),
    )

    val HomeHeroOverlayGradient = listOf(
        Color(0x1F0E0900),
        Color(0x66140C09),
        Color(0xCC140C09),
    )

    val TextPrimary = Color(0xFFF7F1E8)
    val TextSecondary = Color(0xFFD8C8B3)
    val TextMuted = Color(0xFFB89E86)
    val Accent = Color(0xFFC9A45E)
    val GoldSoft = Color(0x7AC9A45E)
    val RedAccent = Color(0xFFB63A27)
    val RedHot = Color(0xFFD94A2B)
    val SurfaceCard = Color(0xCC2A1712)
    val SurfaceCardStrong = Color(0xE5341D17)
    val SurfaceDeep = Color(0xA61B130F)
    val SurfacePanel = Color(0xEE321A15)
    val BorderSubtle = Color(0xFF5B3A2B)
    val GlassSurface = SurfaceCard
    val GlassSurfaceSoft = SurfaceDeep
    val GlassSurfaceStrong = SurfaceCardStrong
    val GlassBorderSubtle = BorderSubtle
    val GlassBorderFocused = Accent
    val CardFocusedSurface = SurfaceCardStrong
    val CategorySelectedSurface = RedAccent
    val BadgeSurface = SurfaceDeep
}
