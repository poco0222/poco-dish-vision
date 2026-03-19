/**
 * @file BrowseBackBehaviorTest.kt
 * @author PopoY
 * @date 2026-03-19
 * @description 验证浏览页返回键（Back behavior）优先关闭详情浮层。
 */
package com.poco.dishvision.feature.menu

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.requestFocus
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
     * 当详情浮层已展开时，第一次返回键必须优先关闭浮层，而不是离开 Browse。
     */
    @Test
    fun back_closes_expanded_detail_panel_before_leaving_browse() {
        composeTestRule.setContent {
            MenuRoute()
        }

        composeTestRule.onNodeWithTag("category-mains").requestFocus()
        composeTestRule.onRoot().performKeyInput {
            keyDown(Key.DirectionRight)
            keyUp(Key.DirectionRight)
        }
        composeTestRule.onNodeWithTag("menu-item-mains-0").performClick()
        composeTestRule.onNodeWithTag("detail-panel").assertExists()

        composeTestRule.onRoot().performKeyInput {
            keyDown(Key.Back)
            keyUp(Key.Back)
        }

        composeTestRule.onNodeWithTag("detail-panel").assertDoesNotExist()
        composeTestRule.onNodeWithTag("browse-screen").assertExists()
    }
}
