/**
 * @file MenuItem.kt
 * @author PopoY
 * @date 2026-03-18
 * @description 定义菜单菜品（Menu Item）领域模型。
 */
package com.poco.dishvision.core.model.menu

import kotlinx.serialization.Serializable

/**
 * 菜品（Menu Item）领域对象，承载展示、价格与供应信息。
 *
 * @property itemId 菜品唯一标识。
 * @property name 菜品名称。
 * @property description 菜品描述。
 * @property imageUrl 菜品图片 URL（统一占位于本地或网络资源）。
 * @property priceInfo 菜品价格信息。
 * @property availabilityWindows 菜品可售时段列表。
 * @property displayBadges 菜品徽章列表，例如 NEW/HOT。
 * @property tags 检索与筛选标签。
 */
@Serializable
data class MenuItem(
    val itemId: String,
    val name: String,
    val description: String,
    val imageUrl: String,
    val priceInfo: PriceInfo,
    val availabilityWindows: List<AvailabilityWindow>,
    val displayBadges: List<DisplayBadge>,
    val tags: List<String>,
)
