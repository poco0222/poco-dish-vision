/**
 * @file app/src/main/java/com/poco/dishvision/MainActivity.kt
 * @author PopoY
 * @date 2026-03-18
 * @description Android TV 主入口 Activity，当前为最小可启动骨架。
 */
package com.poco.dishvision

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.poco.dishvision.core.data.repository.MenuRepository
import com.poco.dishvision.navigation.AppDestination
import com.poco.dishvision.navigation.AppNavHost
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Android TV 的 Launcher Activity。
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var startDestination: AppDestination

    @Inject
    lateinit var menuRepository: MenuRepository

    /**
     * Activity 创建回调。
     *
     * @param savedInstanceState 系统恢复状态；首次启动时为 null。
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppNavHost(
                startDestination = startDestination,
                menuRepository = menuRepository,
            )
        }
    }
}
