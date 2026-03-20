/**
 * @file HomeRoute.kt
 * @author PopoY
 * @date 2026-03-19
 * @description 首页 Route，负责桥接 ViewModel 与 attract mode UI。
 */
package com.poco.dishvision.feature.home

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.tv.material3.Text
import com.poco.dishvision.core.data.repository.MenuRepository
import com.poco.dishvision.core.ui.components.PocoAsyncImage
import com.poco.dishvision.core.ui.theme.ColorTokens
import com.poco.dishvision.core.ui.theme.Dimens
import com.poco.dishvision.core.ui.theme.LocalScreenProportions
import com.poco.dishvision.core.ui.theme.PocoTheme

/** Wipe 擦除过渡动画时长（ms） */
private const val WIPE_DURATION_MS = 400

/**
 * 首页入口 Route。未注入仓储时回退到 preview 状态，便于 UI 测试先行。
 *
 * @param menuRepository 菜单仓储；测试为空时使用 preview 状态。
 * @param modifier 外层 Modifier。
 */
@Composable
fun HomeRoute(
    menuRepository: MenuRepository? = null,
    onBrowseRequested: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    if (menuRepository == null) {
        PocoTheme {
            HomeScreen(
                uiState = previewHomeUiState(),
                onBrowseRequested = onBrowseRequested,
                modifier = modifier,
            )
        }
        return
    }

    val homeViewModel: HomeViewModel = viewModel(
        factory = HomeViewModel.provideFactory(menuRepository),
    )
    val uiState by homeViewModel.uiState.collectAsStateWithLifecycle()
    PocoTheme {
        HomeScreen(
            uiState = uiState,
            onBrowseRequested = onBrowseRequested,
            modifier = modifier,
        )
    }
}

/**
 * 首页 attract mode 视图，强调中下视觉重心与轻量顶部信息。
 *
 * @param uiState 首页 UI 状态。
 * @param modifier 外层 Modifier。
 */
@Composable
private fun HomeScreen(
    uiState: HomeUiState,
    onBrowseRequested: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }
    val carouselController = remember(
        uiState.showcaseItems.size,
        uiState.autoAdvanceIntervalMs,
        uiState.autoResumeAfterInteractionMs,
    ) {
        HomeCarouselController(
            itemCount = uiState.showcaseItems.size,
            autoAdvanceIntervalMs = uiState.autoAdvanceIntervalMs,
            autoResumeAfterInteractionMs = uiState.autoResumeAfterInteractionMs,
            scope = coroutineScope,
        )
    }
    val selectedIndex by carouselController.selectedIndex.collectAsState()
    val currentShowcase = uiState.showcaseItems.getOrElse(selectedIndex) {
        uiState.showcaseItems.first()
    }

    // ── Wipe 过渡状态 ──
    val moveDirection by carouselController.lastMoveDirection.collectAsState()
    // 上一张展示卡索引，过渡期间用于渲染旧画面
    var previousIndex by remember { mutableIntStateOf(selectedIndex) }
    val previousShowcase = uiState.showcaseItems.getOrElse(previousIndex) {
        uiState.showcaseItems.first()
    }
    // 擦除进度：0=完全隐藏新内容，1=完全显示新内容
    val wipeProgress = remember { Animatable(1f) }
    // 擦除方向：true=从左到右（下一张），false=从右到左（上一张）
    val isWipeForward = moveDirection == CarouselDirection.FORWARD

    LaunchedEffect(carouselController, uiState.autoAdvanceEnabled) {
        focusRequester.requestFocus()
        if (uiState.autoAdvanceEnabled) {
            carouselController.startAutoPlay()
        }
    }

    DisposableEffect(carouselController) {
        onDispose {
            carouselController.stop()
        }
    }

    // 当选中项变化时触发 Wipe 过渡动画
    LaunchedEffect(selectedIndex) {
        val targetIndex = selectedIndex
        if (targetIndex != previousIndex) {
            wipeProgress.snapTo(0f)
            wipeProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = WIPE_DURATION_MS,
                    easing = FastOutSlowInEasing,
                ),
            )
            previousIndex = targetIndex
        }
    }

    // 从 CompositionLocal 获取当前屏幕比例化尺寸
    val proportions = LocalScreenProportions.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .focusRequester(focusRequester)
            .focusable()
            .onPreviewKeyEvent { keyEvent ->
                if (keyEvent.type != KeyEventType.KeyDown) {
                    return@onPreviewKeyEvent false
                }

                when {
                    keyEvent.key.isCarouselPreviousKey() -> {
                        carouselController.onManualInteraction()
                        carouselController.onMovePrevious()
                        true
                    }

                    keyEvent.key.isCarouselNextKey() -> {
                        carouselController.onManualInteraction()
                        carouselController.onMoveNext()
                        true
                    }

                    keyEvent.key.isBrowseTriggerKey() -> {
                        onBrowseRequested()
                        true
                    }

                    else -> false
                }
            }
            .background(
                brush = Brush.verticalGradient(
                    colors = ColorTokens.HomeBackgroundGradient,
                ),
            )
            .testTag("home-screen"),
    ) {
        // ── Hero 主图区（Wipe 过渡） ──
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(
                    top = proportions.homeHeroTopPadding,
                    end = proportions.screenHorizontalPadding,
                ),
        ) {
            // 防止选中项已变化但 snapTo(0f) 尚未执行的中间帧闪烁
            val wp = if (selectedIndex != previousIndex && wipeProgress.value >= 1f) {
                0f
            } else {
                wipeProgress.value
            }
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
        }

        HomeBrandHeader(
            brandName = uiState.brandName,
            brandSubtitle = uiState.brandSubtitle,
            seasonBadgeText = uiState.seasonBadgeText,
        )

        // ── 文案区（Wipe 过渡） ──
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(
                    start = proportions.screenHorizontalPadding,
                    top = proportions.homeCopyTopPadding,
                ),
        ) {
            // 防止选中项已变化但 snapTo(0f) 尚未执行的中间帧闪烁
            val wp = if (selectedIndex != previousIndex && wipeProgress.value >= 1f) {
                0f
            } else {
                wipeProgress.value
            }
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
        }

        AttractCarousel(
            showcaseItems = uiState.showcaseItems,
            selectedIndex = selectedIndex,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(
                    start = proportions.screenHorizontalPadding,
                    end = proportions.screenHorizontalPadding,
                    bottom = proportions.homeRecommendationBottomPadding,
                )
                .testTag("home-lower-hero-zone"),
        )
    }
}

