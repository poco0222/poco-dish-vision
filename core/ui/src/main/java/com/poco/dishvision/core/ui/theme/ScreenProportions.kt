/**
 * @file ScreenProportions.kt
 * @author PopoY
 * @date 2026-03-20
 * @description 基于屏幕实际 dp 视口的比例化尺寸系统。
 *
 * 设计稿坐标基于 1920×1080 px，但 Android TV 设备 density 各异
 * （xhdpi=320 → 960×540dp，tvdpi=213 → ~1441×810dp，mdpi=160 → 1920×1080dp），
 * 直接将 px 写为 dp 会导致布局溢出/重叠。
 * 本文件通过比例计算将设计稿坐标映射到任意 dp 视口。
 */
package com.poco.dishvision.core.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** 设计稿基准宽度（px），用于计算水平方向比例 */
private const val DESIGN_WIDTH = 1920f

/** 设计稿基准高度（px），用于计算垂直方向比例 */
private const val DESIGN_HEIGHT = 1080f

/**
 * 屏幕比例化尺寸持有者。
 *
 * 在 [PocoTheme] 中通过 `BoxWithConstraints` 获取实际可用 dp 宽高后构建，
 * 所有布局型尺寸均按设计稿比例换算，小尺寸（圆角、间距、边框）保持固定 dp。
 *
 * @param screenWidth 实际可用屏幕宽度（dp）。
 * @param screenHeight 实际可用屏幕高度（dp）。
 */
@Immutable
data class ScreenProportions(
    val screenWidth: Dp,
    val screenHeight: Dp,
) {
    // ── 水平比例值（基于 screenWidth） ──

    /** 屏幕水平内边距，设计稿 48/1920 */
    val screenHorizontalPadding: Dp = screenWidth * (48f / DESIGN_WIDTH)

    /** Hero 图宽度，设计稿 1152/1920 ≈ 60% */
    val homeHeroWidth: Dp = screenWidth * (1152f / DESIGN_WIDTH)

    /** 左侧文案区宽度，设计稿 560/1920 ≈ 29.2% */
    val homeCopyWidth: Dp = screenWidth * (560f / DESIGN_WIDTH)

    /** 季节标签右侧边距，设计稿 94/1920 */
    val homeSeasonBadgeEndPadding: Dp = screenWidth * (94f / DESIGN_WIDTH)

    // ── 垂直比例值（基于 screenHeight） ──

    /** 屏幕顶部内边距，设计稿 32/1080 */
    val screenTopPadding: Dp = screenHeight * (32f / DESIGN_HEIGHT)

    /** Hero 图距顶部偏移，设计稿 112/1080 */
    val homeHeroTopPadding: Dp = screenHeight * (112f / DESIGN_HEIGHT)

    /** Hero 图高度，设计稿 660/1080 ≈ 61.1% */
    val homeHeroHeight: Dp = screenHeight * (660f / DESIGN_HEIGHT)

    /** 文案区顶部 padding，设计稿 196/1080 */
    val homeCopyTopPadding: Dp = screenHeight * (196f / DESIGN_HEIGHT)

    /** 文案区总高度，设计稿 (606-196)/1080 = 410/1080 */
    val homeCopySectionHeight: Dp = screenHeight * (410f / DESIGN_HEIGHT)

    /** 推荐卡带底部边距，设计稿 (1080-820-200)/1080 = 60/1080 */
    val homeRecommendationBottomPadding: Dp = screenHeight * (60f / DESIGN_HEIGHT)

    /** 推荐卡高度，设计稿 200/1080 ≈ 18.5% */
    val homeRecommendationCardHeight: Dp = screenHeight * (200f / DESIGN_HEIGHT)

    /** 品牌副标题与品牌名的垂直间距，设计稿 (68-32)/1080 = 36/1080 */
    val brandSubtitleOffset: Dp = screenHeight * (36f / DESIGN_HEIGHT)

    // ── 文案区内部 Y 偏移（均为垂直方向比例） ──

    /** 主标题第一行 Y 偏移，设计稿 (238-196)/1080 = 42/1080 */
    val homeCopyTitle1OffsetY: Dp = screenHeight * (42f / DESIGN_HEIGHT)

    /** 主标题第二行 Y 偏移，设计稿 (328-196)/1080 = 132/1080 */
    val homeCopyTitle2OffsetY: Dp = screenHeight * (132f / DESIGN_HEIGHT)

    /** 描述文字 Y 偏移，设计稿 (438-196)/1080 = 242/1080 */
    val homeCopyDescOffsetY: Dp = screenHeight * (242f / DESIGN_HEIGHT)

    /** 标签胶囊行 Y 偏移，设计稿 (560-196)/1080 = 364/1080 */
    val homeCopyChipsOffsetY: Dp = screenHeight * (364f / DESIGN_HEIGHT)

    // ── 字号缩放 ──

    /** 基于设计稿高度的字号缩放因子（540dp 屏幕 → 0.5，1080dp → 1.0） */
    val fontScale: Float = screenHeight.value / DESIGN_HEIGHT

    /**
     * 按屏幕高度比例缩放字号。
     *
     * 设计稿中 78sp 标题在 960×540dp 视口下缩放为 ~39sp，保持视觉比例一致。
     *
     * @param designSp 设计稿中的原始 sp 值。
     * @return 按比例缩放后的 [TextUnit]。
     */
    fun scaledSp(designSp: Float): TextUnit = (designSp * fontScale).sp
}

/**
 * 提供 [ScreenProportions] 的 CompositionLocal。
 *
 * 默认值为设计稿原始 1920×1080dp，确保 Compose Preview 和 IDE 下正常渲染。
 * 运行时由 [PocoTheme] 中的 `BoxWithConstraints` 注入实际值。
 */
val LocalScreenProportions = staticCompositionLocalOf {
    ScreenProportions(
        screenWidth = DESIGN_WIDTH.dp,
        screenHeight = DESIGN_HEIGHT.dp,
    )
}
