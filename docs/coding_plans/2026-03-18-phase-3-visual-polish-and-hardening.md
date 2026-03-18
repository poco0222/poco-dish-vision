# POCO Dish Vision Phase 3 Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在已有本地与 LAN 数据闭环之上，完成高质量液态玻璃风格、焦点动画、性能基线与高位壁挂 TV 可读性硬化，形成发布前候选版本。

**Architecture:** Phase 3 主要作用于 `core:ui`、`feature:home`、`feature:menu` 与 `benchmark`。所有视觉增强都必须建立在已有状态流与焦点模型之上，不得引入绕过 `UiState` 的动画逻辑；性能优化必须通过 benchmark 与 profile 量化验证。

**Tech Stack:** Compose for TV, TV Material 3, Coil, Compose Animation, Macrobenchmark, Baseline Profiles, Android TV quality guidelines

---

## Phase 3 验收标准

- 形成稳定的玻璃视觉 token 与组件
- 首页与浏览页拥有统一的焦点反馈、层次与过渡
- benchmark 结果稳定，未引入明显掉帧回归
- 文本与对比度适合 55 寸高位壁挂电视约 2 米观看距离
- 发布前核验清单与回归路径可执行

## Phase 3 主要文件

- Create: `core/ui/src/main/java/com/poco/dishvision/core/ui/theme/GlassTokens.kt`
- Modify: `core/ui/src/main/java/com/poco/dishvision/core/ui/theme/ColorTokens.kt`
- Modify: `core/ui/src/main/java/com/poco/dishvision/core/ui/theme/Dimens.kt`
- Modify: `core/ui/src/main/java/com/poco/dishvision/core/ui/theme/PocoTheme.kt`
- Modify: `core/ui/src/main/java/com/poco/dishvision/core/ui/components/GlassSurface.kt`
- Create: `core/ui/src/main/java/com/poco/dishvision/core/ui/components/FocusGlowBorder.kt`
- Create: `core/ui/src/main/java/com/poco/dishvision/core/ui/components/GlassHeroBackdrop.kt`
- Create: `core/ui/src/test/java/com/poco/dishvision/core/ui/theme/GlassTokensTest.kt`
- Modify: `feature/home/src/main/java/com/poco/dishvision/feature/home/AttractCarousel.kt`
- Modify: `feature/home/src/main/java/com/poco/dishvision/feature/home/HomeRoute.kt`
- Modify: `feature/menu/src/main/java/com/poco/dishvision/feature/menu/MenuRoute.kt`
- Modify: `feature/menu/src/main/java/com/poco/dishvision/feature/menu/MenuItemRow.kt`
- Modify: `feature/menu/src/main/java/com/poco/dishvision/feature/menu/ItemDetailPanel.kt`
- Create: `feature/menu/src/androidTest/java/com/poco/dishvision/feature/menu/MenuReadabilityTest.kt`
- Modify: `benchmark/src/main/java/com/poco/dishvision/benchmark/StartupBenchmark.kt`
- Create: `benchmark/src/main/java/com/poco/dishvision/benchmark/BrowseTransitionBenchmark.kt`
- Create: `benchmark/src/main/java/com/poco/dishvision/benchmark/FocusScrollBenchmark.kt`
- Create: `docs/coding_plans/phase-3-release-checklist.md`

### Task 1: Establish Glass Visual Tokens and Base Components

**Files:**
- Create: `core/ui/src/main/java/com/poco/dishvision/core/ui/theme/GlassTokens.kt`
- Modify: `core/ui/src/main/java/com/poco/dishvision/core/ui/theme/ColorTokens.kt`
- Modify: `core/ui/src/main/java/com/poco/dishvision/core/ui/theme/Dimens.kt`
- Modify: `core/ui/src/main/java/com/poco/dishvision/core/ui/theme/PocoTheme.kt`
- Modify: `core/ui/src/main/java/com/poco/dishvision/core/ui/components/GlassSurface.kt`
- Create: `core/ui/src/main/java/com/poco/dishvision/core/ui/components/FocusGlowBorder.kt`
- Create: `core/ui/src/test/java/com/poco/dishvision/core/ui/theme/GlassTokensTest.kt`

- [ ] **Step 1: Write the failing unit test for token defaults**

