/**
 * @file AppNavigationSmokeTest.kt
 * @author PopoY
 * @date 2026-03-18
 * @description 验证应用启动后会进入 Home destination（首页目标）的 smoke test（冒烟测试）。
 */
package com.poco.dishvision

import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Rule
import org.junit.Test

/**
 * 导航冒烟测试，先锁定应用启动后应出现的根标签。
 */
class AppNavigationSmokeTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun app_launch_shows_home_destination() {
        composeTestRule.onNodeWithTag("home-screen").assertExists()
    }

    @Test
    fun app_can_navigate_to_settings_and_show_live_status() {
        composeTestRule.onNodeWithTag("open-settings").performClick()
        composeTestRule.onNodeWithTag("settings-screen").assertExists()
        composeTestRule.onNodeWithText("当前数据源").assertExists()
    }
}
