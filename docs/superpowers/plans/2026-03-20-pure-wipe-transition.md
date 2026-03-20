# 首屏纯 Wipe 裁剪过渡 Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将首屏 Hero 和文案区的 Parallax Wipe 过渡改为纯 Wipe 裁剪——移除所有视差平移，新旧层通过对称 `WipeShape` 裁剪共享同一条移动边界。

**Architecture:** 在 `HomeRoute.kt` 中，旧层改为用 `Box` + `graphicsLayer { clip; shape }` 包裹并使用互补 `WipeShape(1f - wp, !isWipeForward)` 裁剪；新层保留现有 `WipeShape` 裁剪但移除内部 `translationX`；清理所有不再使用的 parallax 常量、变量和注释。

**Tech Stack:** Jetpack Compose Animation (`Animatable`, `graphicsLayer`, 自定义 `Shape`)

**Spec:** `docs/superpowers/specs/2026-03-20-pure-wipe-transition-design.md`

---

### Task 1: 删除 parallax 常量和 directionSign 变量

**Files:**
- Modify: `feature/home/src/main/java/com/poco/dishvision/feature/home/HomeRoute.kt:72-79` (常量)
- Modify: `feature/home/src/main/java/com/poco/dishvision/feature/home/HomeRoute.kt:160` (directionSign)

- [ ] **Step 1: 删除三个 parallax 常量**

删除以下 6 行（第 72-79 行），保留第 69-70 行的 `WIPE_DURATION_MS`：

```kotlin
// 删除这些 ↓
/** Hero 层入场视差偏移比例（背景层，偏移较小） */
private const val HERO_PARALLAX_RATIO = 0.08f

/** 文案层入场视差偏移比例（前景层，偏移较大） */
private const val COPY_PARALLAX_RATIO = 0.25f

/** 旧内容退场视差相对入场的衰减因子 */
private const val EXIT_PARALLAX_FACTOR = 0.6f
```

- [ ] **Step 2: 删除 directionSign 变量**

删除第 160 行：

```kotlin
// 删除这行 ↓
val directionSign = if (isWipeForward) 1f else -1f
```

- [ ] **Step 3: 编译检查**

Run: `./gradlew :feature:home:compileDebugKotlin`

Expected: 编译失败，因为 Hero 区和文案区仍在引用已删除的常量和变量。这是预期的——下一个 Task 会修复。

- [ ] **Step 4: Commit (WIP)**

```bash
git add feature/home/src/main/java/com/poco/dishvision/feature/home/HomeRoute.kt
git commit -m "wip: 删除 parallax 常量和 directionSign 变量"
```

---

### Task 2: Hero 主图区改为对称裁剪

**Files:**
- Modify: `feature/home/src/main/java/com/poco/dishvision/feature/home/HomeRoute.kt:234-270`

- [ ] **Step 1: 改写旧 Hero 层**

将旧 Hero 层（当前第 244-253 行）从 `translationX + alpha` 改为 `Box` 包裹 + `WipeShape` 裁剪：

当前代码：
```kotlin
// 底层：旧 Hero（退场方向轻微平移 + 淡出）
HeroImageCard(
    imageRes = previousShowcase.heroImageRes,
    contentDescription = previousShowcase.cardTitle,
    modifier = Modifier.graphicsLayer {
        translationX = -wp * size.width *
            HERO_PARALLAX_RATIO * EXIT_PARALLAX_FACTOR * directionSign
        alpha = if (wp < 1f) 1f else 0f
    },
)
```

改为：
```kotlin
// 底层：旧 Hero（互补裁剪，内容不动）
Box(
    modifier = Modifier.graphicsLayer {
        clip = true
        shape = WipeShape(1f - wp, !isWipeForward)
    },
) {
    HeroImageCard(
        imageRes = previousShowcase.heroImageRes,
        contentDescription = previousShowcase.cardTitle,
    )
}
```

- [ ] **Step 2: 改写新 Hero 层**

将新 Hero 层（当前第 254-269 行）移除内部 `translationX`：

当前代码：
```kotlin
// 上层：新 Hero（Wipe 裁剪 + 入场方向视差平移）
Box(
    modifier = Modifier.graphicsLayer {
        clip = true
        shape = WipeShape(wp, isWipeForward)
    },
) {
    HeroImageCard(
        imageRes = currentShowcase.heroImageRes,
        contentDescription = currentShowcase.cardTitle,
        modifier = Modifier.graphicsLayer {
            translationX = (1f - wp) * size.width *
                HERO_PARALLAX_RATIO * directionSign
        },
    )
}
```

