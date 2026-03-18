/**
 * @file MenuCategory.kt
 * @author PopoY
 * @date 2026-03-18
 * @description 定义菜单分类（Menu Category）领域模型。
 */
package com.poco.dishvision.core.model.menu

import kotlinx.serialization.Serializable

/**
 * 菜单分类（Menu Category），用于组织同一主题下的菜品。
 *
 * @property categoryId 分类唯一标识。
 * @property displayName 分类展示名称。
 * @property subtitle 分类副标题，用于大屏补充说明。
 * @property sortOrder 分类排序权重，值越小越靠前。
 * @property items 当前分类下的菜品列表。
 */
@Serializable
data class MenuCategory(
    val categoryId: String,
    val displayName: String,
    val subtitle: String,
    val sortOrder: Int,
    val items: List<MenuItem>,
)
