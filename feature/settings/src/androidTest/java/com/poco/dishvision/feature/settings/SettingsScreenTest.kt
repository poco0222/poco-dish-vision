/**
 * @file SettingsScreenTest.kt
 * @author PopoY
 * @date 2026-03-19
 * @description 验证设置页（Settings Screen）基础信息展示的 UI 测试。
 */
package com.poco.dishvision.feature.settings

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test

/**
 * 设置页 smoke test，先锁定当前数据源展示。
 */
class SettingsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun settings_screen_shows_local_data_source_mode() {
        composeTestRule.setContent {
            SettingsRoute()
        }

        composeTestRule.onNodeWithTag("settings-screen").assertExists()
        composeTestRule.onNodeWithText("当前数据源").assertExists()
        composeTestRule.onNodeWithText("Local").assertExists()
    }
}
