# POCO Dish Vision Master Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将 POCO Dish Vision 从空仓库推进到可交付的 Android TV 菜单应用，先完成本地菜单闭环，再扩展到 LAN 数据源，最后完成液态玻璃风格与性能硬化。

**Architecture:** 项目采用 `single-activity + Compose for TV + modular monolith`，数据层统一收敛到 `Room`，通过 `Repository + Flow + ViewModel + UiState` 驱动 UI。阶段划分遵循 `local-first, LAN-ready, performance-first`，每个阶段都能独立形成一个可运行、可测试的里程碑。

**Tech Stack:** Kotlin, Jetpack Compose for TV, androidx.tv.material3, ViewModel, Coroutines, Flow, Room, DataStore, Hilt, Coil, JUnit, Compose UI Test, Macrobenchmark, Baseline Profiles

---

## 输入文档

- 规格文档：`docs/specs/2026-03-18-poco-dish-vision-design.md`
- 本总览文档：`docs/coding_plans/2026-03-18-poco-dish-vision-overview.md`
- 阶段一计划：`docs/coding_plans/2026-03-18-phase-1-foundation-and-local-menu.md`
- 阶段二计划：`docs/coding_plans/2026-03-18-phase-2-lan-sync.md`
- 阶段三计划：`docs/coding_plans/2026-03-18-phase-3-visual-polish-and-hardening.md`

## 实施假设

- 包名与 `applicationId` 暂定为 `com.poco.dishvision`
- 使用当前执行时可用的 Android Studio 稳定版、JDK 17 与兼容 `Compose for TV` 的稳定依赖版本
- `minSdk` 暂定为 28，`targetSdk` 取执行时最新稳定版本
- 首轮开发默认以 Android TV Emulator 为主，后续在真实电视上做性能复核
- `Phase 1` 只依赖本地 `assets` 菜单与本地图片
- `Phase 2` 的 LAN 服务为局域网电脑上的 `HTTP + JSON` 服务，不做自动发现

## 跨阶段硬约束

- 所有实现按 `TDD`（测试驱动开发）推进；基础构建脚手架可先完成，再进入测试优先
- 所有新增 Kotlin 文件必须带文档级注释，作者标记为 `PopoY`
- 每个任务完成后都要执行最小验证命令，不允许“写完不跑”
- 每个任务结束单独提交，提交信息以中文为主，专业术语保留英文
- 任何可能影响流畅度的动画、模糊与图片加载，都要先有验证路径再扩展
- 没有真实收益的抽象、模块或配置一律不提前引入

## 推荐目录与职责总览

```text
.
├── settings.gradle.kts
├── build.gradle.kts
├── gradle.properties
├── gradle/libs.versions.toml
├── app/
│   ├── build.gradle.kts
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── assets/menu/catalog.json
│       │   ├── java/com/poco/dishvision/
│       │   │   ├── PocoDishVisionApplication.kt
│       │   │   ├── MainActivity.kt
│       │   │   ├── di/AppModule.kt
│       │   │   └── navigation/
│       │   │       ├── AppDestination.kt
│       │   │       └── AppNavHost.kt
│       └── androidTest/java/com/poco/dishvision/AppNavigationSmokeTest.kt
├── core/
│   ├── model/
│   │   ├── build.gradle.kts
│   │   └── src/
│   │       ├── main/java/com/poco/dishvision/core/model/menu/
│   │       └── test/java/com/poco/dishvision/core/model/menu/
│   ├── data/
│   │   ├── build.gradle.kts
│   │   └── src/
│   │       ├── main/java/com/poco/dishvision/core/data/
│   │       │   ├── di/
│   │       │   ├── local/
│   │       │   ├── preferences/
│   │       │   ├── remote/
│   │       │   ├── repository/
│   │       │   └── sync/
│   │       ├── test/java/com/poco/dishvision/core/data/
│   │       └── androidTest/java/com/poco/dishvision/core/data/
│   └── ui/
│       ├── build.gradle.kts
│       └── src/main/java/com/poco/dishvision/core/ui/
│           ├── components/
│           └── theme/
├── feature/
│   ├── home/
│   │   ├── build.gradle.kts
│   │   └── src/
│   │       ├── main/java/com/poco/dishvision/feature/home/
│   │       ├── test/java/com/poco/dishvision/feature/home/
│   │       └── androidTest/java/com/poco/dishvision/feature/home/
│   ├── menu/
│   └── settings/
├── benchmark/
│   ├── build.gradle.kts
│   └── src/main/java/com/poco/dishvision/benchmark/
└── docs/
    ├── specs/
    └── coding_plans/
```

## 关键文件与职责映射

- `settings.gradle.kts`
  - 注册所有模块与版本仓库
