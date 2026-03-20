# 菜单页样式对齐收口 Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 修复菜单页真实运行路径的比例失真，让 Browse 首屏恢复稳定 `3x3` 九卡密度、标题区层级和菜品卡描述可见性，同时保持菜单内容顺序继续服从运行数据真值。

**Architecture:** 将 `PocoTheme`（主题）上提到 `AppNavHost`，让 Home/Menu/Settings 共用唯一运行时 `ScreenProportions`（比例系统）来源；菜单页继续通过 `ScreenProportions` 驱动宏观布局，在 `MenuRoute`、`CategoryRail`、`MenuItemGrid` 中只消费比例 token（令牌）；使用 `Compose UI test`（Compose 界面测试）和 `androidTest`（设备测试）共同锁定“九卡首屏 + 标题层级 + 文本节点可见”三个对外行为。

**Tech Stack:** Kotlin, Jetpack Compose UI, AndroidX Compose UI Test, Android Instrumentation Test, Hilt, Android TV emulator

**Spec:** `docs/superpowers/specs/2026-03-20-menu-style-alignment-design.md`

## Execution Status

- Last Updated: 2026-03-20 21:03 CST
- Worktree: `.worktrees/codex-menu-style-alignment`
- Current Checkpoint: `Task 6 / Step 6`
- Task 1 Status: `COMPLETED`
- Task 2 Status: `COMPLETED`
- Task 3 Status: `COMPLETED`
- Task 4 Status: `COMPLETED`
- Task 5 Status: `COMPLETED`
- Task 6 Status: `COMPLETED_WITH_LIMITATION`

---

## Preconditions

- Android TV `emulator`（模拟器）已启动，并满足 spec 中的验证基准：
  - `1920x1080`
  - `16:9`
  - `font scale = 1.0`
  - `display size = default`
- 当前工作树已包含菜单页相关未提交改动；执行者不得回退不属于本计划的已有修改。

## File Map

**Production Files**

- Modify: `app/src/main/java/com/poco/dishvision/navigation/AppNavHost.kt`
  - 把 `PocoTheme` 提升到应用真实运行壳层。
- Modify: `feature/home/src/main/java/com/poco/dishvision/feature/home/HomeRoute.kt`
  - 清理 Route 级重复主题包裹，保持首页依赖根层比例注入。
- Modify: `feature/menu/src/main/java/com/poco/dishvision/feature/menu/MenuRoute.kt`
  - 为 Browse 标题区补 `testTag`（测试标签），并只消费比例 token。
- Modify: `feature/menu/src/main/java/com/poco/dishvision/feature/menu/CategoryRail.kt`
  - 收敛聚焦缩放 `scale`（缩放）并继续消费比例 token。
- Modify: `feature/menu/src/main/java/com/poco/dishvision/feature/menu/MenuItemGrid.kt`
  - 为卡片正文区建立稳定的最小高度预算，补 name/description 标签。
- Modify: `core/ui/src/main/java/com/poco/dishvision/core/ui/theme/ScreenProportions.kt`
  - 如现有 token 不足，新增菜单专属比例 token，例如 `browseRailFocusedScale`、`browseGridCardBodyMinHeight`。

**Tests**

- Modify: `app/src/androidTest/java/com/poco/dishvision/AppNavigationSmokeTest.kt`
  - 锁定真实运行路径上的 Browse 比例契约。
- Modify: `feature/home/src/androidTest/java/com/poco/dishvision/feature/home/HomeScreenTest.kt`
  - 让独立 Route 测试在根层主题上提后继续运行在真实比例环境中。
- Modify: `feature/menu/src/androidTest/java/com/poco/dishvision/feature/menu/BrowseLayoutContractTest.kt`
  - 锁定 Browse helper/title/description/card 文本节点与九卡密度。
- Modify: `feature/menu/src/androidTest/java/com/poco/dishvision/feature/menu/BrowseScreenFocusTest.kt`
  - 让独立 Route 测试在真实比例环境中运行。
