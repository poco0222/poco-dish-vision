/**
 * @file AppNavHost.kt
 * @author PopoY
 * @date 2026-03-19
 * @description 定义应用根壳层，并在 Attract / Browse 间切换。
 */
package com.poco.dishvision.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.poco.dishvision.core.data.repository.MenuRepository
import com.poco.dishvision.feature.home.HomeRoute
import com.poco.dishvision.feature.menu.BrowseModeController
import com.poco.dishvision.feature.menu.MenuRoute
import com.poco.dishvision.feature.menu.UiMode

/**
 * 应用根壳层。
 *
 * @param startDestination 启动目的地。
 * @param menuRepository 菜单仓储。
 * @param modifier 外层 Modifier。
 */
@Composable
fun AppNavHost(
    startDestination: AppDestination,
    menuRepository: MenuRepository,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    val browseModeController = remember {
        BrowseModeController(
            idleTimeoutMs = 15_000L,
            scope = coroutineScope,
        )
    }
    val uiMode by browseModeController.mode.collectAsState()

    when (uiMode) {
        UiMode.Attract -> {
            HomeRoute(
                menuRepository = menuRepository,
                onBrowseRequested = browseModeController::onUserInteraction,
                modifier = modifier.fillMaxSize(),
            )
        }

        UiMode.Browse -> {
            MenuRoute(
                menuRepository = menuRepository,
                onUserInteraction = browseModeController::onUserInteraction,
                modifier = modifier.fillMaxSize(),
            )
        }
    }
}