- `gradle/libs.versions.toml`
  - 固定所有三方依赖与 plugin 版本
- `app/src/main/assets/menu/catalog.json`
  - Phase 1 的本地菜单样例数据
- `core/model/...`
  - 菜单领域模型、校验规则、时段规则
- `core/data/local/...`
  - JSON 导入、Room、DAO、Entity、Mapper
- `core/data/remote/...`
  - Phase 2 的 `HTTP API`、DTO 与网络数据源
- `core/data/sync/...`
  - 数据源切换、版本轮询、同步协调
- `core/ui/theme/...`
  - 设计 token、主题、液态玻璃视觉基础组件
- `feature/home/...`
  - `Attract mode`
- `feature/menu/...`
  - `Browse mode`、分类浏览、详情浮层、空闲超时状态机
- `feature/settings/...`
  - 数据源切换、LAN 地址输入、同步状态
- `benchmark/...`
  - 冷启动、模式切换、焦点移动性能基线

## 阶段拆分

### Phase 1: Foundation and Local Menu

- 目标：
  - 让项目能编译、安装、启动
  - 打通 `Local JSON -> Room -> Repository -> UI`
  - 完成首页、浏览页、详情浮层与双模式切换
- 退出条件：
  - 本地菜单可展示
  - 遥控器可操作
  - 空闲可回到 `Attract mode`
  - 关键导航测试通过
  - 基础 `Macrobenchmark` 可运行
- 详细计划：
  - `docs/coding_plans/2026-03-18-phase-1-foundation-and-local-menu.md`

### Phase 2: LAN Sync

- 目标：
  - 保持 Phase 1 UI 基本不动
  - 接入 LAN 服务地址配置、健康检查、版本轮询、菜单同步
  - 失败时回退到本地缓存
- 退出条件：
  - 可从局域网电脑拉取菜单
  - 断网或服务不可达时仍能展示
  - `Settings` 中可配置与观察同步状态
- 详细计划：
  - `docs/coding_plans/2026-03-18-phase-2-lan-sync.md`

### Phase 3: Visual Polish and Hardening

- 目标：
  - 将视觉语言推进到“类 tvOS 18 液态玻璃”的高完成度版本
  - 完成性能、可读性、焦点动画与 TV 质量核验
- 退出条件：
  - 玻璃质感稳定可控
  - 焦点移动与模式切换掉帧受控
  - TV 观看距离与对比度达标
  - 发布前核验清单完整
- 详细计划：
  - `docs/coding_plans/2026-03-18-phase-3-visual-polish-and-hardening.md`

## 推荐实施顺序

- 先完整执行 `Phase 1`
- 在 `Phase 1` 已稳定后再执行 `Phase 2`
- `Phase 3` 只在 `Phase 2` 有稳定同步闭环后开始

不要并行推进 `Phase 2` 与 `Phase 3`，否则会把视觉调优与数据同步问题混在一起，难以定位回归。

## 每阶段通用执行模板

- [ ] 先阅读对应阶段计划与 spec 中相关章节
- [ ] 逐任务执行，保持一步一验
- [ ] 每个任务结束都运行计划中列出的最小测试命令
- [ ] 每个任务完成立即提交
- [ ] 每个阶段完成后再跑一次阶段级回归命令
- [ ] 若阶段验收不通过，不进入下一阶段

## 阶段级回归命令

### Phase 1 回归

```bash
./gradlew :app:assembleDebug
./gradlew :core:model:test
./gradlew :core:data:testDebugUnitTest
./gradlew :feature:home:connectedDebugAndroidTest
./gradlew :feature:menu:connectedDebugAndroidTest
./gradlew :feature:settings:connectedDebugAndroidTest
```

### Phase 2 回归

```bash
./gradlew :core:data:testDebugUnitTest
./gradlew :feature:settings:testDebugUnitTest
./gradlew :app:assembleDebug
```

### Phase 3 回归

```bash
./gradlew :benchmark:connectedCheck
./gradlew :app:assembleRelease
```

## 风险清单

- `Compose for TV` 的焦点管理如果前期状态设计不清，会在 `Phase 1` 就埋下回归点
- LAN 同步如果绕过 `Room` 直接更新 UI，会破坏 `SSOT`
- 玻璃风格如果用大面积实时 `blur` 来实现，会在 `Phase 3` 直接拖垮性能
- 未尽早建立 `benchmark` 与导航测试，会导致“看起来能跑，实际上不稳”

## 完成标准

- 计划文件与 spec 一一对应，没有未决占位标记
- 每个阶段都给出明确文件路径、测试命令、验收出口
- 任何执行者不依赖会话上下文，也能按文档推进实现
