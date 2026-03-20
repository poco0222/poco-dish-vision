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
            selectBrowseCategory(
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
        ?: resolvedBrowseFocusItemId?.let { focusedItemId ->
            interactionState.pendingFocusRequest?.copy(targetItemId = focusedItemId)
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
 * 提供浏览页的本地 preview 分类数据，匹配设计稿"湘味分类"五大分类。
 *
 * @return 预览分类列表。
 */
internal fun previewMenuCategories(): List<MenuCategory> {
    return listOf(
        // ── 招牌热炒（设计稿 9 道菜） ──
        MenuCategory(
            categoryId = "hot-stir-fry",
            displayName = "招牌热炒",
            subtitle = "热锅现炒",
            sortOrder = 1,
            description = "锅气、辣香、下饭感最强的一页，先看最能代表湘味火候的现炒菜。",
            items = listOf(
                previewMenuItem(
                    itemId = "hot-tea-chicken",
                    name = "茶油炒鸡",
                    description = "茶油爆香，锅气足，越吃越香。",
                    amountMinor = 8_800,
                    imageUrl = "",
                    badgeLabel = "招牌",
                ),
                previewMenuItem(
                    itemId = "hot-beef",
                    name = "小炒黄牛肉",
                    description = "鲜辣现炒，肉嫩椒脆，湘味代表。",
                    amountMinor = 7_800,
                    imageUrl = "",
                    badgeLabel = "人气",
                ),
                previewMenuItem(
                    itemId = "hot-liling-pork",
                    name = "醴陵小炒肉",
                    description = "辣椒鲜香，肉香直接，家常头牌。",
                    amountMinor = 5_800,
                    imageUrl = "",
                    badgeLabel = "经典",
                ),
                previewMenuItem(
                    itemId = "hot-youxian-pork",
                    name = "攸县杀猪肉",
                    description = "土味厚香，口感扎实，越吃越耐。",
                    amountMinor = 6_800,
                    imageUrl = "",
                    badgeLabel = "土菜",
                ),
                previewMenuItem(
                    itemId = "hot-intestine",
                    name = "生炒肥肠",
                    description = "爆香重口，肥肠越嚼越香。",
                    amountMinor = 5_800,
                    imageUrl = "",
                    badgeLabel = "重口",
                ),
                previewMenuItem(
                    itemId = "hot-kidney",
                    name = "爆炒腰花",
                    description = "脆嫩见火候，香辣利落。",
                    amountMinor = 5_800,
                    imageUrl = "",
                    badgeLabel = "功夫菜",
                ),
                previewMenuItem(
                    itemId = "hot-tripe",
                    name = "酸辣脆肚",
                    description = "脆爽酸辣，开胃带劲。",
                    amountMinor = 4_800,
                    imageUrl = "",
                    badgeLabel = "开胃",
                ),
                previewMenuItem(
                    itemId = "hot-gizzard",
                    name = "酸辣鸡胗",
                    description = "脆口鲜辣，下饭下酒都稳。",
                    amountMinor = 3_800,
                    imageUrl = "",
                    badgeLabel = "下酒",
                ),
                previewMenuItem(
                    itemId = "hot-seasonal",
                    name = "时蔬炒肉",
                    description = "家常热炒，配饭最顺口。",
                    amountMinor = 3_800,
                    imageUrl = "",
                    badgeLabel = "家常",
                ),
            ),
        ),
        // ── 香辣口味 ──
        MenuCategory(
            categoryId = "spicy",
            displayName = "香辣口味",
            subtitle = "辣香下饭",
            sortOrder = 2,
            description = "无辣不欢，从剁椒鱼头到口味虾，辣出层次、辣得过瘾。",
            items = listOf(
                previewMenuItem(
                    itemId = "spicy-fish-head",
                    name = "剁椒鱼头",
                    description = "双色剁椒铺满鱼头，鲜辣霸道。",
                    amountMinor = 12_800,
                    imageUrl = "",
                    badgeLabel = "招牌",
                ),
                previewMenuItem(
                    itemId = "spicy-crayfish",
                    name = "口味虾",
                    description = "十三香油焖，壳脆肉弹。",
                    amountMinor = 9_800,
                    imageUrl = "",
                    badgeLabel = "人气",
                ),
            ),
        ),
        // ── 鱼鲜大菜 ──
        MenuCategory(
            categoryId = "fish",
            displayName = "鱼鲜大菜",
            subtitle = "鲜味主打",
            sortOrder = 3,
            description = "以鲜为本，花椒提麻，鱼片嫩滑，一锅鲜汤暖全席。",
            items = listOf(
                previewMenuItem(
                    itemId = "fish-sichuan",
                    name = "青花椒鱼片",
                    description = "鲜麻先起，鱼片嫩滑，越吃越开胃。",
                    amountMinor = 9_600,
                    imageUrl = "",
                    badgeLabel = "鲜麻",
                ),
                previewMenuItem(
                    itemId = "fish-jumping",
                    name = "跳水鱼",
                    description = "汤底鲜辣，鱼肉入味，整桌先抢这一锅。",
                    amountMinor = 11_800,
                    imageUrl = "",
                    badgeLabel = "锅气足",
                ),
            ),
        ),
        // ── 风味小菜 ──
        MenuCategory(
            categoryId = "side",
            displayName = "风味小菜",
            subtitle = "佐餐小味",
            sortOrder = 4,
            description = "几道佐餐小味，擂辣椒、凉拌菜，简单却最下饭。",
            items = listOf(
                previewMenuItem(
                    itemId = "side-preserved-egg",
                    name = "擂辣椒皮蛋",
                    description = "辣椒擂香，皮蛋绵密，最配白饭。",
                    amountMinor = 2_800,
                    imageUrl = "",
                    badgeLabel = "经典",
                ),
            ),
        ),
        // ── 家常土菜 ──
        MenuCategory(
            categoryId = "home-style",
            displayName = "家常土菜",
            subtitle = "乡土滋味",
            sortOrder = 5,
            description = "腊味蒸菜、乡土食材，妈妈味道的朴素好菜。",
            items = listOf(
                previewMenuItem(
                    itemId = "home-smoked-pork",
                    name = "腊肉蒸干豆角",
                    description = "腊味浓香，干豆角吸满肉汁。",
                    amountMinor = 4_800,
                    imageUrl = "",
                    badgeLabel = "土味",
                ),
                previewMenuItem(
                    itemId = "home-steamed-eggplant",
                    name = "擂椒蒸茄子",
                    description = "茄香软糯，辣椒一拌就下饭。",
                    amountMinor = 2_800,
                    imageUrl = "",
                    badgeLabel = "家常",
                ),
                previewMenuItem(
                    itemId = "home-bean-curd",
                    name = "香干炒腊肉",
                    description = "豆香扎实，腊香直接，越嚼越有味。",
                    amountMinor = 3_800,
                    imageUrl = "",
                    badgeLabel = "经典",
                ),
                previewMenuItem(
                    itemId = "home-potato",
                    name = "酸豆角炒土豆片",
                    description = "脆酸带劲，土豆片吸满锅气。",
                    amountMinor = 2_600,
                    imageUrl = "",
                    badgeLabel = "下饭",
                ),
                previewMenuItem(
                    itemId = "home-pumpkin",
                    name = "小炒南瓜藤",
                    description = "清鲜脆嫩，乡土香气很足。",
                    amountMinor = 2_800,
                    imageUrl = "",
                    badgeLabel = "时令",
                ),
                previewMenuItem(
                    itemId = "home-dried-radish",
                    name = "萝卜干炒腊肠",
                    description = "咸香脆口，越吃越开胃。",
                    amountMinor = 3_600,
                    imageUrl = "",
                    badgeLabel = "乡味",
                ),
                previewMenuItem(
                    itemId = "home-tofu",
                    name = "农家豆腐煲",
                    description = "豆腐吸汁，热气腾腾，最有家里味道。",
                    amountMinor = 3_800,
                    imageUrl = "",
                    badgeLabel = "暖胃",
                ),
                previewMenuItem(
                    itemId = "home-bacon-bamboo",
                    name = "腊味笋干钵",
                    description = "笋干耐嚼，腊香厚实，越煨越香。",
                    amountMinor = 4_600,
                    imageUrl = "",
                    badgeLabel = "钵菜",
                ),
                previewMenuItem(
                    itemId = "home-lotus-root",
                    name = "藕尖炒肉丝",
                    description = "爽脆清香，肉丝细嫩，口感利落。",
                    amountMinor = 3_600,
                    imageUrl = "",
                    badgeLabel = "爽口",
                ),
                previewMenuItem(
                    itemId = "home-squash",
                    name = "椒香炒嫩南瓜",
                    description = "锅气温和，带一点甜辣的家常口感。",
                    amountMinor = 2_900,
                    imageUrl = "",
                    badgeLabel = "第10道",
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
