/**
 * @file MenuItemGridFocusMotionTest.kt
 * @author PopoY
 * @date 2026-03-21
 * @description 验证 Browse 网格“单卡放大 + 局部推挤”动画参数解析契约。
 */
package com.poco.dishvision.feature.menu

import com.poco.dishvision.core.model.menu.DisplayBadge
import com.poco.dishvision.core.model.menu.MenuItem
import com.poco.dishvision.core.model.menu.PriceInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * `resolveBrowseCardFocusMotion` / `resolveBrowseFocusChipLabels` 行为契约测试。
 */
class MenuItemGridFocusMotionTest {

    /**
     * @description 左上角焦点卡应只向右下扩张（pivot=0,0）并开启详情区。
     * @author PopoY
     */
    @Test
    fun top_left_focused_card_should_anchor_to_left_top_and_expand_details() {
        val motion = resolveBrowseCardFocusMotion(
            itemIndex = 0,
            focusedItemIndex = 0,
            visibleRowStart = 0,
            columns = 3,
            visibleRows = 3,
        )

        assertEquals(0f, motion.pivotX, 0f)
        assertEquals(0f, motion.pivotY, 0f)
        assertEquals(0f, motion.offsetXRatio, 0f)
        assertEquals(0f, motion.offsetYRatio, 0f)
        assertEquals(1.24f, motion.scale, 0.0001f)
        assertTrue(motion.showExpandedDetails)
        assertTrue(motion.zIndex > 0f)
    }

    /**
     * @description 左上角获焦后，右邻卡应向右推挤并缩小，避免与主卡重叠。
     * @author PopoY
     */
    @Test
    fun right_neighbor_should_push_right_and_shrink_when_top_left_is_focused() {
        val motion = resolveBrowseCardFocusMotion(
            itemIndex = 1,
            focusedItemIndex = 0,
            visibleRowStart = 0,
            columns = 3,
            visibleRows = 3,
        )

        assertTrue(motion.offsetXRatio > 0f)
        assertEquals(0f, motion.offsetYRatio, 0f)
        assertEquals(0.82f, motion.scale, 0.0001f)
        assertEquals(0.72f, motion.alpha, 0.0001f)
        assertFalse(motion.showExpandedDetails)
    }

    /**
     * @description 左边界卡在焦点位于右侧时，不应继续向左推挤侵占分类导轨区域。
     * @author PopoY
     */
    @Test
    fun left_edge_card_should_not_push_left_when_focus_is_on_right() {
        val motion = resolveBrowseCardFocusMotion(
            itemIndex = 0,
            focusedItemIndex = 1,
            visibleRowStart = 0,
            columns = 3,
            visibleRows = 3,
        )

        assertEquals(0f, motion.offsetXRatio, 0f)
    }

    /**
     * @description 顶行卡在焦点位于下方时，不应继续向上推挤侵占顶部说明文案区域。
     * @author PopoY
     */
    @Test
    fun top_row_card_should_not_push_up_when_focus_is_below() {
        val motion = resolveBrowseCardFocusMotion(
            itemIndex = 0,
            focusedItemIndex = 3,
            visibleRowStart = 0,
            columns = 3,
            visibleRows = 3,
        )

        assertEquals(0f, motion.offsetYRatio, 0f)
    }

    /**
     * @description 中左焦点卡（第二行第一列）应具备“上+右+下”扩张锚点（pivotX=0, pivotY=0.5）。
     * @author PopoY
     */
    @Test
    fun middle_left_focused_card_should_anchor_to_left_center() {
        val motion = resolveBrowseCardFocusMotion(
            itemIndex = 3,
            focusedItemIndex = 3,
            visibleRowStart = 0,
            columns = 3,
            visibleRows = 3,
        )

        assertEquals(0f, motion.pivotX, 0f)
        assertEquals(0.5f, motion.pivotY, 0f)
        assertEquals(1.24f, motion.scale, 0.0001f)
    }

