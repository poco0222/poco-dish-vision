/**
 * @file MenuItemRowTest.kt
 * @author PopoY
 * @date 2026-03-19
 * @description 验证浏览页菜品行使用 lazy list 能力的 UI 测试。
 */
package com.poco.dishvision.feature.menu

import androidx.compose.runtime.remember
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollToIndex
import com.poco.dishvision.core.model.menu.DisplayBadge
import com.poco.dishvision.core.model.menu.MenuItem
import com.poco.dishvision.core.model.menu.PriceInfo
import org.junit.Rule
import org.junit.Test

/**
 * 锁定菜品行的 lazy 横向滚动能力，避免回退到一次性绘制全部卡片的实现。
 */
@OptIn(ExperimentalTestApi::class)
class MenuItemRowTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun menu_item_row_supports_scroll_to_far_item_index() {
        val items = buildMenuItems(count = 8)

        composeTestRule.setContent {
            val firstItemFocusRequester = remember { FocusRequester() }
            MenuItemRow(
                selectedCategoryId = "performance",
                items = items,
                firstItemFocusRequester = firstItemFocusRequester,
                onItemFocused = {},
                onItemConfirmed = {},
            )
        }

        composeTestRule.onNodeWithTag("menu-item-row").performScrollToIndex(7)
        composeTestRule.onNodeWithTag("menu-item-performance-7").assertExists()
    }
}

/**
 * 构造足够长的菜品列表，用于验证横向 lazy 滚动。
 *
 * @param count 生成的菜品数量。
 * @return 供测试使用的菜品列表。
 */
private fun buildMenuItems(count: Int): List<MenuItem> {
    return List(count) { index ->
        MenuItem(
            itemId = "item-$index",
            name = "Menu Item $index",
            description = "Description $index",
            imageUrl = "",
            priceInfo = PriceInfo(
                currencyCode = "CNY",
                amountMinor = 2_000 + index * 100,
                originalAmountMinor = 2_000 + index * 100,
                unitLabel = "份",
            ),
            availabilityWindows = emptyList(),
            displayBadges = listOf(
                DisplayBadge(
                    badgeId = "badge-$index",
                    label = "TEST",
                    styleKey = "test",
                ),
            ),
            tags = listOf("test"),
        )
    }
}
