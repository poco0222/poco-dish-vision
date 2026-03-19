/**
 * @file HomeViewModel.kt
 * @author PopoY
 * @date 2026-03-19
 * @description 负责构建首页专用的湘味展示状态，并预热本地菜单数据。
 */
package com.poco.dishvision.feature.home

import com.poco.dishvision.feature.home.R
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.poco.dishvision.core.data.repository.MenuRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 首页 ViewModel，首页内容使用独立的湘味展示数据，同时在后台预热菜单仓储。
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
            runCatching {
                menuRepository.refreshFromLocalAsset()
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
 * 首页 preview / fallback 状态，保证测试与冷启动占位一致。
 *
 * @return 首页 UI 状态。
 */
internal fun previewHomeUiState(): HomeUiState {
    return HomeUiState(
        brandName = "新华饭店",
        brandSubtitle = "剁椒热炒 · 腊味土菜 · 今日现炒",
        seasonBadgeText = "老牌湘菜 · 热锅上桌",
        categoryChips = listOf("热锅现炒", "剁椒头牌", "腊味土菜"),
        showcaseItems = previewHomeShowcaseItems(),
        autoAdvanceEnabled = true,
        autoAdvanceIntervalMs = 5_000L,
        autoResumeAfterInteractionMs = 10_000L,
    )
}

/**
 * 返回湘味首屏 5 张固定展示卡。
 */
private fun previewHomeShowcaseItems(): List<HomeShowcaseItem> {
    // 用户已允许在 generated-* 图片池里随机挑选 5 张主图，先以本地 drawable-nodpi 固定资源名落盘，
    // 后续若设计补齐明确映射，可只替换资源文件而不改动 Home 的展示契约。
    return listOf(
        HomeShowcaseItem(
            heroEyebrow = "当前主推",
            heroTitlePrimary = "茶油炒鸡",
            heroTitleSecondary = "鲜辣上桌",
            heroDescription = "茶油一爆香，鸡肉紧实带汁，红椒和蒜香一起起锅，这一屏先看这道最稳的下饭菜。",
            cardTitle = "茶油炒鸡",
            cardPriceLabel = "¥88 / 例",
            cardDescription = "茶油爆香，鸡肉紧实，香辣下饭。",
            cardPrompt = "当前轮播",
            heroImageRes = R.drawable.home_showcase_tea_oil_chicken,
        ),
        HomeShowcaseItem(
            heroEyebrow = "鲜麻开胃",
            heroTitlePrimary = "青花椒鱼片",
            heroTitleSecondary = "滑嫩鲜亮",
            heroDescription = "鲜麻先起，鱼片嫩滑，越吃越开胃，适合作为第一轮抢手的开桌硬菜。",
            cardTitle = "青花椒鱼片",
            cardPriceLabel = "¥96 / 例",
            cardDescription = "鲜麻先起，鱼片嫩滑，越吃越开胃。",
            cardPrompt = "鲜麻开胃",
            heroImageRes = R.drawable.home_showcase_green_pepper_fish,
        ),
        HomeShowcaseItem(
            heroEyebrow = "茶油头牌",
            heroTitlePrimary = "钵钵茶油鸭",
            heroTitleSecondary = "鸭香厚实",
            heroDescription = "鸭香厚实，茶油提香，入口先闻香再吃肉，特别适合多人分享和整桌压轴。",
            cardTitle = "钵钵茶油鸭",
            cardPriceLabel = "¥108 / 例",
            cardDescription = "鸭香厚实，茶油提香，适合多人分享。",
            cardPrompt = "茶油头牌",
            heroImageRes = R.drawable.home_showcase_tea_oil_duck,
        ),
        HomeShowcaseItem(
            heroEyebrow = "锅气最足",
            heroTitlePrimary = "跳水鱼",
            heroTitleSecondary = "鲜辣滚汤",
            heroDescription = "汤底鲜辣，鱼肉入味，端上桌就能把整桌节奏拉起来，适合热闹聚餐先点。",
            cardTitle = "跳水鱼",
            cardPriceLabel = "¥118 / 份",
            cardDescription = "汤底鲜辣，鱼肉入味，整桌先抢这一锅。",
            cardPrompt = "锅气足",
            heroImageRes = R.drawable.home_showcase_tiaoshui_fish,
        ),
        HomeShowcaseItem(
            heroEyebrow = "重辣过瘾",
            heroTitlePrimary = "水煮活鱼",
            heroTitleSecondary = "红油翻香",
            heroDescription = "红油沸香，鱼肉滑嫩，椒麻和辣香一起顶上来，是偏重口客群最容易下单的一道。",
            cardTitle = "水煮活鱼",
            cardPriceLabel = "¥128 / 份",
            cardDescription = "红油沸香，鱼肉滑嫩，重口一桌必点。",
            cardPrompt = "重辣过瘾",
            heroImageRes = R.drawable.home_showcase_boiled_fish,
        ),
    )
}
