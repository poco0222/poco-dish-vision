/**
 * @file AttractCarousel.kt
 * @author PopoY
 * @date 2026-03-19
 * @description 首页吸引模式下的推荐卡带，使用视觉聚焦表达当前选中项。
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Text
import com.poco.dishvision.core.ui.components.GlassSurface
import com.poco.dishvision.core.ui.theme.ColorTokens
import com.poco.dishvision.core.ui.theme.Dimens

/**
 * 推荐卡带，当前选中项通过高亮边框与更深底色表现。
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
    Row(
        modifier = modifier
            .fillMaxWidth()
            .testTag("attract-carousel"),
        horizontalArrangement = Arrangement.spacedBy(Dimens.HomeRecommendationGap),
    ) {
        showcaseItems.forEachIndexed { index, item ->
            val isSelected = index == selectedIndex
            GlassSurface(
                modifier = Modifier
                    .weight(1f)
                    .height(Dimens.HomeRecommendationCardHeight)
                    .semantics {
                        stateDescription = if (isSelected) "selected" else "unselected"
                    }
                    .testTag("home-showcase-card-$index"),
                containerColor = if (isSelected) {
                    ColorTokens.SurfaceCardStrong
                } else {
                    ColorTokens.SurfaceCard
                },
                borderColor = if (isSelected) {
                    ColorTokens.Accent
                } else {
                    ColorTokens.BorderSubtle
                },
                shape = RoundedCornerShape(Dimens.SurfaceMediumCorner),
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
                )
                Text(
                    text = item.cardPriceLabel,
                    color = ColorTokens.Accent,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = item.cardDescription,
                    color = ColorTokens.TextSecondary,
                )
                Text(
                    text = item.cardPrompt,
                    color = ColorTokens.TextMuted,
                )
            }
        }
    }
}
