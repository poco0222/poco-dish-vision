/**
 * @file HomeScreenTest.kt
 * @author PopoY
 * @date 2026-03-18
 * @description 验证首页 attract mode（吸引模式）基础结构的 UI 测试。
 */
package com.poco.dishvision.feature.home

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test

/**
 * 首页冒烟测试，锁定中下视觉重心所需的三个关键节点。
 */
class HomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun home_screen_renders_featured_section() {
        composeTestRule.setContent {
            HomeRoute()
        }

        composeTestRule.onNodeWithText("本店推荐").assertExists()
        composeTestRule.onNodeWithTag("attract-carousel").assertExists()
        composeTestRule.onNodeWithTag("home-lower-hero-zone").assertExists()
    }
}