    /**
     * @description 中段视口的中列焦点卡应以中心为 pivot，并保持最高层级。
     * @author PopoY
     */
    @Test
    fun middle_window_center_focused_card_should_anchor_to_center() {
        val motion = resolveBrowseCardFocusMotion(
            itemIndex = 13,
            focusedItemIndex = 13,
            visibleRowStart = 9,
            columns = 3,
            visibleRows = 3,
        )

        assertEquals(0.5f, motion.pivotX, 0.0001f)
        assertEquals(0.5f, motion.pivotY, 0.0001f)
        assertEquals(1.24f, motion.scale, 0.0001f)
        assertEquals(8f, motion.zIndex, 0.0001f)
    }

    /**
     * @description 中段视口里，中心焦点左邻卡应向左推挤并处于近邻层级。
     * @author PopoY
     */
    @Test
    fun middle_window_left_neighbor_should_keep_near_neighbor_pose_for_center_focus() {
        val motion = resolveBrowseCardFocusMotion(
            itemIndex = 12,
            focusedItemIndex = 13,
            visibleRowStart = 9,
            columns = 3,
            visibleRows = 3,
        )

        assertEquals(0f, motion.offsetXRatio, 0.0001f)
        assertEquals(0f, motion.offsetYRatio, 0.0001f)
        assertEquals(0.82f, motion.scale, 0.0001f)
        assertEquals(0.72f, motion.alpha, 0.0001f)
        assertEquals(2f, motion.zIndex, 0.0001f)
    }

    /**
     * @description 非行对齐 visibleRowStart 输入应先对齐，避免 pivot 锚点错误。
     * @author PopoY
     */
    @Test
    fun visible_row_start_should_align_by_row_before_computing_pivot() {
        val motion = resolveBrowseCardFocusMotion(
            itemIndex = 3,
            focusedItemIndex = 3,
            visibleRowStart = 2,
            columns = 3,
            visibleRows = 3,
        )

        assertEquals(0.5f, motion.pivotY, 0.0001f)
    }

    /**
     * @description 窗外卡片不参与强动画，仅执行轻量衰减（缩放/透明度）。
     * @author PopoY
     */
    @Test
    fun outside_viewport_should_only_apply_light_decay() {
        val motion = resolveBrowseCardFocusMotion(
            itemIndex = 12,
            focusedItemIndex = 4,
            visibleRowStart = 0,
            columns = 3,
            visibleRows = 3,
        )

        assertEquals(0.94f, motion.scale, 0.0001f)
        assertEquals(0.88f, motion.alpha, 0.0001f)
        assertEquals(0f, motion.offsetXRatio, 0.0001f)
        assertEquals(0f, motion.offsetYRatio, 0.0001f)
        assertEquals(0f, motion.zIndex, 0.0001f)
        assertFalse(motion.showExpandedDetails)
    }

    /**
     * @description 右列卡片在焦点位于左侧时，不应继续向右推挤越过网格边界。
     * @author PopoY
     */
    @Test
    fun right_edge_card_should_not_push_right_when_focus_is_on_left() {
        val motion = resolveBrowseCardFocusMotion(
            itemIndex = 2,
            focusedItemIndex = 1,
            visibleRowStart = 0,
            columns = 3,
            visibleRows = 3,
        )

        assertEquals(0f, motion.offsetXRatio, 0.0001f)
    }

    /**
     * @description 底行卡片在焦点位于上方时，不应继续向下推挤越过底边界。
     * @author PopoY
     */
    @Test
    fun bottom_row_card_should_not_push_down_when_focus_is_above() {
        val motion = resolveBrowseCardFocusMotion(
            itemIndex = 8,
            focusedItemIndex = 5,
            visibleRowStart = 0,
            columns = 3,
            visibleRows = 3,
        )

        assertEquals(0f, motion.offsetYRatio, 0.0001f)
    }

