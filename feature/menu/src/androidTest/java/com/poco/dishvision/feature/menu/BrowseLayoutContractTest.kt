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

import com.poco.dishvision.core.ui.theme.PocoTheme

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
            PocoTheme {
                MenuRoute()
            }
        }

        composeTestRule.onNodeWithText("44道湘味热菜 · 按分类浏览").assertExists()
    }

    @Test
    fun first_screen_shows_all_nine_signature_cards_without_scroll() {
        composeTestRule.setContent {
            PocoTheme {
                MenuRoute()
            }
        }

        composeTestRule.onNodeWithTag("menu-item-hot-stir-fry-8").assertIsDisplayed()
    }

    /**
     * @description 验证浏览页头部标签暴露完整的辅助文案、主标签及描述
     * @author PopoY
     */
    @Test
    fun browse_header_exposes_helper_and_title_tags() {
        composeTestRule.setContent {
            PocoTheme {
                MenuRoute()
            }
        }

        composeTestRule.onNodeWithTag("browse-helper-copy").assertIsDisplayed()
        composeTestRule.onNodeWithTag("browse-main-label").assertIsDisplayed()
        composeTestRule.onNodeWithTag("browse-main-title").assertIsDisplayed()
        composeTestRule.onNodeWithTag("browse-main-description").assertIsDisplayed()
    }

    /**
     * @description 首行前两张卡片必须提供名称与描述节点以支撑契约校验
     * @author PopoY
     */
    @Test
    fun first_row_cards_expose_name_and_description_nodes() {
        composeTestRule.setContent {
            PocoTheme {
                MenuRoute()
            }
        }

        composeTestRule.onNodeWithTag("menu-item-hot-stir-fry-0-name").assertIsDisplayed()
        composeTestRule.onNodeWithTag("menu-item-hot-stir-fry-0-description").assertIsDisplayed()
        composeTestRule.onNodeWithTag("menu-item-hot-stir-fry-1-name").assertIsDisplayed()
        composeTestRule.onNodeWithTag("menu-item-hot-stir-fry-1-description").assertIsDisplayed()
    }
}
