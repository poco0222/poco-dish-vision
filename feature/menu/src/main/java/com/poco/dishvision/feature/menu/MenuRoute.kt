/**
 * @file MenuRoute.kt
 * @author PopoY
 * @date 2026-03-19
 * @description 浏览页 Route，负责桥接 ViewModel 与 Browse mode UI。
 *              布局匹配设计稿"湘味分类/招牌热炒"：左侧分类导轨 + 右侧 3 列菜品网格。
 */
package com.poco.dishvision.feature.menu

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.tv.material3.Text
import com.poco.dishvision.core.data.repository.MenuRepository
import com.poco.dishvision.core.ui.theme.ColorTokens
import com.poco.dishvision.core.ui.theme.LocalScreenProportions

/**
 * 浏览页入口 Route。未注入仓储时使用 preview 数据，以便先完成 UI 与交互测试。
 *
 * @param menuRepository 菜单仓储；为空时进入 preview 模式。
 * @param modifier 外层 Modifier。
 */
@Composable
fun MenuRoute(
    menuRepository: MenuRepository? = null,
    onUserInteraction: () -> Unit = {},
    onBackFromBrowseRoot: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    if (menuRepository == null) {
        PreviewMenuRoute(
            modifier = modifier,
            onUserInteraction = onUserInteraction,
            onBackFromBrowseRoot = onBackFromBrowseRoot,
        )
        return
    }

    val menuViewModel: MenuViewModel = viewModel(
        factory = MenuViewModel.provideFactory(menuRepository),
    )
    val uiState by menuViewModel.uiState.collectAsStateWithLifecycle()

    MenuScreen(
        uiState = uiState,
        modifier = modifier,
        onUserInteraction = onUserInteraction,
        onCategoryFocused = menuViewModel::onCategoryFocused,
        onCategoryItemsRequested = menuViewModel::onCategoryItemsRequested,
        onBrowseItemFocused = menuViewModel::onBrowseItemFocused,
        onBrowseViewportChanged = menuViewModel::onBrowseViewportChanged,
        onFocusSceneItemFocused = menuViewModel::onFocusSceneItemFocused,
        onItemConfirmed = { menuViewModel.onFocusedItemConfirmed() },
        onDismissFocusScene = menuViewModel::dismissFocusScene,
        onBackFromBrowseRoot = onBackFromBrowseRoot,
    )
}

/**
 * Preview 模式下的浏览页 Route，直接在 Compose 内维护本地状态。
 *
 * @param modifier 外层 Modifier。
 */
@Composable
private fun PreviewMenuRoute(
    onUserInteraction: () -> Unit,
    onBackFromBrowseRoot: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val categories = remember { previewMenuCategories() }
    var interactionState by remember {
        mutableStateOf(
            MenuInteractionState(
                selectedCategoryId = DEFAULT_BROWSE_CATEGORY_ID,
                browseFocusedItemId = "hot-tea-chicken",
                categoryBrowseStates = mapOf(
                    DEFAULT_BROWSE_CATEGORY_ID to CategoryBrowseState(
                        focusedItemId = "hot-tea-chicken",
                        firstVisibleItemIndex = 0,
                        firstVisibleItemScrollOffset = 0,
                    ),
                ),
            ),
        )
    }

    val uiState = remember(
        categories,
        interactionState,
    ) {
        buildMenuUiState(
            categories = categories,
            interactionState = interactionState,
        )
    }

    MenuScreen(
        uiState = uiState,
        modifier = modifier,
        onUserInteraction = onUserInteraction,
        onCategoryFocused = { categoryId ->
            interactionState = handleCategoryRailFocus(
                currentState = interactionState,
                categories = categories,
                categoryId = categoryId,
            )
        },
        onCategoryItemsRequested = { categoryId ->
            interactionState = requestBrowseItemFocus(
                currentState = interactionState,
                categories = categories,
                categoryId = categoryId,
            )
        },
        onBrowseItemFocused = { itemId ->
            interactionState = recordBrowseItemFocus(
                currentState = interactionState,
                categories = categories,
                itemId = itemId,
            )
        },
        onBrowseViewportChanged = { firstVisibleItemIndex, firstVisibleItemScrollOffset ->
            interactionState = recordBrowseViewport(
                currentState = interactionState,
                categories = categories,
                firstVisibleItemIndex = firstVisibleItemIndex,
                firstVisibleItemScrollOffset = firstVisibleItemScrollOffset,
            )
        },
        onFocusSceneItemFocused = { itemId ->
            interactionState = recordFocusSceneItemFocus(
                currentState = interactionState,
                categories = categories,
                itemId = itemId,
            )
        },
        onItemConfirmed = {
            interactionState = enterFocusScene(
                currentState = interactionState,
                categories = categories,
            )
        },
        onDismissFocusScene = {
            interactionState = exitFocusScene(
                currentState = interactionState,
                categories = categories,
            )
        },
        onBackFromBrowseRoot = onBackFromBrowseRoot,
    )
}

