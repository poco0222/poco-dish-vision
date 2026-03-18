/**
 * @file AppNavigationSmokeTest.kt
 * @author PopoY
 * @date 2026-03-18
 * @description 验证应用启动后会进入 Home destination（首页目标）的 smoke test（冒烟测试）。
 */
package com.poco.dishvision

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
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
}