/**
 * 品牌头部与季节标签。
 */
@Composable
private fun BoxScope.HomeBrandHeader(
    brandName: String,
    brandSubtitle: String,
    seasonBadgeText: String,
) {
    val proportions = LocalScreenProportions.current

    Text(
        text = brandName,
        modifier = Modifier
            .padding(
                start = proportions.screenHorizontalPadding,
                top = proportions.screenTopPadding,
            ),
        color = ColorTokens.TextPrimary,
        fontWeight = FontWeight.SemiBold,
        style = TextStyle(fontSize = proportions.scaledSp(24f)),
    )

    Text(
        text = brandSubtitle,
        modifier = Modifier
            .padding(
                start = proportions.screenHorizontalPadding,
                top = proportions.screenTopPadding + proportions.brandSubtitleOffset,
            ),
        color = ColorTokens.TextMuted,
        style = TextStyle(fontSize = proportions.scaledSp(18f)),
    )

    Box(
        modifier = Modifier
            .padding(
                top = proportions.screenTopPadding,
                end = proportions.homeSeasonBadgeEndPadding,
            )
            .align(Alignment.TopEnd)
            .clip(RoundedCornerShape(Dimens.SurfaceChipCorner))
            .background(ColorTokens.SurfaceDeep)
            .border(
                width = 1.dp,
                color = ColorTokens.GoldSoft,
                shape = RoundedCornerShape(Dimens.SurfaceChipCorner),
            )
            .padding(
                horizontal = Dimens.HomeSeasonBadgeHorizontalPadding,
                vertical = Dimens.HomeSeasonBadgeVerticalPadding,
            ),
    ) {
        Text(
            text = seasonBadgeText,
            color = ColorTokens.Accent,
            style = TextStyle(fontSize = proportions.scaledSp(18f)),
        )
    }
}

/**
 * 左侧文案区，与当前轮播条目同步切换。
 * 使用 Box + offset 绝对定位，精确对齐设计稿坐标（设计稿 layout="none"）。
 *
 * @param uiState 首页状态。
 * @param showcaseItem 当前选中的展示条目。
 * @param modifier 外层 Modifier。
 */