- Modify: `feature/menu/src/androidTest/java/com/poco/dishvision/feature/menu/BrowseBackBehaviorTest.kt`
  - 让独立 Route 测试在真实比例环境中运行。
- Reuse: `feature/menu/src/test/java/com/poco/dishvision/feature/menu/MenuPreviewCatalogContractTest.kt`
  - 继续锁定分类顺序与总数真值。

---

### Task 1: 锁定真实运行路径的 Browse 比例契约

**Files:**
- Modify: `app/src/androidTest/java/com/poco/dishvision/AppNavigationSmokeTest.kt:35-74`
- Test: `app/src/androidTest/java/com/poco/dishvision/AppNavigationSmokeTest.kt`

- [x] **Step 1: 先写失败的 app-shell 布局契约测试**

在 `AppNavigationSmokeTest` 中新增一个测试，沿用现有 `openSettingsFromHome()` -> `pressBackUnconditionally()` 路径进入 Browse，然后断言 helper 文案和第 9 张卡片在真实运行壳层中可见：

```kotlin
@Test
fun browse_from_app_shell_keeps_helper_copy_and_ninth_card_visible() {
    openSettingsFromHome()
    composeTestRule.onNodeWithTag("settings-screen").assertExists()

    pressBackUnconditionally()

    composeTestRule.onNodeWithTag("browse-screen").assertExists()
    composeTestRule.onNodeWithText("44道湘味热菜 · 按分类浏览").assertExists()
    composeTestRule.onNodeWithTag("menu-item-hot-stir-fry-8").assertIsDisplayed()
}
```

- [x] **Step 2: 运行测试，确认当前实现失败**

Run: `./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.poco.dishvision.AppNavigationSmokeTest`

Expected: FAIL。失败点应落在新加的 Browse 比例契约断言上，例如第 9 张卡没有 `assertIsDisplayed()`，或 Browse 壳层下的 helper 文案/布局未对齐。

Execution Note (2026-03-20 19:55 CST): 实际结果为 `PASS`。`AppNavigationSmokeTest` 共运行 5 条 instrumentation tests，全部通过，说明真实 app-shell 路径上的 Browse helper 文案与第 9 张卡当前已满足契约；后续工作继续聚焦独立 Route 主题注入、Browse 顶部标签和卡片正文可见性。

- [x] **Step 3: Commit (WIP)**

```bash
git add app/src/androidTest/java/com/poco/dishvision/AppNavigationSmokeTest.kt
git commit -m "test: 补充菜单页真实运行路径比例契约"
```

Execution Note (2026-03-20 19:57 CST): 已提交检查点 `18b95ce`（`test: 补充菜单页真实运行路径比例契约`）。

---

### Task 2: 上提 `PocoTheme` 并修正独立 Route 测试入口

**Files:**
- Modify: `app/src/main/java/com/poco/dishvision/navigation/AppNavHost.kt:42-123`
- Modify: `feature/home/src/main/java/com/poco/dishvision/feature/home/HomeRoute.kt:63-107`
- Modify: `feature/home/src/androidTest/java/com/poco/dishvision/feature/home/HomeScreenTest.kt:30-98`
- Modify: `feature/menu/src/androidTest/java/com/poco/dishvision/feature/menu/BrowseLayoutContractTest.kt:24-40`
- Modify: `feature/menu/src/androidTest/java/com/poco/dishvision/feature/menu/BrowseScreenFocusTest.kt:31-54`
- Modify: `feature/menu/src/androidTest/java/com/poco/dishvision/feature/menu/BrowseBackBehaviorTest.kt:34-97`

- [x] **Step 1: 在 `AppNavHost` 根层包裹 `PocoTheme`**

把 `Box(...) { when (currentRoute) { ... } }` 提升到 `PocoTheme { ... }` 内，形成唯一运行时比例入口：

