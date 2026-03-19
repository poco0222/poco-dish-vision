/**
 * @file HomeViewModel.kt
 * @author PopoY
 * @date 2026-03-18
 * @description 负责从 MenuRepository（菜单仓储）构建首页 UI 状态。
 */
package com.poco.dishvision.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.poco.dishvision.core.data.repository.MenuRepository
import com.poco.dishvision.core.model.menu.MenuCatalog
import com.poco.dishvision.core.model.menu.MenuItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

/**
 * 首页 ViewModel，从仓储侧生成 attract mode 所需文案与推荐列表。
 *
 * @param menuRepository 菜单仓储。
 */
class HomeViewModel(
    private val menuRepository: MenuRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(previewHomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            menuRepository.refreshFromLocalAsset()
            menuRepository.observeCatalog()
                .catch {
                    // Phase 1 保持回退到 preview 状态，避免首页直接空白。
                    _uiState.value = previewHomeUiState()
                }
                .collect { catalog ->
                    _uiState.value = catalog.toHomeUiState()
                }
        }
    }

    companion object {

        /**
         * 提供与 Compose `viewModel()` 协作的 Factory。
         *
         * @param menuRepository 菜单仓储。
         * @return ViewModelProvider.Factory。
         */
        fun provideFactory(menuRepository: MenuRepository): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return HomeViewModel(menuRepository) as T
                }
            }
        }
    }
}

/**
 * 将目录模型映射为首页状态。
 *
 * @return 首页 UI 状态。
 */
private fun MenuCatalog.toHomeUiState(): HomeUiState {
    val featuredItems = categories.flatMap { it.items }.take(3)
    val heroItem = featuredItems.firstOrNull()
    return HomeUiState(
        heroTitle = heroItem?.name ?: restaurantName,
        heroSubtitle = heroItem?.description ?: "按方向键浏览菜单",
        featuredItems = featuredItems,
        autoAdvanceEnabled = true,
    )
}

/**
 * 首页 preview / fallback 状态，保证测试与冷启动占位一致。
 *
 * @return 首页 UI 状态。
 */
internal fun previewHomeUiState(): HomeUiState {
    val featuredItems = listOf(
        placeholderMenuItem(
            itemId = "preview-ribeye",
            name = "Smoked Ribeye",
            description = "慢熏肋眼牛排，搭配黑椒汁与烤时蔬",
        ),
        placeholderMenuItem(
            itemId = "preview-pasta",
            name = "Truffle Pasta",
            description = "黑松露奶油意面，入口香浓",
        ),
        placeholderMenuItem(
            itemId = "preview-cheesecake",
            name = "Burnt Cheesecake",
            description = "巴斯克风味芝士蛋糕，绵密细腻",
        ),
    )
    return HomeUiState(
        heroTitle = "今晚主厨精选",
        heroSubtitle = "按方向键浏览菜单",
        featuredItems = featuredItems,
        autoAdvanceEnabled = true,
    )
}

/**
 * 构造首页占位菜品。
 */
private fun placeholderMenuItem(
    itemId: String,
    name: String,
    description: String,
): MenuItem {
    return MenuItem(
        itemId = itemId,
        name = name,
        description = description,
        imageUrl = "",
        priceInfo = com.poco.dishvision.core.model.menu.PriceInfo(
            currencyCode = "CNY",
            amountMinor = 8800,
            originalAmountMinor = 9800,
            unitLabel = "份",
        ),
        availabilityWindows = emptyList(),
        displayBadges = emptyList(),
        tags = emptyList(),
    )
}
