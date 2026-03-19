/**
 * @file GlassSurface.kt
 * @author PopoY
 * @date 2026-03-19
 * @description 提供 Phase 1 共享的玻璃质感容器组件。
 */
package com.poco.dishvision.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.poco.dishvision.core.ui.theme.ColorTokens
import com.poco.dishvision.core.ui.theme.Dimens

/**
 * 共享玻璃容器组件。
 *
 * @param modifier 外层 Modifier。
 * @param containerColor 容器底色。
 * @param borderColor 边框颜色。
 * @param shape 容器圆角。
 * @param contentPadding 内容内边距。
 * @param contentSpacing 子内容间距。
 * @param content 容器内容。
 */
@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    containerColor: Color = ColorTokens.GlassSurface,
    borderColor: Color = ColorTokens.GlassBorderSubtle,
    shape: Shape = RoundedCornerShape(Dimens.SurfaceMediumCorner),
    contentPadding: PaddingValues = PaddingValues(
        horizontal = Dimens.SurfaceHorizontalPadding,
        vertical = Dimens.SurfaceVerticalPadding,
    ),
    contentSpacing: Dp = 0.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .clip(shape)
            .background(color = containerColor, shape = shape)
            .border(width = 1.dp, color = borderColor, shape = shape)
            .padding(contentPadding),
        verticalArrangement = if (contentSpacing > 0.dp) {
            Arrangement.spacedBy(contentSpacing)
        } else {
            Arrangement.Top
        },
        content = content,
    )
}
