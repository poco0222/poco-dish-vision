/**
 * @file app/src/main/java/com/poco/dishvision/MainActivity.kt
 * @author PopoY
 * @date 2026-03-18
 * @description Android TV 主入口 Activity，当前为最小可启动骨架。
 */
package com.poco.dishvision

import android.app.Activity
import android.os.Bundle

/**
 * Android TV 的 Launcher Activity。
 */
class MainActivity : Activity() {

    /**
     * Activity 创建回调。
     *
     * @param savedInstanceState 系统恢复状态；首次启动时为 null。
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Task 1 仅保证可启动，不引入 Task 2+ 的 UI 与导航实现。
    }
}
