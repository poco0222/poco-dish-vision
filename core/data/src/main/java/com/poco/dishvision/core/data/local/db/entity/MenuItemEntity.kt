/**
 * @file MenuItemEntity.kt
 * @author PopoY
 * @date 2026-03-18
 * @description 定义菜单菜品在 Room（本地数据库）中的持久化实体。
 */
package com.poco.dishvision.core.data.local.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 菜品实体。复杂结构以 JSON 字符串落库，保持 schema（结构）可演进。
 *
 * @property itemId 菜品唯一标识。
 * @property catalogId 所属目录唯一标识。
 * @property categoryId 所属分类唯一标识。
 * @property itemSortOrder 分类内排序权重。
 * @property name 菜品名称。
 * @property description 菜品描述。
 * @property imageUrl 菜品图片 URL。
 * @property currencyCode ISO-4217 货币代码。
 * @property amountMinor 当前价格（最小货币单位）。
 * @property originalAmountMinor 原价（最小货币单位）。
 * @property unitLabel 计量单位文案。
 * @property availabilityWindowsJson 可售时段 JSON。
 * @property displayBadgesJson 徽章列表 JSON。
 * @property tagsJson 标签列表 JSON。
 */
@Entity(
    tableName = "menu_items",
    foreignKeys = [
        ForeignKey(
            entity = MenuCategoryEntity::class,
            parentColumns = ["categoryId"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["catalogId"]),
        Index(value = ["categoryId"]),
        Index(value = ["categoryId", "itemSortOrder"]),
    ],
)
data class MenuItemEntity(
    @PrimaryKey
    val itemId: String,
    val catalogId: String,
    val categoryId: String,
    val itemSortOrder: Int,
    val name: String,
    val description: String,
    val imageUrl: String,
    val currencyCode: String,
    val amountMinor: Int,
    val originalAmountMinor: Int,
    val unitLabel: String,
    val availabilityWindowsJson: String,
    val displayBadgesJson: String,
    val tagsJson: String,
)
