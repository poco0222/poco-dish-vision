/**
 * @file MenuUiState.kt
 * @author PopoY
 * @date 2026-03-19
 * @description 定义浏览页（Browse Screen）的 UI 状态。
 */
package com.poco.dishvision.feature.menu

import com.poco.dishvision.core.model.menu.MenuCategory
import com.poco.dishvision.core.model.menu.MenuItem

/**
 * 浏览页状态，聚焦于当前分类、可见菜品与详情展开状态。
 *
 * @property selectedCategoryId 当前选中的分类 ID。
 * @property categories 当前目录中的全部分类。
 * @property visibleItems 当前分类下可见的菜品列表。
 * @property focusedItemId 当前焦点所在菜品 ID。
 * @property isDetailPanelVisible 是否显示详情浮层。
 */
data class MenuUiState(
    val selectedCategoryId: String,
    val categories: List<MenuCategory>,
    val visibleItems: List<MenuItem>,
    val focusedItemId: String?,
    val isDetailPanelVisible: Boolean,
)
