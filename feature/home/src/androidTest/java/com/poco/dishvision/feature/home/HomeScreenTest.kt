/**
 * @file HomeScreenTest.kt
 * @author PopoY
 * @date 2026-03-19
 * @description 验证首页 attract mode（吸引模式）交互与结构的 UI 测试。
 */
package com.poco.dishvision.feature.home

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.requestFocus
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/**
 * 首页 UI 测试，锁定首屏结构、轮播切换与 Browse 跳转入口。
 */
@OptIn(ExperimentalTestApi::class)
class HomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun home_screen_renders_brand_chips_five_cards_and_first_hero() {
        composeTestRule.setContent {
            HomeRoute()
        }

        composeTestRule.onNodeWithText("新华饭店").assertExists()
        composeTestRule.onNodeWithText("热锅现炒").assertExists()
        composeTestRule.onNodeWithText("剁椒头牌").assertExists()
        composeTestRule.onNodeWithText("腊味土菜").assertExists()
        composeTestRule.onNodeWithTag("home-showcase-card-0").assertExists()
        composeTestRule.onNodeWithTag("home-showcase-card-4").assertExists()
        composeTestRule.onNodeWithTag("home-hero-primary-title").assertTextEquals("茶油炒鸡")
        composeTestRule.onNodeWithTag("home-hero-image").assertExists()
    }

    @Test
    fun right_key_moves_to_next_showcase_and_updates_selected_card() {
        composeTestRule.setContent {
            HomeRoute()
        }

        composeTestRule.onNodeWithTag("home-screen").requestFocus()
        composeTestRule.onRoot().performKeyInput {
            keyDown(Key.DirectionRight)
            keyUp(Key.DirectionRight)
        }

        composeTestRule.onNodeWithTag("home-hero-primary-title").assertTextEquals("青花椒鱼片")
        composeTestRule.onNodeWithTag("home-showcase-card-1").assert(
            hasStateDescription("selected"),
        )
    }

    @Test
    fun left_key_from_first_showcase_wraps_to_last_card() {
        composeTestRule.setContent {
            HomeRoute()
        }

        composeTestRule.onNodeWithTag("home-screen").requestFocus()
        composeTestRule.onRoot().performKeyInput {
            keyDown(Key.DirectionLeft)
            keyUp(Key.DirectionLeft)
        }

        composeTestRule.onNodeWithTag("home-hero-primary-title").assertTextEquals("水煮活鱼")
        composeTestRule.onNodeWithTag("home-showcase-card-4").assert(
            hasStateDescription("selected"),
        )
    }

    @Test
    fun up_down_and_center_keys_request_browse() {
        var browseRequestCount = 0

        composeTestRule.setContent {
            HomeRoute(
                onBrowseRequested = {
                    browseRequestCount += 1
                },
            )
        }

        composeTestRule.onNodeWithTag("home-screen").requestFocus()
        composeTestRule.onRoot().performKeyInput {
            keyDown(Key.DirectionUp)
            keyUp(Key.DirectionUp)
            keyDown(Key.DirectionDown)
            keyUp(Key.DirectionDown)
            keyDown(Key.DirectionCenter)
            keyUp(Key.DirectionCenter)
        }

        composeTestRule.runOnIdle {
            assertEquals(3, browseRequestCount)
        }
    }
}

/**
 * 构造 `stateDescription`（状态描述）断言。
 */
private fun hasStateDescription(expected: String): SemanticsMatcher {
    return SemanticsMatcher.expectValue(
        SemanticsProperties.StateDescription,
        expected,
    )
}
