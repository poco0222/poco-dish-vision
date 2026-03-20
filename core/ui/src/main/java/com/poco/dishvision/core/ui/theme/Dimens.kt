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

    // ── Browse 页（分类浏览）专用尺寸 ──

    /** 左侧分类导轨宽度，设计稿 240px。 */
    val BrowseRailWidth = 240.dp
    /** 分类导轨项圆角，设计稿 cornerRadius=20。 */
    val BrowseRailItemCorner = 20.dp
    /** 分类导轨项垂直内边距，设计稿 padding=[14,18] → vertical=14。 */
    val BrowseRailItemVerticalPadding = 14.dp
    /** 分类导轨项水平内边距，设计稿 padding=[14,18] → horizontal=18。 */
    val BrowseRailItemHorizontalPadding = 18.dp
    /** 分类导轨项间距，设计稿 gap=10。 */
    val BrowseRailItemSpacing = 10.dp

    /** 菜品网格间距，设计稿 gap=20。 */
    val BrowseGridGap = 20.dp
    /** 菜品网格卡片圆角，设计稿 cornerRadius=24。 */
    val BrowseGridCardCorner = 24.dp
    /** 菜品网格卡片图片高度，设计稿 180px。 */
    val BrowseGridCardImageHeight = 180.dp
    /** 菜品网格卡片文本区内边距，设计稿 padding=[0,18,18,18]。 */
    val BrowseGridCardBodyPadding = 18.dp
    /** 菜品网格卡片文本区间距，设计稿 gap=6。 */
    val BrowseGridCardBodySpacing = 6.dp
    /** 菜品网格卡片图片到文本区间距，设计稿 gap=10。 */
    val BrowseGridCardContentSpacing = 10.dp

    /** 导轨与主内容区间距，设计稿 328-48-240=40px。 */
    val BrowseRailToContentGap = 40.dp

    // ── FocusStage 聚焦舞台固定尺寸 ──

    /** 中央大卡圆角，设计稿 cornerRadius=30。 */
    val FocusMidCorner = 30.dp
    /** 中央大卡边框宽度，设计稿 stroke thickness=2。 */
    val FocusMidBorderWidth = 2.dp
    /** 中央大卡 body 区水平内边距，设计稿 padding=[0,22,22,22] → h=22。 */
    val FocusMidBodyPaddingH = 22.dp
    /** 中央大卡 body 区底部内边距，设计稿 padding=[0,22,22,22] → bottom=22。 */
    val FocusMidBodyPaddingBottom = 22.dp
    /** 中央大卡 body 区内部间距，设计稿 gap=12。 */
    val FocusMidBodyGap = 12.dp
    /** 中央大卡图片到 body 的间距，设计稿 gap=14。 */
    val FocusMidContentSpacing = 14.dp
    /** 中央大卡阴影模糊半径，设计稿 blur=30。 */
    val FocusMidShadowBlur = 30.dp
    /** 中央大卡阴影 Y 偏移，设计稿 offset.y=14。 */
    val FocusMidShadowOffsetY = 14.dp
    /** 中央大卡价格徽章圆角，设计稿 cornerRadius=18。 */
    val FocusMidPriceBadgeCorner = 18.dp
    /** 中央大卡标题区内部间距，设计稿 gap=6。 */
    val FocusMidTitleGroupGap = 6.dp

    /** 周围小卡圆角，设计稿 cornerRadius=22。 */
    val FocusSmallCorner = 22.dp
    /** 周围小卡 body 水平内边距，设计稿 padding=[0,14,14,14] → h=14。 */
    val FocusSmallBodyPaddingH = 14.dp
    /** 周围小卡 body 底部内边距，设计稿 padding=[0,14,14,14] → bottom=14。 */
    val FocusSmallBodyPaddingBottom = 14.dp
    /** 周围小卡 name-desc 间距，设计稿 gap=4。 */
    val FocusSmallBodySpacing = 4.dp
    /** 周围小卡 image-body 间距，设计稿 gap=8。 */
    val FocusSmallContentSpacing = 8.dp

    /** 风味标签芯片水平内边距，设计稿 padding=[10,14] → h=14。 */
    val FocusChipPaddingH = 14.dp
    /** 风味标签芯片垂直内边距，设计稿 padding=[10,14] → v=10。 */
    val FocusChipPaddingV = 10.dp
    /** 风味标签芯片行间距，设计稿 gap=10。 */
    val FocusChipRowGap = 10.dp
}
