/**
 * @file HomeRoute.kt
 * @author PopoY
 * @date 2026-03-18
 * @description 首页 Route，负责桥接 ViewModel 与 attract mode UI。
 */
package com.poco.dishvision.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.tv.material3.Text
import com.poco.dishvision.core.data.repository.MenuRepository

/**
 * 首页入口 Route。未注入仓储时回退到 preview 状态，便于 UI 测试先行。
 *
 * @param menuRepository 菜单仓储；测试为空时使用 preview 状态。
 * @param modifier 外层 Modifier。
 */
@Composable
fun HomeRoute(
    menuRepository: MenuRepository? = null,
    onBrowseRequested: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    if (menuRepository == null) {
        HomeScreen(
            uiState = previewHomeUiState(),
            onBrowseRequested = onBrowseRequested,
            modifier = modifier,
        )
        return
    }

    val homeViewModel: HomeViewModel = viewModel(
        factory = HomeViewModel.provideFactory(menuRepository),
    )
    val uiState by homeViewModel.uiState.collectAsStateWithLifecycle()
    HomeScreen(
        uiState = uiState,
        onBrowseRequested = onBrowseRequested,
        modifier = modifier,
    )
}

/**
 * 首页 attract mode 视图，强调中下视觉重心与轻量顶部信息。
 *
 * @param uiState 首页 UI 状态。
 * @param modifier 外层 Modifier。
 */
@Composable
private fun HomeScreen(
    uiState: HomeUiState,
    onBrowseRequested: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .onPreviewKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown && keyEvent.key.isBrowseTriggerKey()) {
                    onBrowseRequested()
                    true
                } else {
                    false
                }
            }
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF10151F),
                        Color(0xFF182235),
                        Color(0xFF0C1118),
                    ),
                ),
            )
            .testTag("home-screen"),
    ) {
        Text(
            text = "POCO Dish Vision",
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 40.dp, top = 28.dp),
            color = Color(0xFFF4F7FB),
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 40.dp, vertical = 48.dp)
                .testTag("home-lower-hero-zone"),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Text(
                text = uiState.heroTitle,
                color = Color.White,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = uiState.heroSubtitle,
                color = Color(0xFFE4EAF4),
            )
            AttractCarousel(featuredItems = uiState.featuredItems)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "按方向键浏览菜单",
                color = Color(0xCCF4F7FB),
            )
        }
    }
}

/**
 * Attract mode 下任意方向键都会切换到 Browse mode。
 */
private fun Key.isBrowseTriggerKey(): Boolean {
    return this == Key.DirectionUp ||
        this == Key.DirectionDown ||
        this == Key.DirectionLeft ||
        this == Key.DirectionRight
}
