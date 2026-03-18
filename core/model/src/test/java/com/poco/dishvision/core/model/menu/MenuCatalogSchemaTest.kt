/**
 * @file MenuCatalogSchemaTest.kt
 * @author PopoY
 * @date 2026-03-18
 * @description 菜单目录 fixture（夹具）Schema（结构）契约测试。
 */
package com.poco.dishvision.core.model.menu

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MenuCatalogSchemaTest {

    // 统一 JSON 解码策略：Phase 1 严格校验 fixture 结构，避免静默吞字段错误。
    private val strictJsonDecoder = Json {
        ignoreUnknownKeys = false
        explicitNulls = true
    }

    /**
     * 验证本地菜单 fixture 使用受支持的 schemaVersion（模式版本），并包含至少一个分类。
     */
    @Test
    fun `menu catalog fixture uses supported schema and contains categories`() {
        val catalog = loadCatalogFixture("menu/catalog.json")
        assertEquals(1, catalog.schemaVersion)
        assertTrue(catalog.categories.isNotEmpty())
    }

    /**
     * 从测试资源目录加载并解析菜单目录 fixture。
     *
     * @param path fixture 资源路径。
     * @return 解析后的菜单目录模型。
     */
    private fun loadCatalogFixture(path: String): MenuCatalog {
        val resourceStream = checkNotNull(this::class.java.classLoader?.getResourceAsStream(path)) {
            "Fixture resource not found: $path"
        }
        val rawJsonText = resourceStream.bufferedReader(Charsets.UTF_8).use { reader ->
            reader.readText()
        }
        return strictJsonDecoder.decodeFromString<MenuCatalog>(rawJsonText)
    }
}
