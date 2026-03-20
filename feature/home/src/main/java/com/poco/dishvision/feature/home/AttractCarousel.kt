/**
 * @file AttractCarousel.kt
 * @author PopoY
 * @date 2026-03-19
 * @description 首页吸引模式下的推荐卡带，使用共享 visual focus surface 表达当前选中项。
 */
package com.poco.dishvision.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Text
import com.poco.dishvision.core.ui.components.VisualFocusSurface
import com.poco.dishvision.core.ui.theme.ColorTokens
import com.poco.dishvision.core.ui.theme.Dimens
import com.poco.dishvision.core.ui.theme.LocalScreenProportions

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

            VisualFocusSurface(
                isFocused = isSelected,
                modifier = Modifier
                    .weight(1f)
                    .height(proportions.homeRecommendationCardHeight)
                    .semantics {
                        stateDescription = if (isSelected) "selected" else "unselected"
                    }
                    .testTag("home-showcase-card-$index"),
                containerColor = ColorTokens.SurfaceCard,
                focusedContainerColor = ColorTokens.SurfaceCardStrong,
                borderColor = ColorTokens.BorderSubtle,
                focusedBorderColor = ColorTokens.FocusBorderSoft,
                borderWidth = 1.dp,
                focusedBorderWidth = 1.5.dp,
                shape = cardShape,
                glowCornerRadius = Dimens.SurfaceMediumCorner,
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
