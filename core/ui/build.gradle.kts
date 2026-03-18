/**
 * @file core/ui/build.gradle.kts
 * @author PopoY
 * @date 2026-03-18
 * @description UI 公共模块构建配置（Phase 1 仅搭建骨架）。
 */
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.poco.dishvision.core.ui"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        minSdk = 24
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(libs.androidx.tv.material3)
    implementation(libs.coil.compose)
}
