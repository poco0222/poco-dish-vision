# POCO Dish Vision Phase 1 Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 从零搭建 Android TV 工程骨架，完成本地菜单展示闭环与 `Hybrid` 交互模式的 MVP。

**Architecture:** Phase 1 只接入本地 `assets` 数据，但从一开始就按最终架构拆分模块与数据流，确保 `Local JSON -> Room -> Repository -> ViewModel -> UiState -> Compose for TV UI` 一次成型。UI 优先保证焦点路径、双模式切换与高位壁挂场景下的大屏可读性，首页采用中下视觉重心，浏览页采用左侧分类加底部 `detail dock` 的信息布局。

**Tech Stack:** Kotlin, Compose for TV, TV Material 3, Hilt, Room, DataStore, Coil, JUnit, Turbine, Compose UI Test, Macrobenchmark

## 执行状态

- 执行状态：`In Progress`
- 文档基线提交：`7373cfc`
- 当前工作分支：`codex/phase-1-foundation-local-menu`
- 当前检查点：`Task 1` 已完成并通过 `:app:assembleDebug` 验证
- 执行备注：为满足 `./gradlew` 验证，已补入 `Gradle Wrapper`（包装器）支撑文件

---

## Phase 1 验收标准

- 应用可在 Android TV Emulator 安装并启动
- 首页默认进入 `Attract mode`
- 遥控器可切换到 `Browse mode`
- 浏览页可以查看分类、菜品卡片、底部 `detail dock` 与详情浮层
- 菜单数据来自本地 `assets`，并通过 `Room` 提供给 UI
- `Settings` 页面可看到当前数据源模式与本地数据状态
- 最少一条冷启动 benchmark 可运行

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

- [ ] **Step 1: Write the failing schema test**

```kotlin
@Test
fun `menu catalog fixture uses supported schema and contains categories`() {
    val catalog = loadCatalogFixture("menu/catalog.json")
    assertEquals(1, catalog.schemaVersion)
    assertTrue(catalog.categories.isNotEmpty())
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :core:model:test --tests "com.poco.dishvision.core.model.menu.MenuCatalogSchemaTest"`  
Expected: FAIL with missing model or fixture loader error

- [ ] **Step 3: Implement domain models and a real sample fixture**

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

- [ ] **Step 4: Re-run the model test**

Run: `./gradlew :core:model:test --tests "com.poco.dishvision.core.model.menu.MenuCatalogSchemaTest"`  
Expected: PASS

- [ ] **Step 5: Commit**

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

- [ ] **Step 1: Write the failing importer test**

```kotlin
@Test
fun `importer stores fixture categories and items into room`() = runTest {
    importer.import(catalogFixture)
    assertThat(categoryDao.observeAll().first()).hasSize(3)
    assertThat(itemDao.observeByCategory("mains").first()).isNotEmpty()
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :core:data:testDebugUnitTest --tests "com.poco.dishvision.core.data.local.MenuCatalogImporterTest"`  
Expected: FAIL with missing importer or DAO implementation

- [ ] **Step 3: Implement entities, DAO, importer, repository**

```kotlin
interface MenuRepository {
    fun observeCatalog(): Flow<MenuCatalog>
    suspend fun refreshFromLocalAsset()
}
```

- [ ] **Step 4: Write and pass repository flow test**

```kotlin
@Test
fun `repository emits imported catalog`() = runTest {
    repository.refreshFromLocalAsset()
    val catalog = repository.observeCatalog().first()
    assertEquals("POCO Dish Vision", catalog.restaurantName)
}
```

Run: `./gradlew :core:data:testDebugUnitTest --tests "com.poco.dishvision.core.data.repository.DefaultMenuRepositoryTest"`  
Expected: PASS

- [ ] **Step 5: Commit**

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

- [ ] **Step 1: Write the failing smoke navigation test**

```kotlin
@Test
fun app_launch_shows_home_destination() {
    composeTestRule.onNodeWithTag("home-screen").assertExists()
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.poco.dishvision.AppNavigationSmokeTest`  
Expected: FAIL because root navigation graph is missing

- [ ] **Step 3: Implement `Application`, Hilt setup, and `NavHost`**

```kotlin
setContent {
    PocoTheme {
        AppNavHost(startDestination = AppDestination.Home)
    }
}
```

- [ ] **Step 4: Re-run smoke test on emulator**

Run: `./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.poco.dishvision.AppNavigationSmokeTest`  
Expected: PASS

- [ ] **Step 5: Commit**

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

- [ ] **Step 1: Write the failing home UI test**

