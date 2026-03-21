/**
 * @file MenuItemGrid.kt
 * @author PopoY
 * @date 2026-03-20
 * @description 提供浏览页菜品 3 列网格区域，匹配收窄左栏后的 Browse 布局。
 */
package com.poco.dishvision.feature.menu

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.tv.material3.Text
import com.poco.dishvision.core.model.menu.MenuItem
import com.poco.dishvision.core.ui.components.GlassSurface
import com.poco.dishvision.core.ui.components.PocoAsyncImage
import com.poco.dishvision.core.ui.theme.ColorTokens
import com.poco.dishvision.core.ui.theme.Dimens
import com.poco.dishvision.core.ui.theme.LocalScreenProportions
import com.poco.dishvision.core.ui.theme.ScreenProportions
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.math.abs

/** 网格列数：当前版本为每行 3 张卡片。 */
private const val GRID_COLUMNS = 3

/** Browse 首屏目标行数，保持 3x3 九卡信息密度。 */
private const val GRID_VISIBLE_ROWS = 3

/** 动效令牌：聚焦卡默认缩放倍率。 */
private const val MOTION_TOKEN_FOCUSED_SCALE = 1.24f

/** 动效令牌：近邻卡缩放倍率（manhattan 距离 = 1）。 */
private const val MOTION_TOKEN_NEAR_SCALE = 0.82f

/** 动效令牌：次邻卡缩放倍率（manhattan 距离 = 2）。 */
private const val MOTION_TOKEN_MID_SCALE = 0.88f

/** 动效令牌：远邻卡缩放倍率（manhattan 距离 >= 3）。 */
private const val MOTION_TOKEN_FAR_SCALE = 0.94f

/** 动效令牌：水平推挤比率，按卡片宽度计算。 */
private const val MOTION_TOKEN_PUSH_X_RATIO = 0.24f

/** 动效令牌：垂直推挤比率，按卡片高度计算。 */
private const val MOTION_TOKEN_PUSH_Y_RATIO = 0.18f

/** 动效令牌：近邻卡透明度。 */
private const val MOTION_TOKEN_NEAR_ALPHA = 0.72f

/** 动效令牌：次邻卡透明度。 */
private const val MOTION_TOKEN_MID_ALPHA = 0.80f

/** 动效令牌：远邻卡透明度。 */
private const val MOTION_TOKEN_FAR_ALPHA = 0.88f

/** 聚焦缩放约束下限。 */
private const val MOTION_CONSTRAINT_FOCUSED_SCALE_MIN = 1.12f

/** 聚焦缩放约束上限。 */
private const val MOTION_CONSTRAINT_FOCUSED_SCALE_MAX = 1.28f

/** 水平推挤比率约束上限。 */
private const val MOTION_CONSTRAINT_PUSH_X_MAX = 0.26f

/** 垂直推挤比率约束上限。 */
private const val MOTION_CONSTRAINT_PUSH_Y_MAX = 0.20f

/** 非聚焦透明度约束下限。 */
private const val MOTION_CONSTRAINT_ALPHA_MIN = 0.70f

/** 聚焦详情区最多展示 3 个 chip，避免信息过载。 */
private const val MAX_FOCUS_CHIPS = 3

/** 本地食物图片 drawable 资源列表，作为远程图片的 fallback */
internal val LOCAL_FOOD_DRAWABLES = listOf(
    R.drawable.menu_food_1,
    R.drawable.menu_food_2,
    R.drawable.menu_food_3,
    R.drawable.menu_food_4,
    R.drawable.menu_food_5,
)

/**
 * Browse 网格卡片尺寸计算结果。
 *
 * @param cardWidth 卡片最终宽度。
 * @param cardHeight 卡片最终高度。
 * @param cardHorizontalInset 网格单元格内用于收窄卡片的水平内缩。
 * @param imageHeight 卡片图片区域最终高度。
 * @author PopoY
 */
internal data class BrowseGridCardMetrics(
    val cardWidth: androidx.compose.ui.unit.Dp,
    val cardHeight: androidx.compose.ui.unit.Dp,
    val cardHorizontalInset: androidx.compose.ui.unit.Dp,
    val imageHeight: androidx.compose.ui.unit.Dp,
)

/**
 * 菜品卡在“聚焦放大 + 局部推挤”下的动画参数。
 *
 * @param scale 卡片缩放倍率。
 * @param alpha 卡片透明度。
 * @param offsetXRatio 水平位移比例（乘以父级给定 push 距离）。
 * @param offsetYRatio 垂直位移比例（乘以父级给定 push 距离）。
 * @param pivotX 水平缩放锚点（0=左, 0.5=中, 1=右）。
 * @param pivotY 垂直缩放锚点（0=上, 0.5=中, 1=下）。
 * @param zIndex 卡片层级；仅 3x3 可视窗内参与强层级重排。
 * @param showExpandedDetails 是否展示详情扩展区。
 * @author PopoY
 */
internal data class BrowseCardFocusMotion(
    val scale: Float = 1f,
    val alpha: Float = 1f,
    val offsetXRatio: Float = 0f,
    val offsetYRatio: Float = 0f,
    val pivotX: Float = 0.5f,
    val pivotY: Float = 0.5f,
    val zIndex: Float = 0f,
    val showExpandedDetails: Boolean = false,
)

/**
 * Browse 网格动效令牌（motion tokens）。
 *
 * 聚焦缩放、邻卡缩放/透明度、位移比率全部集中在此定义，统一被边界约束裁剪。
 *
 * @author PopoY
 */
internal data class BrowseGridMotionTokens(
    val focusedScale: Float = MOTION_TOKEN_FOCUSED_SCALE,
    val unfocusedNearScale: Float = MOTION_TOKEN_NEAR_SCALE,
    val unfocusedMidScale: Float = MOTION_TOKEN_MID_SCALE,
    val unfocusedFarScale: Float = MOTION_TOKEN_FAR_SCALE,
    val pushXRatio: Float = MOTION_TOKEN_PUSH_X_RATIO,
    val pushYRatio: Float = MOTION_TOKEN_PUSH_Y_RATIO,
    val unfocusedNearAlpha: Float = MOTION_TOKEN_NEAR_ALPHA,
    val unfocusedMidAlpha: Float = MOTION_TOKEN_MID_ALPHA,
    val unfocusedFarAlpha: Float = MOTION_TOKEN_FAR_ALPHA,
)

