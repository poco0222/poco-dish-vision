/**
 * @file AppNavHostLogicTest.kt
 * @author PopoY
 * @date 2026-03-21
 * @description 验证 AppNavHost 非首屏 idle 返回逻辑的纯函数判定。
 */
package com.poco.dishvision.navigation

import com.poco.dishvision.feature.menu.UiMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * `AppNavHost` 关键路由判定逻辑测试。
 */
class AppNavHostLogicTest {

    @Test
    fun `home attract is first screen only`() {
        assertTrue(
            isFirstScreen(
                currentRoute = AppDestination.Home.route,
                uiMode = UiMode.Attract,
            ),
        )
        assertFalse(
            isFirstScreen(
                currentRoute = AppDestination.Home.route,
                uiMode = UiMode.Browse,
            ),
        )
        assertFalse(
            isFirstScreen(
                currentRoute = AppDestination.Settings.route,
                uiMode = UiMode.Attract,
            ),
        )
    }

    @Test
    fun `settings with attract mode should return home route after timeout`() {
        assertTrue(
            shouldForceReturnToHomeRoute(
                currentRoute = AppDestination.Settings.route,
                uiMode = UiMode.Attract,
            ),
        )
        assertFalse(
            shouldForceReturnToHomeRoute(
                currentRoute = AppDestination.Settings.route,
                uiMode = UiMode.Browse,
            ),
        )
    }

    @Test
    fun `non first screen idle timeout should be five minutes`() {
        assertEquals(300_000L, NON_FIRST_SCREEN_IDLE_TIMEOUT_MS)
    }
}
