/**
 * @file AppNavHost.kt
 * @author PopoY
 * @date 2026-03-19
 * @description 定义应用根壳层，并在 Attract / Browse 间切换。
 */
package com.poco.dishvision.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Text
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
    val rootFocusRequester = remember { FocusRequester() }
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

    LaunchedEffect(currentRoute, uiMode) {
        // Attract mode 没有天然焦点目标，根节点主动持有焦点后才能稳定接收遥控器方向键。
        if (currentRoute == AppDestination.Home.route && uiMode == UiMode.Attract) {
            rootFocusRequester.requestFocus()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .focusRequester(rootFocusRequester)
            .focusable()
            .onPreviewKeyEvent { keyEvent ->
                if (
                    currentRoute == AppDestination.Home.route &&
                    uiMode == UiMode.Attract &&
                    keyEvent.type == KeyEventType.KeyDown &&
                    keyEvent.key.isBrowseTriggerKey()
                ) {
                    browseModeController.onUserInteraction()
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

        if (currentRoute == AppDestination.Home.route) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(horizontal = 40.dp, vertical = 28.dp)
                    .background(
                        color = Color(0xCC152131),
                        shape = RoundedCornerShape(18.dp),
                    )
                    .clickable {
                        // 无论当前在 Attract 还是 Browse，进入 Settings 前都先把底层模式切到 Browse，
                        // 这样后续 Back 才能回到稳定的浏览态而不是退出应用。
                        browseModeController.onUserInteraction()
                        currentRoute = AppDestination.Settings.route
                    }
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .testTag("open-settings"),
            ) {
                Text(
                    text = "设置",
                    color = Color.White,
                )
            }
        }
    }
}

/**
 * 应用根层用于进入 Browse mode 的方向键集合。
 */
private fun Key.isBrowseTriggerKey(): Boolean {
    return this == Key.DirectionUp ||
        this == Key.DirectionDown ||
        this == Key.DirectionLeft ||
        this == Key.DirectionRight
}
