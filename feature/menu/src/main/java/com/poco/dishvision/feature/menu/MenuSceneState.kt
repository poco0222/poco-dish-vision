/**
 * @file MenuSceneState.kt
 * @author PopoY
 * @date 2026-03-20
 * @description 定义菜单 Browse 单事实源（single source of truth）的交互状态与纯函数 reducer（归约器）。
 */
package com.poco.dishvision.feature.menu

import com.poco.dishvision.core.model.menu.MenuCategory

/**
 * BrowseScene 下某个分类对应的锚点状态。
 *
 * @property focusedItemId 浏览态下最后一次真实聚焦的菜品 ID。
 * @property rowIndex 网格首个可见行索引（row-level anchor）。
 */
internal data class CategoryBrowseState(
    val focusedItemId: String? = null,
    val rowIndex: Int = 0,
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
 * 菜单页内部交互状态。
 *
 * 菜单页运行时已收敛到 Browse 单链路，只保留分类、焦点与行锚点恢复语义。
 *
 * @property selectedCategoryId 当前选中的分类 ID。
 * @property browseFocusedItemId BrowseScene 当前锚定的菜品 ID。
 * @property categoryBrowseStates 各分类的浏览锚点缓存。
 * @property pendingViewportRequest 待执行的网格视口恢复请求。
 * @property pendingFocusRequest 待执行的网格焦点恢复请求。
 * @property nextBrowseRequestId 下一个浏览请求 ID，用于驱动一次性 UI side effect（副作用）。
 */
internal data class MenuInteractionState(
    val selectedCategoryId: String? = null,
    val browseFocusedItemId: String? = null,
    val categoryBrowseStates: Map<String, CategoryBrowseState> = emptyMap(),
    val pendingViewportRequest: BrowseViewportRequest? = null,
    val pendingFocusRequest: BrowseFocusRequest? = null,
    val nextBrowseRequestId: Long = 1L,
)

/**
 * 处理 BrowseScene 分类切换。
 *
 * 只有真正切换到新的分类时，才会把该分类重置到顶部；
 * 如果当前选中的就是同一分类，则保留现有浏览锚点。
 *
 * @param currentState 当前交互状态。
 * @param categories 当前分类列表。
 * @param categoryId 新的分类 ID。
 * @return 更新后的交互状态。
 */
internal fun selectBrowseCategory(
    currentState: MenuInteractionState,
    categories: List<MenuCategory>,
    categoryId: String,
): MenuInteractionState {
    val sortedCategories = categories.sortedBy(MenuCategory::sortOrder)
    val targetCategory = sortedCategories.firstOrNull { it.categoryId == categoryId } ?: return currentState
    val resolvedSelectedCategoryId = resolveSelectedCategoryId(sortedCategories, currentState.selectedCategoryId)
    if (resolvedSelectedCategoryId == targetCategory.categoryId) {
        return currentState
    }

    val topBrowseState = targetCategory.toTopBrowseState()
    val (viewportRequestId, stateWithNextId) = allocateBrowseRequestId(currentState)
    return stateWithNextId.copy(
        selectedCategoryId = targetCategory.categoryId,
        browseFocusedItemId = topBrowseState.focusedItemId,
        categoryBrowseStates = currentState.categoryBrowseStates + (targetCategory.categoryId to topBrowseState),
        pendingViewportRequest = BrowseViewportRequest(
            requestId = viewportRequestId,
            rowIndex = topBrowseState.rowIndex,
        ),
        pendingFocusRequest = null,
    )
}

/**
 * 处理分类导轨自身的获焦事件。
 *
 * 当 BrowseScene 仍在执行“返回恢复”或“导轨切入网格”的焦点恢复时，
 * 系统可能会先把焦点兜底到某个分类项；这类被动获焦不能反向改写当前分类，
 * 否则会把待恢复的 Browse 锚点覆盖掉。
 *
 * @param currentState 当前交互状态。
 * @param categories 当前分类列表。
 * @param categoryId 获焦的分类 ID。
 * @return 更新后的交互状态。
 */
internal fun handleCategoryRailFocus(
    currentState: MenuInteractionState,
    categories: List<MenuCategory>,
    categoryId: String,
): MenuInteractionState {
    if (currentState.pendingFocusRequest != null) {
        return currentState
    }
    return selectBrowseCategory(
        currentState = currentState,
        categories = categories,
        categoryId = categoryId,
    )
}

/**
 * 从分类导轨把焦点切入 BrowseScene 网格。
 *
 * @param currentState 当前交互状态。
 * @param categories 当前分类列表。
 * @param categoryId 目标分类 ID；为空时使用当前已选分类。
 * @return 更新后的交互状态。
 */
internal fun requestBrowseItemFocus(
    currentState: MenuInteractionState,
    categories: List<MenuCategory>,
    categoryId: String? = null,
): MenuInteractionState {
    val selectedState = categoryId?.let {
        selectBrowseCategory(
            currentState = currentState,
            categories = categories,
            categoryId = it,
        )
    } ?: currentState
    val sortedCategories = categories.sortedBy(MenuCategory::sortOrder)
    val resolvedCategoryId = resolveSelectedCategoryId(sortedCategories, selectedState.selectedCategoryId)
        ?: return selectedState
    val browseState = selectedState.categoryBrowseStates[resolvedCategoryId]
        ?: sortedCategories.firstOrNull { it.categoryId == resolvedCategoryId }?.toTopBrowseState()
        ?: return selectedState
    val targetItemId = browseState.focusedItemId ?: return selectedState
    val targetItemIndex = sortedCategories
        .firstOrNull { it.categoryId == resolvedCategoryId }
        ?.indexOfItem(targetItemId)
    val (focusRequestId, stateWithNextId) = allocateBrowseRequestId(selectedState)
    return stateWithNextId.copy(
        pendingFocusRequest = BrowseFocusRequest(
            requestId = focusRequestId,
            targetItemId = targetItemId,
            targetItemIndex = targetItemIndex,
        ),
    )
}

/**
 * 记录 BrowseScene 中的菜品聚焦变化。
 *
 * @param currentState 当前交互状态。
 * @param categories 当前分类列表。
 * @param itemId 新聚焦的菜品 ID。
 * @return 更新后的交互状态。
 */
internal fun recordBrowseItemFocus(
    currentState: MenuInteractionState,
    categories: List<MenuCategory>,
    itemId: String,
): MenuInteractionState {
    val sortedCategories = categories.sortedBy(MenuCategory::sortOrder)
    val resolvedCategoryId = resolveSelectedCategoryId(sortedCategories, currentState.selectedCategoryId)
        ?: return currentState
    val targetCategory = sortedCategories.firstOrNull { it.categoryId == resolvedCategoryId } ?: return currentState
    if (targetCategory.items.none { it.itemId == itemId }) {
        return currentState
    }
    val currentBrowseState = currentState.categoryBrowseStates[resolvedCategoryId]
        ?: targetCategory.toTopBrowseState()
    return currentState.copy(
        browseFocusedItemId = itemId,
        categoryBrowseStates = currentState.categoryBrowseStates + (
            resolvedCategoryId to currentBrowseState.copy(
                focusedItemId = itemId,
            )
        ),
        pendingFocusRequest = currentState.pendingFocusRequest
            ?.takeUnless { pendingFocus -> pendingFocus.targetItemId == itemId },
    )
}

/**
 * 记录 BrowseScene 网格当前滚动位置。
 *
 * @param currentState 当前交互状态。
 * @param categories 当前分类列表。
 * @param rowIndex 网格首个可见行索引。
 * @return 更新后的交互状态。
 */
internal fun recordBrowseViewport(
    currentState: MenuInteractionState,
    categories: List<MenuCategory>,
    rowIndex: Int,
): MenuInteractionState {
    val sortedCategories = categories.sortedBy(MenuCategory::sortOrder)
    val resolvedCategoryId = resolveSelectedCategoryId(sortedCategories, currentState.selectedCategoryId)
        ?: return currentState
    val targetCategory = sortedCategories.firstOrNull { it.categoryId == resolvedCategoryId } ?: return currentState
    val currentBrowseState = currentState.categoryBrowseStates[resolvedCategoryId]
        ?: targetCategory.toTopBrowseState()
    val normalizedRowIndex = rowIndex.coerceAtLeast(0)
    return currentState.copy(
        categoryBrowseStates = currentState.categoryBrowseStates + (
            resolvedCategoryId to currentBrowseState.copy(
                rowIndex = normalizedRowIndex,
            )
        ),
        pendingViewportRequest = currentState.pendingViewportRequest
            ?.takeUnless { pendingViewport ->
                pendingViewport.rowIndex == normalizedRowIndex
            },
    )
}

/**
 * 解析指定菜品在分类中的索引，供 Browse 恢复链路补做视口校正。
 *
 * @param itemId 目标菜品 ID。
 * @return 菜品索引；不存在时返回 null。
 */
private fun MenuCategory.indexOfItem(itemId: String): Int? {
    val resolvedIndex = items.indexOfFirst { item -> item.itemId == itemId }
    return resolvedIndex.takeIf { it >= 0 }
}

/**
 * 为 BrowseScene UI side effect（副作用）分配单调递增请求 ID。
 *
 * @param currentState 当前交互状态。
 * @return 新请求 ID 与请求序列更新后的交互状态。
 */
private fun allocateBrowseRequestId(currentState: MenuInteractionState): Pair<Long, MenuInteractionState> {
    val requestId = currentState.nextBrowseRequestId
    return requestId to currentState.copy(
        nextBrowseRequestId = requestId + 1L,
    )
}

/**
 * 解析当前实际生效的分类 ID。
 *
 * @param categories 已排序分类列表。
 * @param selectedCategoryId 候选分类 ID。
 * @return 实际可用的分类 ID。
 */
internal fun resolveSelectedCategoryId(
    categories: List<MenuCategory>,
    selectedCategoryId: String?,
): String? {
    return categories
        .firstOrNull { it.categoryId == selectedCategoryId }
        ?.categoryId
        ?: categories.firstOrNull { it.categoryId == DEFAULT_BROWSE_CATEGORY_ID }?.categoryId
        ?: categories.firstOrNull()?.categoryId
}

/**
 * 构造某个分类进入 BrowseScene 顶部时的默认浏览锚点。
 *
 * @receiver 目标分类。
 * @return 顶部浏览锚点。
 */
private fun MenuCategory.toTopBrowseState(): CategoryBrowseState {
    return CategoryBrowseState(
        focusedItemId = items.firstOrNull()?.itemId,
        rowIndex = 0,
    )
}
