/**
 * @file CategoryRail.kt
 * @author PopoY
 * @date 2026-03-19
 * @description 提供浏览页左侧分类导轨（Category Rail），匹配设计稿"湘味分类"布局。
 */
package com.poco.dishvision.feature.menu

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Text
import com.poco.dishvision.core.model.menu.MenuCategory
import com.poco.dishvision.core.ui.components.VisualFocusSurface
import com.poco.dishvision.core.ui.theme.ColorTokens
import com.poco.dishvision.core.ui.theme.Dimens
import com.poco.dishvision.core.ui.theme.LocalScreenProportions

/**
 * 浏览页左侧分类导轨。
 *
 * 设计稿：x=48, y=190, width=240, gap=10。
 * 分类项仅显示名称（无副标题），选中态使用红色强调底色 + 金色边框。
 *
 * @param categories 分类列表。
 * @param selectedCategoryId 当前选中分类 ID。
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
    val proportions = LocalScreenProportions.current

    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        // 导轨标签（设计稿 y=156，与按钮列表有独立间距）
        Text(
            text = "分类",
            color = ColorTokens.Accent,
            fontWeight = FontWeight.Medium,
            fontSize = proportions.scaledSp(20f),
        )

        // 标签到按钮列表的间距，设计稿 y(190) - y(156) - 字号行高 ≈ 14px
        Spacer(modifier = Modifier.height(proportions.browseRailLabelToItemsGap))

        // 导轨按钮列表（内部间距 10dp，设计稿 gap=10）
        Column(
            verticalArrangement = Arrangement.spacedBy(proportions.browseRailItemSpacing),
        ) {
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
}

/**
 * 分类导轨单项。
 *
 * 设计稿：cornerRadius=20, padding=[14,18]。
 * 选中态：fill=#B63A27, border=#C9A45E, text=#F7F1E8。
 * 未选中：fill=$--surface-card (#CC2A1712), border=#5B3A2B, text=#D8C8B3。
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
    val proportions = LocalScreenProportions.current
    var isFocused by remember { mutableStateOf(false) }

    // 选中态保留设计稿底色，真实聚焦时再叠加 visual focus 机制。
    val restingBorderColor = if (isSelected) {
        ColorTokens.GlassBorderFocused
    } else {
        ColorTokens.GlassBorderSubtle
    }
    val restingBackgroundColor = if (isSelected) {
        ColorTokens.CategorySelectedSurface
    } else {
        ColorTokens.GlassSurface
    }
    // 选中态文本使用主白色，未选中使用次级灰色
    val textColor = if (isFocused || isSelected) {
        ColorTokens.TextPrimary
    } else {
        ColorTokens.TextSecondary
    }

    VisualFocusSurface(
        isFocused = isFocused,
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
            .semantics {
                stateDescription = when {
                    isFocused -> "focused"
                    isSelected -> "selected"
                    else -> "unselected"
                }
            }
            .testTag("category-${category.categoryId}"),
        containerColor = restingBackgroundColor,
        focusedContainerColor = ColorTokens.CategorySelectedSurface,
        borderColor = restingBorderColor,
        focusedBorderColor = ColorTokens.GlassBorderFocused,
        borderWidth = 1.dp,
        focusedBorderWidth = 1.5.dp,
        shape = RoundedCornerShape(Dimens.BrowseRailItemCorner),
        glowCornerRadius = Dimens.BrowseRailItemCorner,
        contentPadding = PaddingValues(
            horizontal = proportions.browseRailItemHorizontalPadding,
            vertical = proportions.browseRailItemVerticalPadding,
        ),
        focusedScale = 1.05f,
    ) {
        Text(
            text = category.displayName,
            color = textColor,
            fontWeight = FontWeight.SemiBold,
            fontSize = proportions.scaledSp(24f),
        )
    }
}
