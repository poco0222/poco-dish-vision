/**
 * @file AppNavigationSmokeTest.kt
 * @author PopoY
 * @date 2026-03-19
 * @description 验证应用启动后的 Home / Settings / Browse 导航链路。
 */
package com.poco.dishvision

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.requestFocus
import androidx.test.espresso.Espresso.pressBackUnconditionally
import org.junit.Rule
import org.junit.Test

/**
 * 导航冒烟测试，先锁定应用启动后应出现的根标签。
 */
@OptIn(ExperimentalTestApi::class)
class AppNavigationSmokeTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun app_launch_shows_home_destination() {
        composeTestRule.onNodeWithTag("home-screen").assertExists()
    }

    @Test
    fun app_can_navigate_to_settings_and_show_live_status() {
        openSettingsFromHome()
        composeTestRule.onNodeWithTag("settings-screen").assertExists()
        composeTestRule.onNodeWithText("当前数据源").assertExists()
    }

    @Test
    fun back_from_settings_returns_to_browse_instead_of_exiting_app() {
        openSettingsFromHome()
        composeTestRule.onNodeWithTag("settings-screen").assertExists()

        pressBackUnconditionally()

        composeTestRule.onNodeWithTag("browse-screen").assertExists()
    }

    @Test
    fun back_from_browse_returns_to_home_attract_mode() {
        openSettingsFromHome()
        composeTestRule.onNodeWithTag("settings-screen").assertExists()

        pressBackUnconditionally()
        composeTestRule.onNodeWithTag("browse-screen").assertExists()

        pressBackUnconditionally()
        composeTestRule.onNodeWithTag("home-screen").assertExists()
    }

    /**
     * 从 Home 首屏发送一次 Menu key，模拟遥控器进入设置页。
     */
    private fun openSettingsFromHome() {
        composeTestRule.onNodeWithTag("home-screen").requestFocus()
        composeTestRule.onRoot().performKeyInput {
            keyDown(Key.Menu)
            keyUp(Key.Menu)
        }
    }
}
