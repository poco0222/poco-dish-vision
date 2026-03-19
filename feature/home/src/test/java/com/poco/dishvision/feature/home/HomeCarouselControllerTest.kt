/**
 * @file HomeCarouselControllerTest.kt
 * @author PopoY
 * @date 2026-03-19
 * @description 验证首页轮播状态机的循环切换与自动轮播逻辑。
 */
package com.poco.dishvision.feature.home

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * `HomeCarouselController` 的时间驱动测试。
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HomeCarouselControllerTest {

    @Test
    fun `move next and previous wrap around showcase bounds`() = runTest {
        val controller = HomeCarouselController(
            itemCount = 5,
            autoAdvanceIntervalMs = 5_000L,
            autoResumeAfterInteractionMs = 10_000L,
            scope = backgroundScope,
        )

        controller.onMovePrevious()
        assertEquals(4, controller.selectedIndex.value)

        controller.onMoveNext()
        assertEquals(0, controller.selectedIndex.value)
    }

    @Test
    fun `manual interaction pauses auto play immediately`() = runTest {
        val controller = HomeCarouselController(
            itemCount = 5,
            autoAdvanceIntervalMs = 5_000L,
            autoResumeAfterInteractionMs = 10_000L,
            scope = backgroundScope,
        )

        controller.startAutoPlay()
        assertTrue(controller.isAutoPlaying.value)

        controller.onManualInteraction()

        assertFalse(controller.isAutoPlaying.value)
        advanceTimeBy(5_000L)
        runCurrent()
        assertEquals(0, controller.selectedIndex.value)
    }

    @Test
    fun `manual interaction resumes auto play after idle timeout from current card`() = runTest {
        val controller = HomeCarouselController(
            itemCount = 5,
            autoAdvanceIntervalMs = 5_000L,
            autoResumeAfterInteractionMs = 10_000L,
            scope = backgroundScope,
        )

        controller.startAutoPlay()
        controller.onMoveNext()
        controller.onManualInteraction()

        assertEquals(1, controller.selectedIndex.value)
        assertFalse(controller.isAutoPlaying.value)

        advanceTimeBy(10_000L)
        runCurrent()
        assertTrue(controller.isAutoPlaying.value)
        assertEquals(1, controller.selectedIndex.value)

        advanceTimeBy(5_000L)
        runCurrent()
        assertEquals(2, controller.selectedIndex.value)
    }

    @Test
    fun `auto play advances every configured interval`() = runTest {
        val controller = HomeCarouselController(
            itemCount = 5,
            autoAdvanceIntervalMs = 5_000L,
            autoResumeAfterInteractionMs = 10_000L,
            scope = backgroundScope,
        )

        controller.startAutoPlay()

        advanceTimeBy(5_000L)
        runCurrent()
        assertEquals(1, controller.selectedIndex.value)

        advanceTimeBy(5_000L)
        runCurrent()
        assertEquals(2, controller.selectedIndex.value)
    }
}
