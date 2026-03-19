# POCO Dish Vision Phase 1 Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 从零搭建 Android TV 工程骨架，完成本地菜单展示闭环与 `Hybrid` 交互模式的 MVP。

**Architecture:** Phase 1 只接入本地 `assets` 数据，但从一开始就按最终架构拆分模块与数据流，确保 `Local JSON -> Room -> Repository -> ViewModel -> UiState -> Compose for TV UI` 一次成型。UI 优先保证焦点路径、双模式切换与高位壁挂场景下的大屏可读性，首页采用中下视觉重心，浏览页采用左侧分类加底部 `detail dock` 的信息布局。

**Tech Stack:** Kotlin, Compose for TV, TV Material 3, Hilt, Room, DataStore, Coil, JUnit, Turbine, Compose UI Test, Macrobenchmark

## 执行状态

- 执行状态：`In Progress`
- 文档基线提交：`7373cfc`
- 当前工作分支：`codex-phase-1-acceptance-remediation`
- 当前检查点：`Acceptance remediation in progress`，当前已完成 `LazyRow` 对齐，`Phase 1` 性能基线明确为 `Macrobenchmark`，`Baseline Profiles` 延后到 `Phase 3`，人工 emulator 复核待补
- 执行备注：原 Phase 1 实现已完成首轮自动化验证；当前文档按验收修补结果持续回写，避免继续保留误导 review 结论的完成态表述
- 历史验证记录：`JAVA_HOME=/Users/PopoY/Applications/Android Studio.app/Contents/jbr/Contents/Home ANDROID_HOME=/Users/PopoY/Library/Android/sdk ANDROID_SDK_ROOT=/Users/PopoY/Library/Android/sdk ./gradlew --no-daemon :app:assembleDebug`，结果 `BUILD SUCCESSFUL`；`JAVA_HOME=/Users/PopoY/Applications/Android Studio.app/Contents/jbr/Contents/Home ANDROID_HOME=/Users/PopoY/Library/Android/sdk ANDROID_SDK_ROOT=/Users/PopoY/Library/Android/sdk ./gradlew --no-daemon :core:data:testDebugUnitTest --tests "com.poco.dishvision.core.data.local.MenuCatalogImporterTest"`，结果 `BUILD SUCCESSFUL`；`JAVA_HOME=/Users/PopoY/Applications/Android Studio.app/Contents/jbr/Contents/Home ANDROID_HOME=/Users/PopoY/Library/Android/sdk ANDROID_SDK_ROOT=/Users/PopoY/Library/Android/sdk ./gradlew --no-daemon :core:data:testDebugUnitTest --tests "com.poco.dishvision.core.data.repository.DefaultMenuRepositoryTest"`，结果 `BUILD SUCCESSFUL`；`JAVA_HOME=/Users/PopoY/Applications/Android Studio.app/Contents/jbr/Contents/Home ANDROID_HOME=/Users/PopoY/Library/Android/sdk ANDROID_SDK_ROOT=/Users/PopoY/Library/Android/sdk ./gradlew --no-daemon --no-build-cache :core:data:testDebugUnitTest`，结果 `BUILD SUCCESSFUL`；`JAVA_HOME=/Users/PopoY/Applications/Android Studio.app/Contents/jbr/Contents/Home ANDROID_HOME=/Users/PopoY/Library/Android/sdk ANDROID_SDK_ROOT=/Users/PopoY/Library/Android/sdk ./gradlew --no-daemon :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.poco.dishvision.AppNavigationSmokeTest`，结果 `BUILD SUCCESSFUL`；`JAVA_HOME=/Users/PopoY/Applications/Android Studio.app/Contents/jbr/Contents/Home ANDROID_HOME=/Users/PopoY/Library/Android/sdk ANDROID_SDK_ROOT=/Users/PopoY/Library/Android/sdk ./gradlew --no-daemon :feature:home:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.poco.dishvision.feature.home.HomeScreenTest`，结果 `BUILD SUCCESSFUL`；`JAVA_HOME=/Users/PopoY/Applications/Android Studio.app/Contents/jbr/Contents/Home ANDROID_HOME=/Users/PopoY/Library/Android/sdk ANDROID_SDK_ROOT=/Users/PopoY/Library/Android/sdk ./gradlew --no-daemon :feature:menu:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.poco.dishvision.feature.menu.BrowseScreenFocusTest`，结果 `BUILD SUCCESSFUL`；`JAVA_HOME=/Users/PopoY/Applications/Android Studio.app/Contents/jbr/Contents/Home ANDROID_HOME=/Users/PopoY/Library/Android/sdk ANDROID_SDK_ROOT=/Users/PopoY/Library/Android/sdk ./gradlew --no-daemon :feature:menu:testDebugUnitTest --tests "com.poco.dishvision.feature.menu.BrowseModeControllerTest"`，结果 `BUILD SUCCESSFUL`；`JAVA_HOME=/Users/PopoY/Applications/Android Studio.app/Contents/jbr/Contents/Home ANDROID_HOME=/Users/PopoY/Library/Android/sdk ANDROID_SDK_ROOT=/Users/PopoY/Library/Android/sdk ./gradlew --no-daemon :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.poco.dishvision.AppNavigationSmokeTest`，结果 `BUILD SUCCESSFUL`；`JAVA_HOME=/Users/PopoY/Applications/Android Studio.app/Contents/jbr/Contents/Home ANDROID_HOME=/Users/PopoY/Library/Android/sdk ANDROID_SDK_ROOT=/Users/PopoY/Library/Android/sdk ./gradlew --no-daemon :feature:settings:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.poco.dishvision.feature.settings.SettingsScreenTest`，结果 `BUILD SUCCESSFUL`；`JAVA_HOME=/Users/PopoY/Applications/Android Studio.app/Contents/jbr/Contents/Home ANDROID_HOME=/Users/PopoY/Library/Android/sdk ANDROID_SDK_ROOT=/Users/PopoY/Library/Android/sdk ./gradlew --no-daemon :benchmark:connectedCheck`，结果 `BUILD SUCCESSFUL`，并生成 `benchmark/build/outputs/connected_android_test_additional_output/benchmark/connected/TV55C(AVD) - 14/com.poco.dishvision.benchmark-benchmarkData.json`；`JAVA_HOME=/Users/PopoY/Applications/Android Studio.app/Contents/jbr/Contents/Home ANDROID_HOME=/Users/PopoY/Library/Android/sdk ANDROID_SDK_ROOT=/Users/PopoY/Library/Android/sdk ./gradlew --no-daemon :app:assembleDebug :core:model:test :core:data:testDebugUnitTest :feature:home:connectedDebugAndroidTest :feature:menu:connectedDebugAndroidTest :feature:settings:connectedDebugAndroidTest :benchmark:connectedCheck`，结果 `BUILD SUCCESSFUL`，并在 `TV55C(AVD) - 14` 上顺序完成 `feature:settings`、`feature:menu`、`feature:home` 与 `benchmark` 验证

