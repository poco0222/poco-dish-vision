/**
 * @file MenuItemDao.kt
 * @author PopoY
 * @date 2026-03-18
 * @description 定义菜单菜品实体的 Room DAO（数据访问对象）。
 */
package com.poco.dishvision.core.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.poco.dishvision.core.data.local.db.entity.MenuItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MenuItemDao {

    /**
     * 观察全部菜品列表，按分类与分类内排序输出。
     */
    @Query("SELECT * FROM menu_items ORDER BY categoryId ASC, itemSortOrder ASC, itemId ASC")
    fun observeAll(): Flow<List<MenuItemEntity>>

    /**
     * 观察指定分类下的菜品列表。
     *
     * @param categoryId 分类唯一标识。
     */
    @Query("SELECT * FROM menu_items WHERE categoryId = :categoryId ORDER BY itemSortOrder ASC, itemId ASC")
    fun observeByCategory(categoryId: String): Flow<List<MenuItemEntity>>

    /**
     * 批量写入菜品，存在冲突时以新数据覆盖。
     *
     * @param items 菜品实体集合。
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<MenuItemEntity>)

    /**
     * 清空菜品表。
     */
    @Query("DELETE FROM menu_items")
    suspend fun clearAll()
}