/**
 * Browse 网格纵向滚动方向。
 *
 * @author PopoY
 */
internal enum class BrowseRowScrollDirection {
    DirectionUp,
    DirectionDown,
}

/**
 * Browse 网格逐行滚动动作类型。
 *
 * @author PopoY
 */
internal enum class BrowseRowScrollAction {
    NoScroll,
    ScrollUpOneRow,
    ScrollDownOneRow,
}

/**
 * Browse 网格逐行滚动意图解析结果。
 *
 * @param action 逐行滚动动作。
 * @param targetVisibleRowStart 目标可视窗首项索引（行对齐）。
 * @param targetFocusedItemIndex 目标焦点索引（保持同列）。
 * @author PopoY
 */
internal data class BrowseRowScrollIntentResult(
    val action: BrowseRowScrollAction,
    val targetVisibleRowStart: Int,
    val targetFocusedItemIndex: Int,
)

/**
 * 解析 Browse 网格卡片的最终宽高与内缩参数。
 *
 * 设计目标：
 * 1. 首屏保持 3x3 信息密度；
 * 2. 卡片高度轻微收缩，释放底部留白；
 * 3. 卡片宽高按同一比例缩放，维持视觉比例稳定。
 *
 * @param maxWidth 网格可用宽度。
 * @param maxHeight 网格可用高度。
 * @param proportions 屏幕比例与布局令牌。
 * @return 计算后的 [BrowseGridCardMetrics]。
 * @author PopoY
 */
internal fun resolveBrowseGridCardMetrics(
    maxWidth: androidx.compose.ui.unit.Dp,
    maxHeight: androidx.compose.ui.unit.Dp,
    proportions: ScreenProportions,
): BrowseGridCardMetrics {
    // 网格单元格宽度（3 列 + 固定横向间距），用于同步收缩卡片视觉宽度。
    val gridCellWidth = maxOf(
        0.dp,
        (
            maxWidth -
                proportions.browseGridHorizontalGap * (GRID_COLUMNS - 1)
            ) / GRID_COLUMNS,
    )
    val cardWidth = maxOf(0.dp, gridCellWidth * proportions.browseGridCardScale)
    // 使用统一缩放系数同步收缩宽高，避免“只改高度导致卡片被拉扁”。
    val resolvedCardHorizontalInset = maxOf(
        0.dp,
        (gridCellWidth - cardWidth) / 2,
    )

    // 按当前剩余可用视口反推单张卡片高度，避免不同 TV density 下首屏丢失第 9 张卡。
    val designedCardHeight = (
        proportions.browseGridViewportHeight -
            proportions.browseGridVerticalGap * (GRID_VISIBLE_ROWS - 1)
        ) / GRID_VISIBLE_ROWS
    val availableCardHeight = (
        maxHeight -
            proportions.browseGridVerticalGap * (GRID_VISIBLE_ROWS - 1)
        ) / GRID_VISIBLE_ROWS
    val resolvedBaseCardHeight = maxOf(
        0.dp,
        minOf(designedCardHeight, availableCardHeight),
    )
    // 与宽度使用同一比例收缩卡片高度，释放网格底部留白。
    val resolvedCardHeight = resolvedBaseCardHeight * proportions.browseGridCardScale

    // 先保留正文区最小高度，再用剩余空间计算图片高度。
    val resolvedBodyMinHeight = minOf(
        proportions.browseGridCardBodyMinHeight,
        resolvedCardHeight,
    )
    val resolvedImageHeight = maxOf(
        0.dp,
        minOf(
            proportions.browseCardImageHeight,
            resolvedCardHeight -
                resolvedBodyMinHeight -
                proportions.browseGridCardContentSpacing,
        ),
    )

    return BrowseGridCardMetrics(
        cardWidth = cardWidth,
        cardHeight = resolvedCardHeight,
        cardHorizontalInset = resolvedCardHorizontalInset,
        imageHeight = resolvedImageHeight,
    )
}

/**
 * 解析 Browse 网格聚焦卡目标放大倍率（来自动效令牌）。
 *
 * @param motionTokens 当前动效令牌。
 * @return 经过边界约束后的聚焦放大倍率。
 * @author PopoY
 */
internal fun resolveBrowseFocusedCardScale(
    motionTokens: BrowseGridMotionTokens = BrowseGridMotionTokens(),
): Float {
    return motionTokens.withMotionConstraints().focusedScale
}

/**
 * 解析 Browse 网格在聚焦态下的邻卡推挤位移基准。
 *
 * 水平与垂直位移基于卡片当前尺寸按比例计算，用于将聚焦卡周边邻卡推离，
 * 以降低放大态下的视觉重叠。
 *
 * @param cardWidth 当前网格卡片宽度。
 * @param cardHeight 当前网格卡片高度。
 * @param motionTokens 当前动效令牌。
 * @return 位移基准，first=水平位移，second=垂直位移。
 * @author PopoY
 */
internal fun resolveBrowseFocusPushOffsets(
    cardWidth: androidx.compose.ui.unit.Dp,
    cardHeight: androidx.compose.ui.unit.Dp,
    motionTokens: BrowseGridMotionTokens = BrowseGridMotionTokens(),
): Pair<androidx.compose.ui.unit.Dp, androidx.compose.ui.unit.Dp> {
    val resolvedTokens = motionTokens.withMotionConstraints()
    val pushOffsetX = cardWidth * resolvedTokens.pushXRatio
    val pushOffsetY = cardHeight * resolvedTokens.pushYRatio
    return pushOffsetX to pushOffsetY
}

/**
 * 根据焦点与网格位置，解析单卡动画参数（缩放/透明度/位移/锚点）。
 *
 * 规则：
 * 1. 聚焦卡放大并开启详情区；
 * 2. 其他卡按与焦点的 manhattan 距离分级缩小；
 * 3. 邻近卡按“远离焦点”的方向做局部推挤；
 * 4. 左/右/上/下边界通过 pivot 锚点约束扩张方向，避免越界感。
 *
 * @param itemIndex 当前卡片索引。
 * @param focusedItemIndex 当前聚焦索引；为空时返回静态参数。
 * @param visibleRowStart 当前可视窗起始索引（按行对齐语义）。
 * @param columns 网格列数。
 * @param visibleRows 首屏可见行数。
 * @param focusedCardScale 聚焦卡目标缩放倍率。
 * @param motionTokens 当前动效令牌。
 * @return 当前卡片的 [BrowseCardFocusMotion]。
 * @author PopoY
 */