---

## Phase 1 验收标准

- 应用可在 Android TV Emulator 安装并启动
- 首页默认进入 `Attract mode`
- 遥控器可切换到 `Browse mode`
- 浏览页可以查看分类、菜品卡片、底部 `detail dock` 与详情浮层
- 菜单数据来自本地 `assets`，并通过 `Room` 提供给 UI
- `Settings` 页面可看到当前数据源模式与本地数据状态
- 最少一条冷启动 `Macrobenchmark` 可运行，`Baseline Profiles` 明确延后到 `Phase 3`

## Phase 1 文件结构

- Create: `settings.gradle.kts`
- Create: `build.gradle.kts`
- Create: `gradle.properties`
- Create: `gradle/libs.versions.toml`
- Create: `app/build.gradle.kts`
- Create: `app/src/main/AndroidManifest.xml`
- Create: `app/src/main/java/com/poco/dishvision/PocoDishVisionApplication.kt`
- Create: `app/src/main/java/com/poco/dishvision/MainActivity.kt`
- Create: `app/src/main/java/com/poco/dishvision/di/AppModule.kt`
- Create: `app/src/main/java/com/poco/dishvision/navigation/AppDestination.kt`
- Create: `app/src/main/java/com/poco/dishvision/navigation/AppNavHost.kt`
- Create: `app/src/main/assets/menu/catalog.json`
- Create: `app/src/androidTest/java/com/poco/dishvision/AppNavigationSmokeTest.kt`
- Create: `core/model/build.gradle.kts`
- Create: `core/model/src/main/java/com/poco/dishvision/core/model/menu/MenuCatalog.kt`
- Create: `core/model/src/main/java/com/poco/dishvision/core/model/menu/MenuCategory.kt`
- Create: `core/model/src/main/java/com/poco/dishvision/core/model/menu/MenuItem.kt`
- Create: `core/model/src/main/java/com/poco/dishvision/core/model/menu/PriceInfo.kt`
- Create: `core/model/src/main/java/com/poco/dishvision/core/model/menu/AvailabilityWindow.kt`
- Create: `core/model/src/main/java/com/poco/dishvision/core/model/menu/DisplayBadge.kt`
- Create: `core/model/src/main/java/com/poco/dishvision/core/model/menu/ThemeConfig.kt`
- Create: `core/model/src/test/java/com/poco/dishvision/core/model/menu/MenuCatalogSchemaTest.kt`
- Create: `core/model/src/test/resources/menu/catalog.json`
- Create: `core/data/build.gradle.kts`
- Create: `core/data/src/main/java/com/poco/dishvision/core/data/di/DataModule.kt`
- Create: `core/data/src/main/java/com/poco/dishvision/core/data/local/AssetMenuLocalDataSource.kt`
- Create: `core/data/src/main/java/com/poco/dishvision/core/data/local/importer/MenuCatalogImporter.kt`
- Create: `core/data/src/main/java/com/poco/dishvision/core/data/local/db/MenuDatabase.kt`
- Create: `core/data/src/main/java/com/poco/dishvision/core/data/local/db/dao/MenuCategoryDao.kt`
- Create: `core/data/src/main/java/com/poco/dishvision/core/data/local/db/dao/MenuItemDao.kt`
- Create: `core/data/src/main/java/com/poco/dishvision/core/data/local/db/dao/MenuMetadataDao.kt`
- Create: `core/data/src/main/java/com/poco/dishvision/core/data/local/db/entity/MenuCategoryEntity.kt`
- Create: `core/data/src/main/java/com/poco/dishvision/core/data/local/db/entity/MenuItemEntity.kt`
- Create: `core/data/src/main/java/com/poco/dishvision/core/data/local/db/entity/MenuMetadataEntity.kt`
- Create: `core/data/src/main/java/com/poco/dishvision/core/data/local/mapper/MenuEntityMapper.kt`
- Create: `core/data/src/main/java/com/poco/dishvision/core/data/repository/MenuRepository.kt`
- Create: `core/data/src/main/java/com/poco/dishvision/core/data/repository/DefaultMenuRepository.kt`
- Create: `core/data/src/main/java/com/poco/dishvision/core/data/preferences/AppPreferences.kt`
- Create: `core/data/src/test/java/com/poco/dishvision/core/data/local/MenuCatalogImporterTest.kt`
- Create: `core/data/src/test/java/com/poco/dishvision/core/data/repository/DefaultMenuRepositoryTest.kt`
- Create: `core/ui/build.gradle.kts`
- Create: `core/ui/src/main/java/com/poco/dishvision/core/ui/theme/ColorTokens.kt`
- Create: `core/ui/src/main/java/com/poco/dishvision/core/ui/theme/Dimens.kt`
- Create: `core/ui/src/main/java/com/poco/dishvision/core/ui/theme/PocoTheme.kt`
- Create: `core/ui/src/main/java/com/poco/dishvision/core/ui/components/GlassSurface.kt`
- Create: `core/ui/src/main/java/com/poco/dishvision/core/ui/components/FocusableMenuCard.kt`
- Create: `core/ui/src/main/java/com/poco/dishvision/core/ui/components/PocoAsyncImage.kt`
- Create: `feature/home/build.gradle.kts`
- Create: `feature/home/src/main/java/com/poco/dishvision/feature/home/HomeRoute.kt`
- Create: `feature/home/src/main/java/com/poco/dishvision/feature/home/HomeViewModel.kt`
- Create: `feature/home/src/main/java/com/poco/dishvision/feature/home/HomeUiState.kt`
- Create: `feature/home/src/main/java/com/poco/dishvision/feature/home/AttractCarousel.kt`
- Create: `feature/home/src/androidTest/java/com/poco/dishvision/feature/home/HomeScreenTest.kt`
- Create: `feature/menu/build.gradle.kts`
- Create: `feature/menu/src/main/java/com/poco/dishvision/feature/menu/MenuRoute.kt`
- Create: `feature/menu/src/main/java/com/poco/dishvision/feature/menu/MenuViewModel.kt`
- Create: `feature/menu/src/main/java/com/poco/dishvision/feature/menu/MenuUiState.kt`
- Create: `feature/menu/src/main/java/com/poco/dishvision/feature/menu/CategoryRail.kt`
- Create: `feature/menu/src/main/java/com/poco/dishvision/feature/menu/MenuItemRow.kt`
- Create: `feature/menu/src/main/java/com/poco/dishvision/feature/menu/ItemDetailPanel.kt`
- Create: `feature/menu/src/main/java/com/poco/dishvision/feature/menu/BrowseModeController.kt`
- Create: `feature/menu/src/test/java/com/poco/dishvision/feature/menu/BrowseModeControllerTest.kt`
- Create: `feature/menu/src/androidTest/java/com/poco/dishvision/feature/menu/BrowseScreenFocusTest.kt`
- Create: `feature/settings/build.gradle.kts`
- Create: `feature/settings/src/main/java/com/poco/dishvision/feature/settings/SettingsRoute.kt`
- Create: `feature/settings/src/main/java/com/poco/dishvision/feature/settings/SettingsViewModel.kt`
- Create: `feature/settings/src/main/java/com/poco/dishvision/feature/settings/SettingsUiState.kt`
- Create: `feature/settings/src/androidTest/java/com/poco/dishvision/feature/settings/SettingsScreenTest.kt`
- Create: `benchmark/build.gradle.kts`
- Create: `benchmark/src/main/AndroidManifest.xml`
- Create: `benchmark/src/main/java/com/poco/dishvision/benchmark/StartupBenchmark.kt`

