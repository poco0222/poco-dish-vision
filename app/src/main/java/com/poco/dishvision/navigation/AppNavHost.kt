/**
 * @file AppNavHost.kt
 * @author PopoY
 * @date 2026-03-19
 * @description 定义应用根壳层，并在 Attract / Browse 间切换。
 */
package com.poco.dishvision.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.Modifier
import com.poco.dishvision.core.data.preferences.AppPreferences
import com.poco.dishvision.core.data.repository.MenuRepository
import com.poco.dishvision.feature.home.HomeRoute
import com.poco.dishvision.feature.menu.BrowseModeController
import com.poco.dishvision.feature.menu.MenuRoute
import com.poco.dishvision.feature.menu.UiMode
import com.poco.dishvision.feature.settings.SettingsRoute

/**
 * 应用根壳层。
 *
 * @param startDestination 启动目的地。
 * @param menuRepository 菜单仓储。
 * @param appPreferences 应用偏好。
 * @param modifier 外层 Modifier。
 */
@Composable
fun AppNavHost(
    startDestination: AppDestination,
    menuRepository: MenuRepository,
    appPreferences: AppPreferences,
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
    var currentRoute by rememberSaveable { mutableStateOf(startDestination.route) }

    BackHandler(
        enabled = currentRoute == AppDestination.Settings.route ||
            (currentRoute == AppDestination.Home.route && uiMode == UiMode.Browse),
    ) {
        when {
            currentRoute == AppDestination.Settings.route -> {
                currentRoute = AppDestination.Home.route
            }

            currentRoute == AppDestination.Home.route && uiMode == UiMode.Browse -> {
                browseModeController.returnToAttractMode()
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onPreviewKeyEvent { keyEvent ->
                if (
                    currentRoute == AppDestination.Home.route &&
                    keyEvent.type == KeyEventType.KeyDown &&
                    keyEvent.key.isSettingsTriggerKey()
                ) {
                    // 进入 Settings 前统一把 Home 底层模式切到 Browse，确保 Back 链路稳定回到 Browse。
                    browseModeController.onUserInteraction()
                    currentRoute = AppDestination.Settings.route
                    true
                } else {
                    false
                }
            },
    ) {
        when (currentRoute) {
            AppDestination.Home.route -> {
                when (uiMode) {
                    UiMode.Attract -> {
                        HomeRoute(
                            menuRepository = menuRepository,
                            onBrowseRequested = browseModeController::onUserInteraction,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }

                    UiMode.Browse -> {
                        MenuRoute(
                            menuRepository = menuRepository,
                            onUserInteraction = browseModeController::onUserInteraction,
                            onBackFromBrowseRoot = browseModeController::returnToAttractMode,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            }

            AppDestination.Settings.route -> {
                SettingsRoute(
                    menuRepository = menuRepository,
                    appPreferences = appPreferences,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

/**
 * 应用根层用于进入 Settings 的菜单键集合。
 */
private fun Key.isSettingsTriggerKey(): Boolean {
    return this == Key.Menu
}
