/**
 * @file MenuMetadataEntity.kt
 * @author PopoY
 * @date 2026-03-18
 * @description 定义菜单目录元数据在 Room（本地数据库）中的持久化实体。
 */
package com.poco.dishvision.core.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 目录元数据实体，承载 catalog（目录）级信息与主题配置。
 *
 * @property catalogId 目录唯一标识。
 * @property schemaVersion schema 版本。
 * @property restaurantName 餐厅名称。
 * @property lastUpdatedAtEpochMilli 目录业务更新时间（Epoch 毫秒）。
 * @property primaryColorHex 主题主色。
 * @property accentColorHex 主题强调色。
 * @property backgroundColorHex 主题背景色。
 * @property surfaceColorHex 主题容器色。
 * @property textColorHex 主题文本色。
 * @property importedAtEpochMilli 本地导入时间（Epoch 毫秒）。
 */
@Entity(tableName = "menu_metadata")
data class MenuMetadataEntity(
    @PrimaryKey
    val catalogId: String,
    val schemaVersion: Int,
    val restaurantName: String,
    val lastUpdatedAtEpochMilli: Long,
    val primaryColorHex: String,
    val accentColorHex: String,
    val backgroundColorHex: String,
    val surfaceColorHex: String,
    val textColorHex: String,
    val importedAtEpochMilli: Long,
)
