/**
 * @file PocoTheme.kt
 * @author PopoY
 * @date 2026-03-19
 * @description 提供 Phase 1 的最小主题入口，供页面统一接入共享令牌。
 */
package com.poco.dishvision.core.ui.theme

import androidx.compose.runtime.Composable

/**
 * Phase 1 主题入口。
 *
 * 当前阶段先统一颜色与尺寸令牌的消费入口，后续再扩展为更完整的主题系统。
 *
 * @param content 页面内容。
 */
@Composable
fun PocoTheme(
    content: @Composable () -> Unit,
) {
    content()
}
