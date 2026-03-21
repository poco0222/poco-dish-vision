/**
 * @file MenuUiState.kt
 * @author PopoY
 * @date 2026-03-19
 * @description 定义菜单页 Browse 单事实源（single source of truth）的 UI 状态。
 */
package com.poco.dishvision.feature.menu

import com.poco.dishvision.core.model.menu.MenuCategory
import com.poco.dishvision.core.model.menu.MenuItem

/** Browse 网格固定 3 列。 */
internal const val BROWSE_GRID_COLUMN_COUNT = 3

/**
 * 菜单页状态，收敛到 Browse 单事实源。
 *
 * @property selectedCategoryId 当前选中的分类 ID。
 * @property categories 当前目录中的全部分类。
 * @property browseSceneState BrowseScene UI 状态。
 */
data class MenuUiState(
    val selectedCategoryId: String,
    val categories: List<MenuCategory>,
    val browseSceneState: BrowseSceneState,
)

/**
 * BrowseScene UI 状态。
 *
 * @property visibleItems 当前分类下可见的菜品列表。
 * @property focusedItemId 当前 BrowseScene 锚定的菜品 ID。
 * @property viewportRequest 待执行的网格视口恢复请求。
 * @property focusRequest 待执行的网格焦点恢复请求。
 */
data class BrowseSceneState(
    val visibleItems: List<MenuItem>,
    val focusedItemId: String?,
    val viewportRequest: BrowseViewportRequest? = null,
    val focusRequest: BrowseFocusRequest? = null,
)

/**
 * BrowseScene 网格视口恢复请求。
 *
 * @property requestId 请求 ID，用于驱动一次性滚动副作用。
 * @property rowIndex 网格首个可见行索引（row-level anchor）。
 */
data class BrowseViewportRequest(
    val requestId: Long,
    val rowIndex: Int,
) {

    /**
     * 兼容旧调用方：根据行锚点换算首可见 item 索引。
     */
    val firstVisibleItemIndex: Int
        get() = rowIndex * BROWSE_GRID_COLUMN_COUNT

    /**
     * 兼容旧调用方：行锚点恢复固定使用 0 偏移。
     */
    val firstVisibleItemScrollOffset: Int
        get() = 0
}

/**
 * BrowseScene 网格焦点恢复请求。
 *
 * @property requestId 请求 ID，用于驱动一次性焦点副作用。
 * @property targetItemId 目标菜品 ID。
 * @property targetItemIndex 目标菜品在当前分类中的索引，用于在必要时补做视口恢复。
 */
data class BrowseFocusRequest(
    val requestId: Long,
    val targetItemId: String,
    val targetItemIndex: Int? = null,
)

/**
 * FocusScene 状态：历史类型，供已存在组件编译兼容使用，不再进入菜单运行时主链路。
 *
 * @property focusedItem 中央放大详情卡展示的菜品。
 * @property surroundingSlots 周围缩小卡槽位列表（按固定顺序: A1→A3→B1→B3→C1→C2→C3→A2）。
 */
data class FocusSceneState(
    val focusedItem: MenuItem,
    val surroundingSlots: List<StageSlot>,
)

/**
 * 聚焦舞台中的单个卡槽位。
 *
 * @property item 该槽位对应的菜品。
 * @property slotId 槽位标识，对应设计稿中的位置编号（如 "A1", "B3" 等）。
 */
data class StageSlot(
    val item: MenuItem,
    val slotId: String,
)
