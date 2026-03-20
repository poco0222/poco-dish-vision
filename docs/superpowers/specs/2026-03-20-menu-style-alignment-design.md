# 菜单页样式对齐收口设计

**日期**: 2026-03-20
**作者**: PopoY
**状态**: 已批准

## 背景

当前菜单页虽然已经接入 `ScreenProportions`（比例系统）相关 API，但运行结果与 [pencil-new.pen](/Users/PopoY/workingFiles/Projects/POCO/POCO-DISH-VISION/design/pencil-new.pen) 中的菜单页仍存在明显偏差：

- 左侧分类导轨和整体文案显得过大，画面更像“大按钮 + 大列表”
- 菜品卡片首屏信息密度偏低，没有形成设计稿的 `3x3` 九卡概览节奏
- 菜品卡 description（描述）在运行时观感上经常无法稳定露出
- 菜单页没有像首页一样稳定复用同一条 `PocoTheme`（主题） -> `LocalScreenProportions`（局部比例提供器）运行时注入路径，导致菜单页比例逻辑与首页不一致

本次设计只处理菜单页样式和比例收口，不再优先处理交互细节。

## 目标

- 菜单页真实运行时比例逻辑与首页保持一致
- 左侧分类导轨回到“导航导轨”观感，而不是放大按钮
- 右侧标题区与菜品网格的纵向节奏对齐设计稿
- 首屏形成稳定的 `3x3` 九卡信息密度
- 每张菜品卡稳定显示“菜名 1 行 + 描述 2 行”
- 菜单内容顺序和数量以运行数据为准，不再被设计稿中的旧内容顺序覆盖

## 事实来源

本次改造采用双源约束，但职责严格分离：

- 菜单内容、分类顺序、菜品数量的 `source of truth`（事实来源）为 [catalog.json](/Users/PopoY/workingFiles/Projects/POCO/POCO-DISH-VISION/app/src/main/assets/menu/catalog.json) 与 [菜单.md](/Users/PopoY/workingFiles/Projects/POCO/POCO-DISH-VISION/docs/菜单.md)
- 菜单布局、视觉比例、字号层级、卡片节奏的 `source of truth` 为 [pencil-new.pen](/Users/PopoY/workingFiles/Projects/POCO/POCO-DISH-VISION/design/pencil-new.pen)

因此本轮必须保持当前运行数据中的分类顺序：

1. 招牌热炒
2. 香辣口味
3. 鱼鲜大菜
4. 家常土菜
5. 风味小菜

不再使用设计稿中“风味小菜”与“家常土菜”的旧顺序作为实现依据。

## 范围

### 本次包含

- 统一菜单页真实运行路径上的 `PocoTheme` 注入
- 收口菜单页三块视觉区域：
  - 左侧分类导轨
  - 右侧标题区
  - 菜品网格卡片
- 补最小样式契约测试，锁定比例注入、九卡密度与文案可见性
- 通过本地实屏截图做最终视觉复核

### 本次不包含

- `MenuViewModel`（视图模型）和 `scene state`（场景状态）逻辑重构
- `Back`（返回）链路、焦点恢复、`Browse` / `Focus` 交互流程新增需求
- 菜单 schema（数据结构）调整
- 首页视觉改造

## 方案

### 1. 单位与映射规则

本轮所有菜单页视觉收口都必须遵循同一套映射规则，避免实现时再次混用单位：

- 设计稿中的 `24/40/18/20/15` 等字号全部视为设计基准 `sp`（scale-independent pixels，缩放无关像素），实现时必须通过 `ScreenProportions.scaledSp(designSp)` 映射
- 设计稿中的布局坐标、宽高、间距全部视为设计基准 `px`，实现时必须落到 `ScreenProportions` 的比例 token
- `corner radius`（圆角）、`border width`（边框宽度）、glow blur 等 `micro token`（微观令牌）继续留在 `Dimens`
- 菜单页样式收口不允许在 `MenuRoute`、`CategoryRail`、`MenuItemGrid` 中新增脱离比例系统的硬编码 `dp`

本轮涉及的设计字号必须按下列规则使用：

- helper / main label：`scaledSp(18f)`
- rail label：`scaledSp(20f)`
- category label：`scaledSp(24f)`
- title：`scaledSp(40f)`
- grid title：`scaledSp(24f)`
- grid description：`scaledSp(15f)`

### 2. 统一运行时比例注入链路

