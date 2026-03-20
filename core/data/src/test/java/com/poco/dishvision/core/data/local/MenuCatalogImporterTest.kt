/**
 * @file MenuCatalogImporterTest.kt
 * @author PopoY
 * @date 2026-03-18
 * @description 验证菜单 importer（导入器）可将 fixture（夹具）写入 Room（本地数据库）。
 */
package com.poco.dishvision.core.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.poco.dishvision.core.data.local.db.MenuDatabase
import com.poco.dishvision.core.data.local.db.dao.MenuCategoryDao
import com.poco.dishvision.core.data.local.db.dao.MenuItemDao
import com.poco.dishvision.core.data.local.db.dao.MenuMetadataDao
import com.poco.dishvision.core.data.local.importer.MenuCatalogImporter
import com.poco.dishvision.core.data.local.mapper.MenuEntityMapper
import com.poco.dishvision.core.model.menu.MenuCatalog
import java.nio.file.Files
import java.nio.file.Path
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class MenuCatalogImporterTest {

    // 统一 JSON 解析配置，严格校验 fixture 与模型契约。
    private val strictJson = Json {
        ignoreUnknownKeys = false
        explicitNulls = true
    }

    private lateinit var menuDatabase: MenuDatabase
    private lateinit var categoryDao: MenuCategoryDao
    private lateinit var itemDao: MenuItemDao
    private lateinit var metadataDao: MenuMetadataDao
    private lateinit var importer: MenuCatalogImporter

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        menuDatabase = Room.inMemoryDatabaseBuilder(
            context,
            MenuDatabase::class.java,
        ).build()
        categoryDao = menuDatabase.menuCategoryDao()
        itemDao = menuDatabase.menuItemDao()
        metadataDao = menuDatabase.menuMetadataDao()
        importer = MenuCatalogImporter(
            menuDatabase = menuDatabase,
            categoryDao = categoryDao,
            itemDao = itemDao,
            metadataDao = metadataDao,
            menuEntityMapper = MenuEntityMapper(strictJson),
        )
    }

    @After
    fun tearDown() {
        menuDatabase.close()
    }

    @Test
    fun `importer stores fixture categories and items into room`() = runTest {
        importer.importCatalog(loadCatalogFixture())
        val categories = categoryDao.observeAll().first()
        val allItems = itemDao.observeAll().first()

        assertThat(categories).hasSize(5)
        assertThat(categories.map { category -> category.displayName }).containsExactly(
            "招牌热炒",
            "香辣口味",
            "鱼鲜大菜",
            "家常土菜",
            "风味小菜",
        ).inOrder()
        assertThat(allItems).hasSize(44)
        assertThat(itemDao.observeByCategory("hot-stir-fry").first()).hasSize(9)
        assertThat(itemDao.observeByCategory("spicy").first()).hasSize(9)
        assertThat(itemDao.observeByCategory("fish").first()).hasSize(7)
        assertThat(itemDao.observeByCategory("home-style").first()).hasSize(10)
        assertThat(itemDao.observeByCategory("side").first()).hasSize(9)
    }

    /**
     * 从 app assets（资源）加载 catalog fixture，避免重复维护测试数据。
     *
     * @return 解析后的 MenuCatalog 领域模型。
     */
    private fun loadCatalogFixture(): MenuCatalog {
        // 兼容 Gradle 以模块目录执行测试的场景，按候选路径逐个探测。
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
