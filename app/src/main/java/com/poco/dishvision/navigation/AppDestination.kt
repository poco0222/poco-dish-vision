/**
 * @file AppDestination.kt
 * @author PopoY
 * @date 2026-03-18
 * @description 定义应用一级导航目的地。
 */
package com.poco.dishvision.navigation

/**
 * 应用目的地枚举，后续任务会继续扩展 Browse / Settings 等页面。
 *
 * @property route NavHost 中使用的 route（路由）字符串。
 */
enum class AppDestination(
    val route: String,
) {
    Home(route = "home"),
    Settings(route = "settings"),
}