问题根因不是“菜单页缺少比例 token（设计令牌）”，而是菜单页没有像首页那样稳定处于同一条运行时主题注入链路中。当前实现里，首页在 `Route` 层明确包裹 `PocoTheme`，菜单页没有同等保证。

本次改造将 `PocoTheme` 提升到应用真实运行壳层，让 Home/Menu/Settings 共用同一份运行时 `ScreenProportions`。这样菜单页不会再退回 `LocalScreenProportions` 的默认 `1920x1080dp` 基线。

如果根层统一注入影响到首页或设置页，则允许把变化限制在菜单真实运行路径，但首选仍是统一运行时比例来源，而不是继续在菜单页局部硬编码尺寸。

代码落点与 owner（责任归属）：

- `AppNavHost`：负责应用真实运行壳层的 `PocoTheme` 注入
- `HomeRoute`：在根层注入生效后，负责清理本地重复主题包裹
- `MenuRoute`：只消费 `LocalScreenProportions`，不再自行兜底一套独立主题链路

### 3. 左侧分类导轨回归“导航”观感

左侧分类导轨保持现有 5 个分类和运行数据顺序不变，只收视觉比例。

收口原则：

- 分类项高度继续由设计稿 `padding=[14,18]` 决定，不新增放大型高度
- 文案继续以设计稿 `24` 为基准字号，但依赖真实运行时比例，不使用默认基线
- 聚焦态 `scale`（缩放）从当前 `1.05f` 收敛到 `1.02f ~ 1.03f` 区间，推荐落点 `1.03f`
- 保留分类项的红底、金边、米白字层次，但不把分类项做成强卡片感主角

目标观感是“窄导轨 + 稳定标签块 + 清晰焦点反馈”，而不是“放大的功能按钮列”。

代码落点与 owner（责任归属）：

- `CategoryRail`：负责分类项聚焦缩放、文本尺寸与导轨项内边距消费
- `ScreenProportions`：负责导轨宽度、label 与 item list 的垂直节奏

### 4. 右侧标题区按设计稿层级收紧

右侧标题区继续保持设计稿层级关系：

- helper / main label：18
- title：40
- description：18

但要重新校正以下三段纵向节奏：

- `label -> title`
- `title -> description`
- `description -> grid`

这些间距仍由 `ScreenProportions` 承接，且只能由以下 token 控制：

- `browseLabelToTitleGap`
- `browseTitleToSubGap`
- `browseSubToGridGap`

调优目标不再是“看起来更大更满”，而是“把更多可视空间让给网格”，确保首屏稳定展示九张卡片。实现时不允许在 `MenuRoute` 内新增脱离 token 的局部 `Spacer` 硬编码。

顶部 helper 文案固定来自运行时真实汇总数据，形如 `44道湘味热菜 · 按分类浏览`，不再受设计稿旧文案影响。

代码落点与 owner（责任归属）：

- `MenuRoute`：负责右列标题区的文本层级与三段纵向间距消费
- `ScreenProportions`：负责标题区宏观几何 token

### 5. 菜品网格回到九卡节奏

菜品网格的目标不是“大卡片更显眼”，而是恢复设计稿中的九宫格密度和节奏。

本次改造保持以下结构不变：

- 3 列网格
- 首卡高亮
- 卡片结构为“图片区 + 标题 + 描述”

但要重新校正 `browseGridViewportHeight`、`cardHeight`、`imageHeight` 与文本区高度之间的关系，原则如下：

- 网格总可视高度由 `ScreenProportions.browseGridViewportHeight` 负责
- 单卡高度、图片区高度、文本区高度拆分由 `MenuItemGrid` 内的 `BoxWithConstraints` 负责
- 先保证卡片文本区有稳定高度，再决定图片区可占空间
- 首屏必须完整形成 `3x3` 观感，而不是向大卡列表漂移
- 菜名保留 `maxLines = 1`
- 描述保留 `maxLines = 2`
- 文本区必须有足够 body（正文）空间，避免“代码允许两行，但运行时被比例挤没”

具体约束：

- 设计稿当前单卡高度按现实现有比值基线为 `278px`，其中图片区基线为 `180px`
- 因此单卡文本区总预算的设计基线为 `98px`
- 实现时，`MenuItemGrid` 的高度拆分必须保证文本区预算不低于设计基线 `98px` 的比例映射值
- `imageHeight` 可以在可用视口不足时收缩，但不允许通过继续挤压文本区来换取九卡显示
- 描述允许在第 2 行尾部出现 `ellipsis`（省略号），但不允许退化为仅显示 1 行描述的常态布局

