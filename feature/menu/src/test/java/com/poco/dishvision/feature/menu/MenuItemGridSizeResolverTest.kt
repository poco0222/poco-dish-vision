/**
 * @file MenuItemGridSizeResolverTest.kt
 * @author PopoY
 * @date 2026-03-21
 * @description 验证 Browse 网格卡片尺寸计算，确保 3 列 3 行布局下卡片高度与宽度同步缩放并保留底部留白。
 */
package com.poco.dishvision.feature.menu

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.poco.dishvision.core.ui.theme.ScreenProportions
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * `resolveBrowseGridCardMetrics` 的行为契约测试。
 */
class MenuItemGridSizeResolverTest {

    /**
     * Browse 网格目标列数契约：当前版本为 3 列。
     * @author PopoY
     */
    private val expectedGridColumns = 3f

    /**
     * Browse 首屏目标行数契约：当前版本为 3 行。
     * @author PopoY
     */
    private val expectedVisibleRows = 3f

    @Test
    fun `card width and height should apply the same scale factor`() {
        val proportions = ScreenProportions(
            screenWidth = 1920.dp,
            screenHeight = 1080.dp,
        )
        val metrics = resolveBrowseGridCardMetrics(
            maxWidth = proportions.browseContentWidth,
            maxHeight = proportions.browseGridViewportHeight,
            proportions = proportions,
        )
        // 设计稿对应的基准单卡宽高：当前契约为 3 列 3 行。
        val baseCardWidth = (
            proportions.browseContentWidth -
                proportions.browseGridHorizontalGap * (expectedGridColumns - 1f)
            ) / expectedGridColumns
        val baseCardHeight = (
            proportions.browseGridViewportHeight -
                proportions.browseGridVerticalGap * (expectedVisibleRows - 1f)
            ) / expectedVisibleRows

        assertDpEquals(baseCardWidth * proportions.browseGridCardScale, metrics.cardWidth)
        assertDpEquals(baseCardHeight * proportions.browseGridCardScale, metrics.cardHeight)
    }

    @Test
    fun `card width should never exceed available grid cell width`() {
        val proportions = ScreenProportions(
            screenWidth = 1920.dp,
            screenHeight = 1080.dp,
        )
        val constrainedGridWidth = 900.dp
        val metrics = resolveBrowseGridCardMetrics(
            maxWidth = constrainedGridWidth,
            maxHeight = proportions.browseGridViewportHeight,
            proportions = proportions,
        )
        val availableCardWidth = (
            constrainedGridWidth -
                proportions.browseGridHorizontalGap * (expectedGridColumns - 1f)
            ) / expectedGridColumns

        assertTrue(metrics.cardWidth.value <= availableCardWidth.value + 0.01f)
    }

    @Test
    fun `image height should stay inside card content budget`() {
        val proportions = ScreenProportions(
            screenWidth = 1920.dp,
            screenHeight = 1080.dp,
        )
        val metrics = resolveBrowseGridCardMetrics(
            maxWidth = proportions.browseContentWidth,
            maxHeight = proportions.browseGridViewportHeight,
            proportions = proportions,
        )
        val bodyMinHeight = minOf(
            proportions.browseGridCardBodyMinHeight,
            metrics.cardHeight,
        )
        // 图片高度上限 = 卡片总高 - body 最小高 - image/body 间距。
        val imageHeightBudget = maxOf(
            0.dp,
            metrics.cardHeight - bodyMinHeight - proportions.browseGridCardContentSpacing,
        )

        assertTrue(metrics.imageHeight.value >= 0f)
        assertTrue(metrics.imageHeight.value <= proportions.browseCardImageHeight.value + 0.01f)
        assertTrue(metrics.imageHeight.value <= imageHeightBudget.value + 0.01f)
    }

    /**
     * @description 左侧分类栏收窄后，内容区应获得更多宽度预算。
     * @author PopoY
     */
    @Test
    fun `browse rail should shrink and transfer width budget to content panel`() {
        val proportions = ScreenProportions(
            screenWidth = 1920.dp,
            screenHeight = 1080.dp,
        )

        assertDpEquals(200.dp, proportions.browseRailWidth)
        assertDpEquals(1584.dp, proportions.browseContentWidth)
        assertDpEquals(288.dp, proportions.browseContentStartX)
    }

    /**
     * @description 聚焦态推挤位移应使用动效令牌默认比率（X=0.24, Y=0.18）。
     * @author PopoY
     */
    @Test
    fun `focus push offsets should use token default ratios`() {
        val offsets = resolveBrowseFocusPushOffsets(
            cardWidth = 500.dp,
            cardHeight = 300.dp,
        )

        assertDpEquals(120.dp, offsets.first)
        assertDpEquals(54.dp, offsets.second)
    }

    /**
     * @description 推挤令牌超过上限时应被裁剪，避免邻卡位移越界。
     * @author PopoY
     */
    @Test
    fun `focus push offsets should clamp token bounds`() {
        val offsets = resolveBrowseFocusPushOffsets(
            cardWidth = 500.dp,
            cardHeight = 300.dp,
            motionTokens = BrowseGridMotionTokens(
                pushXRatio = 0.40f,
                pushYRatio = 0.32f,
            ),
        )

        assertDpEquals(130.dp, offsets.first)
        assertDpEquals(60.dp, offsets.second)
    }

    /**
     * 比较 Dp 浮点值，避免因浮点误差导致断言抖动。
     *
     * @param expected 期望值。
     * @param actual 实际值。
     * @param tolerance 容差。
     */
    private fun assertDpEquals(expected: Dp, actual: Dp, tolerance: Float = 0.01f) {
        assertEquals(expected.value.toDouble(), actual.value.toDouble(), tolerance.toDouble())
    }
}
