/**
 * @file MenuMetadataDao.kt
 * @author PopoY
 * @date 2026-03-18
 * @description 定义菜单元数据实体的 Room DAO（数据访问对象）。
 */
package com.poco.dishvision.core.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.poco.dishvision.core.data.local.db.entity.MenuMetadataEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MenuMetadataDao {

    /**
     * 观察最新导入的目录元数据。
     */
    @Query("SELECT * FROM menu_metadata ORDER BY importedAtEpochMilli DESC LIMIT 1")
    fun observe(): Flow<MenuMetadataEntity?>

    /**
     * 同步读取最新目录元数据。
     */
    @Query("SELECT * FROM menu_metadata ORDER BY importedAtEpochMilli DESC LIMIT 1")
    suspend fun getLatestOrNull(): MenuMetadataEntity?

    /**
     * 写入目录元数据，catalogId 冲突时覆盖。
     *
     * @param metadata 目录元数据实体。
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(metadata: MenuMetadataEntity)

    /**
     * 清空目录元数据表。
     */
    @Query("DELETE FROM menu_metadata")
    suspend fun clearAll()
}
