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

| 层 | 当前 | 改为 |
|---|---|---|
| 旧 Hero | `translationX` 平移 + `alpha` hack | `WipeShape(1f - wp, !isWipeForward)` 裁剪 |
| 新 Hero | `WipeShape` 裁剪 + `translationX` 平移 | 仅保留 `WipeShape` 裁剪，移除内部 `translationX` |

#### 4. 文案区改为对称裁剪

同 Hero 区域的改法：

| 层 | 当前 | 改为 |
|---|---|---|
| 旧文案 | `translationX` 平移 + `alpha` hack | `WipeShape(1f - wp, !isWipeForward)` 裁剪 |
| 新文案 | `WipeShape` 裁剪 + `translationX` 平移 | 仅保留 `WipeShape` 裁剪，移除内部 `translationX` |

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