```kotlin
PocoTheme {
    Box(
        modifier = modifier
            .fillMaxSize()
            .onPreviewKeyEvent { ... },
    ) {
        when (currentRoute) { ... }
    }
}
```

- [x] **Step 2: 清理 `HomeRoute` 里的重复主题包裹**

把 `HomeRoute()` 中两处 `PocoTheme { HomeScreen(...) }` 改成直接渲染 `HomeScreen(...)`，并移除不再需要的 `PocoTheme` import：

```kotlin
if (menuRepository == null) {
    HomeScreen(
        uiState = previewHomeUiState(),
        onBrowseRequested = onBrowseRequested,
        modifier = modifier,
    )
    return
}

HomeScreen(
    uiState = uiState,
    onBrowseRequested = onBrowseRequested,
    modifier = modifier,
)
```

- [x] **Step 3: 让独立 Route 的 UI 测试显式运行在 `PocoTheme` 中**

因为独立 `createComposeRule()` 测试不会经过 `AppNavHost`，所以把以下所有 `setContent { HomeRoute() }` / `setContent { MenuRoute() }` 改成：

```kotlin
composeTestRule.setContent {
    PocoTheme {
        MenuRoute()
    }
}
```

首页测试对应改为：

```kotlin
composeTestRule.setContent {
    PocoTheme {
        HomeRoute()
    }
}
```

- [x] **Step 4: 重新运行失败测试，确认根层主题注入已修复 app-shell 契约**

Run: `./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.poco.dishvision.AppNavigationSmokeTest`

Expected: PASS。新加的 Browse 比例契约测试应通过，且原有 Home/Settings/Back 链路测试不回归。

Execution Note (2026-03-20 20:07 CST): 首次运行因 `app` 模块缺少对 `:core:ui` 的直接依赖而在 `:app:compileDebugKotlin` 失败；补充 `app/build.gradle.kts` 中的 `implementation(project(":core:ui"))` 后重跑通过，`AppNavigationSmokeTest` 共 5 条 instrumentation tests，全部 `PASS`。

- [x] **Step 5: 编译独立 Route 测试源码，确保主题入口调整没有破坏测试**

Run: `./gradlew :feature:home:compileDebugAndroidTestKotlin :feature:menu:compileDebugAndroidTestKotlin`

Expected: PASS

Execution Note (2026-03-20 20:07 CST): `:feature:home:compileDebugAndroidTestKotlin` 与 `:feature:menu:compileDebugAndroidTestKotlin` 均已通过。

- [x] **Step 6: Commit**

```bash
git add app/src/main/java/com/poco/dishvision/navigation/AppNavHost.kt \
  feature/home/src/main/java/com/poco/dishvision/feature/home/HomeRoute.kt \
  feature/home/src/androidTest/java/com/poco/dishvision/feature/home/HomeScreenTest.kt \
  feature/menu/src/androidTest/java/com/poco/dishvision/feature/menu/BrowseLayoutContractTest.kt \
  feature/menu/src/androidTest/java/com/poco/dishvision/feature/menu/BrowseScreenFocusTest.kt \
  feature/menu/src/androidTest/java/com/poco/dishvision/feature/menu/BrowseBackBehaviorTest.kt
git commit -m "feat: 统一菜单页与首页的主题比例注入链路"
```

Execution Note (2026-03-20 20:10 CST): 已提交检查点 `f325fbb`（`feat: 统一菜单页与首页的主题比例注入链路`）。本次提交额外包含 `app/build.gradle.kts`，用于补齐 `app -> :core:ui` 的直接依赖，让根层 `PocoTheme` 注入可编译。

---

### Task 3: 先锁定 Browse 标题区与卡片文本节点契约

**Files:**
- Modify: `feature/menu/src/androidTest/java/com/poco/dishvision/feature/menu/BrowseLayoutContractTest.kt:24-40`
- Test: `feature/menu/src/androidTest/java/com/poco/dishvision/feature/menu/BrowseLayoutContractTest.kt`

