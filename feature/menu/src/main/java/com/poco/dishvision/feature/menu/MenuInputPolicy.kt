/**
 * @file MenuInputPolicy.kt
 * @author PopoY
 * @date 2026-03-21
 * @description 菜单页输入策略定义，确保 Browse/Focus 仅响应 TV 遥控器交互。
 */
package com.poco.dishvision.feature.menu

import androidx.compose.ui.input.key.Key

/**
 * Browse 网格用户手势滚动开关。
 *
 * 纯 TV 遥控器模式下禁用鼠标滚轮/触摸滑动，滚动仅由焦点导航与程序化恢复驱动。
 * @author PopoY
 */
internal const val BROWSE_GRID_USER_SCROLL_ENABLED = false

/**
 * 判断按键是否属于遥控器“确认”语义键。
 *
 * @return `true` 表示可作为确认输入触发菜品确认动作。
 * @author PopoY
 */
internal fun Key.isRemoteConfirmKey(): Boolean {
    return this == Key.DirectionCenter ||
        this == Key.Enter ||
        this == Key.NumPadEnter
}

/**
 * 将纵向方向键解析为 Browse 网格逐行滚动语义。
 *
 * 仅 `DirectionUp` / `DirectionDown` 会触发逐行滚动策略，其余按键交还给默认焦点系统。
 *
 * @return 对应的 [BrowseRowScrollDirection]；若当前按键不属于纵向滚动则返回 `null`。
 * @author PopoY
 */
internal fun Key.toBrowseRowScrollDirectionOrNull(): BrowseRowScrollDirection? {
    return when (this) {
        Key.DirectionUp -> BrowseRowScrollDirection.DirectionUp
        Key.DirectionDown -> BrowseRowScrollDirection.DirectionDown
        else -> null
    }
}
