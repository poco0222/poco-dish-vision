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

    // 主图叠加层垂直渐变遮罩（上透明→下深色），设计稿 rotation=90 → 从上到下。
    val HomeHeroOverlayGradient = listOf(
        Color(0x001F0E09),   // 顶部：近透明
        Color(0x66140C09),   // 中部：半透明（position=0.65）
        Color(0xCC140C09),   // 底部：深色
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

    /** 聚焦卡弱化边框（Accent 70% 透明度），柔和而不过于醒目 */
    val FocusBorderSoft = Color(0xB3C9A45E)

    /** 聚焦卡弱光扩散颜色（Accent 25% 透明度），自绘 BlurMaskFilter 使用 */
    val FocusGlow = Color(0x40C9A45E)

    val CategorySelectedSurface = RedAccent
    val BadgeSurface = SurfaceDeep

    // ── FocusStage 聚焦舞台颜色 ──

    /** 中央大卡整体填充色，设计稿 $--bg-bottom = #3B1612 */
    val FocusMidFill = Color(0xFF3B1612)
    /** 中央大卡 body 区背景色，设计稿 $--bg-mid = #24110E */
    val FocusMidBodyBg = Color(0xFF24110E)
    /** 中央大卡阴影颜色，设计稿 #00000052 */
    val FocusMidShadow = Color(0x52000000)
    /** 风味标签芯片背景色，设计稿 $--chip-bg = #5E3A20 */
    val ChipBg = Color(0xFF5E3A20)
}
