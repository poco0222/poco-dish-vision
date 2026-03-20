/**
 * @file MenuUiState.kt
 * @author PopoY
 * @date 2026-03-19
 * @description 定义菜单页 BrowseScene / FocusScene 的 UI 状态。
 */
package com.poco.dishvision.feature.menu

import com.poco.dishvision.core.model.menu.MenuCategory
import com.poco.dishvision.core.model.menu.MenuItem

/**
 * 菜单页当前场景。
 */
enum class MenuScene {
    Browse,
    Focus,
}

/**
 * 菜单页状态，聚焦于当前分类以及 BrowseScene / FocusScene 的分层 UI 数据。
 *
 * @property selectedCategoryId 当前选中的分类 ID。
 * @property categories 当前目录中的全部分类。
 * @property scene 当前场景。
 * @property browseSceneState BrowseScene UI 状态。
 * @property focusSceneState FocusScene UI 状态；仅在 scene=Focus 时非 null。
 */
data class MenuUiState(
    val selectedCategoryId: String,
    val categories: List<MenuCategory>,
    val scene: MenuScene,
    val browseSceneState: BrowseSceneState,
    val focusSceneState: FocusSceneState? = null,
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
 * @property firstVisibleItemIndex 网格首个可见项索引。
 * @property firstVisibleItemScrollOffset 网格首个可见项滚动偏移。
 */
data class BrowseViewportRequest(
    val requestId: Long,
    val firstVisibleItemIndex: Int,
    val firstVisibleItemScrollOffset: Int,
)

/**
 * BrowseScene 网格焦点恢复请求。
 *
 * @property requestId 请求 ID，用于驱动一次性焦点副作用。
 * @property targetItemId 目标菜品 ID。
 */
data class BrowseFocusRequest(
    val requestId: Long,
    val targetItemId: String,
)

/**
 * FocusScene 状态：中央大卡 + 周围最多 7 张缩小卡。
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
