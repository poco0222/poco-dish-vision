# POCO Dish Vision 设计规格（Design Spec）

## 文档元信息

- 项目名称：POCO Dish Vision
- 文档类型：Architecture Design Spec（架构设计规格）
- 状态：Approved（已由用户在 2026-03-18 确认）
- 作者：PopoY / Codex
- 创建日期：2026-03-18
- 存放路径：`docs/specs/2026-03-18-poco-dish-vision-design.md`

## 1. 项目背景

POCO Dish Vision 是一个面向 Android TV 的餐厅大屏菜单项目。第一阶段以 `local-first`（本地优先）方式运行，在电视端展示菜单、菜品图片与推荐内容；后续扩展为通过 `WiFi + LAN`（无线网络与局域网）从局域网中的电脑服务获取菜单数据。

项目的核心目标不是堆砌复杂功能，而是在电视设备上实现：

- `Smoothness`（流畅度）：焦点切换、模式切换、页面渲染稳定，尽量保持 60fps。
- `Scalability`（可扩展性）：从本地数据平滑演进到局域网数据，不重写 UI 主体结构。
- `Maintainability`（可维护性）：分层清晰，后续可持续迭代。
- `Premium Visual Design`（高级视觉设计）：整体风格借鉴 Apple `tvOS 18` 的 `liquid glass`（液态玻璃）方向，但采用 Android TV 可承受的性能实现方式。

## 2. 已确认的需求边界

### 2.1 当前阶段

- 运行平台：Android TV
- 产品角色：`Display client`（展示客户端）
- 数据来源：本地菜单数据
- 典型安装环境：55 寸 `4K` 壁挂电视，屏幕离地约 2 米，用户与屏幕的直线观看距离约 2 米，且视线通常低于屏幕中心
- 交互模式：`Hybrid`（混合模式）
  - 默认是 `Attract mode`（自动展示/轮播模式）
  - 用户使用 `D-pad`（方向键）操作时切换到 `Browse mode`（浏览模式）

### 2.2 后续阶段

- 数据来源扩展：局域网中的电脑服务
- 协议方向：`HTTP + JSON`
- 运行要求：电视端保持离线可展示能力，不依赖网络才能启动和展示

### 2.3 明确不在首版范围

- 后台 CMS（内容管理系统）
- `mDNS`（服务自动发现）
- `WebSocket`（实时双向推送）
- 多门店、多语言、多币种
- 复杂套餐价、会员价、库存系统联动
- 大面积实时 `blur`（模糊）和高开销全屏特效

## 3. 技术路线对比与最终结论

### 3.1 备选路线

#### 方案 A：`Compose for TV` 现代方案

- 技术栈：`Kotlin`、`Jetpack Compose for TV`、`androidx.tv.material3`、`AAC ViewModel`、`Coroutines + Flow`、`Repository pattern`（仓库模式）、`Room`、`DataStore`、`Hilt`
- 优点：
  - UI 状态驱动天然适合双模式切换
  - 更容易实现现代化 `focus animation`（焦点动画）与玻璃质感组件
  - 更适合长期扩展
- 风险：
  - 如果状态设计和动画控制不严谨，容易引入 `recomposition`（重组）与掉帧问题

#### 方案 B：`Leanback + XML/View` 保守方案

- 优点：
  - 技术成熟，TV 场景历史包袱和经验多
- 缺点：
  - 难以高质量实现现代玻璃视觉风格
  - 后续定制成本与维护成本偏高

#### 方案 C：重自定义渲染方案

- 技术栈：`Compose` 外壳配合 `SurfaceView / OpenGL`
- 优点：
  - 视觉自由度最高
- 缺点：
  - 实现复杂度与维护成本明显过高
  - 与首版需求不匹配，存在明显 `over-engineering`（过度设计）风险

### 3.2 推荐结论

选择方案 A：`Kotlin + Jetpack Compose for TV + TV Material 3 + MVVM/UDF + Repository`。

这是当前最适合本项目的方案，原因如下：

- 能以原生 Android TV 方式保证性能和焦点导航质量
- 可以较低成本做出接近 `liquid glass` 的高级视觉效果
- 在不破坏 UI 结构的前提下，从本地数据源扩展到局域网数据源
- 与 Android 官方推荐的 TV UI 与分层架构方向一致

## 4. 系统总览

### 4.1 架构原则

