/**
 * @file MenuGridReflowUiTest.kt
 * @author PopoY
 * @date 2026-03-21
 * @description 验证菜单页 in-grid reflow（网格内重排）在单网格运行时下的关键 UI 回归路径。
 */
package com.poco.dishvision.feature.menu

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.requestFocus
import com.poco.dishvision.core.ui.theme.PocoTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/**
 * in-grid reflow UI 测试。
 */
@OptIn(ExperimentalTestApi::class)
class MenuGridReflowUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    /**
     * @description 任何 Browse 交互路径都不应再进入历史 focus-scene 运行时节点。
     * @author PopoY
     */
    @Test
    fun browse_interactions_should_never_render_focus_scene_node() {
        setBrowseContent()

        focusCategoryAndEnterGrid(categoryId = "home-style")
        sendKey(Key.DirectionDown)
        sendKey(Key.DirectionDown)
        sendKey(Key.DirectionRight)

        assertEquals(
            0,
            composeTestRule.onAllNodesWithTag("focus-scene").fetchSemanticsNodes().size,
        )
    }

    /**
     * @description 超过 9 张菜品时，纵向导航应按单行步长推进，且第 10 张卡可通过 D-pad 触达。
     * @author PopoY
     */
    @Test
    fun dpad_vertical_navigation_should_reach_tenth_item_with_one_row_scroll_steps() {
        setBrowseContent()

        focusCategoryAndEnterGrid(categoryId = "home-style")
        composeTestRule.onNodeWithTag("menu-item-home-style-0").assertIsFocused()

        sendKey(Key.DirectionRight)
        composeTestRule.onNodeWithTag("menu-item-home-style-1").assertIsFocused()

        sendKey(Key.DirectionDown)
        composeTestRule.onNodeWithTag("menu-item-home-style-4").assertIsFocused()

        sendKey(Key.DirectionDown)
        composeTestRule.onNodeWithTag("menu-item-home-style-7").assertIsFocused()

        sendKey(Key.DirectionDown)
        composeTestRule.onNodeWithTag("menu-item-home-style-9").assertIsFocused()
    }

    /**
     * @description 聚焦详情区只能出现在当前焦点卡，焦点切换后旧卡详情必须收拢。
     * @author PopoY
     */
    @Test
    fun expanded_details_should_only_exist_on_the_currently_focused_card() {
        setBrowseContent()

        focusCategoryAndEnterGrid(categoryId = "hot-stir-fry")
        composeTestRule.onNodeWithTag(
            "menu-item-hot-stir-fry-0-price",
            useUnmergedTree = true,
        ).assertIsDisplayed()
        assertEquals(
            0,
            composeTestRule.onAllNodesWithTag(
                "menu-item-hot-stir-fry-1-price",
                useUnmergedTree = true,
            ).fetchSemanticsNodes().size,
        )

        sendKey(Key.DirectionRight)

        composeTestRule.onNodeWithTag("menu-item-hot-stir-fry-1").assertIsFocused()
        assertEquals(
            0,
            composeTestRule.onAllNodesWithTag(
                "menu-item-hot-stir-fry-0-price",
                useUnmergedTree = true,
            ).fetchSemanticsNodes().size,
        )
        composeTestRule.onNodeWithTag(
            "menu-item-hot-stir-fry-1-price",
            useUnmergedTree = true,
        ).assertIsDisplayed()
    }

    /**
     * @description 网格重排过程中顶部说明文案与左侧分类导轨应持续可见，不能被内容区侵占。
     * @author PopoY
     */
    @Test
    fun header_copy_and_category_rail_should_remain_visible_during_grid_reflow() {
        setBrowseContent()

        focusCategoryAndEnterGrid(categoryId = "home-style")
        sendKey(Key.DirectionRight)
        sendKey(Key.DirectionDown)
        sendKey(Key.DirectionDown)
        sendKey(Key.DirectionDown)

        composeTestRule.onNodeWithTag("browse-helper-copy").assertIsDisplayed()
        composeTestRule.onNodeWithTag("browse-main-title").assertIsDisplayed()
        composeTestRule.onNodeWithTag("category-home-style").assertIsDisplayed()
    }

    /**
     * 装载 Browse 页面测试内容。
     *
     * @author PopoY
     */
    private fun setBrowseContent() {
        composeTestRule.setContent {
            PocoTheme {
                MenuScreenTestHarness()
            }
        }
    }

    /**
     * 先聚焦分类，再通过右方向键进入对应分类的网格首卡。
     *
     * @param categoryId 目标分类 ID。
     * @author PopoY
     */
    private fun focusCategoryAndEnterGrid(categoryId: String) {
        composeTestRule.onNodeWithTag("category-$categoryId")
            .requestFocus()
            .assertIsFocused()
        sendKey(Key.DirectionRight)
    }

    /**
     * 统一发送遥控器按键并等待 Compose 空闲。
     *
     * @param key 需要发送的 D-pad / remote key。
     * @author PopoY
     */
    private fun sendKey(key: Key) {
        composeTestRule.onRoot().performKeyInput {
            keyDown(key)
            keyUp(key)
        }
        composeTestRule.waitForIdle()
    }
}
