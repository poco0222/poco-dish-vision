# POCO Dish Vision Phase 2 Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在不重写 Phase 1 UI 主结构的前提下，为 TV 客户端接入可配置的 LAN 菜单同步能力，并保证失败时继续展示缓存。

**Architecture:** Phase 2 只在 `core:data` 与 `feature:settings` 进行主改动，UI 页面继续依赖统一的 `MenuRepository`。LAN 同步遵循 `health -> version -> full menu -> Room` 的拉取链路，通过 `Settings` 手动配置服务地址与同步行为。

**Tech Stack:** Kotlin, Retrofit or Ktor Client, Kotlinx Serialization, DataStore, Room, Coroutines, Flow, JUnit, MockWebServer, Compose UI Test

---

## Phase 2 验收标准

- 可在 `Settings` 中手动填写 LAN 服务地址
- App 启动时可执行健康检查与版本检查
- 远端版本变化时可拉取完整菜单并写入 `Room`
- 服务不可达时自动回退到上一个本地可用缓存
- UI 不需要区分本地菜单与 LAN 菜单来源

## Phase 2 主要文件

- Create: `core/data/src/main/java/com/poco/dishvision/core/data/remote/api/MenuService.kt`
- Create: `core/data/src/main/java/com/poco/dishvision/core/data/remote/dto/MenuCatalogDto.kt`
- Create: `core/data/src/main/java/com/poco/dishvision/core/data/remote/dto/MenuVersionDto.kt`
- Create: `core/data/src/main/java/com/poco/dishvision/core/data/remote/mapper/MenuRemoteMapper.kt`
- Create: `core/data/src/main/java/com/poco/dishvision/core/data/remote/LanMenuDataSource.kt`
- Create: `core/data/src/main/java/com/poco/dishvision/core/data/sync/MenuSyncCoordinator.kt`
- Create: `core/data/src/main/java/com/poco/dishvision/core/data/preferences/LanServerConfig.kt`
- Create: `core/data/src/test/java/com/poco/dishvision/core/data/remote/MenuServiceContractTest.kt`
- Create: `core/data/src/test/java/com/poco/dishvision/core/data/remote/LanMenuDataSourceTest.kt`
- Create: `core/data/src/test/java/com/poco/dishvision/core/data/sync/MenuSyncCoordinatorTest.kt`
- Modify: `core/data/src/main/java/com/poco/dishvision/core/data/repository/MenuRepository.kt`
- Modify: `core/data/src/main/java/com/poco/dishvision/core/data/repository/DefaultMenuRepository.kt`
- Modify: `core/data/src/main/java/com/poco/dishvision/core/data/di/DataModule.kt`
- Modify: `core/data/src/main/java/com/poco/dishvision/core/data/preferences/AppPreferences.kt`
- Modify: `feature/settings/src/main/java/com/poco/dishvision/feature/settings/SettingsRoute.kt`
- Modify: `feature/settings/src/main/java/com/poco/dishvision/feature/settings/SettingsViewModel.kt`
- Modify: `feature/settings/src/main/java/com/poco/dishvision/feature/settings/SettingsUiState.kt`
- Create: `feature/settings/src/main/java/com/poco/dishvision/feature/settings/LanServerConfigCard.kt`
- Create: `feature/settings/src/main/java/com/poco/dishvision/feature/settings/SyncStatusCard.kt`
- Create: `feature/settings/src/test/java/com/poco/dishvision/feature/settings/SettingsViewModelTest.kt`
- Modify: `feature/settings/src/androidTest/java/com/poco/dishvision/feature/settings/SettingsScreenTest.kt`

### Task 1: Define LAN Contract and DTO Mapping

**Files:**
- Create: `core/data/src/main/java/com/poco/dishvision/core/data/remote/api/MenuService.kt`
- Create: `core/data/src/main/java/com/poco/dishvision/core/data/remote/dto/MenuCatalogDto.kt`
- Create: `core/data/src/main/java/com/poco/dishvision/core/data/remote/dto/MenuVersionDto.kt`
- Create: `core/data/src/main/java/com/poco/dishvision/core/data/remote/mapper/MenuRemoteMapper.kt`
- Create: `core/data/src/test/java/com/poco/dishvision/core/data/remote/MenuServiceContractTest.kt`

- [ ] **Step 1: Write the failing contract test**

```kotlin
@Test
fun `version endpoint parses summary response`() = runTest {
    server.enqueue(MockResponse().setBody("""{"version":"2026.03.18","updatedAt":"2026-03-18T12:00:00Z"}"""))
    val version = service.getMenuVersion()
    assertEquals("2026.03.18", version.version)
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :core:data:testDebugUnitTest --tests "com.poco.dishvision.core.data.remote.MenuServiceContractTest"`  
Expected: FAIL with missing DTO or API interface

