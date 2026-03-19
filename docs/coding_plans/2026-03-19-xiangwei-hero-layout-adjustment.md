# Xiangwei Hero Layout Adjustment Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将 `湘味首屏` 底部的单个推荐菜卡片替换为 5 张横向推荐菜卡片，并整体下移推荐带，同时保持右侧 `hero image`（主图）空间不变。

**Architecture:** 本次修改只触达 [pencil-new.pen](/Users/PopoY/workingFiles/Projects/POCO/POCO-DISH-VISION/design/pencil-new.pen) 中的 `R3XHK` 首屏节点。实现方式是保留现有 `hero` 主图区与左侧标题文案区，删除旧的 `featureCard`，新增一个底部横向 `recommendation strip`（推荐带）容器并插入 5 张风格统一的推荐菜卡片，最后通过 `screenshot`（截图）和 `layout snapshot`（布局快照）做视觉验收。

**Tech Stack:** Pencil `.pen` design file, MCP Pencil tools, screenshot-based visual verification

---

### Task 1: 调整首屏底部推荐带结构

**Files:**
- Modify: `design/pencil-new.pen`
- Reference: `docs/specs/2026-03-19-xiangwei-hero-layout-adjustment.md`

- [ ] **Step 1: 读取当前首屏与旧推荐卡结构**

读取 `R3XHK` 与 `Fmx6H`，确认旧卡位置、尺寸、层级关系，以及首屏底部可用宽度。

- [ ] **Step 2: 给首屏设置 `placeholder`（占位编辑标记）**

在编辑期间为 `R3XHK` 设置 `placeholder:true`，确保所有后续改动都在同一个目标 screen 内完成。

- [ ] **Step 3: 删除旧单卡并创建新的底部推荐带容器**

移除旧的 `featureCard`，新建横向 `recommendation strip` 容器，放置到比旧卡更靠下的位置，并保留底部安全留白。

- [ ] **Step 4: 插入 5 张推荐菜卡片**

依次创建 `茶油炒鸡`、`青花椒鱼片`、`钵钵茶油鸭`、`跳水鱼`、`水煮活鱼` 五张等宽卡片。每张卡片包含菜名、价格信息与简短推荐理由。

- [ ] **Step 5: 清除 `placeholder`**

完成结构改动后移除 `R3XHK` 的 `placeholder`，避免把临时编辑状态遗留在设计文件中。

### Task 2: 验证布局与主图空间

**Files:**
- Verify: `design/pencil-new.pen`

- [ ] **Step 1: 获取首屏截图**

运行对 `R3XHK` 的 `get_screenshot`，确认 5 张卡片同屏、推荐带位置下移，并且右侧主图未被压缩。

- [ ] **Step 2: 获取布局快照**

运行 `snapshot_layout` 检查是否存在重叠、裁切或越界问题。

- [ ] **Step 3: 如有需要做微调**

若卡片文字拥挤、间距失衡或底边过紧，微调推荐带 `y`、`gap`、`padding` 或单卡高度。

- [ ] **Step 4: 记录最终验证结果**

在交付说明中明确说明：已完成 5 卡推荐带、推荐带已下移、主图空间保持不变，以及使用了哪些验证手段。
