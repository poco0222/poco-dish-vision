/**
 * @file BrowseModeController.kt
 * @author PopoY
 * @date 2026-03-19
 * @description 管理 Attract / Browse 双模式切换的时间状态机。
 */
package com.poco.dishvision.feature.menu

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * UI 模式定义。
 */
enum class UiMode {
    Attract,
    Browse,
}

/**
 * Browse mode 状态机：
 * 1) 默认处于 Attract；
 * 2) 一旦用户产生交互则切到 Browse；
 * 3) 在 `idleTimeoutMs` 内没有新的交互则回落到 Attract。
 *
 * @param idleTimeoutMs 空闲超时毫秒数。
 * @param scope 协程作用域。
 */
class BrowseModeController(
    private val idleTimeoutMs: Long,
    private val scope: CoroutineScope,
) {

    private val _mode = MutableStateFlow(UiMode.Attract)
    val mode: StateFlow<UiMode> = _mode.asStateFlow()

    private var idleTimeoutJob: Job? = null

    /**
     * 记录一次用户交互，并重新安排 idle timeout（空闲超时）。
     */
    fun onUserInteraction() {
        _mode.value = UiMode.Browse
        idleTimeoutJob?.cancel()
        idleTimeoutJob = scope.launch {
            delay(idleTimeoutMs)
            _mode.value = UiMode.Attract
        }
    }
}
