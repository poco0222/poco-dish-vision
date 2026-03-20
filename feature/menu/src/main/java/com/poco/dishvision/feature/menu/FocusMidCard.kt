/**
 * @file FocusMidCard.kt
 * @author PopoY
 * @date 2026-03-20
 * @description 聚焦舞台中央放大详情卡组件，匹配设计稿 npcH9 中的 focusMid 布局。
 *
 * 设计稿参数：w=680, cornerRadius=30, border=2px gold, shadow(blur=30, y=14)。
 * 内部结构：图片区(h=228) → focusBody(eyebrow + 菜名 + 描述 + 价格徽章 + divider + 风味标签)。
 */
package com.poco.dishvision.feature.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Text
import com.poco.dishvision.core.model.menu.DisplayBadge
import com.poco.dishvision.core.model.menu.MenuItem
import com.poco.dishvision.core.ui.components.PocoAsyncImage
import com.poco.dishvision.core.ui.theme.ColorTokens
import com.poco.dishvision.core.ui.theme.Dimens
import com.poco.dishvision.core.ui.theme.LocalScreenProportions

/**
 * 聚焦舞台中央放大详情卡。
 *
 * 不可聚焦（纯展示组件），内容随 [item] 切换由父级 Crossfade 驱动。
 *
 * @param item 中央展示的菜品。
 * @param categoryName 当前分类名，用于 eyebrow 标签。
 * @param itemIndex 菜品在列表中的索引，用于本地图片 fallback 循环。
 * @param modifier 外层 Modifier。
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FocusMidCard(
    item: MenuItem,
    categoryName: String,
    itemIndex: Int,
    modifier: Modifier = Modifier,
) {
    val proportions = LocalScreenProportions.current
    val outerShape = RoundedCornerShape(Dimens.FocusMidCorner)
    // focusBody 仅底部圆角，顶部与图片衔接
    val bodyShape = RoundedCornerShape(
        topStart = 0.dp,
        topEnd = 0.dp,
        bottomStart = Dimens.FocusMidCorner,
        bottomEnd = Dimens.FocusMidCorner,
    )

        Column(
            modifier = modifier
                .shadow(
                elevation = Dimens.FocusMidShadowBlur,
                shape = outerShape,
                ambientColor = ColorTokens.FocusMidShadow,
                spotColor = ColorTokens.FocusMidShadow,
            )
            .clip(outerShape)
            .background(color = ColorTokens.FocusMidFill, shape = outerShape)
            .border(
                width = Dimens.FocusMidBorderWidth,
                color = ColorTokens.GlassBorderFocused,
                shape = outerShape,
            ),
        verticalArrangement = Arrangement.spacedBy(proportions.focusMidContentSpacing),
    ) {
        // ── 图片区 ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(proportions.focusMidImageHeight)
                .clip(
                    RoundedCornerShape(
                        topStart = Dimens.FocusMidCorner,
                        topEnd = Dimens.FocusMidCorner,
                    ),
                ),
        ) {
            PocoAsyncImage(
                model = item.imageUrl.takeIf { it.isNotBlank() }
                    ?: LOCAL_FOOD_DRAWABLES[itemIndex % LOCAL_FOOD_DRAWABLES.size],
                contentDescription = item.name,
            )
        }

        // ── FocusBody 详情区 ──
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = ColorTokens.FocusMidBodyBg, shape = bodyShape)
                .padding(
                    start = proportions.focusMidBodyPaddingHorizontal,
                    end = proportions.focusMidBodyPaddingHorizontal,
                    bottom = proportions.focusMidBodyPaddingBottom,
                ),
            verticalArrangement = Arrangement.spacedBy(proportions.focusMidBodyGap),
        ) {
            // ── 标题行 + 价格徽章 ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                // 左侧: eyebrow + 菜名 + 描述
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(proportions.focusMidTitleGroupGap),
                ) {
                    // Eyebrow 标签
                    Text(
                        text = "今日焦点 · $categoryName",
                        color = ColorTokens.Accent,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = proportions.scaledSp(12f),
                        maxLines = 1,
                    )
                    // 菜名
                    Text(
                        text = item.name,
                        color = ColorTokens.TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = proportions.scaledSp(30f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    // 描述
                    Text(
                        text = item.description,
                        color = ColorTokens.TextSecondary,
                        fontSize = proportions.scaledSp(16f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                // 右侧: 价格徽章
                PriceBadge(
                    amountYuan = item.priceInfo.amountMinor / 100,
                    unitLabel = item.priceInfo.unitLabel,
                )
            }

            // ── 分隔线 ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(ColorTokens.BorderSubtle),
            )

            // ── 风味标签区 ──
            if (item.displayBadges.isNotEmpty() || item.tags.isNotEmpty()) {
                FlavorTagsSection(
                    badges = item.displayBadges,
                    tags = item.tags,
                )
            }
        }
    }
}

/**
 * 价格徽章组件，设计稿右上角的独立价格展示块。
 *
 * @param amountYuan 价格（元）。
 * @param unitLabel 计量单位（如 "份"、"例"）。
 */
