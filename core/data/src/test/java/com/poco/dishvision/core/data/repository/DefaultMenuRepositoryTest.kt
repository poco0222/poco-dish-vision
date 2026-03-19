/**
 * @file DefaultMenuRepositoryTest.kt
 * @author PopoY
 * @date 2026-03-18
 * @description 验证默认菜单仓储可输出导入后的目录数据流。
 */
package com.poco.dishvision.core.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.poco.dishvision.core.data.local.LocalMenuCatalogDataSource
import com.poco.dishvision.core.data.local.db.MenuDatabase
import com.poco.dishvision.core.data.local.importer.MenuCatalogImporter
import com.poco.dishvision.core.data.local.mapper.MenuEntityMapper
import com.poco.dishvision.core.model.menu.MenuCatalog
import java.nio.file.Files
import java.nio.file.Path
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DefaultMenuRepositoryTest {

    // 与模型层契约一致：测试内继续使用严格 JSON 解析策略。
    private val strictJson = Json {
        ignoreUnknownKeys = false
        explicitNulls = true
    }

    private lateinit var menuDatabase: MenuDatabase
    private lateinit var repository: MenuRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        menuDatabase = Room.inMemoryDatabaseBuilder(
            context,
            MenuDatabase::class.java,
        ).build()

        val menuEntityMapper = MenuEntityMapper(strictJson)
        val importer = MenuCatalogImporter(
            menuDatabase = menuDatabase,
            categoryDao = menuDatabase.menuCategoryDao(),
            itemDao = menuDatabase.menuItemDao(),
            metadataDao = menuDatabase.menuMetadataDao(),
            menuEntityMapper = menuEntityMapper,
        )
        val localDataSource = object : LocalMenuCatalogDataSource {
            override suspend fun loadCatalog(): MenuCatalog = loadCatalogFixture()
        }
        repository = DefaultMenuRepository(
            localDataSource = localDataSource,
            menuCatalogImporter = importer,
            metadataDao = menuDatabase.menuMetadataDao(),
            categoryDao = menuDatabase.menuCategoryDao(),
            itemDao = menuDatabase.menuItemDao(),
            menuEntityMapper = menuEntityMapper,
        )
    }

    @After
    fun tearDown() {
        menuDatabase.close()
    }

    @Test
    fun `repository emits imported catalog`() = runTest {
        repository.refreshFromLocalAsset()
        val catalog = repository.observeCatalog().first()
        assertEquals("POCO Dish Vision Kitchen", catalog.restaurantName)
    }

    /**
     * 从 app assets（资源）读取统一 fixture。
     *
     * @return 目录模型。
     */
    private fun loadCatalogFixture(): MenuCatalog {
        // 兼容 Gradle 在不同工作目录执行测试时的路径差异。
        val candidatePaths = listOf(
            Path.of("app/src/main/assets/menu/catalog.json"),
            Path.of("../app/src/main/assets/menu/catalog.json"),
            Path.of("../../app/src/main/assets/menu/catalog.json"),
        )
        val fixturePath = checkNotNull(candidatePaths.firstOrNull(Files::exists)) {
            "Catalog fixture not found from user.dir=${System.getProperty("user.dir")}"
        }
        val rawJsonText = Files.readString(fixturePath)
        return strictJson.decodeFromString<MenuCatalog>(rawJsonText)
    }
}
