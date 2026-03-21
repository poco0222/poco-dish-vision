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
 * 2) 维护当前分类、焦点菜品与 Browse 行锚点恢复状态。
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
     * @param rowIndex 首个可见行索引。
     */
    fun onBrowseViewportChanged(rowIndex: Int) {
        interactionState.update { currentState ->
            recordBrowseViewport(
                currentState = currentState,
                categories = categoriesState.value,
                rowIndex = rowIndex,
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
        browseSceneState = BrowseSceneState(
            visibleItems = visibleItems,
            focusedItemId = resolvedBrowseFocusItemId,
            viewportRequest = interactionState.pendingViewportRequest,
            focusRequest = resolvedFocusRequest,
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
                    rowIndex = 0,
                ),
            ),
        ),
    )
}

/**
 * Browse mode 默认以招牌热炒分类为入口，匹配设计稿首屏。
 */
internal const val DEFAULT_BROWSE_CATEGORY_ID = "hot-stir-fry"