internal fun resolveBrowseCardFocusMotion(
    itemIndex: Int,
    focusedItemIndex: Int?,
    visibleRowStart: Int,
    columns: Int,
    visibleRows: Int,
    focusedCardScale: Float = MOTION_TOKEN_FOCUSED_SCALE,
    motionTokens: BrowseGridMotionTokens = BrowseGridMotionTokens(),
): BrowseCardFocusMotion {
    if (columns <= 0 || visibleRows <= 0) {
        return BrowseCardFocusMotion()
    }

    val resolvedTokens = motionTokens.withMotionConstraints()
    // 先做行对齐，避免 firstVisible 非整行时产生 pivot/位移错位。
    val alignedVisibleRowStart = resolveAlignedVisibleRowStart(
        visibleRowStart = visibleRowStart,
        columns = columns,
    )
    val visibleWindowEnd = alignedVisibleRowStart + columns * visibleRows - 1
    val isInsideVisibleWindow = itemIndex in alignedVisibleRowStart..visibleWindowEnd
    val columnIndex = ((itemIndex % columns) + columns) % columns
    val rowSlot = ((itemIndex - alignedVisibleRowStart) / columns).coerceIn(0, visibleRows - 1)
    val pivotX = resolveBrowseCardPivotAxis(slotIndex = columnIndex, maxSlotIndex = columns - 1)
    val pivotY = resolveBrowseCardPivotAxis(slotIndex = rowSlot, maxSlotIndex = visibleRows - 1)

    val focusedIndex = focusedItemIndex ?: return BrowseCardFocusMotion(
        pivotX = pivotX,
        pivotY = pivotY,
    )
    // 可视窗外只做轻量衰减，不参与强重排（位移/zIndex/详情均关闭）。
    if (!isInsideVisibleWindow) {
        return BrowseCardFocusMotion(
            scale = resolvedTokens.unfocusedFarScale,
            alpha = resolvedTokens.unfocusedFarAlpha,
            pivotX = pivotX,
            pivotY = pivotY,
            zIndex = 0f,
            showExpandedDetails = false,
        )
    }
    if (itemIndex == focusedIndex) {
        return BrowseCardFocusMotion(
            scale = focusedCardScale.coerceIn(
                minimumValue = MOTION_CONSTRAINT_FOCUSED_SCALE_MIN,
                maximumValue = MOTION_CONSTRAINT_FOCUSED_SCALE_MAX,
            ),
            alpha = 1f,
            pivotX = pivotX,
            pivotY = pivotY,
            zIndex = 8f,
            showExpandedDetails = true,
        )
    }

    val focusedRow = focusedIndex / columns
    val focusedColumn = ((focusedIndex % columns) + columns) % columns
    val row = itemIndex / columns
    val rowDistance = row - focusedRow
    val columnDistance = columnIndex - focusedColumn
    val manhattanDistance = abs(rowDistance) + abs(columnDistance)

    val shrinkScale = when {
        manhattanDistance <= 1 -> resolvedTokens.unfocusedNearScale
        manhattanDistance == 2 -> resolvedTokens.unfocusedMidScale
        else -> resolvedTokens.unfocusedFarScale
    }
    val alpha = when {
        manhattanDistance <= 1 -> resolvedTokens.unfocusedNearAlpha
        manhattanDistance == 2 -> resolvedTokens.unfocusedMidAlpha
        else -> resolvedTokens.unfocusedFarAlpha
    }
    // 3x3 可视窗内：近邻最强、次邻次之、远邻最弱；窗外已在上方短路为 0 位移。
    val pushStrength = when {
        manhattanDistance <= 1 -> 1f
        manhattanDistance == 2 -> 0.72f
        else -> 0.42f
    }
    val rawOffsetXRatio = when {
        columnDistance > 0 -> pushStrength
        columnDistance < 0 -> -pushStrength
        else -> 0f
    }
    val rawOffsetYRatio = when {
        rowDistance > 0 -> pushStrength
        rowDistance < 0 -> -pushStrength
        else -> 0f
    }
    val offsetXRatio = clampBrowseCardPushRatioByBoundary(
        rawPushRatio = rawOffsetXRatio,
        slotIndex = columnIndex,
        maxSlotIndex = columns - 1,
    )
    val offsetYRatio = clampBrowseCardPushRatioByBoundary(
        rawPushRatio = rawOffsetYRatio,
        slotIndex = rowSlot,
        maxSlotIndex = visibleRows - 1,
    )

    return BrowseCardFocusMotion(
        scale = shrinkScale,
        alpha = alpha,
        offsetXRatio = offsetXRatio,
        offsetYRatio = offsetYRatio,
        pivotX = pivotX,
        pivotY = pivotY,
        zIndex = when {
            manhattanDistance <= 1 -> 2f
            manhattanDistance == 2 -> 1f
            else -> 0.5f
        },
        showExpandedDetails = false,
    )
}

/**
 * 解析菜品卡重新进入可视窗时的 enter pose（进入姿态）。
 *
 * 若存在最近一次姿态缓存，则优先从缓存姿态过渡；否则退回到轻量衰减 pose，
 * 避免新卡以目标状态“硬切”进入 3x3 可视窗。
 *
 * @param targetMotion 当前卡片的目标姿态。
 * @param cachedPose 最近一次缓存的姿态快照。
 * @return 用于首次入场动画的起始姿态。
 * @author PopoY
 */
internal fun resolveBrowseCardEnterMotion(
    targetMotion: BrowseCardFocusMotion,
    cachedPose: BrowseCardFocusMotion?,
): BrowseCardFocusMotion {
    return cachedPose?.let(::resolveBrowseCardPoseCacheSnapshot) ?: BrowseCardFocusMotion(
        scale = MOTION_TOKEN_FAR_SCALE,
        alpha = MOTION_TOKEN_FAR_ALPHA,
        pivotX = targetMotion.pivotX,
        pivotY = targetMotion.pivotY,
        zIndex = 0f,
        showExpandedDetails = false,
    )
}

