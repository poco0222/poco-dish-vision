/**
 * @file FocusSmallCard.kt
 * @author PopoY
 * @date 2026-03-20
 * @description 聚焦舞台周围缩小卡片组件，匹配设计稿 npcH9 中的 smallA1/B1/C1 等布局。
 *
 * 设计稿参数：w=360, cornerRadius=22, opacity=0.90~0.94,
 * 图片 h=132, body padding=[0,14,14,14], gap=4/8。
 */
package com.poco.dishvision.feature.menu

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Text
import com.poco.dishvision.core.model.menu.MenuItem
import com.poco.dishvision.core.ui.components.GlassSurface
import com.poco.dishvision.core.ui.components.PocoAsyncImage
import com.poco.dishvision.core.ui.theme.ColorTokens
import com.poco.dishvision.core.ui.theme.Dimens
import com.poco.dishvision.core.ui.theme.LocalScreenProportions

/** 小卡默认不透明度（非聚焦态），设计稿 opacity=0.92 */
private const val SMALL_CARD_DEFAULT_ALPHA = 0.92f

/** 小卡聚焦时的不透明度 */
private const val SMALL_CARD_FOCUSED_ALPHA = 1.0f

/**
 * 聚焦舞台周围缩小卡片。
 *
 * 支持 D-pad 焦点导航，聚焦时 alpha 升至 1.0 并切换金色边框。
 * 聚焦/点击触发父级回调，驱动中央大卡内容更新。
 *
 * @param item 菜品数据。
 * @param itemIndex 菜品在列表中的索引，用于本地图片 fallback 循环。
 * @param baseAlpha 非聚焦态默认透明度，设计稿按行区分：Row1=0.94, Row2=0.92, Row3=0.90。
 * @param modifier 外层 Modifier。
 * @param focusRequester 焦点请求器，用于 D-pad 导航目标定位。
 * @param onFocused 获焦回调，通知父级切换中央大卡。
 * @param onClick 确认/点击回调。
 */
@Composable
fun FocusSmallCard(
    item: MenuItem,
    itemIndex: Int,
    baseAlpha: Float = SMALL_CARD_DEFAULT_ALPHA,
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester = remember { FocusRequester() },
    onFocused: () -> Unit = {},
    onClick: () -> Unit = {},
) {
    val proportions = LocalScreenProportions.current
    var isFocused by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(Dimens.FocusSmallCorner)

    // 聚焦态切换边框颜色
    val borderColor = if (isFocused) {
        ColorTokens.GlassBorderFocused
    } else {
        ColorTokens.GlassBorderSubtle
    }

    // 聚焦态切换透明度（baseAlpha 由父级按行传入）
    val alpha = if (isFocused) SMALL_CARD_FOCUSED_ALPHA else baseAlpha

    GlassSurface(
        modifier = modifier
            .graphicsLayer { this.alpha = alpha }
            .focusRequester(focusRequester)
            .onFocusChanged { focusState ->
                val focusedNow = focusState.isFocused
                isFocused = focusedNow
                if (focusedNow) {
                    onFocused()
                }
            }
            .onPreviewKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown && keyEvent.key.isRemoteConfirmKey()) {
                    onClick()
                    true
                } else {
                    false
                }
            }
            .focusable(),
        containerColor = ColorTokens.GlassSurface,
        borderColor = borderColor,
        shape = shape,
        contentPadding = PaddingValues(0.dp),
        contentSpacing = proportions.focusSmallContentSpacing,
    ) {
        // ── 图片区（缩小版：h=132） ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(proportions.focusSmallCardImageHeight)
                .clip(
                    RoundedCornerShape(
                        topStart = Dimens.FocusSmallCorner,
                        topEnd = Dimens.FocusSmallCorner,
                    ),
                ),
        ) {
            PocoAsyncImage(
                model = item.imageUrl.takeIf { it.isNotBlank() }
                    ?: LOCAL_FOOD_DRAWABLES[itemIndex % LOCAL_FOOD_DRAWABLES.size],
                contentDescription = item.name,
            )
        }

        // ── 文本区（缩小版：标题 20sp, 描述 13sp） ──
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = proportions.focusSmallBodyPaddingHorizontal,
                    end = proportions.focusSmallBodyPaddingHorizontal,
                    bottom = proportions.focusSmallBodyPaddingBottom,
                ),
            verticalArrangement = Arrangement.spacedBy(proportions.focusSmallBodySpacing),
        ) {
            Text(
                text = item.name,
                color = ColorTokens.TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = proportions.scaledSp(20f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = item.description,
                color = ColorTokens.TextSecondary,
                fontSize = proportions.scaledSp(13f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
