/**
 * @file BrowseLayoutContractTest.kt
 * @author PopoY
 * @date 2026-03-20
 * @description 锁定 Browse 首屏信息密度与辅助文案，防止菜单页继续偏离设计稿。
 */
package com.poco.dishvision.feature.menu

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test

/**
 * Browse 首屏布局契约测试。
 */
class BrowseLayoutContractTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun helper_copy_matches_full_menu_count() {
        composeTestRule.setContent {
            MenuRoute()
        }

        composeTestRule.onNodeWithText("44道湘味热菜 · 按分类浏览").assertExists()
    }

    @Test
    fun first_screen_shows_all_nine_signature_cards_without_scroll() {
        composeTestRule.setContent {
            MenuRoute()
        }

        composeTestRule.onNodeWithTag("menu-item-hot-stir-fry-8").assertIsDisplayed()
    }
}