```kotlin
@Test
fun home_screen_renders_featured_section() {
    composeTestRule.onNodeWithText("本店推荐").assertExists()
    composeTestRule.onNodeWithTag("attract-carousel").assertExists()
    composeTestRule.onNodeWithTag("home-lower-hero-zone").assertExists()
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :feature:home:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.poco.dishvision.feature.home.HomeScreenTest`  
Expected: FAIL with missing route or UI tags

- [ ] **Step 3: Implement `HomeViewModel` and carousel UI backed by repository state**

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

- [ ] **Step 4: Re-run the home UI test**

Run: `./gradlew :feature:home:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.poco.dishvision.feature.home.HomeScreenTest`  
Expected: PASS

- [ ] **Step 5: Commit**

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

- [ ] **Step 1: Write the failing browse focus test**

```kotlin
@Test
fun dpad_right_moves_focus_from_category_to_first_menu_item() {
    composeTestRule.onNodeWithTag("category-mains").requestFocus()
    composeTestRule.onRoot().performKeyInput { keyDown(Key.DirectionRight); keyUp(Key.DirectionRight) }
    composeTestRule.onNodeWithTag("menu-item-mains-0").assertIsFocused()
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :feature:menu:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.poco.dishvision.feature.menu.BrowseScreenFocusTest`  
Expected: FAIL with missing focusable nodes

- [ ] **Step 3: Implement menu route, category rail, item row, bottom `detail dock`, and expandable detail panel**

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

- [ ] **Step 4: Re-run the browse focus test**

Run: `./gradlew :feature:menu:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.poco.dishvision.feature.menu.BrowseScreenFocusTest`  
Expected: PASS

- [ ] **Step 5: Commit**

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

- [ ] **Step 1: Write the failing state machine test**

```kotlin
@Test
fun `user input enters browse mode and idle timeout returns to attract mode`() = runTest {
    controller.onUserInteraction()
    assertEquals(UiMode.Browse, controller.mode.value)
    advanceTimeBy(15_000)
    assertEquals(UiMode.Attract, controller.mode.value)
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :feature:menu:testDebugUnitTest --tests "com.poco.dishvision.feature.menu.BrowseModeControllerTest"`  
Expected: FAIL with missing controller implementation

- [ ] **Step 3: Implement timeout-based mode controller and route switching**

```kotlin
class BrowseModeController(
    private val idleTimeoutMs: Long,
    private val scope: CoroutineScope,
)
```

- [ ] **Step 4: Re-run controller test and smoke navigation**

Run: `./gradlew :feature:menu:testDebugUnitTest --tests "com.poco.dishvision.feature.menu.BrowseModeControllerTest"`  
Expected: PASS

Run: `./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.poco.dishvision.AppNavigationSmokeTest`  
Expected: PASS

- [ ] **Step 5: Commit**

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

- [ ] **Step 1: Write the failing settings persistence test**

```kotlin
@Test
fun settings_screen_shows_local_data_source_mode() {
    composeTestRule.onNodeWithText("当前数据源").assertExists()
    composeTestRule.onNodeWithText("Local").assertExists()
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :feature:settings:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.poco.dishvision.feature.settings.SettingsScreenTest`  
Expected: FAIL with missing settings route

- [ ] **Step 3: Implement `SettingsViewModel`, `AppPreferences`, and benchmark module**

```kotlin
data class SettingsUiState(
    val sourceModeLabel: String,
    val lastRefreshAt: String?,
    val catalogVersion: String?,
)
```

- [ ] **Step 4: Run settings test and startup benchmark smoke**

Run: `./gradlew :feature:settings:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.poco.dishvision.feature.settings.SettingsScreenTest`  
Expected: PASS

Run: `./gradlew :benchmark:connectedCheck`  
Expected: PASS with at least one startup benchmark result

- [ ] **Step 5: Commit**

```bash
git add feature/settings core/data/src/main/java/com/poco/dishvision/core/data/preferences benchmark
git commit -m "补齐设置页与首轮性能基线"
```

## Phase 1 Final Verification

- [ ] Run: `./gradlew :app:assembleDebug`
- [ ] Run: `./gradlew :core:model:test`
- [ ] Run: `./gradlew :core:data:testDebugUnitTest`
- [ ] Run: `./gradlew :feature:home:connectedDebugAndroidTest`
- [ ] Run: `./gradlew :feature:menu:connectedDebugAndroidTest`
- [ ] Run: `./gradlew :feature:settings:connectedDebugAndroidTest`
- [ ] Run: `./gradlew :benchmark:connectedCheck`
- [ ] Manual check on emulator:
  - 应用启动进入 `Attract mode`
  - 任意方向键能进入浏览态
  - `Back` 行为线性可预测
  - 重要文案位于中下区域而非顶部
  - 浏览态详情信息主要位于底部 `detail dock`
  - 文本在约 2 米观看距离下清晰可读