/**
 * 将当前目标姿态裁剪为可安全缓存的 pose snapshot（姿态快照）。
 *
 * 详情展开态不进入缓存，防止离屏卡片下次回到视口时直接带着展开区抢焦点。
 *
 * @param targetMotion 当前目标姿态。
 * @return 适合写入 pose cache 的姿态快照。
 * @author PopoY
 */
internal fun resolveBrowseCardPoseCacheSnapshot(
    targetMotion: BrowseCardFocusMotion,
): BrowseCardFocusMotion {
    return targetMotion.copy(
        showExpandedDetails = false,
    )
}

/**
 * 解析 Browse 网格按行滚动意图。
 *
 * 仅允许逐行滚动（每次索引 `±columns`），不触发 page scroll；滚动后焦点保持同列。
 *
 * @param direction 当前纵向方向键意图。
 * @param focusedItemIndex 当前焦点索引。
 * @param visibleRowStart 当前可视窗起始索引（可非行对齐）。
 * @param itemCount 当前网格总项数。
 * @param columns 网格列数。
 * @param visibleRows 可视行数。
 * @return 逐行滚动意图结果。
 * @author PopoY
 */
internal fun resolveBrowseRowScrollIntent(
    direction: BrowseRowScrollDirection,
    focusedItemIndex: Int,
    visibleRowStart: Int,
    itemCount: Int,
    columns: Int,
    visibleRows: Int,
): BrowseRowScrollIntentResult {
    if (itemCount <= 0 || columns <= 0 || visibleRows <= 0) {
        return BrowseRowScrollIntentResult(
            action = BrowseRowScrollAction.NoScroll,
            targetVisibleRowStart = 0,
            targetFocusedItemIndex = focusedItemIndex.coerceAtLeast(0),
        )
    }
    val clampedFocusIndex = focusedItemIndex.coerceIn(0, itemCount - 1)
    val alignedVisibleRowStart = resolveAlignedVisibleRowStart(
        visibleRowStart = visibleRowStart,
        columns = columns,
    )
    val visibleStartRow = alignedVisibleRowStart / columns
    val focusedRow = clampedFocusIndex / columns
    val maxVisibleRowStart = resolveMaxVisibleRowStart(
        itemCount = itemCount,
        columns = columns,
        visibleRows = visibleRows,
    )

    return when (direction) {
        BrowseRowScrollDirection.DirectionUp -> {
            val hasPreviousRow = clampedFocusIndex - columns >= 0
            val focusOnTopVisibleRow = focusedRow == visibleStartRow
            if (hasPreviousRow && focusOnTopVisibleRow) {
                BrowseRowScrollIntentResult(
                    action = BrowseRowScrollAction.ScrollUpOneRow,
                    targetVisibleRowStart = maxOf(0, alignedVisibleRowStart - columns),
                    targetFocusedItemIndex = clampedFocusIndex - columns,
                )
            } else {
                BrowseRowScrollIntentResult(
                    action = BrowseRowScrollAction.NoScroll,
                    targetVisibleRowStart = alignedVisibleRowStart,
                    targetFocusedItemIndex = clampedFocusIndex,
                )
            }
        }

        BrowseRowScrollDirection.DirectionDown -> {
            val hasNextRow = clampedFocusIndex + columns < itemCount
            val focusOnBottomVisibleRow = focusedRow == visibleStartRow + visibleRows - 1
            if (hasNextRow && focusOnBottomVisibleRow) {
                BrowseRowScrollIntentResult(
                    action = BrowseRowScrollAction.ScrollDownOneRow,
                    targetVisibleRowStart = minOf(
                        alignedVisibleRowStart + columns,
                        maxVisibleRowStart,
                    ),
                    targetFocusedItemIndex = clampedFocusIndex + columns,
                )
            } else {
                BrowseRowScrollIntentResult(
                    action = BrowseRowScrollAction.NoScroll,
                    targetVisibleRowStart = alignedVisibleRowStart,
                    targetFocusedItemIndex = clampedFocusIndex,
                )
            }
        }
    }
}

/**
 * 解析聚焦详情区要展示的 chip 文案。
 *
 * 优先展示 displayBadge，再补齐 tags，最终去重截断，避免焦点区过载。
 *
 * @param item 当前菜品。
 * @return 最多 3 个可展示 chip 文案。
 * @author PopoY
 */
internal fun resolveBrowseFocusChipLabels(item: MenuItem): List<String> {
    val badgeLabels = item.displayBadges
        .map { badge -> badge.label.trim() }
        .filter { label -> label.isNotEmpty() }
    val tagLabels = item.tags
        .map { tag -> tag.trim() }
        .filter { tag -> tag.isNotEmpty() }

    return (badgeLabels + tagLabels)
        .distinct()
        .take(MAX_FOCUS_CHIPS)
}

/**
 * 统一对动效令牌做边界约束，防止极端参数破坏布局稳定性。
 *
 * @return 约束后的动效令牌。
 * @author PopoY
 */
private fun BrowseGridMotionTokens.withMotionConstraints(): BrowseGridMotionTokens {
    return copy(
        focusedScale = focusedScale.coerceIn(
            minimumValue = MOTION_CONSTRAINT_FOCUSED_SCALE_MIN,
            maximumValue = MOTION_CONSTRAINT_FOCUSED_SCALE_MAX,
        ),
        pushXRatio = pushXRatio.coerceIn(
            minimumValue = 0f,
            maximumValue = MOTION_CONSTRAINT_PUSH_X_MAX,
        ),
        pushYRatio = pushYRatio.coerceIn(
            minimumValue = 0f,
            maximumValue = MOTION_CONSTRAINT_PUSH_Y_MAX,
        ),
        unfocusedNearAlpha = unfocusedNearAlpha.coerceAtLeast(MOTION_CONSTRAINT_ALPHA_MIN),
        unfocusedMidAlpha = unfocusedMidAlpha.coerceAtLeast(MOTION_CONSTRAINT_ALPHA_MIN),
        unfocusedFarAlpha = unfocusedFarAlpha.coerceAtLeast(MOTION_CONSTRAINT_ALPHA_MIN),
    )
}

