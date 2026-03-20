/**
 * @file HomeCarouselController.kt
 * @author PopoY
 * @date 2026-03-19
 * @description 首页轮播状态机，负责左右循环、自动轮播与手动暂停恢复。
 */
package com.poco.dishvision.feature.home

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * 轮播切换方向，用于驱动 Parallax Wipe 过渡动画。
 */
enum class CarouselDirection {
    /** 向前（下一张），对应从左到右的擦除方向 */
    FORWARD,

    /** 向后（上一张），对应从右到左的擦除方向 */
    BACKWARD,
}

/**
 * 首页轮播控制器。
 *
 * @param itemCount 展示项数量。
 * @param autoAdvanceIntervalMs 自动轮播间隔。
 * @param autoResumeAfterInteractionMs 手动交互后的自动恢复时间。
 * @param scope 外部协程作用域。
 */
class HomeCarouselController(
    itemCount: Int,
    private val autoAdvanceIntervalMs: Long,
    private val autoResumeAfterInteractionMs: Long,
    private val scope: CoroutineScope,
) {

    private val safeItemCount = itemCount.coerceAtLeast(1)
    private val _selectedIndex = MutableStateFlow(0)
    private val _isAutoPlaying = MutableStateFlow(false)
    private val _lastMoveDirection = MutableStateFlow(CarouselDirection.FORWARD)
    private var autoPlayJob: Job? = null
    private var autoResumeJob: Job? = null

    val selectedIndex: StateFlow<Int> = _selectedIndex.asStateFlow()
    val isAutoPlaying: StateFlow<Boolean> = _isAutoPlaying.asStateFlow()

    /** 最近一次切换方向，用于驱动 wipe 过渡动画方向 */
    val lastMoveDirection: StateFlow<CarouselDirection> = _lastMoveDirection.asStateFlow()

    /**
     * 切到下一张展示卡，超出尾部时回到首张。
     */
    fun onMoveNext() {
        _lastMoveDirection.value = CarouselDirection.FORWARD
        _selectedIndex.update { currentIndex ->
            (currentIndex + 1) % safeItemCount
        }
    }

    /**
     * 切到上一张展示卡，位于首张时回到末张。
     */
    fun onMovePrevious() {
        _lastMoveDirection.value = CarouselDirection.BACKWARD
        _selectedIndex.update { currentIndex ->
            (currentIndex - 1).floorMod(safeItemCount)
        }
    }

    /**
     * 记录用户手动交互，暂停自动轮播并安排空闲恢复。
     */
    fun onManualInteraction() {
        cancelAutoPlay()
        cancelAutoResume()
        _isAutoPlaying.value = false

        if (safeItemCount <= 1) {
            return
        }

        autoResumeJob = scope.launch {
            delay(autoResumeAfterInteractionMs)
            startAutoPlay()
        }
    }

    /**
     * 开始自动轮播；若已启动则保持现状。
     */
    fun startAutoPlay() {
        cancelAutoResume()

        if (_isAutoPlaying.value || safeItemCount <= 1) {
            return
        }

        _isAutoPlaying.value = true
        autoPlayJob = scope.launch {
            while (isActive) {
                delay(autoAdvanceIntervalMs)
                onMoveNext()
            }
        }
    }

    /**
     * 停止所有自动任务。
     */
    fun stop() {
        cancelAutoPlay()
        cancelAutoResume()
        _isAutoPlaying.value = false
    }

    /**
     * 取消自动轮播协程。
     */
    private fun cancelAutoPlay() {
        autoPlayJob?.cancel()
        autoPlayJob = null
    }

    /**
     * 取消自动恢复协程。
     */
    private fun cancelAutoResume() {
        autoResumeJob?.cancel()
        autoResumeJob = null
    }
}

/**
 * 让负数索引在固定数量范围内回绕。
 *
 * @param divisor 轮播总数。
 * @return 取模后的非负索引。
 */
private fun Int.floorMod(divisor: Int): Int {
    return ((this % divisor) + divisor) % divisor
}
