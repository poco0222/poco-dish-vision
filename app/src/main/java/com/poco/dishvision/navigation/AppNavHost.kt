/**
 * @file AppNavHost.kt
 * @author PopoY
 * @date 2026-03-18
 * @description 定义应用根导航图与最小 Home 占位界面。
 */
package com.poco.dishvision.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.poco.dishvision.core.data.repository.MenuRepository
import com.poco.dishvision.feature.home.HomeRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.tv.material3.Text

/**
 * 应用根导航宿主。
 *
 * @param startDestination 启动目的地。
 * @param modifier 外层 Modifier。
 */
@androidx.compose.runtime.Composable
fun AppNavHost(
    startDestination: AppDestination,
    menuRepository: MenuRepository,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = startDestination.route,
        modifier = modifier,
    ) {
        composable(AppDestination.Home.route) {
            HomeRoute(menuRepository = menuRepository)
        }
    }
}
