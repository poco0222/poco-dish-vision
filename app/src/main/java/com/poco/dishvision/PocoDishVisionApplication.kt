/**
 * @file app/src/main/java/com/poco/dishvision/PocoDishVisionApplication.kt
 * @author PopoY
 * @date 2026-03-18
 * @description 应用入口 Application，当前仅提供最小初始化骨架。
 */
package com.poco.dishvision

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * 应用级生命周期入口。
 */
@HiltAndroidApp
class PocoDishVisionApplication : Application() {

    /**
     * 进程启动时回调，后续任务将在此注入全局初始化逻辑。
     */
    override fun onCreate() {
        super.onCreate()
        // Task 4 仅完成 Hilt 进程初始化，业务初始化仍留到后续任务。
    }
}
