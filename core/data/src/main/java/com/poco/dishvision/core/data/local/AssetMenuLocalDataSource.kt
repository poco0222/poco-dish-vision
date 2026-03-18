/**
 * @file AssetMenuLocalDataSource.kt
 * @author PopoY
 * @date 2026-03-18
 * @description 从 app assets（本地资源）加载菜单目录数据的数据源实现。
 */
package com.poco.dishvision.core.data.local

import android.content.Context
import com.poco.dishvision.core.model.menu.MenuCatalog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

/**
 * 本地菜单数据源抽象，便于 repository（仓储）在测试中替换实现。
 */
interface LocalMenuCatalogDataSource {

    /**
     * 加载菜单目录。
     *
     * @return 解析后的菜单目录模型。
     */
    suspend fun loadCatalog(): MenuCatalog
}

/**
 * 基于 Android assets 的菜单数据源。
 *
 * @param context Android 上下文。
 * @param json JSON 解析器。
 * @param assetPath 菜单资源路径。
 */
class AssetMenuLocalDataSource(
    private val context: Context,
    private val json: Json,
    private val assetPath: String = DEFAULT_ASSET_PATH,
) : LocalMenuCatalogDataSource {

    override suspend fun loadCatalog(): MenuCatalog = withContext(Dispatchers.IO) {
        val rawCatalogJson = context.assets.open(assetPath).bufferedReader(Charsets.UTF_8).use { reader ->
            reader.readText()
        }
        json.decodeFromString<MenuCatalog>(rawCatalogJson)
    }

    companion object {

        // app assets 内菜单目录的默认路径。
        const val DEFAULT_ASSET_PATH: String = "menu/catalog.json"
    }
}