改为：
```kotlin
// 上层：新 Hero（Wipe 裁剪，内容不动）
Box(
    modifier = Modifier.graphicsLayer {
        clip = true
        shape = WipeShape(wp, isWipeForward)
    },
) {
    HeroImageCard(
        imageRes = currentShowcase.heroImageRes,
        contentDescription = currentShowcase.cardTitle,
    )
}
```

- [ ] **Step 3: Commit (WIP)**

```bash
git add feature/home/src/main/java/com/poco/dishvision/feature/home/HomeRoute.kt
git commit -m "wip: Hero 主图区改为对称裁剪"
```

---

### Task 3: 文案区改为对称裁剪

**Files:**
- Modify: `feature/home/src/main/java/com/poco/dishvision/feature/home/HomeRoute.kt:278-314`

- [ ] **Step 1: 改写旧文案层**

将旧文案层（当前第 288-297 行）从 `translationX + alpha` 改为 `Box` + `WipeShape` 裁剪：

当前代码：
```kotlin
// 底层：旧文案（退场方向平移 + 淡出）
HomeCopySection(
    uiState = uiState,
    showcaseItem = previousShowcase,
    modifier = Modifier.graphicsLayer {
        translationX = -wp * size.width *
            COPY_PARALLAX_RATIO * EXIT_PARALLAX_FACTOR * directionSign
        alpha = if (wp < 1f) 1f else 0f
    },
)
```

改为：
```kotlin
// 底层：旧文案（互补裁剪，内容不动）
Box(
    modifier = Modifier.graphicsLayer {
        clip = true
        shape = WipeShape(1f - wp, !isWipeForward)
    },
) {
    HomeCopySection(
        uiState = uiState,
        showcaseItem = previousShowcase,
    )
}
```

- [ ] **Step 2: 改写新文案层**

将新文案层（当前第 298-313 行）移除内部 `translationX`：

当前代码：
```kotlin
// 上层：新文案（Wipe 裁剪 + 入场方向视差平移）
Box(
    modifier = Modifier.graphicsLayer {
        clip = true
        shape = WipeShape(wp, isWipeForward)
    },
) {
    HomeCopySection(
        uiState = uiState,
        showcaseItem = currentShowcase,
        modifier = Modifier.graphicsLayer {
            translationX = (1f - wp) * size.width *
                COPY_PARALLAX_RATIO * directionSign
        },
    )
}
```

改为：
```kotlin
// 上层：新文案（Wipe 裁剪，内容不动）
Box(
    modifier = Modifier.graphicsLayer {
        clip = true
        shape = WipeShape(wp, isWipeForward)
    },
) {
    HomeCopySection(
        uiState = uiState,
        showcaseItem = currentShowcase,
    )
}
```

- [ ] **Step 3: 编译验证**

Run: `./gradlew :feature:home:compileDebugKotlin`

Expected: 编译成功。所有对已删除常量/变量的引用均已移除。

- [ ] **Step 4: Commit (WIP)**

```bash
git add feature/home/src/main/java/com/poco/dishvision/feature/home/HomeRoute.kt
git commit -m "wip: 文案区改为对称裁剪"
```

---

### Task 4: 更新注释并清理未使用 import

**Files:**
- Modify: `feature/home/src/main/java/com/poco/dishvision/feature/home/HomeRoute.kt`

- [ ] **Step 1: 更新 "Parallax Wipe" 注释为 "Wipe"**

替换以下注释中的 "Parallax Wipe"：

| 原文 | 改为 |
|------|------|
| `// ── Parallax Wipe 过渡状态 ──` | `// ── Wipe 过渡状态 ──` |
| `// ── Hero 主图区（Parallax Wipe 过渡） ──` | `// ── Hero 主图区（Wipe 过渡） ──` |
| `// ── 文案区（Parallax Wipe 过渡） ──` | `// ── 文案区（Wipe 过渡） ──` |
| `// 当选中项变化时触发 Parallax Wipe 过渡动画` | `// 当选中项变化时触发 Wipe 过渡动画` |
| WipeShape KDoc: `用于 Parallax Wipe 过渡` | `用于 Wipe 过渡` |

- [ ] **Step 2: 检查并移除未使用的 import**

删除常量后，检查 `FastOutSlowInEasing` 是否仍在使用（是的，`tween` 中仍在用）。无需删除任何 import。

- [ ] **Step 3: 编译验证**

Run: `./gradlew :feature:home:compileDebugKotlin`

Expected: 编译成功，无警告。

- [ ] **Step 4: 最终提交**

将前面的 WIP commits 合并为一个干净的 commit：

```bash
git reset --soft HEAD~3
git add feature/home/src/main/java/com/poco/dishvision/feature/home/HomeRoute.kt
git commit -m "feat: 首屏过渡改为纯 Wipe 裁剪，移除 parallax 视差平移"
```
