/**
 * @file BrowseBackBehaviorTest.kt
 * @author PopoY
 * @date 2026-03-19
 * @description 验证浏览页返回键（Back behavior）在 Browse 根层触发上层回调。
 */
package com.poco.dishvision.feature.menu

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.ExperimentalTestApi
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
 * 浏览页返回键测试。
 */
@OptIn(ExperimentalTestApi::class)
class BrowseBackBehaviorTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    /**
     * 在 Browse 根层按下 Back 时，必须触发宿主回调。
     */
    @Test
    fun back_on_browse_root_triggers_parent_callback() {
        var backInvocationCount = 0
        composeTestRule.setContent {
            PocoTheme {
                MenuScreenTestHarness(
                    initialCategoryId = "home-style",
                    onBackFromBrowseRoot = { backInvocationCount += 1 },
                )
            }
        }

        composeTestRule.onNodeWithTag("category-home-style").requestFocus()
        composeTestRule.onRoot().performKeyInput {
            keyDown(Key.DirectionRight)
            keyUp(Key.DirectionRight)
        }
        composeTestRule.onNodeWithTag("menu-item-home-style-0").assertIsFocused()
        composeTestRule.onRoot().performKeyInput {
            keyDown(Key.DirectionDown)
            keyUp(Key.DirectionDown)
            keyDown(Key.DirectionDown)
            keyUp(Key.DirectionDown)
            keyDown(Key.DirectionDown)
            keyUp(Key.DirectionDown)
        }
        composeTestRule.onNodeWithTag("menu-item-home-style-9").assertIsFocused()

        composeTestRule.onRoot().performKeyInput {
            keyDown(Key.Back)
            keyUp(Key.Back)
        }

        composeTestRule.runOnIdle {
            assertEquals(1, backInvocationCount)
        }
        assertEquals(
            1,
            composeTestRule.onAllNodesWithTag("browse-screen").fetchSemanticsNodes().size,
        )
    }

    @Test
    fun tenth_home_style_item_is_reachable_by_vertical_scroll() {
        composeTestRule.setContent {
            PocoTheme {
                MenuScreenTestHarness(
                    initialCategoryId = "home-style",
                )
            }
        }

        composeTestRule.onNodeWithTag("category-home-style").requestFocus()
        composeTestRule.onRoot().performKeyInput {
            keyDown(Key.DirectionRight)
            keyUp(Key.DirectionRight)
        }

        composeTestRule.onNodeWithTag("menu-item-home-style-0").assertIsFocused()
        composeTestRule.onRoot().performKeyInput {
            keyDown(Key.DirectionDown)
            keyUp(Key.DirectionDown)
            keyDown(Key.DirectionDown)
            keyUp(Key.DirectionDown)
            keyDown(Key.DirectionDown)
            keyUp(Key.DirectionDown)
        }
        assertEquals(
            1,
            composeTestRule.onAllNodesWithTag("menu-item-home-style-9").fetchSemanticsNodes().size,
        )
    }
}
