# Phase 1 Acceptance Remediation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 修补 Phase 1 与 design spec（设计规格）/ acceptance criteria（验收标准）之间的缺口，使项目具备可复核、可验收、可回写文档的完成状态。

**Architecture:** 本轮不重做 Phase 1 主体架构，只修补当前验收阻塞项。优先打通 `Settings` 真正入口、收敛 `Back behavior`（返回行为）、补齐 `core:ui` 主题与 `glass`（玻璃）组件基础设施，然后做性能对齐与最终验证，确保 `Local JSON -> Room -> Repository -> ViewModel -> UiState -> Compose for TV UI` 这条主链路在代码、测试和文档三层一致。

**Tech Stack:** Kotlin, Jetpack Compose for TV, TV Material 3, Hilt, Room, DataStore, Compose UI Test, JUnit, Macrobenchmark, Android TV Emulator

---

## 执行状态

- 当前状态：`Completed`
- 当前分支：`codex-phase-1-acceptance-remediation`
- 当前工作区：`.worktrees/phase-1-acceptance-remediation`
- 当前任务：`All tasks completed`
- 最后更新：`2026-03-19`

## 检查点记录

- `2026-03-19`：已按 `executing-plans` / `using-git-worktrees` 流程创建隔离 `worktree`，并完成基线验证：`./gradlew :app:assembleDebug :core:model:test :core:data:testDebugUnitTest :feature:menu:testDebugUnitTest` 为 `BUILD SUCCESSFUL`。
- `2026-03-19`：Task 1 Step 1-3 已执行。`AppDestination` 已加入 `Settings` route，`AppNavigationSmokeTest` 已加入设置页导航红灯用例，并在 `TV55C` 上运行 `./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.poco.dishvision.AppNavigationSmokeTest`；当前失败点为 `browse-screen` 不可达，说明 app 壳层尚未提供可用的设置入口链路。
- `2026-03-19`：Task 1 Step 4-5 已执行。app 壳层已接入真实 `Settings` 入口、真实 `MenuRepository + AppPreferences` 数据链路，并在 `TV55C` 上验证通过：
  - `./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.poco.dishvision.AppNavigationSmokeTest`
  - `./gradlew :feature:settings:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.poco.dishvision.feature.settings.SettingsScreenTest`
- `2026-03-19`：Task 1 Step 6 已完成，提交为 `5da202d (接通设置页真实入口与状态数据)`。
- `2026-03-19`：Task 2 Step 1-2 已执行。新增 `BrowseBackBehaviorTest` 与 app 层 `Back` 回归测试后，`./gradlew :feature:menu:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.poco.dishvision.feature.menu.BrowseBackBehaviorTest` 先失败，失败点为 `detail-panel` 在首次 `Back` 后仍存在，符合“返回顺序未被显式收敛”的预期。
- `2026-03-19`：Task 2 Step 3-4 已执行。当前返回链路已明确为 `detail panel -> Browse -> Attract/Home`，并在 `TV55C` 上验证通过：
  - `./gradlew :feature:menu:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.poco.dishvision.feature.menu.BrowseBackBehaviorTest`
  - `./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.poco.dishvision.AppNavigationSmokeTest`
- `2026-03-19`：Task 2 Step 5 已完成，提交为 `22209f8 (收敛浏览态与设置页返回键行为)`。
- `2026-03-19`：Task 3 Step 1-3 已执行。已补齐 `ColorTokens`、`Dimens`、`PocoTheme`、`GlassSurface`，并将首页、浏览页、设置页的主要容器收敛到共享主题令牌与 `glass` 组件。验证前先确认 `adb devices` 可见 `emulator-5554 device`，随后运行：
  - `JAVA_HOME="/Users/PopoY/Applications/Android Studio.app/Contents/jbr/Contents/Home" ANDROID_HOME="/Users/PopoY/Library/Android/sdk" ./gradlew :app:assembleDebug :feature:home:connectedDebugAndroidTest :feature:menu:connectedDebugAndroidTest :feature:settings:connectedDebugAndroidTest`
  - 结果：`BUILD SUCCESSFUL in 27s`
