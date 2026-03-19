/**
 * @file CategoryRail.kt
 * @author PopoY
 * @date 2026-03-19
 * @description 提供浏览页左侧分类导轨（Category Rail）。
 */
package com.poco.dishvision.feature.menu

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Text
import com.poco.dishvision.core.model.menu.MenuCategory
import com.poco.dishvision.core.ui.components.GlassSurface
import com.poco.dishvision.core.ui.theme.ColorTokens

/**
 * 浏览页左侧分类导轨。
 *
 * @param categories 分类列表。
 * @param selectedCategoryId 当前选中分类。
 * @param modifier 外层 Modifier。
 * @param onCategoryFocused 分类获得焦点时的回调。
 * @param onMoveFocusToItems 方向键向右时将焦点切到菜品区的回调。
 */
@Composable
fun CategoryRail(
    categories: List<MenuCategory>,
    selectedCategoryId: String,
    modifier: Modifier = Modifier,
    onCategoryFocused: (String) -> Unit,
    onMoveFocusToItems: (String) -> Unit,
) {
    Column(
        modifier = modifier.width(248.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            text = "分类",
            color = ColorTokens.TextMuted,
            fontWeight = FontWeight.Medium,
        )
        categories.forEach { category ->
            CategoryRailItem(
                category = category,
                isSelected = category.categoryId == selectedCategoryId,
                onFocused = { onCategoryFocused(category.categoryId) },
                onMoveFocusToItems = { onMoveFocusToItems(category.categoryId) },
            )
        }
    }
}

/**
 * 分类导轨单项。
 *
 * @param category 分类模型。
 * @param isSelected 是否为当前选中分类。
 * @param onFocused 获得焦点时回调。
 * @param onMoveFocusToItems 向右导航时回调。
 */
@Composable
private fun CategoryRailItem(
    category: MenuCategory,
    isSelected: Boolean,
    onFocused: () -> Unit,
    onMoveFocusToItems: () -> Unit,
) {
    var isFocused by remember { mutableStateOf(false) }
    val borderColor = if (isFocused || isSelected) {
        ColorTokens.GlassBorderFocused
    } else {
        ColorTokens.GlassBorderSubtle
    }
    val backgroundColor = if (isFocused || isSelected) {
        ColorTokens.CategorySelectedSurface
    } else {
        ColorTokens.GlassSurface
    }

    GlassSurface(
        modifier = Modifier
            .fillMaxWidth()
            .onFocusChanged { focusState ->
                val focusedNow = focusState.isFocused
                isFocused = focusedNow
                if (focusedNow) {
                    onFocused()
                }
            }
            .onPreviewKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown && keyEvent.key == Key.DirectionRight) {
                    onMoveFocusToItems()
                    true
                } else {
                    false
                }
            }
            .focusable()
            .testTag("category-${category.categoryId}"),
        containerColor = backgroundColor,
        borderColor = borderColor,
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 18.dp),
        contentSpacing = 6.dp,
    ) {
        Text(
            text = category.displayName,
            color = ColorTokens.TextPrimary,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = category.subtitle,
            color = ColorTokens.TextSecondary,
        )
    }
}
