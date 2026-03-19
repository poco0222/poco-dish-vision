/**
 * @file AttractCarousel.kt
 * @author PopoY
 * @date 2026-03-18
 * @description 首页吸引模式下的推荐轮播区。
 */
package com.poco.dishvision.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Text
import com.poco.dishvision.core.model.menu.MenuItem
import com.poco.dishvision.core.ui.components.GlassSurface
import com.poco.dishvision.core.ui.components.PocoAsyncImage
import com.poco.dishvision.core.ui.theme.ColorTokens
import com.poco.dishvision.core.ui.theme.Dimens

/**
 * 推荐轮播区，当前以首个推荐菜品作为主卡展示。
 *
 * @param featuredItems 推荐菜品列表。
 * @param modifier 外层 Modifier。
 */
@Composable
fun AttractCarousel(
    featuredItems: List<MenuItem>,
    modifier: Modifier = Modifier,
) {
    val featuredItem = featuredItems.firstOrNull()
    GlassSurface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("attract-carousel"),
        containerColor = ColorTokens.GlassSurfaceSoft,
        borderColor = ColorTokens.GlassBorderSubtle,
        contentPadding = PaddingValues(0.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp),
        ) {
            PocoAsyncImage(
                model = featuredItem?.imageUrl?.takeIf { it.isNotBlank() },
                contentDescription = featuredItem?.name,
                modifier = Modifier.fillMaxSize(),
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = Dimens.SurfaceLargeCorner,
                        vertical = Dimens.ScreenTopPadding,
                    ),
                verticalArrangement = Arrangement.spacedBy(Dimens.SurfaceContentSpacing),
            ) {
                Text(
                    text = "本店推荐",
                    color = ColorTokens.TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = featuredItem?.name ?: "POCO Dish Vision",
                    color = ColorTokens.TextPrimary,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = featuredItem?.description ?: "高位壁挂场景下的中下视觉重心展示",
                    color = ColorTokens.TextSecondary,
                )
            }
        }
    }
}
