/**
 * @file MenuRepository.kt
 * @author PopoY
 * @date 2026-03-18
 * @description 定义菜单数据统一访问入口（Repository 接口）。
 */
package com.poco.dishvision.core.data.repository

import com.poco.dishvision.core.model.menu.MenuCatalog
import kotlinx.coroutines.flow.Flow

/**
 * 菜单仓储接口，屏蔽本地来源与持久化细节。
 */
interface MenuRepository {

    /**
     * 观察当前可用菜单目录。
     *
     * @return 持续输出目录快照的 Flow（数据流）。
     */
    fun observeCatalog(): Flow<MenuCatalog>

    /**
     * 从本地 assets 刷新目录并写入 Room。
     */
    suspend fun refreshFromLocalAsset()
}
