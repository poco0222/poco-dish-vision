/**
 * @file MenuViewModel.kt
 * @author PopoY
 * @date 2026-03-19
 * @description 负责从菜单仓储构建浏览页 UI 状态并维护交互选择。
 */
package com.poco.dishvision.feature.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.poco.dishvision.core.data.repository.MenuRepository
import com.poco.dishvision.core.model.menu.MenuCategory
import com.poco.dishvision.core.model.menu.MenuItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 浏览页 ViewModel，负责：
 * 1) 从仓储读取分类与菜品；
 * 2) 维护当前分类、焦点菜品与详情浮层状态。
 *
 * @param menuRepository 菜单仓储。
 */
class MenuViewModel(
    private val menuRepository: MenuRepository,
) : ViewModel() {

    private val categoriesState = MutableStateFlow(previewMenuCategories())
    private val interactionState = MutableStateFlow(MenuInteractionState())

    val uiState: StateFlow<MenuUiState> = combine(
        categoriesState,
        interactionState,
    ) { categories, currentInteractionState ->
        buildMenuUiState(
            categories = categories,
            interactionState = currentInteractionState,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = previewMenuUiState(),
    )

    init {
        viewModelScope.launch {
            runCatching {
                menuRepository.refreshFromLocalAsset()
            }

            menuRepository.observeCatalog()
                .catch {
                    // 保持 preview 数据，避免 Browse mode 在资源异常时出现空白区域。
                }
                .collect { catalog ->
                    categoriesState.value = catalog.categories
                }
        }
    }

    /**
     * 选择分类并重置到该分类下第一个菜品。
     *
     * @param categoryId 新的分类 ID。
     */
    fun onCategoryFocused(categoryId: String) {
        interactionState.update { currentState ->
            handleCategoryRailFocus(
                currentState = currentState,
                categories = categoriesState.value,
                categoryId = categoryId,
            )
        }
    }

    /**
     * 请求把焦点从分类导轨切入 BrowseScene 网格。
     *
     * @param categoryId 当前分类 ID。
     */
    fun onCategoryItemsRequested(categoryId: String) {
        interactionState.update { currentState ->
            requestBrowseItemFocus(
                currentState = currentState,
                categories = categoriesState.value,
                categoryId = categoryId,
            )
        }
    }

    /**
     * 更新 BrowseScene 当前焦点菜品。
     *
     * @param itemId 菜品 ID。
     */
    fun onBrowseItemFocused(itemId: String) {
        interactionState.update { currentState ->
            recordBrowseItemFocus(
                currentState = currentState,
                categories = categoriesState.value,
                itemId = itemId,
            )
        }
    }

    /**
     * 记录 BrowseScene 网格当前滚动位置。
     *
     * @param firstVisibleItemIndex 首个可见项索引。
     * @param firstVisibleItemScrollOffset 首个可见项滚动偏移。
     */
    fun onBrowseViewportChanged(
        firstVisibleItemIndex: Int,
        firstVisibleItemScrollOffset: Int,
    ) {
        interactionState.update { currentState ->
            recordBrowseViewport(
                currentState = currentState,
                categories = categoriesState.value,
                firstVisibleItemIndex = firstVisibleItemIndex,
                firstVisibleItemScrollOffset = firstVisibleItemScrollOffset,
            )
        }
    }

    /**
     * 更新 FocusScene 当前中央展示菜品。
     *
     * @param itemId 菜品 ID。
     */
    fun onFocusSceneItemFocused(itemId: String) {
        interactionState.update { currentState ->
            recordFocusSceneItemFocus(
                currentState = currentState,
                categories = categoriesState.value,
                itemId = itemId,
            )
        }
    }

    /**
     * 从 BrowseScene 进入 FocusScene。
     */
    fun onFocusedItemConfirmed() {
        interactionState.update { currentState ->
            enterFocusScene(
                currentState = currentState,
                categories = categoriesState.value,
            )
        }
    }

    /**
     * 从 FocusScene 返回 BrowseScene，并恢复浏览锚点。
     */
    fun dismissFocusScene() {
        interactionState.update { currentState ->
            exitFocusScene(
                currentState = currentState,
                categories = categoriesState.value,
            )
        }
    }

    companion object {

        /**
         * 提供 Compose `viewModel()` 可消费的 Factory。
         *
         * @param menuRepository 菜单仓储。
         * @return ViewModel 工厂。
         */
        fun provideFactory(menuRepository: MenuRepository): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return MenuViewModel(menuRepository) as T
                }
            }
        }
    }
}

/**
 * 构建浏览页状态，并确保所选分类和焦点菜品始终可解析。
 *
 * @param categories 分类列表。
 * @param interactionState 当前菜单交互状态。
 * @return 完整浏览页状态。
 */
