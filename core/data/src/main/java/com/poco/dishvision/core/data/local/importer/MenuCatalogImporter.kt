/**
 * @file MenuCatalogImporter.kt
 * @author PopoY
 * @date 2026-03-18
 * @description 将 MenuCatalog（目录模型）原子化导入 Room（本地数据库）。
 */
package com.poco.dishvision.core.data.local.importer

import androidx.room.withTransaction
import com.poco.dishvision.core.data.local.db.MenuDatabase
import com.poco.dishvision.core.data.local.db.dao.MenuCategoryDao
import com.poco.dishvision.core.data.local.db.dao.MenuItemDao
import com.poco.dishvision.core.data.local.db.dao.MenuMetadataDao
import com.poco.dishvision.core.data.local.mapper.MenuEntityMapper
import com.poco.dishvision.core.model.menu.MenuCatalog

/**
 * 菜单导入器。策略为全量替换，保证 catalog（目录）快照一致性。
 *
 * @param menuDatabase Room 数据库实例。
 * @param categoryDao 分类 DAO。
 * @param itemDao 菜品 DAO。
 * @param metadataDao 元数据 DAO。
 * @param menuEntityMapper 实体映射器。
 */
class MenuCatalogImporter(
    private val menuDatabase: MenuDatabase,
    private val categoryDao: MenuCategoryDao,
    private val itemDao: MenuItemDao,
    private val metadataDao: MenuMetadataDao,
    private val menuEntityMapper: MenuEntityMapper,
) {

    /**
     * 将目录模型导入数据库。导入过程在单事务内执行。
     *
     * @param catalog 待导入目录模型。
     */
    suspend fun importCatalog(catalog: MenuCatalog) {
        val importTimestamp = System.currentTimeMillis()
        val metadataEntity = menuEntityMapper.toMetadataEntity(catalog, importTimestamp)
        val categoryEntities = menuEntityMapper.toCategoryEntities(catalog)
        val itemEntities = menuEntityMapper.toItemEntities(catalog)

        menuDatabase.withTransaction {
            // 先清子表再清父表，避免外键约束冲突。
            itemDao.clearAll()
            categoryDao.clearAll()
            metadataDao.clearAll()

            metadataDao.upsert(metadataEntity)
            categoryDao.upsertAll(categoryEntities)
            itemDao.upsertAll(itemEntities)
        }
    }
}
