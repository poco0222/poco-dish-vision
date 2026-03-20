/**
 * @file BrowseBackBehaviorTest.kt
 * @author PopoY
 * @date 2026-03-19
 * @description 验证浏览页返回键（Back behavior）优先关闭详情浮层。
 */
package com.poco.dishvision.feature.menu

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.requestFocus
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/**
 * 浏览页返回键测试。
 */
@OptIn(ExperimentalTestApi::class)
class BrowseBackBehaviorTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    /**
     * 当从 FocusScene 返回时，必须恢复到原分类、原滚动位置和原聚焦菜品。
     */
    @Test
    fun back_from_focus_scene_restores_browse_anchor() {
        composeTestRule.setContent {
            MenuRoute()
        }

        composeTestRule.onNodeWithTag("category-home-style").requestFocus()
        composeTestRule.onRoot().performKeyInput {
            keyDown(Key.DirectionRight)
            keyUp(Key.DirectionRight)
        }

        composeTestRule.onNodeWithTag("menu-item-grid").performScrollToIndex(9)
        composeTestRule.onNodeWithTag("menu-item-home-style-9")
            .requestFocus()
            .assertIsFocused()
            .performClick()

        assertEquals(
            1,
            composeTestRule.onAllNodesWithTag("focus-scene").fetchSemanticsNodes().size,
        )

        composeTestRule.onRoot().performKeyInput {
            keyDown(Key.Back)
            keyUp(Key.Back)
        }

        assertEquals(
            0,
            composeTestRule.onAllNodesWithTag("focus-scene").fetchSemanticsNodes().size,
        )
        // FocusScene -> BrowseScene 之间存在 Crossfade 过渡，等待目标卡重新进入语义树并恢复焦点。
        composeTestRule.waitUntilAtLeastOneExists(
            matcher = hasTestTag("menu-item-home-style-9"),
            timeoutMillis = 5_000,
        )
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            val restoredNode = composeTestRule.onAllNodesWithTag("menu-item-home-style-9")
                .fetchSemanticsNodes()
                .singleOrNull()
                ?: return@waitUntil false
            SemanticsProperties.Focused in restoredNode.config &&
                restoredNode.config[SemanticsProperties.Focused]
        }
        composeTestRule.onNodeWithTag("menu-item-home-style-9").assertIsFocused()
        assertEquals(
            1,
            composeTestRule.onAllNodesWithTag("browse-screen").fetchSemanticsNodes().size,
        )
    }

    @Test
    fun tenth_home_style_item_is_reachable_by_vertical_scroll() {
        composeTestRule.setContent {
            MenuRoute()
        }

        composeTestRule.onNodeWithTag("category-home-style").requestFocus()
        composeTestRule.onRoot().performKeyInput {
            keyDown(Key.DirectionRight)
            keyUp(Key.DirectionRight)
        }

        composeTestRule.onNodeWithTag("menu-item-grid").performScrollToIndex(9)
        assertEquals(
            1,
            composeTestRule.onAllNodesWithTag("menu-item-home-style-9").fetchSemanticsNodes().size,
        )
    }
}
