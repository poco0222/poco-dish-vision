# 首屏纯 Wipe 裁剪过渡设计

**日期**: 2026-03-20
**作者**: PopoY
**状态**: 已批准

## 背景

首屏 Hero 主图区和文案区的切换过渡当前使用 Parallax Wipe 效果——内容在被裁剪揭示的同时还附带 `translationX` 视差平移（Hero 层 8%、文案层 25%），导致整体效果仍像"轻滑动"。需要改为纯 wipe 裁剪效果：新旧内容保持原位不动，只有裁剪边界在移动。

## 目标

- 移除所有 parallax 视差平移，只保留 `WipeShape` 裁剪
- 旧层增加互补的 `WipeShape` 裁剪（与新层共享同一条裁剪边界）
- 清理不再使用的常量和变量

## 方案：双层对称裁剪

### 改动范围

仅涉及 `feature/home/src/main/java/com/poco/dishvision/feature/home/HomeRoute.kt` 一个文件。

### 具体变更

#### 1. 删除 parallax 常量

移除以下三个常量（保留 `WIPE_DURATION_MS`）：

- `HERO_PARALLAX_RATIO` (0.08f)
- `COPY_PARALLAX_RATIO` (0.25f)
- `EXIT_PARALLAX_FACTOR` (0.6f)

#### 2. 删除 `directionSign` 变量

该变量仅用于 parallax 平移计算，移除后不再需要。

#### 3. Hero 主图区改为对称裁剪

**旧 Hero 层**：当前直接在 `HeroImageCard` 上应用 `Modifier.graphicsLayer { translationX; alpha }`。改为像新层一样，外包 `Box` + `graphicsLayer { clip=true; shape=WipeShape(1f - wp, !isWipeForward) }` 结构。移除 `translationX` 和 `alpha` hack。

**新 Hero 层**：保留外包 `Box` + `WipeShape(wp, isWipeForward)` 裁剪结构，但移除内部 `HeroImageCard` 上的 `Modifier.graphicsLayer { translationX }` 视差平移。

| 层 | 当前 | 改为 |
|---|---|---|
| 旧 Hero | `graphicsLayer { translationX + alpha }` | `Box { graphicsLayer { clip=true; shape=WipeShape(1f-wp, !isWipeForward) } }` |
| 新 Hero | `Box { WipeShape 裁剪 }` + 内部 `graphicsLayer { translationX }` | `Box { WipeShape 裁剪 }`，移除内部 `graphicsLayer` |

#### 4. 文案区改为对称裁剪

结构改法与 Hero 区域完全一致：

**旧文案层**：外包 `Box` + `graphicsLayer { clip=true; shape=WipeShape(1f - wp, !isWipeForward) }`，移除 `translationX` 和 `alpha` hack。

**新文案层**：保留 `WipeShape` 裁剪，移除内部 `translationX`。

| 层 | 当前 | 改为 |
|---|---|---|
| 旧文案 | `graphicsLayer { translationX + alpha }` | `Box { graphicsLayer { clip=true; shape=WipeShape(1f-wp, !isWipeForward) } }` |
| 新文案 | `Box { WipeShape 裁剪 }` + 内部 `graphicsLayer { translationX }` | `Box { WipeShape 裁剪 }`，移除内部 `graphicsLayer` |

#### 5. 更新注释

文件中 "Parallax Wipe" 相关注释更新为 "Wipe"：

- 文件级区域注释 `// ── Parallax Wipe 过渡状态 ──` → `// ── Wipe 过渡状态 ──`
- Hero 区域注释 `// ── Hero 主图区（Parallax Wipe 过渡） ──` → `// ── Hero 主图区（Wipe 过渡） ──`
- 文案区域注释 `// ── 文案区（Parallax Wipe 过渡） ──` → `// ── 文案区（Wipe 过渡） ──`
- `WipeShape` KDoc 中 "Parallax Wipe" → "Wipe"
- `LaunchedEffect` 上方注释中 "Parallax Wipe" → "Wipe"

### 动画行为

以 FORWARD（→）方向为例，`wipeProgress` 从 `0f → 1f`：

```
wp=0:   |旧旧旧旧旧旧旧旧旧旧|  裁剪线在最左
wp=0.5: |新新新新新|旧旧旧旧旧|  裁剪线在正中
wp=1:   |新新新新新新新新新新|  裁剪线在最右
```

BACKWARD（←）方向则相反（从右向左）。

### 不变的部分

- `WipeShape` 类本身不需修改
- `Animatable` + `tween(400ms, FastOutSlowInEasing)` 动画规格不变
- `LaunchedEffect(selectedIndex)` 触发逻辑不变
- Carousel 卡片聚焦动画不受影响

### 已知限制

- 快速连续切换时，如果前一次动画未完成就触发新一次，`previousIndex` 不会更新（因为赋值在 `animateTo` 完成后），导致"旧层"显示的是更早的画面而非上一张。这是现有行为，不在本次改动范围内。