    /**
     * @description 末段视口的右下焦点卡应以右下角为 pivot，避免继续向外扩张。
     * @author PopoY
     */
    @Test
    fun ending_window_bottom_right_focus_should_anchor_to_bottom_right() {
        val motion = resolveBrowseCardFocusMotion(
            itemIndex = 17,
            focusedItemIndex = 17,
            visibleRowStart = 9,
            columns = 3,
            visibleRows = 3,
        )

        assertEquals(1f, motion.pivotX, 0.0001f)
        assertEquals(1f, motion.pivotY, 0.0001f)
        assertEquals(1.24f, motion.scale, 0.0001f)
        assertEquals(8f, motion.zIndex, 0.0001f)
    }

    /**
     * @description 聚焦卡放大倍率应允许由外部参数覆盖，便于按设计稿目标体量对齐。
     * @author PopoY
     */
    @Test
    fun focused_card_scale_should_use_explicit_scale_parameter() {
        val targetFocusedScale = 1.41f
        val motion = resolveBrowseCardFocusMotion(
            itemIndex = 0,
            focusedItemIndex = 0,
            visibleRowStart = 0,
            columns = 3,
            visibleRows = 3,
            focusedCardScale = targetFocusedScale,
        )

        assertEquals(1.28f, motion.scale, 0.0001f)
    }

    /**
     * @description 默认聚焦倍率应来自动效令牌并保持在约束区间内。
     * @author PopoY
     */
    @Test
    fun focused_scale_resolver_should_use_motion_token_default() {
        val scale = resolveBrowseFocusedCardScale()
        assertEquals(1.24f, scale, 0.0001f)
    }

    /**
     * @description 聚焦倍率解析应对外部令牌应用边界约束。
     * @author PopoY
     */
    @Test
    fun focused_scale_resolver_should_clamp_token_bounds() {
        val upperClamped = resolveBrowseFocusedCardScale(
            motionTokens = BrowseGridMotionTokens(
                focusedScale = 1.48f,
            ),
        )
        val lowerClamped = resolveBrowseFocusedCardScale(
            motionTokens = BrowseGridMotionTokens(
                focusedScale = 1.02f,
            ),
        )

        assertEquals(1.28f, upperClamped, 0.0001f)
        assertEquals(1.12f, lowerClamped, 0.0001f)
    }

    /**
     * @description 透明度令牌低于下限时应自动抬升，避免过暗导致信息不可读。
     * @author PopoY
     */
    @Test
    fun alpha_token_should_be_clamped_to_minimum_floor() {
        val motion = resolveBrowseCardFocusMotion(
            itemIndex = 1,
            focusedItemIndex = 0,
            visibleRowStart = 0,
            columns = 3,
            visibleRows = 3,
            motionTokens = BrowseGridMotionTokens(
                unfocusedNearAlpha = 0.10f,
            ),
        )

        assertEquals(0.70f, motion.alpha, 0.0001f)
    }

    /**
     * @description 聚焦详情 chip 应优先收敛 badge + tags，并去重截断到 3 个。
     * @author PopoY
     */
    @Test
    fun focus_chip_labels_should_merge_and_limit_to_three_items() {
        val item = MenuItem(
            itemId = "focus-test",
            name = "茶油炒鸡",
            description = "锅气足，越吃越香。",
            imageUrl = "",
            priceInfo = PriceInfo(
                currencyCode = "CNY",
                amountMinor = 8800,
                originalAmountMinor = 8800,
                unitLabel = "份",
            ),
            availabilityWindows = emptyList(),
            displayBadges = listOf(
                DisplayBadge(
                    badgeId = "badge-1",
                    label = "招牌",
                    styleKey = "brand",
                ),
                DisplayBadge(
                    badgeId = "badge-2",
                    label = "现炒",
                    styleKey = "brand",
                ),
            ),
            tags = listOf("featured", "现炒", "湘味"),
        )

        val labels = resolveBrowseFocusChipLabels(item)

        assertEquals(3, labels.size)
        assertEquals("招牌", labels[0])
        assertEquals("现炒", labels[1])
        assertEquals("featured", labels[2])
    }
}