首卡高亮样式保留，但只保留层级强调，不再让第一张卡的体量显著重于整行其他卡片。

代码落点与 owner（责任归属）：

- `MenuItemGrid`：负责 card/image/body 高度拆分与九卡首屏可见性
- `ScreenProportions`：负责 `browseGridViewportHeight`、网格间距、卡片 body padding 的宏观比例 token
- 如现有 token 不足，允许新增 `browseGridCardBodyMinHeight` 一类菜单专属比例 token，但不得改成散落在组件内的匿名常量

## 实施顺序

1. 先统一 `PocoTheme` 注入路径，消除运行时比例失真根因
2. 收口左侧分类导轨、右侧标题区、菜品网格三块菜单样式
3. 最后再做细调：
   - 聚焦态缩放幅度
   - 标题区间距
   - 图片区与文本区占比

本轮不允许把样式任务再扩散成状态层或交互层改造。

## 测试与验证

### 验证基准

本轮验证基准固定如下：

- `primary target`（主目标）设备：本地 Android TV `emulator`（模拟器）
- 分辨率：`1920x1080`
- `aspect ratio`（宽高比）：`16:9`
- `font scale`（字体缩放）：`1.0`
- `display size`（显示大小）：系统默认值

“首屏九卡可见”的判定标准：

- 默认分类 `招牌热炒` 下，index `0..8` 的 9 张卡片都必须在首屏视口内 `assertIsDisplayed()`
- 不接受第 9 张卡需要滚动后才露出
- 不接受第 9 张卡只有边缘部分露出

“卡片明细可见”的判定标准：

- 首屏至少抽查第一张卡和一张非高亮卡
- 两者都必须能看到“菜名 1 行 + 描述 2 行”的文本区结构
- 描述第 2 行允许尾部 `ellipsis`，但不允许文本区因为高度不足而退化成只剩 1 行描述

### 自动化验证

自动化验证固定采用两层方案，不新增 `screenshot test`（截图测试）或 `golden test`（金图测试）：

- 第 1 层：`Compose UI test`（Compose 界面测试）语义断言，优先落在现有 [BrowseLayoutContractTest.kt](/Users/PopoY/workingFiles/Projects/POCO/POCO-DISH-VISION/feature/menu/src/androidTest/java/com/poco/dishvision/feature/menu/BrowseLayoutContractTest.kt)
- 第 2 层：应用壳层真实运行路径验证，落在现有 [AppNavigationSmokeTest.kt](/Users/PopoY/workingFiles/Projects/POCO/POCO-DISH-VISION/app/src/androidTest/java/com/poco/dishvision/AppNavigationSmokeTest.kt) 或同级 `androidTest`

补最小样式契约测试，至少覆盖以下行为：

- 菜单真实运行路径使用统一的运行时比例注入
- 浏览态首屏保持 `3x3` 九卡可见密度
- 菜品卡描述区可见，稳定支持两行文本
- 分类顺序保持为运行数据顺序，而不是设计稿旧顺序

为避免比例系统带来的跨机不稳定：

- 自动化断言只锁定语义结果，不比对像素级截图
- 比例是否生效通过“helper 文案正确 + 第 9 张卡首屏可见 + 描述区结构可见”这一组外部行为来间接证明

现有交互测试继续保留，但本次不扩大交互覆盖范围。

### 视觉验证

自动化通过后，必须回到本地设备或模拟器实屏截图做最终视觉复核。验收标准如下：

- 左侧分类不再显得过大
- 右侧标题区相对首屏比例明显收敛
- 菜品网格接近设计稿节奏，而不是大号纵向列表
- 菜品卡稳定显示两层文字信息

## 风险与约束

- 如果根层统一 `PocoTheme` 影响首页或设置页尺寸，允许将变化暂时限制在菜单真实运行路径，但不允许退回菜单页独立硬编码尺寸
- 如果统一比例注入后菜单页仍偏大，则继续收口菜单专属比例 token，而不是修改首页已对齐的比例值
- 任何视觉微调都必须遵循“宏观几何走 `ScreenProportions`，微观视觉走 `Dimens`（尺寸令牌）”的分层原则

## 结果定义

本次设计完成的标志不是“菜单页变小了一点”，而是：

- 菜单页和首页使用同一套运行时比例逻辑
- 菜单页首屏视觉节奏接近设计稿
- 菜单数据顺序继续服从运行数据真值
- 本地实屏复核后，不再出现“分类、字体、卡片整体异常放大”“菜品卡明细观感缺失”的问题
