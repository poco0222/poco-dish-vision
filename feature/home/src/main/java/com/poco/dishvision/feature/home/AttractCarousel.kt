/**
 * @file AttractCarousel.kt
 * @author PopoY
 * @date 2026-03-19
 * @description 首页吸引模式下的推荐卡带，使用视觉聚焦表达当前选中项。
 *              聚焦卡通过 spring 缩放、柔和边框与自绘弱光扩散营造层次感，
 *              切换时各属性动画联动，实现自然流畅的焦点移动效果。
 */
package com.poco.dishvision.feature.home

import android.graphics.BlurMaskFilter
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.tv.material3.Text
import com.poco.dishvision.core.ui.components.GlassSurface
import com.poco.dishvision.core.ui.theme.ColorTokens
import com.poco.dishvision.core.ui.theme.Dimens
import com.poco.dishvision.core.ui.theme.LocalScreenProportions

/** 聚焦卡缩放比例 */
private const val FOCUSED_SCALE = 1.1f

/** 非聚焦卡缩放比例（常态） */
private const val UNFOCUSED_SCALE = 1f

/** 聚焦弱光扩散模糊半径 */
private val FOCUS_GLOW_BLUR_RADIUS = 16.dp

/** 聚焦弱光扩散向外扩展量 */
private val FOCUS_GLOW_SPREAD = 3.dp

/**
 * 推荐卡带，当前选中项通过缩放、柔和边框与自绘弱光扩散表现聚焦状态。
 * 切换时 spring 弹性动画驱动各属性过渡，营造自然移动质感。
 *
 * @param showcaseItems 推荐菜品列表。
 * @param selectedIndex 当前选中项索引。
 * @param modifier 外层 Modifier。
 */
@Composable
fun AttractCarousel(
    showcaseItems: List<HomeShowcaseItem>,
    selectedIndex: Int,
    modifier: Modifier = Modifier,
) {
    val proportions = LocalScreenProportions.current
    // 卡片圆角复用，与 GlassSurface 保持一致
    val cardShape = RoundedCornerShape(Dimens.SurfaceMediumCorner)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .testTag("attract-carousel"),
        horizontalArrangement = Arrangement.spacedBy(Dimens.HomeRecommendationGap),
    ) {
        showcaseItems.forEachIndexed { index, item ->
            val isSelected = index == selectedIndex

            // ── 聚焦缩放：选中卡轻微放大 ──
            val scale by animateFloatAsState(
                targetValue = if (isSelected) FOCUSED_SCALE else UNFOCUSED_SCALE,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMediumLow,
                ),
                label = "card-scale-$index",
            )

            // ── 容器底色平滑过渡 ──
            val animatedContainerColor by animateColorAsState(
                targetValue = if (isSelected) {
                    ColorTokens.SurfaceCardStrong
                } else {
                    ColorTokens.SurfaceCard
                },
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMediumLow,
                ),
                label = "card-bg-$index",
            )

            // ── 边框颜色：瞬时切换，与阴影同步避免退场拖尾 ──
            val animatedBorderColor = if (isSelected) {
                ColorTokens.FocusBorderSoft
            } else {
                ColorTokens.BorderSubtle
            }

            // ── 边框宽度：瞬时切换 ──
            val animatedBorderWidth = if (isSelected) 1.5.dp else 1.dp

            GlassSurface(
                modifier = Modifier
                    .weight(1f)
                    .height(proportions.homeRecommendationCardHeight)
                    // 聚焦卡或缩放尚未收回的退场卡绘制在上层，
                    // 避免 1.1x 溢出部分被相邻卡遮挡导致视觉跳变
                    .zIndex(if (isSelected || scale > UNFOCUSED_SCALE) 1f else 0f)
                    // graphicsLayer 仅影响视觉渲染，不改变布局尺寸
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
                    // 聚焦弱光扩散：BlurMaskFilter 自绘，绕过平台 elevation 阴影
                    .drawBehind {
                        if (isSelected) {
                            val blurPx = FOCUS_GLOW_BLUR_RADIUS.toPx()
                            val spreadPx = FOCUS_GLOW_SPREAD.toPx()
                            val cornerPx = Dimens.SurfaceMediumCorner.toPx()
                            val glowPaint = Paint().apply {
                                color = ColorTokens.FocusGlow
                                asFrameworkPaint().maskFilter = BlurMaskFilter(
                                    blurPx,
                                    BlurMaskFilter.Blur.NORMAL,
                                )
                            }
                            drawIntoCanvas { canvas ->
                                canvas.drawRoundRect(
                                    left = -spreadPx,
                                    top = -spreadPx,
                                    right = size.width + spreadPx,
                                    bottom = size.height + spreadPx,
                                    radiusX = cornerPx,
                                    radiusY = cornerPx,
                                    paint = glowPaint,
                                )
                            }
                        }
                    }
                    .semantics {
                        stateDescription = if (isSelected) "selected" else "unselected"
                    }
                    .testTag("home-showcase-card-$index"),
                containerColor = animatedContainerColor,
                borderColor = animatedBorderColor,
                borderWidth = animatedBorderWidth,
                shape = cardShape,
                contentPadding = PaddingValues(
                    horizontal = Dimens.HomeRecommendationCardHorizontalPadding,
                    vertical = Dimens.HomeRecommendationCardVerticalPadding,
                ),
                contentSpacing = 7.dp,
            ) {
                Text(
                    text = item.cardTitle,
                    color = ColorTokens.TextPrimary,
                    fontWeight = FontWeight.Bold,
                    style = TextStyle(fontSize = proportions.scaledSp(25f)),
                )
                Text(
                    text = item.cardPriceLabel,
                    color = ColorTokens.Accent,
                    fontWeight = FontWeight.SemiBold,
                    style = TextStyle(fontSize = proportions.scaledSp(19f)),
                )
                Text(
                    text = item.cardDescription,
                    color = ColorTokens.TextSecondary,
                    lineHeight = proportions.scaledSp(20f),
                    style = TextStyle(fontSize = proportions.scaledSp(15f)),
                )
                Text(
                    text = item.cardPrompt,
                    color = ColorTokens.TextMuted,
                    style = TextStyle(fontSize = proportions.scaledSp(15f)),
                )
            }
        }
    }
}