### Task 1: Bootstrap Multi-Module Android TV Project

**Files:**
- Create: `settings.gradle.kts`
- Create: `build.gradle.kts`
- Create: `gradle.properties`
- Create: `gradle/libs.versions.toml`
- Create: `app/build.gradle.kts`
- Create: `core/model/build.gradle.kts`
- Create: `core/data/build.gradle.kts`
- Create: `core/ui/build.gradle.kts`
- Create: `feature/home/build.gradle.kts`
- Create: `feature/menu/build.gradle.kts`
- Create: `feature/settings/build.gradle.kts`
- Create: `benchmark/build.gradle.kts`

- [x] **Step 1: Register all modules and plugin management**

```kotlin
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
```

- [x] **Step 2: Add version catalog entries for Compose for TV, Room, Hilt, Coil, benchmark**

```toml
[libraries]
androidx-tv-material = { module = "androidx.tv:tv-material", version.ref = "androidxTv" }
androidx-tv-material3 = { module = "androidx.tv:tv-material3", version.ref = "androidxTv" }
androidx-room-runtime = { module = "androidx.room:room-runtime", version.ref = "room" }
androidx-room-ktx = { module = "androidx.room:room-ktx", version.ref = "room" }
coil-compose = { module = "io.coil-kt:coil-compose", version.ref = "coil" }
```

