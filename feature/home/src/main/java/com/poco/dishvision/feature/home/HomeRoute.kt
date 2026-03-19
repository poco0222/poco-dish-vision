/**
 * @file HomeRoute.kt
 * @author PopoY
 * @date 2026-03-19
 * @description 首页 Route，负责桥接 ViewModel 与 attract mode UI。
 */
package com.poco.dishvision.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.tv.material3.Text
import com.poco.dishvision.core.data.repository.MenuRepository
import com.poco.dishvision.core.ui.components.PocoAsyncImage
import com.poco.dishvision.core.ui.theme.ColorTokens
import com.poco.dishvision.core.ui.theme.Dimens
import com.poco.dishvision.core.ui.theme.PocoTheme

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
        HeroImageCard(
            imageRes = currentShowcase.heroImageRes,
            contentDescription = currentShowcase.cardTitle,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(
                    top = Dimens.HomeHeroTopPadding,
                    end = Dimens.ScreenHorizontalPadding,
                ),
        )

        HomeBrandHeader(
            brandName = uiState.brandName,
            brandSubtitle = uiState.brandSubtitle,
            seasonBadgeText = uiState.seasonBadgeText,
        )

        HomeCopySection(
            uiState = uiState,
            showcaseItem = currentShowcase,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(
                    start = Dimens.ScreenHorizontalPadding,
                    top = Dimens.HomeCopyTopPadding,
                ),
        )

        AttractCarousel(
            showcaseItems = uiState.showcaseItems,
            selectedIndex = selectedIndex,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(
                    horizontal = Dimens.ScreenHorizontalPadding,
                    vertical = Dimens.ScreenBottomPadding,
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
    Text(
        text = brandName,
        modifier = Modifier
            .padding(start = Dimens.ScreenHorizontalPadding, top = Dimens.ScreenTopPadding),
        color = ColorTokens.TextPrimary,
        fontWeight = FontWeight.SemiBold,
        style = TextStyle(fontSize = 24.sp),
    )

    Text(
        text = brandSubtitle,
        modifier = Modifier
            .padding(
                start = Dimens.ScreenHorizontalPadding,
                top = Dimens.ScreenTopPadding + 36.dp,
            ),
        color = ColorTokens.TextMuted,
        style = TextStyle(fontSize = 18.sp),
    )

    Box(
        modifier = Modifier
            .padding(
                top = Dimens.ScreenTopPadding,
                end = Dimens.ScreenHorizontalPadding,
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
            style = TextStyle(fontSize = 18.sp),
        )
    }
}

/**
 * 左侧文案区，与当前轮播条目同步切换。
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
    Column(
        modifier = modifier
            .width(Dimens.HomeCopyWidth),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Text(
            text = showcaseItem.heroEyebrow,
            color = ColorTokens.Accent,
            style = TextStyle(fontSize = 22.sp),
        )
        Text(
            text = showcaseItem.heroTitlePrimary,
            modifier = Modifier.testTag("home-hero-primary-title"),
            color = ColorTokens.TextPrimary,
            fontWeight = FontWeight.Bold,
            lineHeight = 82.sp,
            style = TextStyle(fontSize = 78.sp),
        )
        Text(
            text = showcaseItem.heroTitleSecondary,
            modifier = Modifier.testTag("home-hero-secondary-title"),
            color = ColorTokens.TextPrimary,
            fontWeight = FontWeight.Bold,
            lineHeight = 82.sp,
            style = TextStyle(fontSize = 78.sp),
        )
        Text(
            text = showcaseItem.heroDescription,
            color = ColorTokens.TextSecondary,
            lineHeight = 32.sp,
            style = TextStyle(fontSize = 24.sp),
        )
        Row(
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
            style = TextStyle(fontSize = 18.sp),
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
    Box(
        modifier = modifier
            .width(Dimens.HomeHeroWidth)
            .height(Dimens.HomeHeroHeight)
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
