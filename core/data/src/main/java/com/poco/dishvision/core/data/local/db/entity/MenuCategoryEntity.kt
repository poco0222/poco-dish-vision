/**
 * @file MenuCategoryEntity.kt
 * @author PopoY
 * @date 2026-03-18
 * @description 定义菜单分类在 Room（本地数据库）中的持久化实体。
 */
package com.poco.dishvision.core.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 菜单分类实体，保持与目录层级的稳定映射关系。
 *
 * @property categoryId 分类唯一标识。
 * @property catalogId 所属目录唯一标识。
 * @property displayName 分类展示名称。
 * @property subtitle 分类副标题。
 * @property sortOrder 分类排序权重，值越小越靠前。
 * @property description 分类描述文案。
 */
@Entity(tableName = "menu_categories")
data class MenuCategoryEntity(
    @PrimaryKey
    val categoryId: String,
    val catalogId: String,
    val displayName: String,
    val subtitle: String,
    val sortOrder: Int,
    val description: String = "",
)