/**
 * 解析行对齐后的 visibleRowStart。
 *
 * @param visibleRowStart 当前首可见索引（允许非对齐输入）。
 * @param columns 网格列数。
 * @return 行对齐后的首可见索引。
 * @author PopoY
 */
private fun resolveAlignedVisibleRowStart(
    visibleRowStart: Int,
    columns: Int,
): Int {
    if (columns <= 0 || visibleRowStart <= 0) {
        return 0
    }
    return (visibleRowStart / columns) * columns
}

/**
 * 根据当前可见 item 索引集合推导运行时实际可见行数。
 *
 * 某些屏幕或布局参数下，网格底部可能露出部分下一行；逐行滚动必须基于
 * 真实可见范围而不是固定常量，才能在“部分第 4 行可见”时正确判断底行。
 *
 * @param visibleItemIndices 当前 `LazyVerticalGrid` 可见 item 索引集合。
 * @param columns 网格列数。
 * @param fallbackVisibleRows 当可见集合为空时使用的保底可见行数。
 * @return 运行时实际可见行数，最少为 1。
 * @author PopoY
 */
internal fun resolveBrowseVisibleRowCount(
    visibleItemIndices: List<Int>,
    columns: Int,
    fallbackVisibleRows: Int,
): Int {
    if (columns <= 0) {
        return maxOf(1, fallbackVisibleRows)
    }
    val minVisibleIndex = visibleItemIndices.minOrNull()
    val maxVisibleIndex = visibleItemIndices.maxOrNull()
    if (minVisibleIndex == null || maxVisibleIndex == null) {
        return maxOf(1, fallbackVisibleRows)
    }
    val firstVisibleRow = minVisibleIndex / columns
    val lastVisibleRow = maxVisibleIndex / columns
    return maxOf(1, lastVisibleRow - firstVisibleRow + 1)
}

/**
 * 解析当前数据规模下可滚动到的最大行对齐起点。
 *
 * @param itemCount 当前网格总项数。
 * @param columns 网格列数。
 * @param visibleRows 可视行数。
 * @return 最大行对齐起点索引。
 * @author PopoY
 */
private fun resolveMaxVisibleRowStart(
    itemCount: Int,
    columns: Int,
    visibleRows: Int,
): Int {
    if (itemCount <= 0 || columns <= 0 || visibleRows <= 0) {
        return 0
    }
    val totalRows = ((itemCount - 1) / columns) + 1
    val maxStartRow = (totalRows - visibleRows).coerceAtLeast(0)
    return maxStartRow * columns
}

/**
 * 把离散 slot 索引映射为缩放锚点。
 *
 * @param slotIndex 当前轴向 slot 索引。
 * @param maxSlotIndex 当前轴向最大 slot 索引。
 * @return 0f/0.5f/1f 的锚点值。
 * @author PopoY
 */
private fun resolveBrowseCardPivotAxis(
    slotIndex: Int,
    maxSlotIndex: Int,
): Float {
    return when {
        slotIndex <= 0 -> 0f
        slotIndex >= maxSlotIndex -> 1f
        else -> 0.5f
    }
}

/**
 * 约束邻卡推挤方向，避免边界卡片向外侧位移侵占标题区或分类导轨区。
 *
 * @param rawPushRatio 原始推挤比例（可正可负）。
 * @param slotIndex 当前轴向 slot 索引。
 * @param maxSlotIndex 当前轴向最大 slot 索引。
 * @return 经过边界约束后的推挤比例。
 * @author PopoY
 */
private fun clampBrowseCardPushRatioByBoundary(
    rawPushRatio: Float,
    slotIndex: Int,
    maxSlotIndex: Int,
): Float {
    if (rawPushRatio == 0f) {
        return 0f
    }
    if (slotIndex <= 0 && rawPushRatio < 0f) {
        return 0f
    }
    if (slotIndex >= maxSlotIndex && rawPushRatio > 0f) {
        return 0f
    }
    return rawPushRatio
}

/**
 * 当前分类下的菜品网格区域。
 *
 * 布局规格：右侧内容区宽度 + 3 列 × N 行, gap=20, 卡片 clip+cornerRadius=24。
 * 第一张卡片（index=0）使用强调样式（金色边框 + 深色底），其余使用默认容器色。
 *
 * @param selectedCategoryId 当前选中分类 ID（key 变化时重置列表）。
 * @param items 可见菜品列表。
 * @param viewportRequest 待执行的网格视口恢复请求。
 * @param focusRequest 待执行的网格焦点恢复请求。
 * @param trackViewportChanges 是否允许把当前网格滚动位置回写为 Browse 锚点。
 * @param animationsEnabled 是否启用卡片动效；测试环境可关闭以避免 Compose idling 噪声。
 * @param modifier 外层 Modifier。
 * @param onViewportChanged 网格可视首行变化回调（row-level anchor）。
 * @param onItemFocused 菜品获得焦点回调。
 */