- [x] **Step 1: 在 `BrowseLayoutContractTest` 中增加失败测试，要求关键节点必须有 `testTag`**

新增两个测试，先锁定 Browse 顶部四段文本和首行两张卡的 name/description 节点：

```kotlin
@Test
fun browse_header_exposes_helper_and_title_tags() {
    composeTestRule.setContent {
        PocoTheme {
            MenuRoute()
        }
    }

    composeTestRule.onNodeWithTag("browse-helper-copy").assertIsDisplayed()
    composeTestRule.onNodeWithTag("browse-main-label").assertIsDisplayed()
    composeTestRule.onNodeWithTag("browse-main-title").assertIsDisplayed()
    composeTestRule.onNodeWithTag("browse-main-description").assertIsDisplayed()
}

@Test
fun first_row_cards_expose_name_and_description_nodes() {
    composeTestRule.setContent {
        PocoTheme {
            MenuRoute()
        }
    }

    composeTestRule.onNodeWithTag("menu-item-hot-stir-fry-0-name").assertIsDisplayed()
    composeTestRule.onNodeWithTag("menu-item-hot-stir-fry-0-description").assertIsDisplayed()
    composeTestRule.onNodeWithTag("menu-item-hot-stir-fry-1-name").assertIsDisplayed()
    composeTestRule.onNodeWithTag("menu-item-hot-stir-fry-1-description").assertIsDisplayed()
}
```

- [x] **Step 2: 运行测试，确认它们先失败**

Run: `./gradlew :feature:menu:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.poco.dishvision.feature.menu.BrowseLayoutContractTest`

Expected: FAIL，原因应为上述 `testTag` 尚未存在。

Execution Note (2026-03-20 20:10 CST): 实际结果为 `FAIL`，且失败点与计划一致。当前已确认的 RED 断言包括：
- `browse_header_exposes_helper_and_title_tags` 因 `browse-helper-copy` 不可见而失败
- `first_row_cards_expose_name_and_description_nodes` 因 `menu-item-hot-stir-fry-0-name` 不可见而失败

- [x] **Step 3: Commit (WIP)**

```bash
git add feature/menu/src/androidTest/java/com/poco/dishvision/feature/menu/BrowseLayoutContractTest.kt
git commit -m "test: 锁定菜单页标题区和卡片文本节点契约"
```

Execution Note (2026-03-20 20:11 CST): 已提交检查点 `efdebe4`（`test: 锁定菜单页标题区和卡片文本节点契约`）。

---

### Task 4: 收口标题区与分类导轨比例，并补 Browse 顶部标签

**Files:**
- Modify: `core/ui/src/main/java/com/poco/dishvision/core/ui/theme/ScreenProportions.kt:97-161`
- Modify: `feature/menu/src/main/java/com/poco/dishvision/feature/menu/CategoryRail.kt:53-181`
- Modify: `feature/menu/src/main/java/com/poco/dishvision/feature/menu/MenuRoute.kt:206-397`
- Test: `feature/menu/src/androidTest/java/com/poco/dishvision/feature/menu/BrowseLayoutContractTest.kt`

- [x] **Step 1: 在 `ScreenProportions` 中补菜单页专属 token**

仅在确实需要时新增菜单专属比例 token，优先补这两个：

```kotlin
/** 分类导轨聚焦缩放，按菜单页设计单独收口。 */
val browseRailFocusedScale: Float = 1.03f

/** 菜品卡片正文区最小高度，设计基线 98px。 */
val browseGridCardBodyMinHeight: Dp = screenHeight * (98f / DESIGN_HEIGHT)
```

如果现有标题区 gap token 已足够，只允许微调现有 `browseLabelToTitleGap`、`browseTitleToSubGap`、`browseSubToGridGap` 的比例值，不得在 `MenuRoute` 里额外塞匿名 `Spacer(height = xx.dp)`。

- [x] **Step 2: 在 `CategoryRail` 中消费收口后的聚焦缩放**

