/**
 * @file MenuItemGridRowScrollIntentTest.kt
 * @author PopoY
 * @date 2026-03-21
 * @description 验证 Browse 网格按行滚动意图解析契约（每次仅滚动 1 行）。
 */
package com.poco.dishvision.feature.menu

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * `resolveBrowseRowScrollIntent` 行滚动意图契约测试。
 */
class MenuItemGridRowScrollIntentTest {

    /**
     * @description 底行按下方向键时应仅滚动 +1 行（索引 +3）且保持同列。
     * @author PopoY
     */
    @Test
    fun direction_down_at_bottom_row_should_scroll_down_by_exactly_one_row() {
        val intent = resolveBrowseRowScrollIntent(
            direction = BrowseRowScrollDirection.DirectionDown,
            focusedItemIndex = 7,
            visibleRowStart = 0,
            itemCount = 20,
            columns = 3,
            visibleRows = 3,
        )

        assertEquals(BrowseRowScrollAction.ScrollDownOneRow, intent.action)
        assertEquals(3, intent.targetVisibleRowStart)
        assertEquals(10, intent.targetFocusedItemIndex)
        assertEquals(1, intent.targetFocusedItemIndex % 3)
    }

    /**
     * @description 顶行按上方向键时应仅滚动 -1 行（索引 -3）且保持同列。
     * @author PopoY
     */
    @Test
    fun direction_up_at_top_row_should_scroll_up_by_exactly_one_row() {
        val intent = resolveBrowseRowScrollIntent(
            direction = BrowseRowScrollDirection.DirectionUp,
            focusedItemIndex = 4,
            visibleRowStart = 3,
            itemCount = 20,
            columns = 3,
            visibleRows = 3,
        )

        assertEquals(BrowseRowScrollAction.ScrollUpOneRow, intent.action)
        assertEquals(0, intent.targetVisibleRowStart)
        assertEquals(1, intent.targetFocusedItemIndex)
        assertEquals(1, intent.targetFocusedItemIndex % 3)
    }

    /**
     * @description 非顶/底行不应触发行滚动，避免 page scroll 跳跃。
     * @author PopoY
     */
    @Test
    fun middle_row_navigation_should_not_trigger_row_scroll() {
        val intent = resolveBrowseRowScrollIntent(
            direction = BrowseRowScrollDirection.DirectionDown,
            focusedItemIndex = 4,
            visibleRowStart = 0,
            itemCount = 20,
            columns = 3,
            visibleRows = 3,
        )

        assertEquals(BrowseRowScrollAction.NoScroll, intent.action)
        assertEquals(0, intent.targetVisibleRowStart)
        assertEquals(4, intent.targetFocusedItemIndex)
    }

    /**
     * @description 边界场景无可滚动空间时必须返回 NoScroll。
     * @author PopoY
     */
    @Test
    fun boundary_without_next_or_previous_row_should_not_scroll() {
        val noUp = resolveBrowseRowScrollIntent(
            direction = BrowseRowScrollDirection.DirectionUp,
            focusedItemIndex = 1,
            visibleRowStart = 0,
            itemCount = 20,
            columns = 3,
            visibleRows = 3,
        )
        val noDown = resolveBrowseRowScrollIntent(
            direction = BrowseRowScrollDirection.DirectionDown,
            focusedItemIndex = 16,
            visibleRowStart = 9,
            itemCount = 17,
            columns = 3,
            visibleRows = 3,
        )

        assertEquals(BrowseRowScrollAction.NoScroll, noUp.action)
        assertEquals(0, noUp.targetVisibleRowStart)
        assertEquals(1, noUp.targetFocusedItemIndex)
        assertEquals(BrowseRowScrollAction.NoScroll, noDown.action)
        assertEquals(9, noDown.targetVisibleRowStart)
        assertEquals(16, noDown.targetFocusedItemIndex)
    }

    /**
     * @description 非行对齐 visibleRowStart 输入也必须对齐后再执行 ±3 滚动步长。
     * @author PopoY
     */
    @Test
    fun non_aligned_visible_row_start_should_still_scroll_by_three() {
        val intent = resolveBrowseRowScrollIntent(
            direction = BrowseRowScrollDirection.DirectionDown,
            focusedItemIndex = 10,
            visibleRowStart = 4,
            itemCount = 30,
            columns = 3,
            visibleRows = 3,
        )

        assertEquals(BrowseRowScrollAction.ScrollDownOneRow, intent.action)
        assertEquals(6, intent.targetVisibleRowStart)
        assertEquals(13, intent.targetFocusedItemIndex)
    }

    /**
     * @description 运行时若露出部分第 4 行，可视行数应按真实 visible items 推导而不是写死为 3。
     * @author PopoY
     */
    @Test
    fun runtime_visible_row_count_should_expand_when_partial_fourth_row_is_visible() {
        val visibleRowCount = resolveBrowseVisibleRowCount(
            visibleItemIndices = listOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9),
            columns = 3,
            fallbackVisibleRows = 3,
        )

        assertEquals(4, visibleRowCount)
    }

    /**
     * @description 当运行时真实可见 4 行时，只有焦点落在第 4 个可见行才应触发向下逐行滚动。
     * @author PopoY
     */
    @Test
    fun row_scroll_should_wait_for_actual_runtime_bottom_row() {
        val noScroll = resolveBrowseRowScrollIntent(
            direction = BrowseRowScrollDirection.DirectionDown,
            focusedItemIndex = 7,
            visibleRowStart = 0,
            itemCount = 20,
            columns = 3,
            visibleRows = 4,
        )
        val scroll = resolveBrowseRowScrollIntent(
            direction = BrowseRowScrollDirection.DirectionDown,
            focusedItemIndex = 10,
            visibleRowStart = 0,
            itemCount = 20,
            columns = 3,
            visibleRows = 4,
        )

        assertEquals(BrowseRowScrollAction.NoScroll, noScroll.action)
        assertEquals(7, noScroll.targetFocusedItemIndex)
        assertEquals(BrowseRowScrollAction.ScrollDownOneRow, scroll.action)
        assertEquals(3, scroll.targetVisibleRowStart)
        assertEquals(13, scroll.targetFocusedItemIndex)
    }

    /**
     * @description 连续向下滚动时每一步都必须只推进 1 行（索引 +3），不得演变成 page scroll。
     * @author PopoY
     */
    @Test
    fun repeated_down_scroll_should_advance_by_one_row_each_time() {
        val first = resolveBrowseRowScrollIntent(
            direction = BrowseRowScrollDirection.DirectionDown,
            focusedItemIndex = 7,
            visibleRowStart = 0,
            itemCount = 30,
            columns = 3,
            visibleRows = 3,
        )
        val second = resolveBrowseRowScrollIntent(
            direction = BrowseRowScrollDirection.DirectionDown,
            focusedItemIndex = first.targetFocusedItemIndex,
            visibleRowStart = first.targetVisibleRowStart,
            itemCount = 30,
            columns = 3,
            visibleRows = 3,
        )

        assertEquals(BrowseRowScrollAction.ScrollDownOneRow, first.action)
        assertEquals(3, first.targetVisibleRowStart)
        assertEquals(10, first.targetFocusedItemIndex)
        assertEquals(BrowseRowScrollAction.ScrollDownOneRow, second.action)
        assertEquals(6, second.targetVisibleRowStart)
        assertEquals(13, second.targetFocusedItemIndex)
    }
}