@Composable
fun MenuItemGrid(
    selectedCategoryId: String,
    items: List<MenuItem>,
    viewportRequest: BrowseViewportRequest?,
    focusRequest: BrowseFocusRequest?,
    trackViewportChanges: Boolean,
    animationsEnabled: Boolean = true,
    modifier: Modifier = Modifier,
    onViewportChanged: (Int) -> Unit = {},
    onItemFocused: (String) -> Unit,
) {
    val proportions = LocalScreenProportions.current
    val gridState = rememberLazyGridState()
    val coroutineScope = rememberCoroutineScope()
    val poseCache = remember(selectedCategoryId, animationsEnabled) {
        mutableStateMapOf<String, BrowseCardFocusMotion>()
    }
    var focusedItemIndex by remember(selectedCategoryId) { mutableStateOf<Int?>(null) }
    var localFocusRequestSequence by remember(selectedCategoryId) { mutableStateOf(0L) }
    var localRowScrollFocusRequest by remember(selectedCategoryId) {
        mutableStateOf<BrowseFocusRequest?>(null)
    }
    var isRowScrollInProgress by remember(selectedCategoryId) { mutableStateOf(false) }

    // 分类切换或 FocusScene 返回后，按请求恢复 BrowseScene 网格位置。
    LaunchedEffect(viewportRequest?.requestId, selectedCategoryId) {
        viewportRequest?.let { request ->
            gridState.scrollToItem(
                index = request.firstVisibleItemIndex,
                scrollOffset = request.firstVisibleItemScrollOffset,
            )
        }
    }

    // 焦点恢复先等待视口恢复完成；如果目标卡仍不在可见区，再补一次定向滚动。
    LaunchedEffect(focusRequest?.requestId, viewportRequest?.requestId, selectedCategoryId, items) {
        val request = focusRequest ?: return@LaunchedEffect
        val targetItemIndex = request.targetItemIndex
            ?: items.indexOfFirst { item -> item.itemId == request.targetItemId }
                .takeIf { itemIndex -> itemIndex >= 0 }
            ?: return@LaunchedEffect
        focusedItemIndex = targetItemIndex

        viewportRequest?.let { restoredViewport ->
            snapshotFlow {
                gridState.firstVisibleItemIndex == restoredViewport.firstVisibleItemIndex ||
                    gridState.layoutInfo.visibleItemsInfo.any { visibleItem ->
                        visibleItem.index == targetItemIndex
                    }
            }.filter { isViewportReady -> isViewportReady }
                .first()
        }

        val isTargetVisible = gridState.layoutInfo.visibleItemsInfo.any { visibleItem ->
            visibleItem.index == targetItemIndex
        }
        if (!isTargetVisible) {
            gridState.scrollToItem(index = targetItemIndex)
        }
    }

    // 持续同步网格当前的首个可视行，供 ViewModel 记录分类级 row anchor。
    LaunchedEffect(gridState, selectedCategoryId, trackViewportChanges) {
        snapshotFlow {
            resolveAlignedVisibleRowStart(
                visibleRowStart = gridState.firstVisibleItemIndex,
                columns = GRID_COLUMNS,
            ) / GRID_COLUMNS
        }.distinctUntilChanged()
            .filter { items.isNotEmpty() && trackViewportChanges }
            .collect { rowIndex ->
                onViewportChanged(rowIndex)
            }
    }

    BoxWithConstraints(
        modifier = modifier.fillMaxSize(),
    ) {
        val cardMetrics = resolveBrowseGridCardMetrics(
            maxWidth = maxWidth,
            maxHeight = maxHeight,
            proportions = proportions,
        )
        // 聚焦体量统一由动效令牌驱动，避免组合层继续依赖旧 FocusScene 参数。
        val focusedCardScale = resolveBrowseFocusedCardScale()
        val (pushOffsetX, pushOffsetY) = resolveBrowseFocusPushOffsets(
            cardWidth = cardMetrics.cardWidth,
            cardHeight = cardMetrics.cardHeight,
        )
        // 纯函数接口已切换为显式 visibleRowStart 语义，这里先做一次对齐后再下发。
        val visibleRowStart = resolveAlignedVisibleRowStart(
            visibleRowStart = gridState.firstVisibleItemIndex,
            columns = GRID_COLUMNS,
        )
        val effectiveFocusRequest = localRowScrollFocusRequest ?: focusRequest

        LazyVerticalGrid(
            columns = GridCells.Fixed(GRID_COLUMNS),
            state = gridState,
            userScrollEnabled = BROWSE_GRID_USER_SCROLL_ENABLED,
            modifier = Modifier
                .fillMaxSize()
                .testTag("menu-item-grid"),
            horizontalArrangement = Arrangement.spacedBy(proportions.browseGridHorizontalGap),
            verticalArrangement = Arrangement.spacedBy(proportions.browseGridVerticalGap),
        ) {
            itemsIndexed(
                items = items,
                key = { _, item -> item.itemId },
            ) { index, item ->
                val focusRequester = remember(item.itemId) { FocusRequester() }
                val focusMotion = resolveBrowseCardFocusMotion(
                    itemIndex = index,
                    focusedItemIndex = focusedItemIndex,
                    visibleRowStart = visibleRowStart,
                    columns = GRID_COLUMNS,
                    visibleRows = GRID_VISIBLE_ROWS,
                    focusedCardScale = focusedCardScale,
                )
                val cachedPose = if (animationsEnabled) poseCache[item.itemId] else null
                LaunchedEffect(item.itemId, focusMotion, animationsEnabled) {
                    if (!animationsEnabled) {
                        return@LaunchedEffect
                    }
                    val poseSnapshot = resolveBrowseCardPoseCacheSnapshot(
                        targetMotion = focusMotion,
                    )
                    if (poseCache[item.itemId] != poseSnapshot) {
                        poseCache[item.itemId] = poseSnapshot
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = cardMetrics.cardHorizontalInset),
                ) {
                    BrowseGridCard(
                        item = item,
                        itemIndex = index,
                        isHighlighted = index == 0,
                        testTag = "menu-item-$selectedCategoryId-$index",
                        cardHeight = cardMetrics.cardHeight,
                        imageHeight = cardMetrics.imageHeight,
                        cachedPose = cachedPose,
                        animationsEnabled = animationsEnabled,
                        focusMotion = focusMotion,
                        focusedCardScale = focusedCardScale,
                        pushOffsetX = pushOffsetX,
                        pushOffsetY = pushOffsetY,
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        focusRequest = effectiveFocusRequest,
                        focusRequester = focusRequester,
                        onVerticalDirectionPressed = { direction ->
                            if (isRowScrollInProgress) {
                                false
                            } else {
                                val runtimeVisibleRows = resolveBrowseVisibleRowCount(
                                    visibleItemIndices = gridState.layoutInfo.visibleItemsInfo.map { visibleItem ->
                                        visibleItem.index
                                    },
                                    columns = GRID_COLUMNS,
                                    fallbackVisibleRows = GRID_VISIBLE_ROWS,
                                )
                                val rowScrollIntent = resolveBrowseRowScrollIntent(
                                    direction = direction,
                                    focusedItemIndex = index,
                                    visibleRowStart = visibleRowStart,
                                    itemCount = items.size,
                                    columns = GRID_COLUMNS,
                                    visibleRows = runtimeVisibleRows,
                                )
                                if (rowScrollIntent.action == BrowseRowScrollAction.NoScroll) {
                                    false
                                } else {
                                    val targetItem = items.getOrNull(rowScrollIntent.targetFocusedItemIndex)
                                        ?: return@BrowseGridCard false
                                    isRowScrollInProgress = true
                                    coroutineScope.launch {
                                        try {
                                            gridState.animateScrollToItem(
                                                index = rowScrollIntent.targetVisibleRowStart,
                                                scrollOffset = 0,
                                            )
                                            localFocusRequestSequence += 1L
                                            localRowScrollFocusRequest = BrowseFocusRequest(
                                                requestId = localFocusRequestSequence,
                                                targetItemId = targetItem.itemId,
                                                targetItemIndex = rowScrollIntent.targetFocusedItemIndex,
                                            )
                                            focusedItemIndex = rowScrollIntent.targetFocusedItemIndex
                                        } finally {
                                            isRowScrollInProgress = false
                                        }
                                    }
                                    true
                                }
                            }
                        },
                        onFocused = {
                            focusedItemIndex = index
                            if (localRowScrollFocusRequest?.targetItemId == item.itemId) {
                                // 逐行滚动后的焦点恢复请求只消费一次，避免后续重组再次抢焦点。
                                localRowScrollFocusRequest = null
                            }
                            onItemFocused(item.itemId)
                        },
                    )
                }
            }
        }
    }
}

