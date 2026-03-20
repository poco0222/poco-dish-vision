/**
 * @file MenuDatabase.kt
 * @author PopoY
 * @date 2026-03-18
 * @description 定义菜单数据的 Room Database（数据库）入口。
 */
package com.poco.dishvision.core.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.poco.dishvision.core.data.local.db.dao.MenuCategoryDao
import com.poco.dishvision.core.data.local.db.dao.MenuItemDao
import com.poco.dishvision.core.data.local.db.dao.MenuMetadataDao
import com.poco.dishvision.core.data.local.db.entity.MenuCategoryEntity
import com.poco.dishvision.core.data.local.db.entity.MenuItemEntity
import com.poco.dishvision.core.data.local.db.entity.MenuMetadataEntity

/**
 * 菜单持久化数据库，集中管理 metadata/category/item 三类表。
 */
@Database(
    entities = [
        MenuMetadataEntity::class,
        MenuCategoryEntity::class,
        MenuItemEntity::class,
    ],
    version = 2,
    exportSchema = false,
)
abstract class MenuDatabase : RoomDatabase() {

    abstract fun menuMetadataDao(): MenuMetadataDao

    abstract fun menuCategoryDao(): MenuCategoryDao

    abstract fun menuItemDao(): MenuItemDao

    companion object {

        // 统一数据库名，避免不同调用方硬编码重复字符串。
        const val DATABASE_NAME: String = "menu.db"
    }
}
