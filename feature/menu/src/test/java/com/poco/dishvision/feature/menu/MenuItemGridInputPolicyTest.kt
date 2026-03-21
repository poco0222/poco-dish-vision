/**
 * @file MenuItemGridInputPolicyTest.kt
 * @author PopoY
 * @date 2026-03-21
 * @description 验证菜单页纯 TV 遥控器输入策略（D-pad only）契约。
 */
package com.poco.dishvision.feature.menu

import androidx.compose.ui.input.key.Key
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * `MenuItemGrid` 输入策略契约测试。
 */
class MenuItemGridInputPolicyTest {

    /**
     * @description 纯 TV 遥控器模式下必须禁用用户手势滚动（鼠标滚轮/触摸滑动）。
     * @author PopoY
     */
    @Test
    fun browse_grid_user_scroll_is_disabled_for_tv_remote_only() {
        assertFalse(BROWSE_GRID_USER_SCROLL_ENABLED)
    }

    /**
     * @description 遥控器确认键集合应包含 Center 与 Enter 语义键。
     * @author PopoY
     */
    @Test
    fun remote_confirm_key_detector_accepts_center_and_enter_keys() {
        assertTrue(Key.DirectionCenter.isRemoteConfirmKey())
        assertTrue(Key.Enter.isRemoteConfirmKey())
        assertTrue(Key.NumPadEnter.isRemoteConfirmKey())
    }

    /**
     * @description 导航方向键不应被误识别为确认键。
     * @author PopoY
     */
    @Test
    fun remote_confirm_key_detector_rejects_directional_keys() {
        assertFalse(Key.DirectionUp.isRemoteConfirmKey())
        assertFalse(Key.DirectionDown.isRemoteConfirmKey())
        assertFalse(Key.DirectionLeft.isRemoteConfirmKey())
        assertFalse(Key.DirectionRight.isRemoteConfirmKey())
    }

    /**
     * @description 上下方向键应被解析为逐行滚动方向，供网格拦截底/顶行滚动时复用。
     * @author PopoY
     */
    @Test
    fun vertical_direction_keys_map_to_row_scroll_direction() {
        assertTrue(Key.DirectionUp.toBrowseRowScrollDirectionOrNull() == BrowseRowScrollDirection.DirectionUp)
        assertTrue(Key.DirectionDown.toBrowseRowScrollDirectionOrNull() == BrowseRowScrollDirection.DirectionDown)
    }

    /**
     * @description 非纵向导航键不应触发逐行滚动语义，避免误消费左右导航与确认输入。
     * @author PopoY
     */
    @Test
    fun non_vertical_keys_do_not_map_to_row_scroll_direction() {
        assertTrue(Key.DirectionLeft.toBrowseRowScrollDirectionOrNull() == null)
        assertTrue(Key.DirectionRight.toBrowseRowScrollDirectionOrNull() == null)
        assertTrue(Key.DirectionCenter.toBrowseRowScrollDirectionOrNull() == null)
    }
}
