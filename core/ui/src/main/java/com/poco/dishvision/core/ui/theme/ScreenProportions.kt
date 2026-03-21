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

    // ── Browse 页（分类浏览）比例尺寸 ──

    /** 左侧导轨宽度（收窄版），设计稿 200/1920 */
    val browseRailWidth: Dp = screenWidth * (200f / DESIGN_WIDTH)

    /** 导轨到主内容区间距，设计稿 40/1920。 */
    val browseRailToContentGap: Dp = screenWidth * (40f / DESIGN_WIDTH)

    /**
     * 主内容区宽度。
     *
     * 通过「总宽 - 左右安全边距 - 导轨宽 - 导轨间距」动态求得，
     * 确保收窄左栏后额外空间全部回流到右侧菜品展示区。
     * @author PopoY
     */
    val browseContentWidth: Dp = screenWidth - (screenHorizontalPadding * 2f) - browseRailWidth - browseRailToContentGap

    /** 主内容区起始 X，设计稿 288/1920（48 + 200 + 40） */
    val browseContentStartX: Dp = screenWidth * (288f / DESIGN_WIDTH)

    /** 主标题副文本宽度，设计稿 1180/1920 */
    val browseSubtitleWidth: Dp = screenWidth * (1180f / DESIGN_WIDTH)

    /** FocusScene 副文本宽度，设计稿 1300/1920 */
    val focusSubtitleWidth: Dp = screenWidth * (1300f / DESIGN_WIDTH)

    /** 品牌副标题底部到分类标签的间距，设计稿 (156-86)/1080 ≈ 70px */
    val browseBrandToRailGap: Dp = screenHeight * (70f / DESIGN_HEIGHT)

    /** 右列 label→title 间距，设计稿 (64-32-fontSize≈18)/1080 ≈ 14px */
    val browseLabelToTitleGap: Dp = screenHeight * (14f / DESIGN_HEIGHT)

    /** 右列 title→sub 间距，设计稿 (120-64-fontSize≈40)/1080 ≈ 16px */
    val browseTitleToSubGap: Dp = screenHeight * (16f / DESIGN_HEIGHT)

    /** 右列 sub→grid 间距，设计稿 (174-120-fontSize≈18)/1080 ≈ 36px */
    val browseSubToGridGap: Dp = screenHeight * (36f / DESIGN_HEIGHT)

    /** 导轨聚焦时 scale，设计稿表现为 103% 的微幅放大 */
    val browseRailFocusedScale: Float = 1.03f

    /** 菜品网格卡片图片高度（精品展示加强版），设计稿 240/1080 */
    val browseCardImageHeight: Dp = screenHeight * (240f / DESIGN_HEIGHT)

    /** 菜品网格水平间距，设计稿 20/1920 */
    val browseGridHorizontalGap: Dp = screenWidth * (20f / DESIGN_WIDTH)

    /** 菜品网格垂直间距，设计稿 20/1080 */
    val browseGridVerticalGap: Dp = screenHeight * (20f / DESIGN_HEIGHT)

    /**
     * 浏览页网格卡片缩放系数。
     *
     * 为卡片高度与视觉宽度提供同一缩放比，避免仅降低高度后出现“比例被拉扁”。
     * 0.96 表示轻微收缩，保留首屏 3x3 信息密度并释放底部留白。
     * @author PopoY
     */
    val browseGridCardScale: Float = 0.96f

    /** "分类"标签到导轨按钮列表的间距，设计稿 y(190) - y(156) - 字号行高 ≈ 14px */
    val browseRailLabelToItemsGap: Dp = screenHeight * (14f / DESIGN_HEIGHT)

    /** 分类导轨项垂直内边距，设计稿 14/1080 */
    val browseRailItemVerticalPadding: Dp = screenHeight * (14f / DESIGN_HEIGHT)

    /** 分类导轨项水平内边距，设计稿 18/1920 */
    val browseRailItemHorizontalPadding: Dp = screenWidth * (18f / DESIGN_WIDTH)

    /** 分类导轨项间距，设计稿 10/1080 */
    val browseRailItemSpacing: Dp = screenHeight * (10f / DESIGN_HEIGHT)

    /** 菜品网格视口高度，按主内容区自标题以下的可见区域建模。 */
    val browseGridViewportHeight: Dp = screenHeight * (874f / DESIGN_HEIGHT)

    /** 菜品网格卡片文本区水平内边距，设计稿 18/1920 */
    val browseGridCardBodyPaddingHorizontal: Dp = screenWidth * (18f / DESIGN_WIDTH)

    /** 菜品网格卡片文本区底部内边距，设计稿 18/1080 */
    val browseGridCardBodyPaddingBottom: Dp = screenHeight * (18f / DESIGN_HEIGHT)

    /** 菜品网格卡片文本区间距，设计稿 6/1080 */
    val browseGridCardBodySpacing: Dp = screenHeight * (6f / DESIGN_HEIGHT)

    /** 菜品网格卡片图片到底部文本区的间距，设计稿 10/1080 */
    val browseGridCardContentSpacing: Dp = screenHeight * (10f / DESIGN_HEIGHT)

    /**
     * 菜品网格卡片正文区最小高度，设计稿 98/1080，保证在高度受限时仍保留足够文本空间。
     * @author PopoY
     */
    val browseGridCardBodyMinHeight: Dp = screenHeight * (98f / DESIGN_HEIGHT)

    // ── FocusStage 聚焦舞台比例尺寸 ──

    /** 聚焦舞台总高度，设计稿 794/1080 */
    val focusStageHeight: Dp = screenHeight * (794f / DESIGN_HEIGHT)

    /** 中央大卡宽度，设计稿 680/1544（相对内容区比例） */
    val focusMidWidth: Dp = browseContentWidth * (680f / 1544f)

    /** 中央大卡 X 偏移（相对 focusStage 左侧），设计稿 432/1544 */
    val focusMidX: Dp = browseContentWidth * (432f / 1544f)

    /** 中央大卡 Y 偏移（相对 focusStage 顶部），设计稿 24/794 */
    val focusMidY: Dp = focusStageHeight * (24f / 794f)

    /** 中央大卡图片高度，设计稿 228/1080 */
    val focusMidImageHeight: Dp = screenHeight * (228f / DESIGN_HEIGHT)

    /** 中央大卡 body 区水平内边距，设计稿 22/1920 */
    val focusMidBodyPaddingHorizontal: Dp = screenWidth * (22f / DESIGN_WIDTH)

    /** 中央大卡 body 区底部内边距，设计稿 22/1080 */
    val focusMidBodyPaddingBottom: Dp = screenHeight * (22f / DESIGN_HEIGHT)

    /** 中央大卡 body 区内部间距，设计稿 12/1080 */
    val focusMidBodyGap: Dp = screenHeight * (12f / DESIGN_HEIGHT)

    /** 中央大卡图片区到底部 body 的间距，设计稿 14/1080 */
    val focusMidContentSpacing: Dp = screenHeight * (14f / DESIGN_HEIGHT)

    /** 中央大卡标题组内部间距，设计稿 6/1080 */
    val focusMidTitleGroupGap: Dp = screenHeight * (6f / DESIGN_HEIGHT)

    /** 周围小卡宽度，设计稿 360/1544 */
    val focusSmallCardWidth: Dp = browseContentWidth * (360f / 1544f)

    /** 周围小卡图片高度，设计稿 132/1080 */
    val focusSmallCardImageHeight: Dp = screenHeight * (132f / DESIGN_HEIGHT)

    /** 周围小卡 body 水平内边距，设计稿 14/1920 */
    val focusSmallBodyPaddingHorizontal: Dp = screenWidth * (14f / DESIGN_WIDTH)

    /** 周围小卡 body 底部内边距，设计稿 14/1080 */
    val focusSmallBodyPaddingBottom: Dp = screenHeight * (14f / DESIGN_HEIGHT)

    /** 周围小卡 name-desc 间距，设计稿 4/1080 */
    val focusSmallBodySpacing: Dp = screenHeight * (4f / DESIGN_HEIGHT)

    /** 周围小卡 image-body 间距，设计稿 8/1080 */
    val focusSmallContentSpacing: Dp = screenHeight * (8f / DESIGN_HEIGHT)

    /** 风味标签行间距，设计稿 10/1080 */
    val focusChipRowGap: Dp = screenHeight * (10f / DESIGN_HEIGHT)

    /**
     * 8 个 FocusStage 卡槽的绝对坐标（相对 focusStage 容器），
     * 按设计稿坐标以 contentWidth(1544) / stageHeight(794) 为基准比例化。
     * 顺序: A1, A3, B1, B3, C1, C2, C3, A2
     */
    val focusSlotPositions: List<Pair<Dp, Dp>> = listOf(
        browseContentWidth * (24f / 1544f) to focusStageHeight * (24f / 794f),     // A1 左上
        browseContentWidth * (1158f / 1544f) to focusStageHeight * (24f / 794f),   // A3 右上
        browseContentWidth * (24f / 1544f) to focusStageHeight * (278f / 794f),    // B1 左中
        browseContentWidth * (1158f / 1544f) to focusStageHeight * (278f / 794f),  // B3 右中
        browseContentWidth * (24f / 1544f) to focusStageHeight * (532f / 794f),    // C1 左下
        browseContentWidth * (402f / 1544f) to focusStageHeight * (532f / 794f),   // C2 中下
        browseContentWidth * (780f / 1544f) to focusStageHeight * (532f / 794f),   // C3 中右下
        browseContentWidth * (1158f / 1544f) to focusStageHeight * (532f / 794f),  // A2 右下
    )

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