- `TV-native`（TV 原生）：不采用 WebView 或跨端大一统框架
- `Local-first, LAN-ready`（本地优先，为 LAN 扩展预留）
- `Single source of truth`（单一事实源）
- `State-driven UI`（状态驱动界面）
- `Performance-first`（性能优先）

### 4.2 系统上下文

```text
Local JSON / LAN API
        |
        v
   DataSource Layer
        |
        v
   Parser / Mapper
        |
        v
        Room
        |
        v
    Repository
        |
        v
 ViewModel + UiState
        |
        v
 Compose for TV UI
```

### 4.3 核心运行模式

- `Attract mode`
  - 自动播放品牌主视觉、推荐菜、活动信息
  - 面向远距离观看
- `Browse mode`
  - 遥控器可浏览分类、菜品、详情
  - 面向有明确操作意图的用户

模式切换规则：

- 应用启动默认进入 `Attract mode`
- 任意 `D-pad` 输入立即切换到 `Browse mode`
- 一段时间无操作后平滑回到 `Attract mode`

## 5. 模块拆分（Module Split）

首版采用 `modular monolith`（模块化单体）结构，避免过度拆分：

- `app`
  - 应用入口、`Navigation`（导航）、`DI`（依赖注入）、全局配置
- `core:model`
  - 纯模型定义，不依赖 Android UI
- `core:data`
  - `Repository`、`DataSource`、数据库、JSON 导入、未来 LAN 同步
- `core:ui`
  - 通用 TV 组件、主题、玻璃风格组件、焦点态样式
- `feature:home`
  - 首页和 `Attract mode`
- `feature:menu`
  - 分类浏览、菜品卡片、详情面板
- `feature:settings`
  - 数据源配置、同步状态、版本信息

这样拆分的原因：

- 数据源变化主要收敛在 `core:data`
- 风格变化主要收敛在 `core:ui`
- 页面逻辑按功能隔离，便于测试与迭代

## 6. 数据层设计（Data Layer Design）

### 6.1 数据源抽象

从第一天开始定义统一的 `MenuRepository`，并预留两类数据源：

- `LocalDataSource`
  - 当前阶段从 `assets JSON` 或预置数据导入
- `LanDataSource`
  - 后续从局域网电脑服务拉取菜单数据

UI 层不直接读取 JSON，也不直接访问网络，只消费 `Repository` 输出的统一数据。

### 6.2 单一事实源

统一数据流：

`Local JSON / LAN API -> Parser -> Room -> Repository -> Flow -> ViewModel -> UiState -> Compose UI`

理由：

- 本地与 LAN 两种来源都可以汇聚到同一个数据落点
- UI 不因为数据来源切换而重写
- 具备缓存、回滚、数据验证和离线展示能力

### 6.3 存储策略

首版即引入 `Room`，而不是让 UI 直接读 JSON。

这样做的价值：

- 建立稳定的 `SSOT`（Single Source of Truth，单一事实源）
- 后续 LAN 同步时可以无缝写入本地缓存
- 为版本比较、增量更新和数据校验预留扩展点

### 6.4 配置存储

使用 `DataStore` 保存轻量配置，例如：

- 当前数据源模式
- 局域网服务地址
- 上次同步时间
- 空闲超时时间

## 7. 数据模型设计（Data Model）

首版数据结构就采用 `versioned schema`（带版本号的结构定义）。

### 7.1 顶层对象

- `MenuCatalog`
  - `schemaVersion`
  - `catalogId`
  - `restaurantName`
  - `lastUpdatedAt`
  - `themeConfig`
  - `categories`

### 7.2 分类对象

- `MenuCategory`
  - `id`
  - `title`
  - `subtitle`
  - `sortOrder`
  - `heroImage`
  - `availabilityWindow`

### 7.3 菜品对象

- `MenuItem`
  - `id`
  - `categoryId`
  - `name`
  - `description`
  - `priceInfo`
  - `image`
  - `badges`
  - `sortOrder`
  - `availabilityWindow`
  - `isFeatured`

### 7.4 辅助对象

- `PriceInfo`
  - 当前价格、展示文案、未来扩展多规格能力
- `DisplayBadge`
  - `New`、`Chef's Special`、`Spicy` 等标签
- `AvailabilityWindow`
  - 早餐、午餐、晚餐等展示时段规则
- `MediaAsset`
  - 菜品图、背景图、品牌图
- `ThemeConfig`
  - 颜色、背景层、玻璃层、动画节奏等视觉配置

### 7.5 设计约束