把：

```kotlin
focusedScale = 1.05f
```

改成：

```kotlin
focusedScale = proportions.browseRailFocusedScale
```

- [x] **Step 3: 在 `MenuRoute` 中为顶部四段文本补 `testTag`，并保持全部字号来自 `scaledSp(...)`**

目标代码形态：

```kotlin
Text(
    text = "${totalItemCount}道湘味热菜 · 按分类浏览",
    modifier = Modifier.testTag("browse-helper-copy"),
    fontSize = proportions.scaledSp(18f),
)

Text(
    text = animatedCategory?.subtitle ?: "热锅现炒",
    modifier = Modifier.testTag("browse-main-label"),
    fontSize = proportions.scaledSp(18f),
)

Text(
    text = animatedCategory?.displayName ?: "招牌热炒",
    modifier = Modifier.testTag("browse-main-title"),
    fontSize = proportions.scaledSp(40f),
)

Text(
    text = animatedCategory?.description ?: "...",
    modifier = Modifier
        .width(proportions.browseSubtitleWidth)
        .testTag("browse-main-description"),
    fontSize = proportions.scaledSp(18f),
)
```

- [x] **Step 4: 运行 `BrowseLayoutContractTest`，确认顶部标签契约恢复，且第 9 张卡仍在首屏**

Run: `./gradlew :feature:menu:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.poco.dishvision.feature.menu.BrowseLayoutContractTest`

Expected: 先前新增的标题区 tag 断言变为 PASS；若卡片文本节点测试仍失败，失败点应只剩 `menu-item-...-name/description` 相关标签，交由下一 Task 修复。

Execution Note (2026-03-20 20:18 CST): 实际结果与预期一致。`BrowseLayoutContractTest` 当前只剩 `first_row_cards_expose_name_and_description_nodes` 失败，失败点为 `menu-item-hot-stir-fry-0-name` 不可见；顶部标签契约与 `menu-item-hot-stir-fry-8` 首屏断言均已恢复。

- [x] **Step 5: Commit**

```bash
git add core/ui/src/main/java/com/poco/dishvision/core/ui/theme/ScreenProportions.kt \
  feature/menu/src/main/java/com/poco/dishvision/feature/menu/CategoryRail.kt \
  feature/menu/src/main/java/com/poco/dishvision/feature/menu/MenuRoute.kt
git commit -m "feat: 收口菜单页标题区与分类导轨比例"
```

Execution Note (2026-03-20 20:19 CST): 已提交检查点 `700e4ad`（`feat: 收口菜单页标题区与分类导轨比例`）。

---

### Task 5: 收口菜品卡正文高度并补 name/description 标签

**Files:**
- Modify: `feature/menu/src/main/java/com/poco/dishvision/feature/menu/MenuItemGrid.kt:149-323`
- Modify: `core/ui/src/main/java/com/poco/dishvision/core/ui/theme/ScreenProportions.kt:148-161`
- Test: `feature/menu/src/androidTest/java/com/poco/dishvision/feature/menu/BrowseLayoutContractTest.kt`

- [x] **Step 1: 在 `MenuItemGrid` 中先实现“正文区预算优先”的高度拆分**

不要再只按 `180 / 278` 图片比例反推卡片。改成先从单卡高度里预留正文区最小预算，再计算图片区高度：

```kotlin
val resolvedBodyMinHeight = minOf(
    proportions.browseGridCardBodyMinHeight,
    resolvedCardHeight,
)
val resolvedImageHeight = maxOf(
    0.dp,
    minOf(
        proportions.browseCardImageHeight,
        resolvedCardHeight - resolvedBodyMinHeight,
    ),
)
```

正文区 `Column` 需要带上最小高度约束，保证描述节点不会被图片区继续挤压：

```kotlin
Column(
    modifier = Modifier
        .fillMaxWidth()
        .heightIn(min = proportions.browseGridCardBodyMinHeight)
        .padding(...),
)
```

