/**
 * @file Dimens.kt
 * @author PopoY
 * @date 2026-03-19
 * @description 汇总 Phase 1 共享尺寸令牌（Dimension Tokens）。
 */
package com.poco.dishvision.core.ui.theme

import androidx.compose.ui.unit.dp

/**
 * 应用共享尺寸令牌。
 */
object Dimens {
    val ScreenHorizontalPadding = 48.dp
    val ScreenTopPadding = 32.dp
    val ScreenBottomPadding = 32.dp
    val HeroBottomPadding = 48.dp
    val SectionSpacing = 24.dp
    val HeroSpacing = 18.dp
    val SurfaceContentSpacing = 12.dp
    val SurfaceHorizontalPadding = 24.dp
    val SurfaceVerticalPadding = 20.dp
    val SurfaceLargeCorner = 36.dp
    val SurfaceMediumCorner = 28.dp
    val SurfaceSmallCorner = 24.dp
    val SurfaceChipCorner = 999.dp
    val SettingsCardCorner = 28.dp
    val CardWidth = 280.dp
    val CardImageHeight = 176.dp
    val HomeHeroTopPadding = 112.dp
    val HomeHeroWidth = 1152.dp
    val HomeHeroHeight = 660.dp
    val HomeCopyTopPadding = 196.dp
    val HomeCopyWidth = 560.dp
    val HomeChipSpacing = 12.dp

    /** 文案区整体高度，设计稿 y=196→606（chips 底部）= 410dp。 */
    val HomeCopySectionHeight = 410.dp
    /** title1 相对文案区顶部的 Y 偏移，设计稿 y=238-196 = 42dp。 */
    val HomeCopyTitle1OffsetY = 42.dp
    /** title2 相对文案区顶部的 Y 偏移，设计稿 y=328-196 = 132dp。 */
    val HomeCopyTitle2OffsetY = 132.dp
    /** 描述文字相对文案区顶部的 Y 偏移，设计稿 y=438-196 = 242dp。 */
    val HomeCopyDescOffsetY = 242.dp
    /** 标签胶囊行相对文案区顶部的 Y 偏移，设计稿 y=560-196 = 364dp。 */
    val HomeCopyChipsOffsetY = 364.dp

    val HomeRecommendationGap = 18.dp
    val HomeRecommendationCardHeight = 200.dp
    /** 推荐卡水平内边距，设计稿 padding=[22,24] → horizontal=24。 */
    val HomeRecommendationCardHorizontalPadding = 24.dp
    /** 推荐卡垂直内边距，设计稿 padding=[22,24] → vertical=22。 */
    val HomeRecommendationCardVerticalPadding = 22.dp
    /** 推荐卡带底部留白，设计稿 y=820+h=200 → 底部=1080-1020=60dp。 */
    val HomeRecommendationBottomPadding = 60.dp
    val HomeSeasonBadgeHorizontalPadding = 18.dp
    val HomeSeasonBadgeVerticalPadding = 10.dp
    /** 季节标签右边距，设计稿 1920-(1630+196)=94dp。 */
    val HomeSeasonBadgeEndPadding = 94.dp
}
