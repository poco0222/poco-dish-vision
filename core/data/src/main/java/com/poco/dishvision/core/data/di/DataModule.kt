/**
 * @file DataModule.kt
 * @author PopoY
 * @date 2026-03-18
 * @description 提供 core:data 层基础对象工厂，供后续 DI（依赖注入）接入复用。
 */
package com.poco.dishvision.core.data.di

import com.poco.dishvision.core.data.local.mapper.MenuEntityMapper
import kotlinx.serialization.json.Json

/**
 * 数据层对象提供器。
 *
 * 说明：Task 3 保持轻量工厂形态，不展开 Hilt 注解细节。
 */
object DataModule {

    /**
     * 提供严格 JSON 解析器，避免静默吞掉 schema 字段问题。
     *
     * @return JSON 解析器实例。
     */
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
    fun provideMenuEntityMapper(json: Json): MenuEntityMapper {
        return MenuEntityMapper(json)
    }
}