/**
 * 浏览页网格卡片组件。
 *
 * 设计稿结构：顶部图片区（180px）+ 底部文本区（name + description）。
 * 聚焦时升级边框/背景对比度；[isHighlighted] 首张卡片常态即带金色边框。
 *
 * @param item 菜品数据。
 * @param itemIndex 菜品在列表中的索引，用于本地图片 fallback 循环。
 * @param isHighlighted 是否为高亮卡片（第一张，设计稿 cardA1 样式）。
 * @param testTag UI 测试标签。
 * @param cardHeight 当前视口下的卡片总高度。
 * @param imageHeight 当前视口下的图片区域高度。
 * @param cachedPose 当前卡片最近一次缓存的姿态快照。
 * @param animationsEnabled 是否启用卡片动效。
 * @param focusMotion 当前卡片动画参数。
 * @param focusedCardScale 聚焦态目标放大倍率（与 FocusMid 大卡体量对齐）。
 * @param pushOffsetX 水平推挤位移基准。
 * @param pushOffsetY 垂直推挤位移基准。
 * @param focusRequest 待执行的网格焦点恢复请求。
 * @param focusRequester 当前卡片 FocusRequester（焦点请求器）。
 * @param modifier 外层 Modifier。
 * @param onVerticalDirectionPressed 纵向方向键拦截回调；返回 `true` 表示已改由逐行滚动处理。
 * @param onFocused 聚焦回调。
 */
