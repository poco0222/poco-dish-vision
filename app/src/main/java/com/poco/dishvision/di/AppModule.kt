/**
 * @file AppModule.kt
 * @author PopoY
 * @date 2026-03-18
 * @description 提供 app 壳层级依赖的 Hilt 模块。
 */
package com.poco.dishvision.di

import com.poco.dishvision.navigation.AppDestination
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 应用壳层依赖模块，当前仅提供导航起始目的地。
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /**
     * 提供应用启动后的默认目的地。
     *
     * @return Home destination。
     */
    @Provides
    @Singleton
    fun provideStartDestination(): AppDestination = AppDestination.Home
}