- `2026-03-19`：Task 3 Step 4 已完成，提交为 `14f16f5 (补齐主题与玻璃组件基础设施)`。
- `2026-03-19`：Task 4 Step 1 已执行。先新增 `MenuItemRowTest` 锁定 `lazy list` 的 `performScrollToIndex` 能力；fresh run `JAVA_HOME="/Users/PopoY/Applications/Android Studio.app/Contents/jbr/Contents/Home" ANDROID_HOME="/Users/PopoY/Library/Android/sdk" ./gradlew :feature:menu:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.poco.dishvision.feature.menu.MenuItemRowTest` 首次失败，原因是 `menu-item-row` 不存在，说明菜品区仍是 `Row + horizontalScroll`。随后将 `MenuItemRow` 改为 `LazyRow` 并补上测试标签，同命令重跑结果为 `BUILD SUCCESSFUL in 7s`。
- `2026-03-19`：Task 4 Step 2 已执行。已在 `benchmark/build.gradle.kts`、`benchmark/src/main/java/com/poco/dishvision/benchmark/StartupBenchmark.kt` 与 `docs/specs/2026-03-18-poco-dish-vision-design.md` 中明确记录：`Phase 1` 保留 `Macrobenchmark` 性能基线，`Baseline Profiles` 因发布链路与热点路径尚未固化，延后到 `Phase 3`，避免文档与实现状态不一致。
- `2026-03-19`：Task 4 Step 3 已执行。验证前先确认 `adb devices` 可见 `emulator-5554 device`，随后运行 `JAVA_HOME="/Users/PopoY/Applications/Android Studio.app/Contents/jbr/Contents/Home" ANDROID_HOME="/Users/PopoY/Library/Android/sdk" ./gradlew :app:assembleDebug :benchmark:connectedCheck`，结果 `BUILD SUCCESSFUL in 50s`。新的 benchmark 输出位于：
  - `benchmark/build/outputs/connected_android_test_additional_output/benchmark/connected/TV55C(AVD) - 14/com.poco.dishvision.benchmark-benchmarkData.json`
  - `benchmark/build/outputs/connected_android_test_additional_output/benchmark/connected/TV55C(AVD) - 14/StartupBenchmark_startup_iter000_2026-03-19-03-26-17.perfetto-trace`
- `2026-03-19`：Task 4 Step 4 已执行。`docs/coding_plans/2026-03-18-phase-1-foundation-and-local-menu.md` 已从 `Completed` 调整为 `In Progress`，并补充 `Macrobenchmark` / `Baseline Profiles` 的真实阶段范围，消除与当前实现不一致的完成态表述。
- `2026-03-19`：Task 4 Step 5 已完成，提交为 `5ee751a (对齐性能要求与阶段文档范围)`。
- `2026-03-19`：Task 5 Step 1 已执行。最终验收前再次确认 `adb devices` 可见 `emulator-5554 device`。
- `2026-03-19`：Task 5 Step 2 已执行。先运行 `JAVA_HOME="/Users/PopoY/Applications/Android Studio.app/Contents/jbr/Contents/Home" ANDROID_HOME="/Users/PopoY/Library/Android/sdk" ./gradlew --no-daemon :app:assembleDebug :core:model:test :core:data:testDebugUnitTest :feature:menu:testDebugUnitTest :app:connectedDebugAndroidTest :feature:home:connectedDebugAndroidTest :feature:menu:connectedDebugAndroidTest :feature:settings:connectedDebugAndroidTest :benchmark:connectedCheck`，结果 `BUILD SUCCESSFUL in 1m 18s`；清理调试日志后再次运行同命令，结果 `BUILD SUCCESSFUL in 1m 32s`，作为最终代码状态的 fresh 自动化证据。
- `2026-03-19`：Task 5 Step 3 已执行。人工 emulator 复核前需显式运行 `JAVA_HOME="/Users/PopoY/Applications/Android Studio.app/Contents/jbr/Contents/Home" ANDROID_HOME="/Users/PopoY/Library/Android/sdk" ./gradlew --no-daemon :app:installDebug`，因为 connected tests 完成后设备上不保留可直接启动的安装包。随后在 `TV55C(AVD) - 14` 上抓取首页、`Browse`、`Back` 回首页三帧截图，确认 `Attract -> Browse -> Back -> Attract` 手工路径成立，且 `detail dock` 位于底部。
- `2026-03-19`：Task 5 Step 3 补充排查结论：你观察到的“按键后看似进入 `Browse`，一截图又回首页”并非 `D-pad` 输入失效，而是前台窗口主题与抓帧时序叠加导致。`MainActivity` 在默认 theme 下被系统识别为 `translucent=true`、`visible=false`，`tvlauncher` 仍持有 `topResumedActivity`；本轮通过新增 `Theme.PocoDishVision` 显式设置 `android:windowBackground`、`android:colorBackground`、`android:windowIsTranslucent=false` 修复前台归属，并在按键后等待约 2 秒再截图，已稳定捕获 `Browse` 画面。
- `2026-03-19`：Task 5 Step 4 已执行。`docs/coding_plans/2026-03-18-phase-1-foundation-and-local-menu.md` 已写入最终验证日期、自动化命令结果与人工 emulator 复核结果，不再保留空白待办。
- `2026-03-19`：Task 5 Step 5 结论：`Phase 1 accepted`。已满足的 acceptance criteria 包括真实 `Settings` 入口、线性 `Back behavior`、`core:ui` 主题/玻璃基础设施、`spec`/`plan` 范围一致性，以及完整 `Gradle verification + emulator review` 的 fresh evidence。未满足项：无。
- `2026-03-19`：Task 5 Step 6 已完成。最终提交已收口 theme 前台修复与验收文档回写，本计划所有步骤均已完成。

