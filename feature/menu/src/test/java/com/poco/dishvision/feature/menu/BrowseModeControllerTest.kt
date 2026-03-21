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

    // 需求约束：非首屏 5 分钟无操作需要回到首屏吸引态。
    private val fiveMinuteIdleTimeoutMs = 300_000L

    @Test
    fun `user input enters browse mode and five minute idle timeout returns to attract mode`() = runTest {
        val controller = BrowseModeController(
            idleTimeoutMs = fiveMinuteIdleTimeoutMs,
            scope = backgroundScope,
        )

        controller.onUserInteraction()

        assertEquals(UiMode.Browse, controller.mode.value)
        advanceTimeBy(fiveMinuteIdleTimeoutMs)
        runCurrent()
        assertEquals(UiMode.Attract, controller.mode.value)
    }

    @Test
    fun `latest user interaction resets five minute idle timeout window`() = runTest {
        val controller = BrowseModeController(
            idleTimeoutMs = fiveMinuteIdleTimeoutMs,
            scope = backgroundScope,
        )

        controller.onUserInteraction()
        advanceTimeBy(240_000L)
        controller.onUserInteraction()

        advanceTimeBy(240_000L)
        runCurrent()
        assertEquals(UiMode.Browse, controller.mode.value)

        advanceTimeBy(60_000L)
        runCurrent()
        assertEquals(UiMode.Attract, controller.mode.value)
    }
}
