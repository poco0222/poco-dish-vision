/**
 * @file MenuCategoryDao.kt
 * @author PopoY
 * @date 2026-03-18
 * @description 定义菜单分类实体的 Room DAO（数据访问对象）。
 */
package com.poco.dishvision.core.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.poco.dishvision.core.data.local.db.entity.MenuCategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MenuCategoryDao {

    /**
     * 观察全部分类列表，按 sortOrder 保持稳定顺序。
     */
    @Query("SELECT * FROM menu_categories ORDER BY sortOrder ASC, categoryId ASC")
    fun observeAll(): Flow<List<MenuCategoryEntity>>

    /**
     * 批量写入分类，存在冲突时以新数据覆盖。
     *
     * @param categories 分类实体集合。
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(categories: List<MenuCategoryEntity>)

    /**
     * 清空分类表。
     */
    @Query("DELETE FROM menu_categories")
    suspend fun clearAll()
}