@Composable
private fun PriceBadge(
    amountYuan: Int,
    unitLabel: String,
) {
    val proportions = LocalScreenProportions.current
    val shape = RoundedCornerShape(Dimens.FocusMidPriceBadgeCorner)

    Column(
        modifier = Modifier
            .border(
                width = 1.dp,
                color = ColorTokens.GoldSoft,
                shape = shape,
            )
            .background(color = ColorTokens.FocusMidBodyBg, shape = shape)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = "参考价格",
            color = ColorTokens.TextMuted,
            fontWeight = FontWeight.Medium,
            fontSize = proportions.scaledSp(12f),
        )
        Text(
            text = "¥$amountYuan",
            color = ColorTokens.Accent,
            fontWeight = FontWeight.Bold,
            fontSize = proportions.scaledSp(36f),
        )
        Text(
            text = "元 / $unitLabel",
            color = ColorTokens.TextSecondary,
            fontWeight = FontWeight.Medium,
            fontSize = proportions.scaledSp(12f),
        )
    }
}

/**
 * 风味标签区域，含 "风味标签" 标题 + chip 列表。
 *
 * @param badges 展示徽章列表（优先渲染）。
 * @param tags 菜品标签（当 badges 不足时补充显示）。
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FlavorTagsSection(
    badges: List<DisplayBadge>,
    tags: List<String>,
) {
    val proportions = LocalScreenProportions.current

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // 小标题
        Text(
            text = "风味标签",
            color = ColorTokens.TextMuted,
            fontWeight = FontWeight.SemiBold,
            fontSize = proportions.scaledSp(12f),
        )

        // Chip 行
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(proportions.focusChipRowGap),
            verticalArrangement = Arrangement.spacedBy(proportions.focusChipRowGap),
        ) {
            // 首先渲染 badges
            badges.forEachIndexed { index, badge ->
                FlavorChip(
                    text = badge.label,
                    style = when (index) {
                        0 -> ChipStyle.PRIMARY   // 第一个 badge 使用红色强调
                        else -> ChipStyle.SECONDARY
                    },
                )
            }
            // 然后渲染 tags（作为补充）
            tags.forEach { tag ->
                FlavorChip(
                    text = tag,
                    style = ChipStyle.TERTIARY,
                )
            }
        }
    }
}

/**
 * 风味标签芯片样式枚举。
 */
private enum class ChipStyle {
    /** 红色强调芯片，设计稿 fill=$--red-accent, text=$--gold */
    PRIMARY,
    /** 次级芯片，设计稿 fill=$--chip-bg, text=$--text-primary */
    SECONDARY,
    /** 三级芯片，设计稿 fill=$--bg-mid, text=$--text-secondary */
    TERTIARY,
}

/**
 * 单个风味标签芯片。
 *
 * @param text 标签文案。
 * @param style 芯片样式。
 */
@Composable
private fun FlavorChip(
    text: String,
    style: ChipStyle,
) {
    val proportions = LocalScreenProportions.current
    val shape = RoundedCornerShape(Dimens.SurfaceChipCorner)

    // 根据样式选择颜色
    val bgColor: Color
    val borderColor: Color
    val textColor: Color
    val fontWeight: FontWeight
    when (style) {
        ChipStyle.PRIMARY -> {
            bgColor = ColorTokens.RedAccent
            borderColor = ColorTokens.GoldSoft
            textColor = ColorTokens.Accent
            fontWeight = FontWeight.Bold
        }
        ChipStyle.SECONDARY -> {
            bgColor = ColorTokens.ChipBg
            borderColor = ColorTokens.BorderSubtle
            textColor = ColorTokens.TextPrimary
            fontWeight = FontWeight.SemiBold
        }
        ChipStyle.TERTIARY -> {
            bgColor = ColorTokens.FocusMidBodyBg
            borderColor = ColorTokens.BorderSubtle
            textColor = ColorTokens.TextSecondary
            fontWeight = FontWeight.SemiBold
        }
    }

    Text(
        text = text,
        color = textColor,
        fontWeight = fontWeight,
        fontSize = proportions.scaledSp(13f),
        modifier = Modifier
            .background(color = bgColor, shape = shape)
            .border(width = 1.dp, color = borderColor, shape = shape)
            .padding(
                horizontal = Dimens.FocusChipPaddingH,
                vertical = Dimens.FocusChipPaddingV,
            ),
    )
}
