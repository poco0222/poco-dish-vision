/**
 * @file DefaultMenuRepository.kt
 * @author PopoY
 * @date 2026-03-18
 * @description 基于 Room + Asset 数据源的菜单仓储实现。
 */
package com.poco.dishvision.core.data.repository

import com.poco.dishvision.core.data.local.LocalMenuCatalogDataSource
import com.poco.dishvision.core.data.local.db.dao.MenuCategoryDao
import com.poco.dishvision.core.data.local.db.dao.MenuItemDao
import com.poco.dishvision.core.data.local.db.dao.MenuMetadataDao
import com.poco.dishvision.core.data.local.importer.MenuCatalogImporter
import com.poco.dishvision.core.data.local.mapper.MenuEntityMapper
import com.poco.dishvision.core.data.preferences.AppPreferences
import com.poco.dishvision.core.model.menu.MenuCatalog
import java.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull

/**
 * 默认菜单仓储实现：
 * 1) `refreshFromLocalAsset` 触发导入；
 * 2) `observeCatalog` 组合三张表重建目录模型。
 */
class DefaultMenuRepository(
    private val localDataSource: LocalMenuCatalogDataSource,
    private val menuCatalogImporter: MenuCatalogImporter,
    private val metadataDao: MenuMetadataDao,
    private val categoryDao: MenuCategoryDao,
    private val itemDao: MenuItemDao,
    private val menuEntityMapper: MenuEntityMapper,
    private val appPreferences: AppPreferences? = null,
) : MenuRepository {

    override fun observeCatalog(): Flow<MenuCatalog> {
        return combine(
            metadataDao.observe(),
            categoryDao.observeAll(),
            itemDao.observeAll(),
        ) { metadata, categories, items ->
            metadata?.let { nonNullMetadata ->
                menuEntityMapper.toCatalog(
                    metadata = nonNullMetadata,
                    categories = categories,
                    items = items,
                )
            }
        }.filterNotNull()
    }

    override suspend fun refreshFromLocalAsset() {
        val catalogFromAsset = localDataSource.loadCatalog()
        menuCatalogImporter.importCatalog(catalogFromAsset)
        appPreferences?.markLocalSourceRefreshed(refreshedAt = Instant.now())
    }
}