- [x] **Step 3: Create minimal `app` skeleton with TV launcher activity**

```xml
<intent-filter>
    <action android:name="android.intent.action.MAIN" />
    <category android:name="android.intent.category.LEANBACK_LAUNCHER" />
</intent-filter>
```

- [x] **Step 4: Verify the project can sync and assemble**

Run: `./gradlew :app:assembleDebug`  
Expected: `BUILD SUCCESSFUL`

- [x] **Step 5: Commit**

```bash
git add settings.gradle.kts build.gradle.kts gradle.properties gradle/libs.versions.toml app core feature benchmark
git commit -m "初始化 Android TV 多模块工程骨架"
```

### Task 2: Define Domain Model and Local Fixture Schema

**Files:**
- Create: `core/model/src/main/java/com/poco/dishvision/core/model/menu/MenuCatalog.kt`
- Create: `core/model/src/main/java/com/poco/dishvision/core/model/menu/MenuCategory.kt`
- Create: `core/model/src/main/java/com/poco/dishvision/core/model/menu/MenuItem.kt`
- Create: `core/model/src/main/java/com/poco/dishvision/core/model/menu/PriceInfo.kt`
- Create: `core/model/src/main/java/com/poco/dishvision/core/model/menu/AvailabilityWindow.kt`
- Create: `core/model/src/main/java/com/poco/dishvision/core/model/menu/DisplayBadge.kt`
- Create: `core/model/src/main/java/com/poco/dishvision/core/model/menu/ThemeConfig.kt`
- Create: `core/model/src/test/java/com/poco/dishvision/core/model/menu/MenuCatalogSchemaTest.kt`
- Create: `core/model/src/test/resources/menu/catalog.json`
- Create: `app/src/main/assets/menu/catalog.json`

- [x] **Step 1: Write the failing schema test**

```kotlin
@Test
fun `menu catalog fixture uses supported schema and contains categories`() {
    val catalog = loadCatalogFixture("menu/catalog.json")
    assertEquals(1, catalog.schemaVersion)
    assertTrue(catalog.categories.isNotEmpty())
}
```

