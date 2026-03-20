/**
 * @file VisualFocusSurface.kt
 * @author PopoY
 * @date 2026-03-20
 * @description 提供可复用的 visual focus surface（视觉聚焦表面），复用首页与菜单分类的焦点动效语言。
 */
package com.poco.dishvision.core.ui.components

import android.graphics.BlurMaskFilter
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.poco.dishvision.core.ui.theme.ColorTokens

/** 聚焦态缩放比例，沿用首页卡带已验证的默认值。 */
private const val DEFAULT_FOCUSED_SCALE = 1.1f

/** 常态缩放比例。 */
private const val DEFAULT_UNFOCUSED_SCALE = 1f

/** 聚焦辉光默认模糊半径。 */
private val DEFAULT_GLOW_BLUR_RADIUS = 16.dp

/** 聚焦辉光默认外扩距离。 */
private val DEFAULT_GLOW_SPREAD = 3.dp

/**
 * 共享视觉聚焦容器。
 *
 * 只抽象 spring scale（弹簧缩放）、glow（辉光）、zIndex 抬升与容器色过渡；
 * 颜色体系、圆角、padding（内边距）等具体视觉由调用方传入，避免不同场景样式被硬绑定。
 *
 * @param isFocused 当前是否处于视觉聚焦态。
 * @param modifier 外层 Modifier。
 * @param containerColor 常态容器色。
 * @param focusedContainerColor 聚焦态容器色。
 * @param borderColor 常态边框色。
 * @param focusedBorderColor 聚焦态边框色。
 * @param borderWidth 常态边框宽度。
 * @param focusedBorderWidth 聚焦态边框宽度。
 * @param shape 容器圆角形状。
 * @param contentPadding 内容内边距。
 * @param contentSpacing 子内容间距。
 * @param focusedScale 聚焦态缩放比例。
 * @param unfocusedScale 常态缩放比例。
 * @param glowColor 聚焦态辉光颜色。
 * @param glowBlurRadius 聚焦态辉光模糊半径。
 * @param glowSpread 聚焦态辉光外扩距离。
 * @param glowCornerRadius 聚焦态辉光圆角半径。
 * @param content 容器内容。
 */
@Composable
fun VisualFocusSurface(
    isFocused: Boolean,
    modifier: Modifier = Modifier,
    containerColor: Color,
    focusedContainerColor: Color,
    borderColor: Color,
    focusedBorderColor: Color,
    borderWidth: Dp = 1.dp,
    focusedBorderWidth: Dp = borderWidth,
    shape: Shape,
    contentPadding: PaddingValues,
    contentSpacing: Dp = 0.dp,
    focusedScale: Float = DEFAULT_FOCUSED_SCALE,
    unfocusedScale: Float = DEFAULT_UNFOCUSED_SCALE,
    glowColor: Color = ColorTokens.FocusGlow,
    glowBlurRadius: Dp = DEFAULT_GLOW_BLUR_RADIUS,
    glowSpread: Dp = DEFAULT_GLOW_SPREAD,
    glowCornerRadius: Dp = 0.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    val scale by animateFloatAsState(
        targetValue = if (isFocused) focusedScale else unfocusedScale,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "visual-focus-scale",
    )
    val animatedContainerColor by animateColorAsState(
        targetValue = if (isFocused) focusedContainerColor else containerColor,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "visual-focus-container",
    )
    val resolvedBorderColor = if (isFocused) focusedBorderColor else borderColor
    val resolvedBorderWidth = if (isFocused) focusedBorderWidth else borderWidth

    GlassSurface(
        modifier = modifier
            // 聚焦卡或缩放尚未完全回收的退场卡抬到上层，避免溢出区域被邻近卡遮挡。
            .zIndex(if (isFocused || scale > unfocusedScale) 1f else 0f)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .drawBehind {
                if (isFocused) {
                    val blurPx = glowBlurRadius.toPx()
                    val spreadPx = glowSpread.toPx()
                    val cornerPx = glowCornerRadius.toPx()
                    val glowPaint = Paint().apply {
                        color = glowColor
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
            },
        containerColor = animatedContainerColor,
        borderColor = resolvedBorderColor,
        borderWidth = resolvedBorderWidth,
        shape = shape,
        contentPadding = contentPadding,
        contentSpacing = contentSpacing,
        content = content,
    )
}
