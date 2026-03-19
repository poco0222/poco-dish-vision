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
import com.poco.dishvision.core.model.menu.DisplayBadge
import com.poco.dishvision.core.model.menu.MenuCategory
import com.poco.dishvision.core.model.menu.MenuItem
import com.poco.dishvision.core.model.menu.PriceInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
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
    private val selectedCategoryState = MutableStateFlow<String?>(null)
    private val focusedItemState = MutableStateFlow<String?>(null)
    private val detailPanelState = MutableStateFlow(false)

    val uiState: StateFlow<MenuUiState> = combine(
        categoriesState,
        selectedCategoryState,
        focusedItemState,
        detailPanelState,
    ) { categories, selectedCategoryId, focusedItemId, isDetailPanelVisible ->
        buildMenuUiState(
            categories = categories,
            selectedCategoryId = selectedCategoryId,
            focusedItemId = focusedItemId,
            isDetailPanelVisible = isDetailPanelVisible,
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
    fun onCategorySelected(categoryId: String) {
        selectedCategoryState.value = categoryId
        focusedItemState.value = categoriesState.value
            .firstOrNull { category -> category.categoryId == categoryId }
            ?.items
            ?.firstOrNull()
            ?.itemId
        detailPanelState.value = false
    }

    /**
     * 更新当前焦点菜品。
     *
     * @param itemId 菜品 ID。
     */
    fun onItemFocused(itemId: String) {
        focusedItemState.value = itemId
    }

    /**
     * 展开当前焦点菜品的详情浮层。
     */
    fun onFocusedItemConfirmed() {
        if (resolveFocusedItem(categoriesState.value, selectedCategoryState.value, focusedItemState.value) != null) {
            detailPanelState.value = true
        }
    }

    /**
     * 关闭详情浮层。
     */
    fun dismissDetailPanel() {
        detailPanelState.value = false
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
 * @param selectedCategoryId 当前选中分类。
 * @param focusedItemId 当前焦点菜品。
 * @param isDetailPanelVisible 是否显示详情浮层。
 * @return 完整浏览页状态。
 */
internal fun buildMenuUiState(
    categories: List<MenuCategory>,
    selectedCategoryId: String?,
    focusedItemId: String?,
    isDetailPanelVisible: Boolean,
): MenuUiState {
    val sortedCategories = categories.sortedBy(MenuCategory::sortOrder)
    val resolvedSelectedCategoryId = sortedCategories
        .firstOrNull { category -> category.categoryId == selectedCategoryId }
        ?.categoryId
        ?: sortedCategories.firstOrNull { category -> category.categoryId == DEFAULT_BROWSE_CATEGORY_ID }
            ?.categoryId
        ?: sortedCategories.firstOrNull()
            ?.categoryId
        ?: ""
    val visibleItems = sortedCategories
        .firstOrNull { category -> category.categoryId == resolvedSelectedCategoryId }
        ?.items
        .orEmpty()
    val resolvedFocusedItemId = resolveFocusedItem(
        categories = sortedCategories,
        selectedCategoryId = resolvedSelectedCategoryId,
        focusedItemId = focusedItemId,
    )?.itemId

    return MenuUiState(
        selectedCategoryId = resolvedSelectedCategoryId,
        categories = sortedCategories,
        visibleItems = visibleItems,
        focusedItemId = resolvedFocusedItemId,
        isDetailPanelVisible = isDetailPanelVisible,
    )
}

/**
 * 提供浏览页的本地 preview 分类数据。
 *
 * @return 预览分类列表。
 */
internal fun previewMenuCategories(): List<MenuCategory> {
    return listOf(
        MenuCategory(
            categoryId = "starters",
            displayName = "Starters",
            subtitle = "开场小食",
            sortOrder = 1,
            items = listOf(
                previewMenuItem(
                    itemId = "starter-crispy-calamari",
                    name = "Crispy Calamari",
                    description = "蒜香脆炸鱿鱼配柠檬胡椒蘸酱",
                    amountMinor = 4_800,
                    imageUrl = "https://images.unsplash.com/photo-1604909053333-7e5f1f7f5c9a",
                    badgeLabel = "POPULAR",
                ),
                previewMenuItem(
                    itemId = "starter-garden-salad",
                    name = "Garden Salad",
                    description = "羽衣甘蓝、番茄与坚果的轻食沙拉",
                    amountMinor = 3_600,
                    imageUrl = "https://images.unsplash.com/photo-1546069901-ba9599a7e63c",
                    badgeLabel = "FRESH",
                ),
            ),
        ),
        MenuCategory(
            categoryId = "mains",
            displayName = "Mains",
            subtitle = "招牌主菜",
            sortOrder = 2,
            items = listOf(
                previewMenuItem(
                    itemId = "main-smoked-ribeye",
                    name = "Smoked Ribeye",
                    description = "慢熏肋眼牛排，搭配黑椒汁与烤时蔬",
                    amountMinor = 12_800,
                    imageUrl = "https://images.unsplash.com/photo-1544025162-d76694265947",
                    badgeLabel = "SIGNATURE",
                ),
                previewMenuItem(
                    itemId = "main-truffle-pasta",
                    name = "Truffle Pasta",
                    description = "黑松露奶油意面，入口香浓",
                    amountMinor = 7_600,
                    imageUrl = "https://images.unsplash.com/photo-1621996346565-e3dbc646d9a9",
                    badgeLabel = "NEW",
                ),
            ),
        ),
        MenuCategory(
            categoryId = "desserts",
            displayName = "Desserts",
            subtitle = "甜点与饮品",
            sortOrder = 3,
            items = listOf(
                previewMenuItem(
                    itemId = "dessert-burnt-cheesecake",
                    name = "Burnt Cheesecake",
                    description = "巴斯克风味芝士蛋糕，绵密细腻",
                    amountMinor = 3_600,
                    imageUrl = "https://images.unsplash.com/photo-1578985545062-69928b1d9587",
                    badgeLabel = "CHEF PICK",
                ),
                previewMenuItem(
                    itemId = "dessert-cold-brew",
                    name = "Cold Brew Latte",
                    description = "冷萃与鲜奶调和，口感顺滑",
                    amountMinor = 2_800,
                    imageUrl = "https://images.unsplash.com/photo-1495474472287-4d71bcdd2085",
                    badgeLabel = "CLASSIC",
                ),
            ),
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
        selectedCategoryId = DEFAULT_BROWSE_CATEGORY_ID,
        focusedItemId = "main-smoked-ribeye",
        isDetailPanelVisible = false,
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
 * 构造 preview 菜品对象，减少重复模板代码。
 *
 * @param itemId 菜品 ID。
 * @param name 菜品名称。
 * @param description 菜品描述。
 * @param amountMinor 菜品金额（分）。
 * @param imageUrl 菜品图片 URL。
 * @param badgeLabel 菜品徽章文案。
 * @return 菜品模型。
 */
private fun previewMenuItem(
    itemId: String,
    name: String,
    description: String,
    amountMinor: Int,
    imageUrl: String,
    badgeLabel: String,
): MenuItem {
    return MenuItem(
        itemId = itemId,
        name = name,
        description = description,
        imageUrl = imageUrl,
        priceInfo = PriceInfo(
            currencyCode = "CNY",
            amountMinor = amountMinor,
            originalAmountMinor = amountMinor,
            unitLabel = "份",
        ),
        availabilityWindows = emptyList(),
        displayBadges = listOf(
            DisplayBadge(
                badgeId = "badge-$itemId",
                label = badgeLabel,
                styleKey = "brand",
            ),
        ),
        tags = listOf("poco", "featured"),
    )
}

/**
 * Browse mode 默认以主菜分类为入口，兼顾点单效率与演示效果。
 */
private const val DEFAULT_BROWSE_CATEGORY_ID = "mains"
