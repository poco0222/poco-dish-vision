/**
 * @file ThemeConfig.kt
 * @author PopoY
 * @date 2026-03-18
 * @description 定义菜单主题配置（Theme Config）领域模型。
 */
package com.poco.dishvision.core.model.menu

import kotlinx.serialization.Serializable

/**
 * 菜单主题配置（Theme Config），用于驱动浏览页基础视觉风格。
 *
 * @property primaryColorHex 主色（HEX）。
 * @property accentColorHex 强调色（HEX）。
 * @property backgroundColorHex 背景色（HEX）。
 * @property surfaceColorHex 容器色（HEX）。
 * @property textColorHex 文本色（HEX）。
 */
@Serializable
data class ThemeConfig(
    val primaryColorHex: String,
    val accentColorHex: String,
    val backgroundColorHex: String,
    val surfaceColorHex: String,
    val textColorHex: String,
)
