/**
 * @file BrowseModeControllerTest.kt
 * @author PopoY
 * @date 2026-03-19
 * @description 验证 Hybrid mode（混合模式）状态机的超时切换逻辑。
 */
package com.poco.dishvision.feature.menu

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * `BrowseModeController` 的时间驱动测试。
 */
@OptIn(ExperimentalCoroutinesApi::class)
class BrowseModeControllerTest {

    @Test
    fun `user input enters browse mode and idle timeout returns to attract mode`() = runTest {
        val controller = BrowseModeController(
            idleTimeoutMs = 15_000L,
            scope = backgroundScope,
        )

        controller.onUserInteraction()

        assertEquals(UiMode.Browse, controller.mode.value)
        advanceTimeBy(15_000L)
        runCurrent()
        assertEquals(UiMode.Attract, controller.mode.value)
    }
}