- [x] **Step 2: Run test to verify it fails**

Run: `./gradlew :core:model:testDebugUnitTest --tests "com.poco.dishvision.core.model.menu.MenuCatalogSchemaTest"`  
Expected: FAIL with missing model or fixture loader error

- [x] **Step 3: Implement domain models and a real sample fixture**

```kotlin
data class MenuCatalog(
    val schemaVersion: Int,
    val catalogId: String,
    val restaurantName: String,
    val lastUpdatedAt: Instant,
    val themeConfig: ThemeConfig,
    val categories: List<MenuCategory>,
)
```

同时将同一份样例 JSON 写入：
- `core/model/src/test/resources/menu/catalog.json`
- `app/src/main/assets/menu/catalog.json`

- [x] **Step 4: Re-run the model test**

Run: `./gradlew :core:model:testDebugUnitTest --tests "com.poco.dishvision.core.model.menu.MenuCatalogSchemaTest"`  
Expected: PASS

- [x] **Step 5: Commit**

```bash
git add core/model app/src/main/assets/menu/catalog.json
git commit -m "定义菜单领域模型与本地 fixture"
```

### Task 3: Build Room Persistence and Local Repository

**Files:**
- Create: `core/data/src/main/java/com/poco/dishvision/core/data/local/AssetMenuLocalDataSource.kt`
- Create: `core/data/src/main/java/com/poco/dishvision/core/data/local/importer/MenuCatalogImporter.kt`
- Create: `core/data/src/main/java/com/poco/dishvision/core/data/local/db/MenuDatabase.kt`
- Create: `core/data/src/main/java/com/poco/dishvision/core/data/local/db/dao/MenuCategoryDao.kt`
- Create: `core/data/src/main/java/com/poco/dishvision/core/data/local/db/dao/MenuItemDao.kt`
- Create: `core/data/src/main/java/com/poco/dishvision/core/data/local/db/dao/MenuMetadataDao.kt`
- Create: `core/data/src/main/java/com/poco/dishvision/core/data/local/db/entity/MenuCategoryEntity.kt`
- Create: `core/data/src/main/java/com/poco/dishvision/core/data/local/db/entity/MenuItemEntity.kt`
- Create: `core/data/src/main/java/com/poco/dishvision/core/data/local/db/entity/MenuMetadataEntity.kt`
- Create: `core/data/src/main/java/com/poco/dishvision/core/data/local/mapper/MenuEntityMapper.kt`
- Create: `core/data/src/main/java/com/poco/dishvision/core/data/repository/MenuRepository.kt`
- Create: `core/data/src/main/java/com/poco/dishvision/core/data/repository/DefaultMenuRepository.kt`
- Create: `core/data/src/test/java/com/poco/dishvision/core/data/local/MenuCatalogImporterTest.kt`
- Create: `core/data/src/test/java/com/poco/dishvision/core/data/repository/DefaultMenuRepositoryTest.kt`

- [x] **Step 1: Write the failing importer test**

```kotlin
@Test
fun `importer stores fixture categories and items into room`() = runTest {
    importer.import(catalogFixture)
    assertThat(categoryDao.observeAll().first()).hasSize(3)
    assertThat(itemDao.observeByCategory("mains").first()).isNotEmpty()
}
```

- [x] **Step 2: Run test to verify it fails**

Run: `./gradlew :core:data:testDebugUnitTest --tests "com.poco.dishvision.core.data.local.MenuCatalogImporterTest"`  
Expected: FAIL with missing importer or DAO implementation
Actual: FAIL，`MenuCatalogImporter.importCatalog(...)` 未实现，fresh run 结果为 `:core:data:compileDebugUnitTestKotlin FAILED`

- [x] **Step 3: Implement entities, DAO, importer, repository**

```kotlin
interface MenuRepository {
    fun observeCatalog(): Flow<MenuCatalog>
    suspend fun refreshFromLocalAsset()
}
```

- [x] **Step 4: Write and pass repository flow test**

```kotlin
@Test
fun `repository emits imported catalog`() = runTest {
    repository.refreshFromLocalAsset()
    val catalog = repository.observeCatalog().first()
    assertEquals("POCO Dish Vision Kitchen", catalog.restaurantName)
}
```