- [x] **Step 2: 为卡片 title / description 节点补 `testTag`**

在 `BrowseGridCard` 中补以下标签：

```kotlin
Text(
    text = item.name,
    modifier = Modifier.testTag("$testTag-name"),
    ...
)
Text(
    text = item.description,
    modifier = Modifier.testTag("$testTag-description"),
    ...
)
```

- [x] **Step 3: 重新运行失败测试，确认卡片文本节点契约转绿**

Run: `./gradlew :feature:menu:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.poco.dishvision.feature.menu.BrowseLayoutContractTest`

Expected: PASS。以下行为同时成立：

- `browse-helper-copy`
- `browse-main-label`
- `browse-main-title`
- `browse-main-description`
- `menu-item-hot-stir-fry-0-name`
- `menu-item-hot-stir-fry-0-description`
- `menu-item-hot-stir-fry-1-name`
- `menu-item-hot-stir-fry-1-description`
- `menu-item-hot-stir-fry-8`

Execution Note (2026-03-20 20:33 CST): `BrowseLayoutContractTest` 已 `PASS`。本次修复包含两部分：
- 生产代码：`MenuItemGrid` 计算 `resolvedImageHeight` 时额外扣除了 `browseGridCardContentSpacing`，避免图片区与正文区间距继续挤压文本可见空间。
- 测试代码：首行 card 文本节点断言改为 `useUnmergedTree = true`，因为 card root 的可交互语义会在默认 merged tree 中吞并 leaf text 节点；真实节点和 `testTag` 已存在且可见，但需要在 unmerged tree 中稳定校验。

- [x] **Step 4: Commit**

```bash
git add core/ui/src/main/java/com/poco/dishvision/core/ui/theme/ScreenProportions.kt \
  feature/menu/src/main/java/com/poco/dishvision/feature/menu/MenuItemGrid.kt
git commit -m "feat: 修复菜单菜品卡正文区高度与文本可见性"
```

Execution Note (2026-03-20 20:34 CST): 已提交检查点 `e3c3997`（`feat: 修复菜单菜品卡正文区高度与文本可见性`）。本次提交额外包含 `BrowseLayoutContractTest.kt`，用于将 card 文本节点断言切换到 `useUnmergedTree = true`，匹配 Compose leaf text 的真实语义暴露方式。

---

### Task 6: 完整验证并做实屏视觉复核

**Files:**
- Reuse: `feature/menu/src/test/java/com/poco/dishvision/feature/menu/MenuPreviewCatalogContractTest.kt`
- Reuse: `feature/menu/src/androidTest/java/com/poco/dishvision/feature/menu/BrowseLayoutContractTest.kt`
- Reuse: `feature/menu/src/androidTest/java/com/poco/dishvision/feature/menu/BrowseScreenFocusTest.kt`
- Reuse: `feature/menu/src/androidTest/java/com/poco/dishvision/feature/menu/BrowseBackBehaviorTest.kt`
- Reuse: `app/src/androidTest/java/com/poco/dishvision/AppNavigationSmokeTest.kt`

- [x] **Step 1: 运行菜单顺序和数量的单元测试**

Run: `./gradlew :feature:menu:testDebugUnitTest --tests "com.poco.dishvision.feature.menu.MenuPreviewCatalogContractTest"`

Expected: PASS。分类顺序仍为 `招牌热炒 -> 香辣口味 -> 鱼鲜大菜 -> 家常土菜 -> 风味小菜`，总数仍为 `44`。

Execution Note (2026-03-20 20:37 CST): `MenuPreviewCatalogContractTest` 已 `PASS`。

- [x] **Step 2: 运行菜单关键 UI 测试**

Run: `./gradlew :feature:menu:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.poco.dishvision.feature.menu.BrowseLayoutContractTest,com.poco.dishvision.feature.menu.BrowseScreenFocusTest,com.poco.dishvision.feature.menu.BrowseBackBehaviorTest`

