/**
 * @file BrowseScreenFocusTest.kt
 * @author PopoY
 * @date 2026-03-19
 * @description 验证浏览页（Browse Screen）基础焦点路径的 UI 测试。
 */
package com.poco.dishvision.feature.menu

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.requestFocus
import org.junit.Rule
import org.junit.Test

/**
 * 浏览页焦点测试，锁定分类栏到菜品卡片的横向导航行为。
 */
@OptIn(ExperimentalTestApi::class)
class BrowseScreenFocusTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun default_category_is_selected_and_right_moves_focus_to_first_menu_item() {
        composeTestRule.setContent {
            MenuRoute()
        }

        composeTestRule.onNodeWithTag("category-hot-stir-fry")
            .assert(
                SemanticsMatcher.expectValue(
                    SemanticsProperties.StateDescription,
                    "selected",
                ),
            )
            .requestFocus()
            .assertIsFocused()
            .assert(
                SemanticsMatcher.expectValue(
                    SemanticsProperties.StateDescription,
                    "focused",
                ),
            )

        composeTestRule.onRoot().performKeyInput {
            keyDown(Key.DirectionRight)
            keyUp(Key.DirectionRight)
        }
        composeTestRule.onNodeWithTag("menu-item-hot-stir-fry-0").assertIsFocused()
    }
}