- 所有核心对象必须有稳定 `id`
- 所有列表必须具备显式 `sortOrder`
- 所有资源引用必须允许 `fallback`（降级）
- 顶层必须保留 `schemaVersion`

## 8. LAN 协议设计（Future LAN Integration）

### 8.1 首选协议

后续局域网集成采用：

`HTTP + JSON + local cache`

不在首版引入 `WebSocket`，原因如下：

- 电脑服务实现更简单
- 电视端接入成本最低
- 排障更容易
- 对菜单这种低频更新数据已经足够

### 8.2 最小接口集合

- `GET /health`
  - 服务健康检查
- `GET /menu/version`
  - 返回菜单版本、更新时间、摘要
- `GET /menu`
  - 返回完整菜单
- `GET /assets/...`
  - 返回图片等资源

### 8.3 同步策略

- 应用启动时尝试检查远端版本
- 周期性 `polling`（轮询）版本信息
- 仅当版本变化时再拉取完整菜单
- 拉取成功后写入 `Room`
- 若拉取失败，继续使用上次可用缓存

### 8.4 后续增强方向

- LAN 首阶段通过 `Settings` 页手动录入服务器地址与端口，不依赖服务发现
- 第二阶段再考虑 `mDNS`
- 若更新频率明显升高，再考虑 `SSE`（Server-Sent Events）或 `WebSocket`

## 9. UI 与交互设计（UI and Interaction）

### 9.1 页面结构

- `Home / Attract Screen`
  - 自动轮播主视觉、推荐菜、活动信息
  - 首屏视觉重心下移，核心文案和推荐菜信息放在屏幕中下区域
  - 顶部只保留轻量品牌条或状态信息，不承载核心阅读内容
- `Browse Screen`
  - 左侧分类栏、中部大尺寸菜品卡片区、底部 `detail dock`（详情停靠条）
  - 当前焦点菜品的核心信息优先展示在底部区域，而不是右侧边缘区域
- `Item Detail Panel`
  - 由浏览态的底部 `detail dock` 进入
  - 采用中下位置的 `floating panel`（浮动详情卡），不做高位满屏跳转
- `Settings Screen`
  - 面向店员或安装人员的内部配置页

### 9.2 导航原则

- 采用 `single-activity`（单 Activity）结构
- 页面导航尽量浅，不做深层嵌套
- `Back`（返回键）行为线性可预测
- 焦点路径必须稳定、可重复、无跳跃感
- 重要交互反馈和主阅读区域优先放在屏幕中下半区，减少高位壁挂带来的仰视阅读负担

### 9.3 焦点模型

- 分类轴和菜品轴清晰分离
- 焦点元素具备明确视觉反馈
- 首焦点位置固定且可测试
- 模式切换时保证焦点回退逻辑一致
- 焦点移动后，详情信息优先在底部 `detail dock` 更新，避免用户频繁在左右边缘间扫视

## 10. 视觉风格设计（Liquid Glass Inspired）

### 10.1 视觉方向

整体视觉参考 Apple `tvOS 18` 的 `liquid glass` 风格，但不追求逐像素复刻，而是追求：

- 高级感
- 清晰可读
- 焦点反馈明确
- 对 Android TV 硬件更友好的实现
- 更适配高位壁挂、轻仰视观看场景的实现

### 10.2 推荐视觉组成

- 偏 `frosted glass`（磨砂玻璃）而非强镜面反射的半透明卡片与浮层
- 柔和描边与高光
- 焦点时轻微 `scale`（缩放）与 `glow`（发光）
- 分层渐变背景
- 少量 `parallax`（视差）与主视觉光效
- 首页主视觉和重点信息采用中下布局，减少顶部信息密度

### 10.3 明确禁止

- 大面积实时全屏 `blur`
- 同屏大量无限动画
- 依赖高开销 shader 的炫技式实现
- 因高位灯光反射而放大眩光问题的强镜面玻璃效果

### 10.4 设计原则

- 从远距离观看仍然清晰
- 文本层级与对比度必须适应约 2 米观看距离和轻仰视角
- 主要信息和价格尽量落在屏幕中下半区
- 信息密度适合电视观看
- 焦点反馈优先于装饰动画
- 动画节奏偏慢、稳定、克制

## 11. 性能策略（Performance Strategy）

### 11.1 基本原则

- `Performance-first`
- `Focus latency`（焦点延迟）优先于复杂特效
- 首屏展示不依赖网络

### 11.2 具体要求