Expected: PASS

Execution Note (2026-03-20 20:39 CST): 菜单关键 UI 测试共 `7/7` 通过。

- [x] **Step 3: 运行 app-shell 冒烟测试**

Run: `./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.poco.dishvision.AppNavigationSmokeTest`

Expected: PASS

Execution Note (2026-03-20 20:41 CST): `AppNavigationSmokeTest` 共 `5/5` 通过。

- [x] **Step 4: 运行编译回归**

Run: `./gradlew :app:assembleDebug :feature:home:compileDebugAndroidTestKotlin :feature:menu:compileDebugAndroidTestKotlin`

Expected: PASS

Execution Note (2026-03-20 20:42 CST): `:app:assembleDebug`、`:feature:home:compileDebugAndroidTestKotlin`、`:feature:menu:compileDebugAndroidTestKotlin` 全部通过。

- [x] **Step 5: 记录验证基准并抓取菜单页实屏**

先确认设备基准：

```bash
adb shell wm size
adb shell settings get system font_scale
```

Expected:

- `Physical size: 1920x1080`
- `1.0`

然后抓图：

```bash
adb exec-out screencap -p > /tmp/poco-menu-style-alignment.png
```

人工复核以下 4 点：

- 左侧分类导轨不再显得过大
- 右侧标题区明显收紧，不再压缩网格空间
- `menu-item-hot-stir-fry-8` 首屏完整可见
- 首行至少两张卡的 name/description 节点都稳定可见

Execution Note (2026-03-20 21:03 CST): 设备基准实测为：
- `adb shell wm size` 返回 `Physical size: 3840x2160` 与 `Override size: 1920x1080`
- `adb shell settings get system font_scale` 与 `adb shell settings get secure font_scale` 均返回 `null`，表示当前设备未写入显式覆盖值；该结果更接近“系统默认值”而非计划中的显式 `1.0`

实屏抓图方面，已成功抓到 Home 与 Settings 页面，但多次尝试通过 adb keyevent、临时 screenshot harness 和 instrumentation 前台停留窗口抓取 Browse 页面时，设备前台会回落到 Home 或系统 launcher，未能稳定得到可用于人工复核的 Browse 实屏。因此，Step 5 的“设备基准采样”已完成，但 Browse 页面 4 个视觉点的人工截图复核结论仍以自动化测试证据为主。

- [x] **Step 6: Final Commit**

```bash
git add app/src/main/java/com/poco/dishvision/navigation/AppNavHost.kt \
  feature/home/src/main/java/com/poco/dishvision/feature/home/HomeRoute.kt \
  feature/home/src/androidTest/java/com/poco/dishvision/feature/home/HomeScreenTest.kt \
  feature/menu/src/main/java/com/poco/dishvision/feature/menu/MenuRoute.kt \
  feature/menu/src/main/java/com/poco/dishvision/feature/menu/CategoryRail.kt \
  feature/menu/src/main/java/com/poco/dishvision/feature/menu/MenuItemGrid.kt \
  core/ui/src/main/java/com/poco/dishvision/core/ui/theme/ScreenProportions.kt \
  app/src/androidTest/java/com/poco/dishvision/AppNavigationSmokeTest.kt \
  feature/menu/src/androidTest/java/com/poco/dishvision/feature/menu/BrowseLayoutContractTest.kt \
  feature/menu/src/androidTest/java/com/poco/dishvision/feature/menu/BrowseScreenFocusTest.kt \
  feature/menu/src/androidTest/java/com/poco/dishvision/feature/menu/BrowseBackBehaviorTest.kt \
  feature/menu/src/test/java/com/poco/dishvision/feature/menu/MenuPreviewCatalogContractTest.kt
git commit -m "feat: 收口菜单页样式比例与首屏信息密度"
```

Execution Note (2026-03-20 21:05 CST): 已提交最终收口检查点 `e93278d`（`feat: 收口菜单页样式比例与首屏信息密度`）。
