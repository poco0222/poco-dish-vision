/**
 * @file PocoTheme.kt
 * @author PopoY
 * @date 2026-03-19
 * @description 提供全局主题入口，注入屏幕比例化尺寸系统。
 */
package com.poco.dishvision.core.ui.theme

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

/**
 * 全局主题入口。
 *
 * 通过 `BoxWithConstraints` 测量实际可用 dp 视口，构建 [ScreenProportions]
 * 并通过 [LocalScreenProportions] 向下注入，让所有子组件按比例适配不同 density 的 TV 设备。
 *
 * @param content 页面内容。
 */
@Composable
fun PocoTheme(
    content: @Composable () -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        // 基于实际可用空间计算比例化尺寸，屏幕尺寸不变时复用同一实例
        val proportions = remember(maxWidth, maxHeight) {
            ScreenProportions(
                screenWidth = maxWidth,
                screenHeight = maxHeight,
            )
        }
        CompositionLocalProvider(
            LocalScreenProportions provides proportions,
        ) {
            content()
        }
    }
}