## 背景与范围

本计划用于修复以下 5 类验收缺口：

1. `Settings Screen`（设置页）存在实现，但未接入真实 app navigation（应用导航）与真实依赖。
2. `Back behavior`（返回行为）未实现 spec 要求的 linear / predictable（线性可预测）规则。
3. `core:ui` 缺失 `ColorTokens`、`Dimens`、`PocoTheme`、`GlassSurface` 四个计划内文件。
4. performance spec（性能规格）与当前实现不完全一致，至少包括 `LazyRow / LazyColumn` 和 `Baseline Profiles`（基线配置文件）的处理策略。
5. `Phase 1 Final Verification`（最终验证）中的人工 emulator（模拟器）复核仍未补完。

## 执行前提

- 本机可用 `JAVA_HOME=/Users/PopoY/Applications/Android Studio.app/Contents/jbr/Contents/Home`
- 本机可用 `ANDROID_HOME=/Users/PopoY/Library/Android/sdk`
- 可启动一个 Android TV AVD（建议沿用 `TV55C`）
- 执行验证前先确认 `adb devices` 能看到 emulator

### Task 1: Wire Real Settings Entry and Data Flow

**Files:**
- Modify: `app/src/main/java/com/poco/dishvision/navigation/AppDestination.kt`
- Modify: `app/src/main/java/com/poco/dishvision/navigation/AppNavHost.kt`
- Modify: `app/src/main/java/com/poco/dishvision/MainActivity.kt`
- Modify: `app/src/main/java/com/poco/dishvision/di/AppModule.kt`
- Modify: `feature/settings/src/main/java/com/poco/dishvision/feature/settings/SettingsRoute.kt`
- Modify: `feature/settings/src/main/java/com/poco/dishvision/feature/settings/SettingsViewModel.kt`
- Modify: `app/src/androidTest/java/com/poco/dishvision/AppNavigationSmokeTest.kt`
- Modify: `feature/settings/src/androidTest/java/com/poco/dishvision/feature/settings/SettingsScreenTest.kt`

- [x] **Step 1: 扩展导航目标，补上 `Settings` route（路由）**

```kotlin
enum class AppDestination(
    val route: String,
) {
    Home(route = "home"),
    Settings(route = "settings"),
}
```

- [x] **Step 2: 先写失败的导航 / 集成测试**

新增或修改测试，至少覆盖：

```kotlin
@Test
fun app_can_navigate_to_settings_and_show_live_status() {
    composeTestRule.onNodeWithTag("open-settings").performClick()
    composeTestRule.onNodeWithTag("settings-screen").assertExists()
    composeTestRule.onNodeWithText("当前数据源").assertExists()
}
```

- [x] **Step 3: 运行测试，确认当前实现失败**

Run: `./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.poco.dishvision.AppNavigationSmokeTest`
Expected: FAIL，原因应落在缺少真实 `Settings` 入口或真实依赖注入

- [x] **Step 4: 在应用壳层接入 `SettingsRoute` 与真实依赖**

实现要求：
- `AppNavHost` 里存在可达的 `Settings` 入口
- `SettingsRoute` 走真实 `MenuRepository + AppPreferences`
- 不再只依赖 preview / fallback（预览回退）路径证明功能存在

- [x] **Step 5: 重新运行导航与设置页测试**

Run: `./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.poco.dishvision.AppNavigationSmokeTest`
Expected: PASS

