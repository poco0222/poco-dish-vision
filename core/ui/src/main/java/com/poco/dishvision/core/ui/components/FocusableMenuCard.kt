/**
 * @file FocusableMenuCard.kt
 * @author PopoY
 * @date 2026-03-19
 * @description 提供浏览页使用的可聚焦菜品卡片组件。
 */
package com.poco.dishvision.core.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Text
import com.poco.dishvision.core.model.menu.MenuItem
import com.poco.dishvision.core.model.menu.PriceInfo
import com.poco.dishvision.core.ui.theme.ColorTokens
import com.poco.dishvision.core.ui.theme.Dimens
import java.util.Locale

/**
 * 菜品卡片组件，在获得焦点时提升边框与背景对比度。
 *
 * @param item 当前展示的菜品。
 * @param testTag UI 测试标签。
 * @param modifier 外层 Modifier。
 * @param onFocused 焦点进入回调。
 * @param onClick 确认键或点击回调。
 */
@Composable
fun FocusableMenuCard(
    item: MenuItem,
    testTag: String,
    modifier: Modifier = Modifier,
    onFocused: () -> Unit = {},
    onClick: () -> Unit = {},
) {
    var isFocused by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(Dimens.SurfaceMediumCorner)
    val interactionSource = remember { MutableInteractionSource() }
    val borderColor = if (isFocused) {
        ColorTokens.GlassBorderFocused
    } else {
        ColorTokens.GlassBorderSubtle
    }
    val containerColor = if (isFocused) {
        ColorTokens.CardFocusedSurface
    } else {
        ColorTokens.GlassSurface
    }

    GlassSurface(
        modifier = modifier
            .onFocusChanged { focusState ->
                val focusedNow = focusState.isFocused
                isFocused = focusedNow
                if (focusedNow) {
                    onFocused()
                }
            }
            .focusable()
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .testTag(testTag),
        containerColor = containerColor,
        borderColor = borderColor,
        shape = shape,
        contentPadding = PaddingValues(0.dp),
        contentSpacing = Dimens.SurfaceContentSpacing,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(Dimens.CardImageHeight)
                .clip(
                    RoundedCornerShape(
                        topStart = Dimens.SurfaceMediumCorner,
                        topEnd = Dimens.SurfaceMediumCorner,
                    ),
                ),
        ) {
            PocoAsyncImage(
                model = item.imageUrl.takeIf { it.isNotBlank() },
                contentDescription = item.name,
                modifier = Modifier.fillMaxSize(),
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = Dimens.SurfaceVerticalPadding,
                    end = Dimens.SurfaceVerticalPadding,
                    bottom = Dimens.SurfaceVerticalPadding,
                ),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = item.name,
                color = ColorTokens.TextPrimary,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = formatPrice(item.priceInfo),
                color = ColorTokens.Accent,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = item.description,
                color = ColorTokens.TextSecondary,
                maxLines = 2,
            )
        }
    }
}

/**
 * 将价格对象转换成适合大屏展示的简洁文案。
 *
 * @param priceInfo 价格模型。
 * @return 价格展示字符串。
 */
private fun formatPrice(priceInfo: PriceInfo): String {
    val amount = priceInfo.amountMinor / 100.0
    return String.format(Locale.US, "¥%.2f / %s", amount, priceInfo.unitLabel)
}