Run: `./gradlew :core:data:testDebugUnitTest --tests "com.poco.dishvision.core.data.repository.DefaultMenuRepositoryTest"`  
Expected: PASS
Actual: PASS，fresh run 结果为 `:core:data:testDebugUnitTest BUILD SUCCESSFUL`

- [x] **Step 5: Commit**

```bash
git add core/data
git commit -m "实现本地菜单导入与 Room Repository"
```

### Task 4: Wire Application Shell, DI, and Navigation

**Files:**
- Create: `app/src/main/java/com/poco/dishvision/PocoDishVisionApplication.kt`
- Create: `app/src/main/java/com/poco/dishvision/MainActivity.kt`
- Create: `app/src/main/java/com/poco/dishvision/di/AppModule.kt`
- Create: `app/src/main/java/com/poco/dishvision/navigation/AppDestination.kt`
- Create: `app/src/main/java/com/poco/dishvision/navigation/AppNavHost.kt`
- Create: `app/src/androidTest/java/com/poco/dishvision/AppNavigationSmokeTest.kt`
- Create: `core/data/src/main/java/com/poco/dishvision/core/data/di/DataModule.kt`

- [x] **Step 1: Write the failing smoke navigation test**

```kotlin
@Test
fun app_launch_shows_home_destination() {
    composeTestRule.onNodeWithTag("home-screen").assertExists()
}
```

- [x] **Step 2: Run test to verify it fails**

Run: `./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.poco.dishvision.AppNavigationSmokeTest`  
Expected: FAIL because root navigation graph is missing
Actual: FAIL，fresh run 结果为 `No compose hierarchies found in the app`

- [x] **Step 3: Implement `Application`, Hilt setup, and `NavHost`**

```kotlin
setContent {
    AppNavHost(startDestination = startDestination)
}
```

- [x] **Step 4: Re-run smoke test on emulator**

Run: `./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.poco.dishvision.AppNavigationSmokeTest`  
Expected: PASS
Actual: PASS，fresh run 结果 `BUILD SUCCESSFUL`

- [x] **Step 5: Commit**

```bash
git add app core/data/src/main/java/com/poco/dishvision/core/data/di
git commit -m "接入 Hilt 与应用导航壳层"
```

### Task 5: Build `Attract mode` Home Screen with Lower Visual Center

**Files:**
- Create: `feature/home/src/main/java/com/poco/dishvision/feature/home/HomeRoute.kt`
- Create: `feature/home/src/main/java/com/poco/dishvision/feature/home/HomeViewModel.kt`
- Create: `feature/home/src/main/java/com/poco/dishvision/feature/home/HomeUiState.kt`
- Create: `feature/home/src/main/java/com/poco/dishvision/feature/home/AttractCarousel.kt`
- Create: `feature/home/src/androidTest/java/com/poco/dishvision/feature/home/HomeScreenTest.kt`
- Create: `core/ui/src/main/java/com/poco/dishvision/core/ui/components/PocoAsyncImage.kt`

- [x] **Step 1: Write the failing home UI test**

```kotlin
@Test
fun home_screen_renders_featured_section() {
    composeTestRule.onNodeWithText("本店推荐").assertExists()
    composeTestRule.onNodeWithTag("attract-carousel").assertExists()
    composeTestRule.onNodeWithTag("home-lower-hero-zone").assertExists()
}
```

- [x] **Step 2: Run test to verify it fails**

Run: `./gradlew :feature:home:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.poco.dishvision.feature.home.HomeScreenTest`  
Expected: FAIL with missing route or UI tags
Actual: FAIL，`HomeRoute` 缺失，fresh run 结果为 `:feature:home:compileDebugAndroidTestKotlin FAILED`

- [x] **Step 3: Implement `HomeViewModel` and carousel UI backed by repository state**

```kotlin
data class HomeUiState(
    val heroTitle: String,
    val heroSubtitle: String,
    val featuredItems: List<MenuItem>,
    val autoAdvanceEnabled: Boolean,
)
```

实现要求：
- 首屏主文案、价格提示和推荐内容落在中下区域
- 顶部仅保留轻量品牌或状态栏
- 底部提供“按方向键浏览菜单”之类的轻提示

- [x] **Step 4: Re-run the home UI test**

Run: `./gradlew :feature:home:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.poco.dishvision.feature.home.HomeScreenTest`  
Expected: PASS
Actual: PASS，fresh run 结果 `BUILD SUCCESSFUL`

- [x] **Step 5: Commit**

```bash
git add feature/home core/ui/src/main/java/com/poco/dishvision/core/ui/components/PocoAsyncImage.kt
git commit -m "实现首页 attract 模式基础展示"
```