internal fun buildMenuUiState(
    categories: List<MenuCategory>,
    interactionState: MenuInteractionState,
): MenuUiState {
    val sortedCategories = categories.sortedBy(MenuCategory::sortOrder)
    val resolvedSelectedCategoryId = resolveSelectedCategoryId(
        categories = sortedCategories,
        selectedCategoryId = interactionState.selectedCategoryId,
    ).orEmpty()
    val selectedCategory = sortedCategories.firstOrNull { category ->
        category.categoryId == resolvedSelectedCategoryId
    }
    val resolvedBrowseFocusItemId = selectedCategory
        ?.items
        ?.firstOrNull { item ->
            item.itemId == (
                interactionState.categoryBrowseStates[resolvedSelectedCategoryId]?.focusedItemId
                    ?: interactionState.browseFocusedItemId
                )
        }
        ?.itemId
        ?: selectedCategory?.items?.firstOrNull()?.itemId
    val visibleItems = sortedCategories
        .firstOrNull { category -> category.categoryId == resolvedSelectedCategoryId }
        ?.items
        .orEmpty()
    val resolvedFocusSceneItemId = resolveFocusedItem(
        categories = sortedCategories,
        selectedCategoryId = resolvedSelectedCategoryId,
        focusedItemId = interactionState.focusSceneItemId,
    )?.itemId
    val resolvedFocusRequest = interactionState.pendingFocusRequest
        ?.takeIf { focusRequest ->
            visibleItems.any { item -> item.itemId == focusRequest.targetItemId }
        }
        ?.copy(
            targetItemIndex = visibleItems.indexOfFirst { item ->
                item.itemId == interactionState.pendingFocusRequest.targetItemId
            }.takeIf { itemIndex -> itemIndex >= 0 },
        )
        ?: resolvedBrowseFocusItemId?.let { focusedItemId ->
            interactionState.pendingFocusRequest?.copy(
                targetItemId = focusedItemId,
                targetItemIndex = visibleItems.indexOfFirst { item ->
                    item.itemId == focusedItemId
                }.takeIf { itemIndex -> itemIndex >= 0 },
            )
        }

    return MenuUiState(
        selectedCategoryId = resolvedSelectedCategoryId,
        categories = sortedCategories,
        scene = interactionState.scene,
        browseSceneState = BrowseSceneState(
            visibleItems = visibleItems,
            focusedItemId = resolvedBrowseFocusItemId,
            viewportRequest = interactionState.pendingViewportRequest,
            focusRequest = resolvedFocusRequest,
        ),
        focusSceneState = buildFocusSceneState(
            visibleItems = visibleItems,
            focusedItemId = resolvedFocusSceneItemId,
            isFocusSceneVisible = interactionState.scene == MenuScene.Focus,
        ),
    )
}

/**
 * 提供浏览页 preview 状态。
 *
 * @return 预览状态。
 */
internal fun previewMenuUiState(): MenuUiState {
    return buildMenuUiState(
        categories = previewMenuCategories(),
        interactionState = MenuInteractionState(
            selectedCategoryId = DEFAULT_BROWSE_CATEGORY_ID,
            browseFocusedItemId = "hot-tea-chicken",
            categoryBrowseStates = mapOf(
                DEFAULT_BROWSE_CATEGORY_ID to CategoryBrowseState(
                    focusedItemId = "hot-tea-chicken",
                    firstVisibleItemIndex = 0,
                    firstVisibleItemScrollOffset = 0,
                ),
            ),
        ),
    )
}

/**
 * 解析当前状态下应该展示的焦点菜品。
 *
 * @param categories 分类列表。
 * @param selectedCategoryId 选中分类 ID。
 * @param focusedItemId 当前焦点菜品 ID。
 * @return 实际可解析的焦点菜品，若无则返回 null。
 */
internal fun resolveFocusedItem(
    categories: List<MenuCategory>,
    selectedCategoryId: String?,
    focusedItemId: String?,
): MenuItem? {
    val selectedCategory = categories.firstOrNull { category -> category.categoryId == selectedCategoryId }
        ?: categories.firstOrNull()
    return selectedCategory
        ?.items
        ?.firstOrNull { item -> item.itemId == focusedItemId }
        ?: selectedCategory
            ?.items
            ?.firstOrNull()
}

/**
 * Browse mode 默认以招牌热炒分类为入口，匹配设计稿首屏。
 */
internal const val DEFAULT_BROWSE_CATEGORY_ID = "hot-stir-fry"

/** FocusStage 周围小卡最大数量 */
private const val MAX_SURROUNDING_SLOTS = 7

/**
 * 构建聚焦舞台状态。
 *
 * 当详情浮层可见且存在有效焦点菜品时，从当前分类菜品列表中选出
 * 中央大卡（focusedItem）和最多 7 张周围小卡。
 *
 * @param visibleItems 当前分类下的可见菜品。
 * @param focusedItemId 当前焦点菜品 ID。
 * @param isDetailPanelVisible 是否处于详情展示状态。
 * @return 聚焦舞台状态，若条件不满足则返回 null。
 */
internal fun buildFocusSceneState(
    visibleItems: List<MenuItem>,
    focusedItemId: String?,
    isFocusSceneVisible: Boolean,
): FocusSceneState? {
    if (!isFocusSceneVisible || focusedItemId == null) return null

    val focusedItem = visibleItems.firstOrNull { it.itemId == focusedItemId }
        ?: return null

    // 排除中央卡后取最多 7 张周围卡，按原始列表顺序
    val surrounding = visibleItems
        .filter { it.itemId != focusedItemId }
        .take(MAX_SURROUNDING_SLOTS)

    // 按固定卡槽顺序分配 slotId: A1→A3→B1→B3→C1→C2→C3→A2
    val slots = surrounding.mapIndexed { index, item ->
        StageSlot(
            item = item,
            slotId = SLOT_IDS[index],
        )
    }

    return FocusSceneState(
        focusedItem = focusedItem,
        surroundingSlots = slots,
    )
}