@Composable
private fun BrowseGridCard(
    item: MenuItem,
    itemIndex: Int,
    isHighlighted: Boolean,
    testTag: String,
    cardHeight: androidx.compose.ui.unit.Dp,
    imageHeight: androidx.compose.ui.unit.Dp,
    cachedPose: BrowseCardFocusMotion?,
    animationsEnabled: Boolean,
    focusMotion: BrowseCardFocusMotion,
    focusedCardScale: Float,
    pushOffsetX: androidx.compose.ui.unit.Dp,
    pushOffsetY: androidx.compose.ui.unit.Dp,
    focusRequest: BrowseFocusRequest?,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier,
    onVerticalDirectionPressed: (BrowseRowScrollDirection) -> Boolean = { false },
    onFocused: () -> Unit = {},
) {
    val proportions = LocalScreenProportions.current
    var isFocused by remember { mutableStateOf(false) }
    var renderedFocusMotion by remember(item.itemId, animationsEnabled) {
        mutableStateOf(
            if (animationsEnabled) {
                resolveBrowseCardEnterMotion(
                    targetMotion = focusMotion,
                    cachedPose = cachedPose,
                )
            } else {
                focusMotion
            },
        )
    }
    val shape = RoundedCornerShape(Dimens.BrowseGridCardCorner)
    val showExpandedDetails = renderedFocusMotion.showExpandedDetails || isFocused

    LaunchedEffect(focusMotion) {
        renderedFocusMotion = focusMotion
    }

    val scaleTarget = if (isFocused) {
        maxOf(focusedCardScale, renderedFocusMotion.scale)
    } else {
        renderedFocusMotion.scale
    }
    val alphaTarget = if (isFocused) 1f else renderedFocusMotion.alpha
    val offsetXTarget = pushOffsetX * renderedFocusMotion.offsetXRatio
    val offsetYTarget = pushOffsetY * renderedFocusMotion.offsetYRatio
    val animatedScale = if (animationsEnabled) {
        val value by animateFloatAsState(
            targetValue = scaleTarget,
            animationSpec = spring(
                dampingRatio = 0.72f,
                stiffness = Spring.StiffnessMediumLow,
            ),
            label = "browse-card-scale",
        )
        value
    } else {
        scaleTarget
    }
    val animatedAlpha = if (animationsEnabled) {
        val value by animateFloatAsState(
            targetValue = alphaTarget,
            animationSpec = spring(
                dampingRatio = 0.78f,
                stiffness = Spring.StiffnessLow,
            ),
            label = "browse-card-alpha",
        )
        value
    } else {
        alphaTarget
    }
    val animatedOffsetX = if (animationsEnabled) {
        val value by animateDpAsState(
            targetValue = offsetXTarget,
            animationSpec = spring(
                dampingRatio = 0.75f,
                stiffness = Spring.StiffnessMediumLow,
            ),
            label = "browse-card-offset-x",
        )
        value
    } else {
        offsetXTarget
    }
    val animatedOffsetY = if (animationsEnabled) {
        val value by animateDpAsState(
            targetValue = offsetYTarget,
            animationSpec = spring(
                dampingRatio = 0.75f,
                stiffness = Spring.StiffnessMediumLow,
            ),
            label = "browse-card-offset-y",
        )
        value
    } else {
        offsetYTarget
    }

    // 边框：聚焦态 → 金色；高亮首卡 → 金色；其余 → 暗边框
    val borderColor = when {
        isFocused -> ColorTokens.GlassBorderFocused
        isHighlighted -> ColorTokens.GlassBorderFocused
        else -> ColorTokens.GlassBorderSubtle
    }
    // 背景：聚焦态 → 强调面；高亮首卡 → 强调面；其余 → 默认卡面
    val containerColor = when {
        isFocused -> ColorTokens.GlassSurfaceStrong
        isHighlighted -> ColorTokens.GlassSurfaceStrong
        else -> ColorTokens.GlassSurface
    }

    // 目标卡片在进入视口后主动请求焦点，驱动分类切入或 FocusScene 返回恢复。
    LaunchedEffect(focusRequest?.requestId) {
        if (focusRequest?.targetItemId == item.itemId) {
            focusRequester.requestFocus()
        }
    }

    GlassSurface(
        modifier = modifier
            .height(cardHeight)
            .zIndex(
                when {
                    showExpandedDetails -> 8f
                    isFocused -> 7f
                    else -> renderedFocusMotion.zIndex
                },
            )
            .offset(x = animatedOffsetX, y = animatedOffsetY)
            .graphicsLayer {
                scaleX = animatedScale
                scaleY = animatedScale
                alpha = animatedAlpha
                transformOrigin = TransformOrigin(
                    pivotFractionX = renderedFocusMotion.pivotX,
                    pivotFractionY = renderedFocusMotion.pivotY,
                )
            }
            .onPreviewKeyEvent { keyEvent ->
                if (keyEvent.type != KeyEventType.KeyDown) {
                    return@onPreviewKeyEvent false
                }
                val rowScrollDirection = keyEvent.key.toBrowseRowScrollDirectionOrNull()
                    ?: return@onPreviewKeyEvent false
                onVerticalDirectionPressed(rowScrollDirection)
            }
            .onFocusChanged { focusState ->
                val focusedNow = focusState.isFocused
                isFocused = focusedNow
                if (focusedNow) {
                    onFocused()
                }
            }
            .focusable()
            .testTag(testTag),
        containerColor = containerColor,
        borderColor = borderColor,
        shape = shape,
        contentPadding = PaddingValues(0.dp),
        contentSpacing = proportions.browseGridCardContentSpacing,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(imageHeight)
                .clip(
                    RoundedCornerShape(
                        topStart = Dimens.BrowseGridCardCorner,
                        topEnd = Dimens.BrowseGridCardCorner,
                    ),
                ),
        ) {
            PocoAsyncImage(
                model = item.imageUrl.takeIf { it.isNotBlank() }
                    ?: LOCAL_FOOD_DRAWABLES[itemIndex % LOCAL_FOOD_DRAWABLES.size],
                contentDescription = item.name,
                modifier = Modifier.fillMaxSize(),
            )
        }
        // 文本区：菜品名 + 描述
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = proportions.browseGridCardBodyMinHeight)
                .padding(
                    start = proportions.browseGridCardBodyPaddingHorizontal,
                    end = proportions.browseGridCardBodyPaddingHorizontal,
                    bottom = proportions.browseGridCardBodyPaddingBottom,
                ),
            verticalArrangement = Arrangement.spacedBy(proportions.browseGridCardBodySpacing),
        ) {
            Text(
                text = item.name,
                color = ColorTokens.TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = proportions.scaledSp(24f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.testTag("$testTag-name"),
            )
            Text(
                text = item.description,
                color = ColorTokens.TextSecondary,
                fontSize = proportions.scaledSp(15f),
                maxLines = if (showExpandedDetails) 2 else 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.testTag("$testTag-description"),
            )
            if (animationsEnabled) {
                AnimatedVisibility(
                    visible = showExpandedDetails,
                    enter = fadeIn(animationSpec = tween(180)) + expandVertically(
                        animationSpec = spring(
                            dampingRatio = 0.74f,
                            stiffness = Spring.StiffnessMediumLow,
                        ),
                    ),
                    exit = fadeOut(animationSpec = tween(120)) + shrinkVertically(
                        animationSpec = tween(120),
                    ),
                ) {
                    BrowseFocusDetailSection(
                        item = item,
                        testTag = testTag,
                    )
                }
            } else if (showExpandedDetails) {
                BrowseFocusDetailSection(
                    item = item,
                    testTag = testTag,
                )
            }
        }
    }
}

/**
 * 聚焦卡扩展详情区：价格 + 精简标签 chip。
 *
 * @param item 当前菜品。
 * @param testTag 当前卡片测试标签前缀。
 * @author PopoY
 */
@Composable
private fun BrowseFocusDetailSection(
    item: MenuItem,
    testTag: String,
) {
    val proportions = LocalScreenProportions.current
    val chips = remember(item.itemId, item.displayBadges, item.tags) {
        resolveBrowseFocusChipLabels(item)
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "¥${item.priceInfo.amountMinor / 100} / ${item.priceInfo.unitLabel}",
                color = ColorTokens.Accent,
                fontWeight = FontWeight.SemiBold,
                fontSize = proportions.scaledSp(16f),
                maxLines = 1,
                modifier = Modifier.testTag("$testTag-price"),
            )
            val primaryBadgeLabel = item.displayBadges.firstOrNull()?.label
            if (!primaryBadgeLabel.isNullOrBlank()) {
                Text(
                    text = primaryBadgeLabel,
                    color = ColorTokens.TextMuted,
                    fontSize = proportions.scaledSp(13f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.testTag("$testTag-highlight"),
                )
            }
        }

        if (chips.isNotEmpty()) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                chips.forEachIndexed { chipIndex, chipLabel ->
                    BrowseFocusChip(
                        text = chipLabel,
                        testTag = "$testTag-chip-$chipIndex",
                    )
                }
            }
        }
    }
}

/**
 * 聚焦详情区 chip 组件。
 *
 * @param text chip 文案。
 * @param testTag 测试标签。
 * @author PopoY
 */
@Composable
private fun BrowseFocusChip(
    text: String,
    testTag: String,
) {
    val proportions = LocalScreenProportions.current
    Box(
        modifier = Modifier
            .border(
                width = 1.dp,
                color = ColorTokens.GoldSoft,
                shape = RoundedCornerShape(999.dp),
            )
            .background(
                color = ColorTokens.ChipBg,
                shape = RoundedCornerShape(999.dp),
            )
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(
            text = text,
            color = ColorTokens.TextPrimary,
            fontSize = proportions.scaledSp(12f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.testTag(testTag),
        )
    }
}