### Task 6: Build Browse Screen, Category Rail, and Bottom Detail Dock

**Files:**
- Create: `feature/menu/src/main/java/com/poco/dishvision/feature/menu/MenuRoute.kt`
- Create: `feature/menu/src/main/java/com/poco/dishvision/feature/menu/MenuViewModel.kt`
- Create: `feature/menu/src/main/java/com/poco/dishvision/feature/menu/MenuUiState.kt`
- Create: `feature/menu/src/main/java/com/poco/dishvision/feature/menu/CategoryRail.kt`
- Create: `feature/menu/src/main/java/com/poco/dishvision/feature/menu/MenuItemRow.kt`
- Create: `feature/menu/src/main/java/com/poco/dishvision/feature/menu/ItemDetailPanel.kt`
- Create: `feature/menu/src/androidTest/java/com/poco/dishvision/feature/menu/BrowseScreenFocusTest.kt`
- Create: `core/ui/src/main/java/com/poco/dishvision/core/ui/components/FocusableMenuCard.kt`

- [x] **Step 1: Write the failing browse focus test**

```kotlin
@Test
fun dpad_right_moves_focus_from_category_to_first_menu_item() {
    composeTestRule.onNodeWithTag("category-mains").requestFocus()
    composeTestRule.onRoot().performKeyInput { keyDown(Key.DirectionRight); keyUp(Key.DirectionRight) }
    composeTestRule.onNodeWithTag("menu-item-mains-0").assertIsFocused()
}
```

- [x] **Step 2: Run test to verify it fails**

Run: `./gradlew :feature:menu:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.poco.dishvision.feature.menu.BrowseScreenFocusTest`  
Expected: FAIL with missing focusable nodes
Actual: FAIL，初次失败先暴露 `feature:menu` Compose Compiler plugin 缺口；修正测试基础设施后，fresh run 失败点收敛到 `MenuRoute` 缺失

- [x] **Step 3: Implement menu route, category rail, item row, bottom `detail dock`, and expandable detail panel**

```kotlin
data class MenuUiState(
    val selectedCategoryId: String,
    val categories: List<MenuCategory>,
    val visibleItems: List<MenuItem>,
    val focusedItemId: String?,
)
```

实现要求：
- 浏览态底部持续显示当前焦点菜品的名称、价格、标签和短描述
- 避免在浏览态把详情信息固定放在右侧边缘
- 按确认键可从底部 `detail dock` 进入详情浮层

- [x] **Step 4: Re-run the browse focus test**

Run: `./gradlew :feature:menu:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.poco.dishvision.feature.menu.BrowseScreenFocusTest`  
Expected: PASS
Actual: PASS，使用 `TV55C` emulator（Android TV AVD）fresh run 结果 `BUILD SUCCESSFUL`

- [x] **Step 5: Commit**

```bash
git add feature/menu core/ui/src/main/java/com/poco/dishvision/core/ui/components/FocusableMenuCard.kt
git commit -m "实现浏览页焦点导航与底部详情区"
```

### Task 7: Implement Hybrid Mode State Machine

**Files:**
- Create: `feature/menu/src/main/java/com/poco/dishvision/feature/menu/BrowseModeController.kt`
- Create: `feature/menu/src/test/java/com/poco/dishvision/feature/menu/BrowseModeControllerTest.kt`
- Modify: `feature/home/src/main/java/com/poco/dishvision/feature/home/HomeRoute.kt`
- Modify: `feature/menu/src/main/java/com/poco/dishvision/feature/menu/MenuRoute.kt`
- Modify: `app/src/main/java/com/poco/dishvision/navigation/AppNavHost.kt`

- [x] **Step 1: Write the failing state machine test**

```kotlin
@Test
fun `user input enters browse mode and idle timeout returns to attract mode`() = runTest {
    controller.onUserInteraction()
    assertEquals(UiMode.Browse, controller.mode.value)
    advanceTimeBy(15_000)
    assertEquals(UiMode.Attract, controller.mode.value)
}
```

- [x] **Step 2: Run test to verify it fails**

Run: `./gradlew :feature:menu:testDebugUnitTest --tests "com.poco.dishvision.feature.menu.BrowseModeControllerTest"`  
Expected: FAIL with missing controller implementation
Actual: FAIL，fresh run 报告 `BrowseModeController` / `UiMode` 未解析，符合状态机尚未实现的预期

- [x] **Step 3: Implement timeout-based mode controller and route switching**