@Composable
private fun HomeCopySection(
    uiState: HomeUiState,
    showcaseItem: HomeShowcaseItem,
    modifier: Modifier = Modifier,
) {
    val proportions = LocalScreenProportions.current

    Box(
        modifier = modifier
            .width(proportions.homeCopyWidth)
            .height(proportions.homeCopySectionHeight),
    ) {
        // eyebrow 标签（偏移 0，与 section 顶部对齐）
        Text(
            text = showcaseItem.heroEyebrow,
            color = ColorTokens.Accent,
            style = TextStyle(fontSize = proportions.scaledSp(22f)),
        )
        // 主标题第一行
        Text(
            text = showcaseItem.heroTitlePrimary,
            modifier = Modifier
                .offset(y = proportions.homeCopyTitle1OffsetY)
                .testTag("home-hero-primary-title"),
            color = ColorTokens.TextPrimary,
            fontWeight = FontWeight.Bold,
            lineHeight = proportions.scaledSp(82f),
            style = TextStyle(fontSize = proportions.scaledSp(78f)),
        )
        // 主标题第二行
        Text(
            text = showcaseItem.heroTitleSecondary,
            modifier = Modifier
                .offset(y = proportions.homeCopyTitle2OffsetY)
                .testTag("home-hero-secondary-title"),
            color = ColorTokens.TextPrimary,
            fontWeight = FontWeight.Bold,
            lineHeight = proportions.scaledSp(82f),
            style = TextStyle(fontSize = proportions.scaledSp(78f)),
        )
        // 描述文案
        Text(
            text = showcaseItem.heroDescription,
            modifier = Modifier.offset(y = proportions.homeCopyDescOffsetY),
            color = ColorTokens.TextSecondary,
            lineHeight = proportions.scaledSp(32f),
            style = TextStyle(fontSize = proportions.scaledSp(24f)),
        )
        // 分类标签胶囊行
        Row(
            modifier = Modifier.offset(y = proportions.homeCopyChipsOffsetY),
            horizontalArrangement = Arrangement.spacedBy(Dimens.HomeChipSpacing),
        ) {
            uiState.categoryChips.forEach { chipText ->
                HomeChip(text = chipText)
            }
        }
    }
}

/**
 * 首页标签胶囊。
 *
 * @param text 标签文案。
 */
@Composable
private fun HomeChip(text: String) {
    val proportions = LocalScreenProportions.current

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(Dimens.SurfaceChipCorner))
            .background(ColorTokens.SurfaceCard)
            .border(
                width = 1.dp,
                color = ColorTokens.GoldSoft,
                shape = RoundedCornerShape(Dimens.SurfaceChipCorner),
            )
            .padding(horizontal = 16.dp, vertical = 10.dp),
    ) {
        Text(
            text = text,
            color = ColorTokens.TextPrimary,
            style = TextStyle(fontSize = proportions.scaledSp(18f)),
        )
    }
}

/**
 * 右侧主图容器。
 *
 * @param imageRes 本地 drawable 资源。
 * @param contentDescription 无障碍描述。
 * @param modifier 外层 Modifier。
 */
@Composable
private fun HeroImageCard(
    imageRes: Int,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    val proportions = LocalScreenProportions.current

    Box(
        modifier = modifier
            .width(proportions.homeHeroWidth)
            .height(proportions.homeHeroHeight)
            .clip(RoundedCornerShape(Dimens.SurfaceLargeCorner))
            .background(ColorTokens.SurfaceCard)
            .border(
                width = 1.dp,
                color = ColorTokens.GoldSoft,
                shape = RoundedCornerShape(Dimens.SurfaceLargeCorner),
            )
            .testTag("home-hero-image"),
    ) {
        PocoAsyncImage(
            model = imageRes,
            contentDescription = contentDescription,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        // 叠加层：设计稿 rotation=90 → 从上透明到下深色的垂直渐变
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = ColorTokens.HomeHeroOverlayGradient,
                    ),
                ),
        )
    }
}

/**
 * 首屏内部消费的浏览触发键。
 */
private fun Key.isBrowseTriggerKey(): Boolean {
    return this == Key.DirectionUp ||
        this == Key.DirectionDown ||
        this == Key.DirectionCenter
}

/**
 * 左移切换轮播。
 */
private fun Key.isCarouselPreviousKey(): Boolean {
    return this == Key.DirectionLeft
}

/**
 * 右移切换轮播。
 */
private fun Key.isCarouselNextKey(): Boolean {
    return this == Key.DirectionRight
}

/**
 * 水平擦除裁剪形状，用于 Wipe 过渡。
 *
 * 根据 [progress] 逐步展开可见区域，配合 graphicsLayer.clip 使用。
 *
 * @param progress 擦除进度 [0f, 1f]，0=完全隐藏，1=完全显示。
 * @param fromStart true 时从 start 侧展开，false 时从 end 侧展开。
 */
private class WipeShape(
    private val progress: Float,
    private val fromStart: Boolean,
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val revealWidth = size.width * progress.coerceIn(0f, 1f)
        // 根据布局方向将 start/end 映射为 left/right
        val fromLeft = if (layoutDirection == LayoutDirection.Ltr) fromStart else !fromStart
        val left = if (fromLeft) 0f else size.width - revealWidth
        // 仅做水平裁剪；垂直方向扩展余量，避免截断溢出内容（如 chip 行）
        val verticalOverflow = size.height
        return Outline.Rectangle(
            Rect(left, -verticalOverflow, left + revealWidth, size.height + verticalOverflow),
        )
    }
}
