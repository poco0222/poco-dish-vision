/**
 * @file DataModule.kt
 * @author PopoY
 * @date 2026-03-18
 * @description 提供 core:data 层的 Hilt 依赖装配。
 */
package com.poco.dishvision.core.data.di

import android.content.Context
import androidx.room.Room
import com.poco.dishvision.core.data.local.AssetMenuLocalDataSource
import com.poco.dishvision.core.data.local.LocalMenuCatalogDataSource
import com.poco.dishvision.core.data.local.db.MenuDatabase
import com.poco.dishvision.core.data.local.db.dao.MenuCategoryDao
import com.poco.dishvision.core.data.local.db.dao.MenuItemDao
import com.poco.dishvision.core.data.local.db.dao.MenuMetadataDao
import com.poco.dishvision.core.data.local.importer.MenuCatalogImporter
import com.poco.dishvision.core.data.local.mapper.MenuEntityMapper
import com.poco.dishvision.core.data.repository.DefaultMenuRepository
import com.poco.dishvision.core.data.repository.MenuRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.serialization.json.Json

/**
 * 数据层 Hilt 模块，负责装配 JSON、Room、导入器与仓储。
 */
@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    /**
     * 提供严格 JSON 解析器，避免静默吞掉 schema 字段问题。
     *
     * @return JSON 解析器实例。
     */
    @Provides
    @Singleton
    fun provideStrictJson(): Json {
        return Json {
            ignoreUnknownKeys = false
            explicitNulls = true
        }
    }

    /**
     * 提供菜单实体映射器。
     *
     * @param json JSON 解析器。
     * @return 实体映射器。
     */
    @Provides
    @Singleton
    fun provideMenuEntityMapper(json: Json): MenuEntityMapper {
        return MenuEntityMapper(json)
    }

    /**
     * 提供 Room 数据库实例。
     *
     * @param context Application context（应用上下文）。
     * @return 菜单数据库实例。
     */
    @Provides
    @Singleton
    fun provideMenuDatabase(@ApplicationContext context: Context): MenuDatabase {
        return Room.databaseBuilder(
            context,
            MenuDatabase::class.java,
            MenuDatabase.DATABASE_NAME,
        ).build()
    }

    /**
     * 提供本地 asset 数据源。
     *
     * @param context Application context（应用上下文）。
     * @param json JSON 解析器。
     * @return 本地菜单数据源。
     */
    @Provides
    @Singleton
    fun provideLocalMenuCatalogDataSource(
        @ApplicationContext context: Context,
        json: Json,
    ): LocalMenuCatalogDataSource {
        return AssetMenuLocalDataSource(context = context, json = json)
    }

    /**
     * 提供目录元数据 DAO。
     *
     * @param database 菜单数据库。
     * @return 元数据 DAO。
     */
    @Provides
    fun provideMenuMetadataDao(database: MenuDatabase): MenuMetadataDao = database.menuMetadataDao()

    /**
     * 提供分类 DAO。
     *
     * @param database 菜单数据库。
     * @return 分类 DAO。
     */
    @Provides
    fun provideMenuCategoryDao(database: MenuDatabase): MenuCategoryDao = database.menuCategoryDao()

    /**
     * 提供菜品 DAO。
     *
     * @param database 菜单数据库。
     * @return 菜品 DAO。
     */
    @Provides
    fun provideMenuItemDao(database: MenuDatabase): MenuItemDao = database.menuItemDao()

    /**
     * 提供菜单导入器。
     *
     * @param database 菜单数据库。
     * @param categoryDao 分类 DAO。
     * @param itemDao 菜品 DAO。
     * @param metadataDao 元数据 DAO。
     * @param menuEntityMapper 实体映射器。
     * @return 菜单导入器。
     */
    @Provides
    @Singleton
    fun provideMenuCatalogImporter(
        database: MenuDatabase,
        categoryDao: MenuCategoryDao,
        itemDao: MenuItemDao,
        metadataDao: MenuMetadataDao,
        menuEntityMapper: MenuEntityMapper,
    ): MenuCatalogImporter {
        return MenuCatalogImporter(
            menuDatabase = database,
            categoryDao = categoryDao,
            itemDao = itemDao,
            metadataDao = metadataDao,
            menuEntityMapper = menuEntityMapper,
        )
    }

    /**
     * 提供菜单仓储接口实现。
     *
     * @param localDataSource 本地数据源。
     * @param menuCatalogImporter 菜单导入器。
     * @param metadataDao 元数据 DAO。
     * @param categoryDao 分类 DAO。
     * @param itemDao 菜品 DAO。
     * @param menuEntityMapper 实体映射器。
     * @return 菜单仓储。
     */
    @Provides
    @Singleton
    fun provideMenuRepository(
        localDataSource: LocalMenuCatalogDataSource,
        menuCatalogImporter: MenuCatalogImporter,
        metadataDao: MenuMetadataDao,
        categoryDao: MenuCategoryDao,
        itemDao: MenuItemDao,
        menuEntityMapper: MenuEntityMapper,
    ): MenuRepository {
        return DefaultMenuRepository(
            localDataSource = localDataSource,
            menuCatalogImporter = menuCatalogImporter,
            metadataDao = metadataDao,
            categoryDao = categoryDao,
            itemDao = itemDao,
            menuEntityMapper = menuEntityMapper,
        )
    }
}