```kotlin
class BrowseModeController(
    private val idleTimeoutMs: Long,
    private val scope: CoroutineScope,
)
```

- [x] **Step 4: Re-run controller test and smoke navigation**

Run: `./gradlew :feature:menu:testDebugUnitTest --tests "com.poco.dishvision.feature.menu.BrowseModeControllerTest"`  
Expected: PASS
Actual: PASS，fresh run 结果 `BUILD SUCCESSFUL`

Run: `./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.poco.dishvision.AppNavigationSmokeTest`  
Expected: PASS
Actual: PASS，使用 `TV55C` emulator（Android TV AVD）fresh run 结果 `BUILD SUCCESSFUL`

- [x] **Step 5: Commit**

```bash
git add feature/menu feature/home app/src/main/java/com/poco/dishvision/navigation/AppNavHost.kt
git commit -m "实现 attract 与 browse 双模式状态机"
```

### Task 8: Add Settings Screen and Benchmark Scaffold

**Files:**
- Create: `feature/settings/src/main/java/com/poco/dishvision/feature/settings/SettingsRoute.kt`
- Create: `feature/settings/src/main/java/com/poco/dishvision/feature/settings/SettingsViewModel.kt`
- Create: `feature/settings/src/main/java/com/poco/dishvision/feature/settings/SettingsUiState.kt`
- Create: `feature/settings/src/androidTest/java/com/poco/dishvision/feature/settings/SettingsScreenTest.kt`
- Create: `core/data/src/main/java/com/poco/dishvision/core/data/preferences/AppPreferences.kt`
- Create: `benchmark/src/main/AndroidManifest.xml`
- Create: `benchmark/src/main/java/com/poco/dishvision/benchmark/StartupBenchmark.kt`

- [x] **Step 1: Write the failing settings persistence test**

```kotlin
@Test
fun settings_screen_shows_local_data_source_mode() {
    composeTestRule.onNodeWithText("当前数据源").assertExists()
    composeTestRule.onNodeWithText("Local").assertExists()
}
```

- [x] **Step 2: Run test to verify it fails**

Run: `./gradlew :feature:settings:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.poco.dishvision.feature.settings.SettingsScreenTest`  
Expected: FAIL with missing settings route
Actual: FAIL，fresh run 失败点为 `SettingsRoute` 未解析，符合设置页尚未实现的预期

- [x] **Step 3: Implement `SettingsViewModel`, `AppPreferences`, and benchmark module**

```kotlin
data class SettingsUiState(
    val sourceModeLabel: String,
    val lastRefreshAt: String?,
    val catalogVersion: String?,
)
```

- [x] **Step 4: Run settings test and startup benchmark smoke**

Run: `./gradlew :feature:settings:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.poco.dishvision.feature.settings.SettingsScreenTest`  
Expected: PASS
Actual: PASS，使用 `TV55C` emulator（Android TV AVD）fresh run 结果 `BUILD SUCCESSFUL`

Run: `./gradlew :benchmark:connectedCheck`  
Expected: PASS with at least one startup benchmark result
Actual: PASS，fresh run 结果 `BUILD SUCCESSFUL`，并生成 `benchmark/build/outputs/connected_android_test_additional_output/benchmark/connected/TV55C(AVD) - 14/com.poco.dishvision.benchmark-benchmarkData.json` 与 `.perfetto-trace`

- [x] **Step 5: Commit**

```bash
git add feature/settings core/data/src/main/java/com/poco/dishvision/core/data/preferences benchmark
git commit -m "补齐设置页与首轮性能基线"
```

## Phase 1 Final Verification

- [x] Run: `./gradlew :app:assembleDebug`
- [x] Run: `./gradlew :core:model:test`
- [x] Run: `./gradlew :core:data:testDebugUnitTest`
- [x] Run: `./gradlew :feature:home:connectedDebugAndroidTest`
- [x] Run: `./gradlew :feature:menu:connectedDebugAndroidTest`
- [x] Run: `./gradlew :feature:settings:connectedDebugAndroidTest`
- [x] Run: `./gradlew :benchmark:connectedCheck`
- [ ] Manual check on emulator: 本轮未执行人工复核，以下条目保留待确认
  - 应用启动进入 `Attract mode`
  - 任意方向键能进入浏览态
  - `Back` 行为线性可预测
  - 重要文案位于中下区域而非顶部
  - 浏览态详情信息主要位于底部 `detail dock`
  - 文本在约 2 米观看距离下清晰可读