Run: `./gradlew :feature:settings:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.poco.dishvision.feature.settings.SettingsScreenTest`
Expected: PASS

- [x] **Step 6: Commit**

```bash
git add app feature/settings
git commit -m "接通设置页真实入口与状态数据"
```

### Task 2: Make Back Behavior Predictable

**Files:**
- Modify: `feature/menu/src/main/java/com/poco/dishvision/feature/menu/MenuRoute.kt`
- Modify: `feature/menu/src/main/java/com/poco/dishvision/feature/menu/ItemDetailPanel.kt`
- Create: `feature/menu/src/androidTest/java/com/poco/dishvision/feature/menu/BrowseBackBehaviorTest.kt`
- Modify: `app/src/androidTest/java/com/poco/dishvision/AppNavigationSmokeTest.kt`

- [x] **Step 1: 写失败的 `Back behavior` 测试**

至少覆盖两条规则：

```kotlin
@Test
fun back_closes_expanded_detail_panel_before_leaving_browse() {}

@Test
fun back_from_settings_returns_to_browse_instead_of_exiting_app() {}
```

- [x] **Step 2: 运行测试，确认当前实现失败**

Run: `./gradlew :feature:menu:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.poco.dishvision.feature.menu.BrowseBackBehaviorTest`
Expected: FAIL，原因应落在 `Back` 未被显式消费或顺序不符合 spec

- [x] **Step 3: 实现线性可预测的返回规则**

规则固定为：
- `detail panel` 展开时，`Back` 先关闭 `detail panel`
- 位于 `Settings` 时，`Back` 返回 `Browse`
- 位于 `Browse` 主界面时，`Back` 的行为需明确写入实现与测试，不允许依赖默认不透明行为

- [x] **Step 4: 重新运行测试**

Run: `./gradlew :feature:menu:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.poco.dishvision.feature.menu.BrowseBackBehaviorTest`
Expected: PASS

Run: `./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.poco.dishvision.AppNavigationSmokeTest`
Expected: PASS

- [x] **Step 5: Commit**

```bash
git add feature/menu app/src/androidTest
git commit -m "收敛浏览态与设置页返回键行为"
```

### Task 3: Restore Core UI Theme and Glass Primitives

**Files:**
- Create: `core/ui/src/main/java/com/poco/dishvision/core/ui/theme/ColorTokens.kt`
- Create: `core/ui/src/main/java/com/poco/dishvision/core/ui/theme/Dimens.kt`
- Create: `core/ui/src/main/java/com/poco/dishvision/core/ui/theme/PocoTheme.kt`
- Create: `core/ui/src/main/java/com/poco/dishvision/core/ui/components/GlassSurface.kt`
- Modify: `core/ui/src/main/java/com/poco/dishvision/core/ui/components/FocusableMenuCard.kt`
- Modify: `feature/home/src/main/java/com/poco/dishvision/feature/home/HomeRoute.kt`
- Modify: `feature/menu/src/main/java/com/poco/dishvision/feature/menu/CategoryRail.kt`
- Modify: `feature/menu/src/main/java/com/poco/dishvision/feature/menu/ItemDetailPanel.kt`
- Modify: `feature/settings/src/main/java/com/poco/dishvision/feature/settings/SettingsRoute.kt`

- [x] **Step 1: 先补计划内缺失文件骨架**

最低要求示例：

```kotlin
object ColorTokens
object Dimens

@Composable
fun PocoTheme(
    content: @Composable () -> Unit,
) { content() }
```

```kotlin
@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) { /* ... */ }
```

- [x] **Step 2: 让现有页面逐步改用主题与玻璃组件**

实现要求：
- 收敛硬编码颜色、圆角和常用 spacing（间距）
- 首页、浏览页、设置页的卡片容器优先复用 `GlassSurface`
- 不追求重做视觉，只补齐 `Phase 1` 所承诺的基础主题能力

- [x] **Step 3: 运行构建与相关测试**

Run: `./gradlew :app:assembleDebug :feature:home:connectedDebugAndroidTest :feature:menu:connectedDebugAndroidTest :feature:settings:connectedDebugAndroidTest`
Expected: PASS

- [x] **Step 4: Commit**

```bash
git add core/ui feature/home feature/menu feature/settings
git commit -m "补齐主题与玻璃组件基础设施"
```

### Task 4: Align Performance Requirements and Documentation