- 使用 `LazyRow / LazyColumn` 处理列表
- `UiState` 保持不可变
- 重计算放在 `ViewModel` 或数据层
- 避免在 `Composable` 中执行复杂计算
- 动画聚焦在当前项与模式切换区域
- 图片统一异步加载并做缓存

### 11.3 性能工程

首版性能工程策略调整为：

- `Macrobenchmark`：保留在 `Phase 1`，用于建立冷启动与交互性能基线
- `Baseline Profiles`：延后到 `Phase 3`，待发布构建链路和稳定热点路径明确后再接入，避免文档先宣称完成而实现尚未落地

核心观测点：

- 冷启动时间
- 首屏可交互时间
- 焦点快速移动稳定性
- `Attract mode -> Browse mode` 切换流畅度

## 12. 异常处理与降级策略（Error Handling）

### 12.1 目标

电视端永远优先“可展示”，而不是优先“严格同步成功”。

### 12.2 错误分类

- `NetworkError`
- `ParseError`
- `DataValidationError`
- `AssetError`

### 12.3 降级行为

- 有缓存则直接展示缓存
- 远端不可达则回退本地数据
- 解析失败则保留上一个可用版本
- 图片缺失则显示占位图
- 技术性错误只在 `Settings` 页暴露，不在顾客主屏全屏报错

## 13. 测试策略（Testing Strategy）

### 13.1 `Unit test`

- `Repository` 行为
- JSON 解析与数据校验
- 版本比较
- 模式切换状态机
- 空闲超时回到轮播逻辑

### 13.2 `Integration test`

- `LocalDataSource -> Room -> Repository`
- 后续扩展 `LanDataSource -> Room`

### 13.3 `UI test`

- `D-pad navigation`
- 首焦点位置
- `Back` 行为
- 模式切换
- 焦点恢复逻辑

### 13.4 `Macrobenchmark`

- 冷启动
- 首页进入浏览页
- 焦点快速切换

## 14. MVP 范围（Phase 1）

首版必须交付：

- 本地 `JSON -> Room -> UI` 全链路
- 品牌首页
- 分类浏览页
- 浏览态底部 `detail dock`
- 菜品详情浮层
- `Attract mode / Browse mode`
- 基础玻璃风格主题
- 本地图片加载
- 简单设置页
- 关键导航测试
- 基础性能基线（以 `Macrobenchmark` 为准，`Baseline Profiles` 延后到 `Phase 3`）

首版不做：

- 局域网自动发现
- 实时推送
- 后台 CMS
- 多租户能力
- 复杂价格体系
- 过度视觉特效

## 15. 实施顺序建议（Implementation Order）

### 15.1 Phase 1

- 建立工程骨架与模块结构
- 完成主题、导航和基础页面框架
- 打通本地 JSON 导入到 `Room`
- 实现首页、浏览页、底部 `detail dock` 和详情浮层
- 接入模式切换状态机
- 建立 `Macrobenchmark` 性能与测试基线

### 15.2 Phase 2

- 新增 `LanDataSource`
- 加入服务地址配置和同步状态
- 完成 LAN 拉取与本地缓存联动

### 15.3 Phase 3

- 增强视觉效果
- 优化同步机制
- 接入 `Baseline Profiles`
- 再评估是否需要 `mDNS` 或更实时的更新通道

## 16. 关键设计结论

- 产品角色固定为 `Display client`
- 架构采用 `local-first, LAN-ready`
- UI 采用 `Compose for TV`
- 数据统一通过 `Room` 建立 `SSOT`
- 视觉采用“受 tvOS 18 启发”的 `liquid glass` 语言，而非高开销复刻
- 性能、焦点导航和可降级能力属于一等公民，不作为后补优化项

## 17. 参考资料（Official References）

- Android Developers: [Compose for TV](https://developer.android.com/training/tv/playback/compose)
- Android Developers: [Guide to app architecture](https://developer.android.com/topic/architecture)
- Android Developers: [Architecture recommendations](https://developer.android.com/topic/architecture/recommendations)
- Android Developers: [TV navigation](https://developer.android.com/training/tv/start/navigation)
- Android Developers: [Compose performance best practices](https://developer.android.com/develop/ui/compose/performance/bestpractices)
- Android Developers: [Baseline Profiles overview](https://developer.android.com/topic/performance/baselineprofiles/overview)
- Android Developers: [TV app quality](https://developer.android.com/docs/quality-guidelines/tv-app-quality)
