/**
 * @file DisplayBadge.kt
 * @author PopoY
 * @date 2026-03-18
 * @description 定义展示徽章（Display Badge）领域模型。
 */
package com.poco.dishvision.core.model.menu

import kotlinx.serialization.Serializable

/**
 * 展示徽章（Display Badge），用于突出菜品卖点。
 *
 * @property badgeId 徽章唯一标识。
 * @property label 徽章展示文案。
 * @property styleKey 样式键，用于映射 UI token（设计令牌）。
 */
@Serializable
data class DisplayBadge(
    val badgeId: String,
    val label: String,
    val styleKey: String,
)
