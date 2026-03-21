/**
 * @file MenuScreenTestHarness.kt
 * @author PopoY
 * @date 2026-03-21
 * @description 为菜单页 androidTest 提供稳定的 MenuScreen 测试基座，避免通过 MenuRoute 预览入口引入额外状态噪声。
 */
package com.poco.dishvision.feature.menu

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.poco.dishvision.core.model.menu.MenuCategory

/**
 * 菜单页测试基座。
 *
 * 仅复用 `MenuScreen + reducer` 的交互能力，不回写 viewport anchor，
 * 让 UI 测试聚焦在焦点路径、节点可见性与 Back 行为本身。
 *
 * @param initialCategoryId 初始选中分类 ID。
 * @param onBackFromBrowseRoot Browse 根层 Back 回调。
 * @author PopoY
 */
@Composable
internal fun MenuScreenTestHarness(
    initialCategoryId: String = DEFAULT_BROWSE_CATEGORY_ID,
    onBackFromBrowseRoot: () -> Unit = {},
) {
    val categories = remember { previewMenuCategories() }
    var interactionState by remember(initialCategoryId) {
        mutableStateOf(
            createInitialInteractionState(
                categories = categories,
                initialCategoryId = initialCategoryId,
            ),
        )
    }
    val uiState = remember(categories, interactionState) {
        buildMenuUiState(
            categories = categories,
            interactionState = interactionState,
        )
    }

    MenuScreen(
        uiState = uiState,
        onUserInteraction = {},
        onCategoryFocused = { categoryId ->
            interactionState = handleCategoryRailFocus(
                currentState = interactionState,
                categories = categories,
                categoryId = categoryId,
            )
        },
        onCategoryItemsRequested = { categoryId ->
            interactionState = requestBrowseItemFocus(
                currentState = interactionState,
                categories = categories,
                categoryId = categoryId,
            )
        },
        onBrowseItemFocused = { itemId ->
            interactionState = recordBrowseItemFocus(
                currentState = interactionState,
                categories = categories,
                itemId = itemId,
            )
        },
        onBrowseViewportChanged = {},
        onBackFromBrowseRoot = onBackFromBrowseRoot,
        animationsEnabled = false,
    )
}

/**
 * 生成 androidTest 使用的初始交互状态。
 *
 * @param categories 测试用分类列表。
 * @param initialCategoryId 初始分类 ID。
 * @return 初始 Browse 单事实源状态。
 * @author PopoY
 */
private fun createInitialInteractionState(
    categories: List<MenuCategory>,
    initialCategoryId: String,
): MenuInteractionState {
    val selectedCategory = categories.firstOrNull { category ->
        category.categoryId == initialCategoryId
    } ?: categories.first()
    val initialFocusedItemId = selectedCategory.items.firstOrNull()?.itemId

    return MenuInteractionState(
        selectedCategoryId = selectedCategory.categoryId,
        browseFocusedItemId = initialFocusedItemId,
        categoryBrowseStates = mapOf(
            selectedCategory.categoryId to CategoryBrowseState(
                focusedItemId = initialFocusedItemId,
                rowIndex = 0,
            ),
        ),
    )
}