```kotlin
@Test
fun `glass tokens expose readable alpha and border values`() {
    assertTrue(GlassTokens.surfaceAlpha in 0.55f..0.85f)
    assertTrue(GlassTokens.focusedScale > 1f)
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :core:ui:testDebugUnitTest --tests "*GlassTokens*"`  
Expected: FAIL with missing tokens

- [ ] **Step 3: Implement glass tokens and reusable focus border**

```kotlin
object GlassTokens {
    const val surfaceAlpha = 0.72f
    const val focusedScale = 1.04f
    val borderColor = Color(0x66FFFFFF)
}
```

- [ ] **Step 4: Re-run token test**

Run: `./gradlew :core:ui:testDebugUnitTest --tests "*GlassTokens*"`  
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add core/ui/src/main/java/com/poco/dishvision/core/ui/theme core/ui/src/main/java/com/poco/dishvision/core/ui/components
git commit -m "建立液态玻璃视觉 token 与基础组件"
```

### Task 2: Polish Home Screen Hero Layer and Lower Visual Hierarchy

**Files:**
- Create: `core/ui/src/main/java/com/poco/dishvision/core/ui/components/GlassHeroBackdrop.kt`
- Modify: `feature/home/src/main/java/com/poco/dishvision/feature/home/AttractCarousel.kt`
- Modify: `feature/home/src/main/java/com/poco/dishvision/feature/home/HomeRoute.kt`
- Modify: `feature/home/src/androidTest/java/com/poco/dishvision/feature/home/HomeScreenTest.kt`

- [ ] **Step 1: Extend home screen test with hero layer assertions**

```kotlin
@Test
fun home_screen_shows_glass_hero_backdrop() {
    composeTestRule.onNodeWithTag("glass-hero-backdrop").assertExists()
    composeTestRule.onNodeWithTag("home-lower-hero-zone").assertExists()
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :feature:home:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.poco.dishvision.feature.home.HomeScreenTest`  
Expected: FAIL because hero backdrop tags are missing

- [ ] **Step 3: Implement layered gradient, image tint, restrained auto-advance motion, and lower-centered hero composition**

```kotlin
GlassHeroBackdrop(
    imageUrl = state.heroImageUrl,
    gradientStrength = GlassTokens.heroGradientStrength,
)
```

实现要求：
- 强化中下区域的菜名、价格和短描述
- 顶部信息保持轻量，避免把主信息堆在高位区域
- 玻璃层更偏 `frosted glass`，避免强反射感

- [ ] **Step 4: Re-run home UI test**

Run: `./gradlew :feature:home:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.poco.dishvision.feature.home.HomeScreenTest`  
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add core/ui/src/main/java/com/poco/dishvision/core/ui/components/GlassHeroBackdrop.kt feature/home
git commit -m "打磨首页 hero 区与轮播动效"
```

### Task 3: Polish Browse Screen Focus Feedback and Bottom Detail Hierarchy

**Files:**
- Modify: `feature/menu/src/main/java/com/poco/dishvision/feature/menu/MenuRoute.kt`
- Modify: `feature/menu/src/main/java/com/poco/dishvision/feature/menu/MenuItemRow.kt`
- Modify: `feature/menu/src/main/java/com/poco/dishvision/feature/menu/ItemDetailPanel.kt`
- Create: `feature/menu/src/androidTest/java/com/poco/dishvision/feature/menu/MenuReadabilityTest.kt`
- Modify: `feature/menu/src/androidTest/java/com/poco/dishvision/feature/menu/BrowseScreenFocusTest.kt`

- [ ] **Step 1: Add failing readability/focus test**

```kotlin
@Test
fun focused_item_updates_bottom_detail_dock_and_can_expand_overlay() {
    composeTestRule.onNodeWithTag("menu-item-mains-0").requestFocus()
    composeTestRule.onNodeWithTag("detail-dock").assertExists()
    composeTestRule.onRoot().performKeyInput { keyDown(Key.DirectionCenter); keyUp(Key.DirectionCenter) }
    composeTestRule.onNodeWithTag("detail-overlay").assertExists()
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :feature:menu:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.poco.dishvision.feature.menu.MenuReadabilityTest`  
Expected: FAIL with missing bottom detail hierarchy or focus styling hooks

- [ ] **Step 3: Implement focused scale, highlight, readable bottom detail dock, and optional center-lower detail overlay**

```kotlin
Modifier.graphicsLayer {
    scaleX = if (focused) GlassTokens.focusedScale else 1f
    scaleY = if (focused) GlassTokens.focusedScale else 1f
}
```

实现要求：
- 浏览态优先强化底部 `detail dock`
- 详情浮层展开位置保持在中下区域
- 避免把说明文本和价格固定在右边缘

- [ ] **Step 4: Re-run focus and readability tests**

Run: `./gradlew :feature:menu:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.poco.dishvision.feature.menu.BrowseScreenFocusTest`  
Expected: PASS

Run: `./gradlew :feature:menu:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.poco.dishvision.feature.menu.MenuReadabilityTest`  
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add feature/menu
git commit -m "打磨浏览页焦点反馈与详情层级"
```

### Task 4: Quantify Performance with Benchmarks and Baseline Profiles

**Files:**
- Modify: `benchmark/src/main/java/com/poco/dishvision/benchmark/StartupBenchmark.kt`
- Create: `benchmark/src/main/java/com/poco/dishvision/benchmark/BrowseTransitionBenchmark.kt`
- Create: `benchmark/src/main/java/com/poco/dishvision/benchmark/FocusScrollBenchmark.kt`
- Optionally Modify: `app/build.gradle.kts`

- [ ] **Step 1: Add failing or placeholder benchmark targets**

```kotlin
@Test
fun browse_transition_has_no_major_jank() = benchmarkRule.measureRepeated {
    startActivityAndWait()
    device.pressDPadRight()
}
```

- [ ] **Step 2: Run benchmark suite to capture baseline**

Run: `./gradlew :benchmark:connectedCheck`  
Expected: PASS with benchmark output files

- [ ] **Step 3: Add baseline profile generation hooks and optimize hot paths**

```kotlin
baselineProfileRule.collect(
    packageName = "com.poco.dishvision",
) {
    startActivityAndWait()
    device.pressDPadRight()
}
```

- [ ] **Step 4: Re-run benchmark suite after optimization**

Run: `./gradlew :benchmark:connectedCheck`  
Expected: PASS with no obvious regression relative to Step 2

- [ ] **Step 5: Commit**

```bash
git add benchmark app/build.gradle.kts
git commit -m "量化并优化关键 TV 场景性能"
```

### Task 5: Final TV Quality Pass and Release Checklist

**Files:**
- Create: `docs/coding_plans/phase-3-release-checklist.md`
- Modify: `feature/home/src/androidTest/java/com/poco/dishvision/feature/home/HomeScreenTest.kt`
- Modify: `feature/menu/src/androidTest/java/com/poco/dishvision/feature/menu/MenuReadabilityTest.kt`
- Modify: `feature/settings/src/androidTest/java/com/poco/dishvision/feature/settings/SettingsScreenTest.kt`

- [ ] **Step 1: Create release checklist covering TV quality, readability, and fallback behavior**

```markdown
- [ ] 约 2 米观看距离下主要标题和价格可读
- [ ] 高位壁挂场景下主信息不依赖顶部区域
- [ ] 焦点始终可见且无丢失
- [ ] LAN 失效时不阻塞菜单展示
- [ ] `Back` 行为无死循环
```

- [ ] **Step 2: Expand instrumentation tests for final QA paths**

Run: `./gradlew :feature:home:connectedDebugAndroidTest`  
Expected: PASS

Run: `./gradlew :feature:menu:connectedDebugAndroidTest`  
Expected: PASS

Run: `./gradlew :feature:settings:connectedDebugAndroidTest`  
Expected: PASS

- [ ] **Step 3: Assemble release candidate**

Run: `./gradlew :app:assembleRelease`  
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 4: Commit**

```bash
git add docs/coding_plans/phase-3-release-checklist.md feature/home feature/menu feature/settings
git commit -m "完成 TV 质量核验与发布清单"
```

## Phase 3 Final Verification

- [ ] Run: `./gradlew :benchmark:connectedCheck`
- [ ] Run: `./gradlew :app:assembleRelease`
- [ ] Manual check:
  - 焦点切换无明显掉帧
  - 玻璃卡片不会影响文本可读性
  - 不依赖全屏实时 `blur` 也能保持高级质感
  - 约 2 米高位壁挂观看场景下层级清晰