- [ ] **Step 3: Implement DTOs, API interface, and mapper**

```kotlin
interface MenuService {
    @GET("health")
    suspend fun getHealth(): HealthDto

    @GET("menu/version")
    suspend fun getMenuVersion(): MenuVersionDto

    @GET("menu")
    suspend fun getMenu(): MenuCatalogDto
}
```

- [ ] **Step 4: Re-run contract test**

Run: `./gradlew :core:data:testDebugUnitTest --tests "com.poco.dishvision.core.data.remote.MenuServiceContractTest"`  
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add core/data/src/main/java/com/poco/dishvision/core/data/remote core/data/src/test/java/com/poco/dishvision/core/data/remote
git commit -m "定义 LAN 接口契约与 DTO 映射"
```

### Task 2: Extend Preferences for LAN Server Configuration

**Files:**
- Create: `core/data/src/main/java/com/poco/dishvision/core/data/preferences/LanServerConfig.kt`
- Modify: `core/data/src/main/java/com/poco/dishvision/core/data/preferences/AppPreferences.kt`
- Create: `feature/settings/src/test/java/com/poco/dishvision/feature/settings/SettingsViewModelTest.kt`

- [ ] **Step 1: Write the failing ViewModel test**

```kotlin
@Test
fun `saving server config updates ui state`() = runTest {
    viewModel.saveServerConfig(host = "192.168.1.8", port = 8080)
    assertEquals("192.168.1.8:8080", viewModel.uiState.value.serverAddress)
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :feature:settings:testDebugUnitTest --tests "com.poco.dishvision.feature.settings.SettingsViewModelTest"`  
Expected: FAIL with missing LAN config fields

- [ ] **Step 3: Implement persistent LAN config model and preference accessors**

```kotlin
data class LanServerConfig(
    val host: String,
    val port: Int,
    val enabled: Boolean,
)
```

- [ ] **Step 4: Re-run the ViewModel test**

Run: `./gradlew :feature:settings:testDebugUnitTest --tests "com.poco.dishvision.feature.settings.SettingsViewModelTest"`  
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add core/data/src/main/java/com/poco/dishvision/core/data/preferences feature/settings/src/test/java/com/poco/dishvision/feature/settings/SettingsViewModelTest.kt
git commit -m "扩展 LAN 服务地址与配置持久化"
```

### Task 3: Implement LAN Data Source and Health/Version Flow

**Files:**
- Create: `core/data/src/main/java/com/poco/dishvision/core/data/remote/LanMenuDataSource.kt`
- Create: `core/data/src/test/java/com/poco/dishvision/core/data/remote/LanMenuDataSourceTest.kt`
- Modify: `core/data/src/main/java/com/poco/dishvision/core/data/di/DataModule.kt`

- [ ] **Step 1: Write the failing LAN data source test**

```kotlin
@Test
fun `data source returns remote catalog after healthy version check`() = runTest {
    enqueueHealthyServerWithMenu()
    val result = dataSource.fetchCatalogIfNewer("2026.03.17")
    assertTrue(result is FetchResult.Updated)
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :core:data:testDebugUnitTest --tests "com.poco.dishvision.core.data.remote.LanMenuDataSourceTest"`  
Expected: FAIL with missing data source implementation

- [ ] **Step 3: Implement health check, version check, and fetch logic**

```kotlin
sealed interface FetchResult {
    data class Updated(val catalog: MenuCatalog) : FetchResult
    data object NotModified : FetchResult
    data class Failure(val throwable: Throwable) : FetchResult
}
```

- [ ] **Step 4: Re-run LAN data source test**

Run: `./gradlew :core:data:testDebugUnitTest --tests "com.poco.dishvision.core.data.remote.LanMenuDataSourceTest"`  
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add core/data/src/main/java/com/poco/dishvision/core/data/remote core/data/src/main/java/com/poco/dishvision/core/data/di/DataModule.kt
git commit -m "实现 LAN 数据源与版本检查流程"
```

### Task 4: Sync LAN Data into Room with Fallback

**Files:**
- Create: `core/data/src/main/java/com/poco/dishvision/core/data/sync/MenuSyncCoordinator.kt`
- Create: `core/data/src/test/java/com/poco/dishvision/core/data/sync/MenuSyncCoordinatorTest.kt`
- Modify: `core/data/src/main/java/com/poco/dishvision/core/data/repository/MenuRepository.kt`
- Modify: `core/data/src/main/java/com/poco/dishvision/core/data/repository/DefaultMenuRepository.kt`

- [ ] **Step 1: Write the failing sync coordinator test**

```kotlin
@Test
fun `failed remote sync keeps last cached catalog`() = runTest {
    seedLocalCatalog(version = "2026.03.17")
    remoteResult = FetchResult.Failure(IOException("offline"))
    coordinator.sync()
    assertEquals("2026.03.17", metadataDao.observe().first().catalogVersion)
    assertEquals("POCO Dish Vision", repository.observeCatalog().first().restaurantName)
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :core:data:testDebugUnitTest --tests "com.poco.dishvision.core.data.sync.MenuSyncCoordinatorTest"`  
Expected: FAIL with missing coordinator or fallback behavior

- [ ] **Step 3: Implement sync orchestration and repository integration**

```kotlin
interface MenuRepository {
    fun observeCatalog(): Flow<MenuCatalog>
    suspend fun refreshFromLocalAsset()
    suspend fun syncFromLanIfEnabled(): SyncStatus
}
```

- [ ] **Step 4: Re-run sync coordinator test and repository tests**

Run: `./gradlew :core:data:testDebugUnitTest --tests "com.poco.dishvision.core.data.sync.MenuSyncCoordinatorTest"`  
Expected: PASS

Run: `./gradlew :core:data:testDebugUnitTest --tests "com.poco.dishvision.core.data.repository.DefaultMenuRepositoryTest"`  
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add core/data/src/main/java/com/poco/dishvision/core/data/sync core/data/src/main/java/com/poco/dishvision/core/data/repository
git commit -m "实现 LAN 同步协调与缓存回退"
```

### Task 5: Surface LAN Config and Sync Status in Settings UI

**Files:**
- Create: `feature/settings/src/main/java/com/poco/dishvision/feature/settings/LanServerConfigCard.kt`
- Create: `feature/settings/src/main/java/com/poco/dishvision/feature/settings/SyncStatusCard.kt`
- Modify: `feature/settings/src/main/java/com/poco/dishvision/feature/settings/SettingsRoute.kt`
- Modify: `feature/settings/src/main/java/com/poco/dishvision/feature/settings/SettingsViewModel.kt`
- Modify: `feature/settings/src/main/java/com/poco/dishvision/feature/settings/SettingsUiState.kt`
- Modify: `feature/settings/src/androidTest/java/com/poco/dishvision/feature/settings/SettingsScreenTest.kt`

- [ ] **Step 1: Extend the failing settings UI test**

```kotlin
@Test
fun settings_screen_displays_server_address_and_sync_state() {
    composeTestRule.onNodeWithText("服务器地址").assertExists()
    composeTestRule.onNodeWithText("同步状态").assertExists()
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :feature:settings:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.poco.dishvision.feature.settings.SettingsScreenTest`  
Expected: FAIL because LAN config cards do not exist

- [ ] **Step 3: Implement config form, sync status card, and manual refresh action**

```kotlin
data class SettingsUiState(
    val sourceModeLabel: String,
    val serverAddress: String,
    val syncStatusLabel: String,
    val lastSyncAt: String?,
)
```

- [ ] **Step 4: Re-run settings tests**

Run: `./gradlew :feature:settings:testDebugUnitTest --tests "com.poco.dishvision.feature.settings.SettingsViewModelTest"`  
Expected: PASS

Run: `./gradlew :feature:settings:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.poco.dishvision.feature.settings.SettingsScreenTest`  
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add feature/settings
git commit -m "在设置页接入 LAN 地址配置与同步状态"
```

### Task 6: Verify Phase 2 End-to-End Behavior

**Files:**
- Modify: `benchmark/src/main/java/com/poco/dishvision/benchmark/StartupBenchmark.kt`
- Optionally Create: `benchmark/src/main/java/com/poco/dishvision/benchmark/LanRefreshBenchmark.kt`

- [ ] **Step 1: Add a smoke benchmark or scripted manual refresh path**

```kotlin
@Test
fun startup_after_cached_remote_sync_is_stable() = benchmarkRule.measureRepeated {
    startActivityAndWait()
}
```

- [ ] **Step 2: Run Phase 2 regression**

Run: `./gradlew :core:data:testDebugUnitTest`  
Expected: PASS

Run: `./gradlew :feature:settings:testDebugUnitTest`  
Expected: PASS

Run: `./gradlew :app:assembleDebug`  
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 3: Manual LAN verification on emulator/device**

Expected:
- 填入 LAN 地址后可连接服务
- 关闭服务后仍显示旧菜单
- 重启应用后缓存菜单仍可读取

- [ ] **Step 4: Commit**

```bash
git add benchmark
git commit -m "完成 LAN 同步回归验证"
```

## Phase 2 Final Verification

- [ ] Run: `./gradlew :core:data:testDebugUnitTest`
- [ ] Run: `./gradlew :feature:settings:testDebugUnitTest`
- [ ] Run: `./gradlew :app:assembleDebug`
- [ ] Manual check:
  - 可编辑服务器地址
  - 远端版本更新时内容能刷新
  - 网络失败时 UI 不报阻塞性错误
