/**
 * @file MenuItemRow.kt
 * @author PopoY
 * @date 2026-03-19
 * @description 提供浏览页菜品横向卡片区。
 */
package com.poco.dishvision.feature.menu

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Text
import com.poco.dishvision.core.model.menu.MenuItem
import com.poco.dishvision.core.ui.components.FocusableMenuCard
import com.poco.dishvision.core.ui.theme.ColorTokens

/**
 * 当前分类下的菜品横向卡片区。
 *
 * @param selectedCategoryId 当前选中分类 ID。
 * @param items 可见菜品列表。
 * @param firstItemFocusRequester 第一个菜品的 FocusRequester（聚焦请求器）。
 * @param modifier 外层 Modifier。
 * @param onItemFocused 菜品获得焦点回调。
 * @param onItemConfirmed 菜品确认回调。
 */
@Composable
fun MenuItemRow(
    selectedCategoryId: String,
    items: List<MenuItem>,
    firstItemFocusRequester: FocusRequester,
    modifier: Modifier = Modifier,
    onItemFocused: (String) -> Unit,
    onItemConfirmed: (String) -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "菜品",
            color = ColorTokens.TextMuted,
            fontWeight = FontWeight.Medium,
        )
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
                .testTag("menu-item-row"),
            horizontalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            itemsIndexed(items = items) { index, item ->
                val cardModifier = if (index == 0) {
                    Modifier
                        .width(280.dp)
                        .focusRequester(firstItemFocusRequester)
                } else {
                    Modifier.width(280.dp)
                }
                FocusableMenuCard(
                    item = item,
                    testTag = "menu-item-$selectedCategoryId-$index",
                    modifier = cardModifier,
                    onFocused = { onItemFocused(item.itemId) },
                    onClick = { onItemConfirmed(item.itemId) },
                )
            }
        }
    }
}
