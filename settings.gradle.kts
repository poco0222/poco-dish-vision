/**
 * @file settings.gradle.kts
 * @author PopoY
 * @date 2026-03-18
 * @description 配置 Gradle plugin management、依赖仓库与多模块注册。
 */
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "POCO-DISH-VISION"

include(
    ":app",
    ":core:model",
    ":core:data",
    ":core:ui",
    ":feature:home",
    ":feature:menu",
    ":feature:settings",
    ":benchmark",
)