/**
 * 浏览页主界面，匹配设计稿"湘味分类/招牌热炒"。
 *
 * 布局结构：
 * - 背景：BrowseBackgroundGradient 渐变
 * - 左上角：品牌名 + 副标题
 * - 左侧：分类导轨 (CategoryRail)
 * - 右侧顶部：分类标签 + 分类标题 + 分类描述
 * - 右侧主体：3 列菜品网格 (MenuItemGrid) 或聚焦舞台 (FocusStageLayout)
 *
 * @param uiState 浏览页 UI 状态。
 * @param modifier 外层 Modifier。
 * @param onCategoryFocused 分类获焦回调。
 * @param onCategoryItemsRequested 请求把焦点从分类导轨切入菜品区的回调。
 * @param onBrowseItemFocused BrowseScene 菜品聚焦回调。
 * @param onBrowseViewportChanged BrowseScene 网格滚动回调。
 * @param onFocusSceneItemFocused FocusScene 小卡聚焦回调。
 * @param onItemConfirmed 菜品确认回调。
 * @param onDismissFocusScene 关闭 FocusScene 回调。
 * @param onBackFromBrowseRoot 浏览态根层 Back 回调。
 */
@Composable
private fun MenuScreen(
    uiState: MenuUiState,
    modifier: Modifier = Modifier,
    onUserInteraction: () -> Unit,
    onCategoryFocused: (String) -> Unit,
    onCategoryItemsRequested: (String) -> Unit,
    onBrowseItemFocused: (String) -> Unit,
    onBrowseViewportChanged: (Int, Int) -> Unit,
    onFocusSceneItemFocused: (String) -> Unit,
    onItemConfirmed: () -> Unit,
    onDismissFocusScene: () -> Unit,
    onBackFromBrowseRoot: () -> Unit,
) {
    val proportions = LocalScreenProportions.current

    // 菜品总数（全分类汇总）
    val totalItemCount = remember(uiState.categories) {
        uiState.categories.sumOf { it.items.size }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onPreviewKeyEvent { keyEvent ->
                if (keyEvent.key == Key.Back) {
                    if (keyEvent.type == KeyEventType.KeyDown) {
                        onUserInteraction()
                        if (uiState.scene == MenuScene.Focus) {
                            onDismissFocusScene()
                        } else {
                            onBackFromBrowseRoot()
                        }
                    }
                    // Back 的 KeyUp 必须一并消费，否则宿主 Activity 仍可能收到系统返回事件。
                    true
                } else if (keyEvent.type == KeyEventType.KeyDown && keyEvent.key.isBrowseInteractionKey()) {
                    onUserInteraction()
                    false
                } else {
                    false
                }
            }
            .background(
                brush = Brush.verticalGradient(
                    colors = ColorTokens.BrowseBackgroundGradient,
                ),
            )
            .testTag("browse-screen"),
    ) {
        // ── 主体区域：左右两列顶部对齐，匹配设计稿坐标 ──
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = proportions.screenHorizontalPadding,
                    end = proportions.screenHorizontalPadding,
                    top = proportions.screenTopPadding,
                ),
        ) {
            // ── 左列：品牌区 + 分类导轨 ──
            Column(
                modifier = Modifier.width(proportions.browseRailWidth),
            ) {
                // 品牌名（设计稿 y=32）
                Text(
                    text = "新华饭店",
                    color = ColorTokens.TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = proportions.scaledSp(24f),
                )
                // 副标题（设计稿 y=68）
                Text(
                    text = "${totalItemCount}道湘味热菜 · 按分类浏览",
                    color = ColorTokens.TextMuted,
                    fontSize = proportions.scaledSp(18f),
                    modifier = Modifier.testTag("browse-helper-copy"),
                )

                // 品牌区到分类导轨的间距（设计稿 y=86→156 ≈ 70px）
                Spacer(modifier = Modifier.height(proportions.browseBrandToRailGap))

                // 分类导轨（包含 "分类" 标签 + 分类项）
                CategoryRail(
                    categories = uiState.categories,
                    selectedCategoryId = uiState.selectedCategoryId,
                    onCategoryFocused = onCategoryFocused,
                    onMoveFocusToItems = onCategoryItemsRequested,
                )
            }

            Spacer(modifier = Modifier.width(proportions.browseRailToContentGap))

            // ── 右列：分类标题区 + 菜品网格 ──
            Column(
                modifier = Modifier
                    .width(proportions.browseContentWidth)
                    .fillMaxHeight(),
            ) {
                // 分类切换时整个右列内容区使用 fadeIn/fadeOut 过渡
                AnimatedContent(
                    targetState = uiState.selectedCategoryId,
                    transitionSpec = {
                        fadeIn(tween(200)) togetherWith fadeOut(tween(150))
                    },
                    label = "category-switch",
                    modifier = Modifier.fillMaxSize(),
                ) { animatedCategoryId ->
                    // 在动画作用域中重新解析分类数据
                    val animatedCategory = uiState.categories
                        .firstOrNull { it.categoryId == animatedCategoryId }

                    Column(modifier = Modifier.fillMaxSize()) {
                        // 分类标签（金色小字，设计稿 y=32）
                        Text(
                            text = animatedCategory?.subtitle ?: "热锅现炒",
                            color = ColorTokens.Accent,
                            fontWeight = FontWeight.Medium,
                            fontSize = proportions.scaledSp(18f),
                            modifier = Modifier.testTag("browse-main-label"),
                        )

                        Spacer(modifier = Modifier.height(proportions.browseLabelToTitleGap))

                        // 分类大标题（设计稿 y=64）
                        Text(
                            text = animatedCategory?.displayName ?: "招牌热炒",
                            color = ColorTokens.TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = proportions.scaledSp(40f),
                            modifier = Modifier.testTag("browse-main-title"),
                        )

                        Spacer(modifier = Modifier.height(proportions.browseTitleToSubGap))

                        // 分类描述（设计稿 y=120, width=1180）
                        Text(
                            text = animatedCategory?.description
                                ?: "锅气、辣香、下饭感最强的一页，先看最能代表湘味火候的现炒菜。",
                            color = ColorTokens.TextSecondary,
                            fontSize = proportions.scaledSp(18f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .width(
                                    if (uiState.scene == MenuScene.Focus) {
                                        proportions.focusSubtitleWidth
                                    } else {
                                        proportions.browseSubtitleWidth
                                    },
                                )
                                .testTag("browse-main-description"),
                        )

                        Spacer(modifier = Modifier.height(proportions.browseSubToGridGap))

                        // Grid ↔ FocusStage 切换使用 Crossfade 过渡
                        Crossfade(
                            targetState = uiState.scene,
                            animationSpec = tween(durationMillis = 200),
                            label = "grid-stage-crossfade",
                            modifier = Modifier.weight(1f),
                        ) { scene ->
                            if (scene == MenuScene.Focus && uiState.focusSceneState != null) {
                                // 聚焦舞台：中央大卡 + 周围小卡
                                FocusStageLayout(
                                    stageState = uiState.focusSceneState,
                                    selectedCategoryName = animatedCategory?.displayName ?: "",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(proportions.focusStageHeight),
                                    onSmallCardFocused = onFocusSceneItemFocused,
                                    onDismiss = onDismissFocusScene,
                                )
                            } else {
                                // 3 列菜品网格（设计稿 y=174）
                                MenuItemGrid(
                                    selectedCategoryId = uiState.selectedCategoryId,
                                    items = uiState.browseSceneState.visibleItems,
                                    viewportRequest = uiState.browseSceneState.viewportRequest,
                                    focusRequest = uiState.browseSceneState.focusRequest,
                                    trackViewportChanges = uiState.scene == MenuScene.Browse,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .fillMaxHeight(),
                                    onViewportChanged = onBrowseViewportChanged,
                                    onItemFocused = onBrowseItemFocused,
                                    onItemConfirmed = { onItemConfirmed() },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 定义 Browse mode 下会重置 idle timeout 的按键集合。
 *
 * 这里覆盖方向键、确认键与返回键，保证遥控器常用操作都能刷新活跃态。
 */
private fun Key.isBrowseInteractionKey(): Boolean {
    return this == Key.DirectionUp ||
        this == Key.DirectionDown ||
        this == Key.DirectionLeft ||
        this == Key.DirectionRight ||
        this == Key.DirectionCenter ||
        this == Key.Enter ||
        this == Key.NumPadEnter
}