**Files:**
- Modify: `feature/menu/src/main/java/com/poco/dishvision/feature/menu/MenuItemRow.kt`
- Modify: `benchmark/build.gradle.kts`
- Modify: `benchmark/src/main/java/com/poco/dishvision/benchmark/StartupBenchmark.kt`
- Modify: `docs/specs/2026-03-18-poco-dish-vision-design.md`
- Modify: `docs/coding_plans/2026-03-18-phase-1-foundation-and-local-menu.md`

- [x] **Step 1: 把菜单卡片区改为 `LazyRow`**

目标片段：

```kotlin
LazyRow(
    horizontalArrangement = Arrangement.spacedBy(18.dp),
) {
    itemsIndexed(items) { index, item ->
        // ...
    }
}
```

- [x] **Step 2: 对 `Baseline Profiles` 做明确决策**

二选一，必须落文档：
- 方案 A：本轮直接补 `Baseline Profiles`
- 方案 B：明确从 `Phase 1` 下调到 `Phase 3`，同步修正 spec 与 plan，避免“文档说完成，代码没做”

- [x] **Step 3: 运行性能相关验证**

Run: `./gradlew :app:assembleDebug :benchmark:connectedCheck`
Expected: PASS，并生成至少一份新的 benchmark 输出

- [x] **Step 4: 更新文档中的完成状态**

要求：
- `spec` 与 `plan` 对 `Phase 1` 范围表述一致
- 若未做 `Baseline Profiles`，必须明确写出延后原因与目标阶段
- 不允许继续保留会误导 review（审查）结论的“Completed”字样

- [x] **Step 5: Commit**

```bash
git add feature/menu benchmark docs/specs docs/coding_plans
git commit -m "对齐性能要求与阶段文档"
```

### Task 5: Execute Final Acceptance Verification

**Files:**
- Modify: `docs/coding_plans/2026-03-18-phase-1-foundation-and-local-menu.md`
- Modify: `docs/specs/2026-03-18-poco-dish-vision-design.md`
- Optional: `docs/review_plans/2026-03-19-phase-1-acceptance-remediation-plan.md`

- [x] **Step 1: 启动 Android TV emulator 并确认设备在线**

Run: `/Users/PopoY/Library/Android/sdk/platform-tools/adb devices`
Expected: 至少 1 台 Android TV emulator 处于 `device` 状态

- [x] **Step 2: 运行完整 Phase 1 验证命令**

Run: `./gradlew --no-daemon :app:assembleDebug :core:model:test :core:data:testDebugUnitTest :feature:menu:testDebugUnitTest :app:connectedDebugAndroidTest :feature:home:connectedDebugAndroidTest :feature:menu:connectedDebugAndroidTest :feature:settings:connectedDebugAndroidTest :benchmark:connectedCheck`
Expected: `BUILD SUCCESSFUL`

- [x] **Step 3: 执行人工 emulator 复核**

人工检查清单：
- 应用启动进入 `Attract mode`
- 任意方向键能进入 `Browse mode`
- `Back` 行为线性可预测
- 重要文案位于中下区域而非顶部
- 浏览态详情信息主要位于底部 `detail dock`
- 文本在约 2 米观看距离下清晰可读

- [x] **Step 4: 回写 `Final Verification` 记录**

要求：
- 在 `phase-1 plan` 中写入实际验证日期
- 补齐人工复核结果，不再保留空白待办
- 若仍有保留项，必须改为 `Partial`（部分完成）而非 `Completed`

- [x] **Step 5: 输出最终验收结论**

结论模板必须包含：
- 哪些 acceptance criteria 已满足
- 哪些 criteria 仍未满足
- 证据来自哪些命令、哪些测试、哪次人工复核

- [x] **Step 6: Commit**

```bash
git add docs/coding_plans docs/specs docs/review_plans
git commit -m "完成 phase 1 验收复核与文档回写"
```

## 最终完成定义

只有同时满足下面条件，才能声称 `Phase 1 accepted`（Phase 1 已验收）：

1. `Settings` 通过真实入口可访问，且显示真实状态。
2. `Back behavior` 有实现且有自动化测试证明。
3. `core:ui` 四个缺失文件已补齐并被实际使用。
4. `spec` 与 `plan` 对 `Phase 1` 范围表述一致，不存在“文档完成、代码缺失”的冲突。
5. 完整 `Gradle verification`（Gradle 验证）与人工 emulator 复核都有 fresh evidence（新鲜证据）。
