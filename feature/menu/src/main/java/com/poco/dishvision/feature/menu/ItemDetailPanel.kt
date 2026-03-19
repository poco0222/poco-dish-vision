/**
 * @file ItemDetailPanel.kt
 * @author PopoY
 * @date 2026-03-19
 * @description 提供浏览页底部 detail dock 与详情浮层。
 */
package com.poco.dishvision.feature.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Text
import com.poco.dishvision.core.model.menu.DisplayBadge
import com.poco.dishvision.core.model.menu.MenuItem
import com.poco.dishvision.core.model.menu.PriceInfo
import java.util.Locale

/**
 * 浏览页底部持续详情区。
 *
 * @param item 当前焦点菜品。
 * @param isExpanded 是否展开详情浮层。
 * @param modifier 外层 Modifier。
 * @param onDismissRequest 浮层关闭回调。
 */
@Composable
fun ItemDetailPanel(
    item: MenuItem?,
    isExpanded: Boolean,
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
) {
    if (item == null) {
        return
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = Color(0xDD142030), shape = RoundedCornerShape(28.dp))
                .padding(horizontal = 28.dp, vertical = 24.dp)
                .testTag("detail-dock"),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = item.name,
                color = Color.White,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = formatDockPrice(item.priceInfo),
                color = Color(0xFFFFD166),
                fontWeight = FontWeight.SemiBold,
            )
            BadgeRow(badges = item.displayBadges)
            Text(
                text = item.description,
                color = Color(0xFFE4EAF4),
            )
            Text(
                text = "按确认键展开详情",
                color = Color(0xCCF4F7FB),
            )
        }

        if (isExpanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = Color(0xF2253550), shape = RoundedCornerShape(32.dp))
                    .clickable(onClick = onDismissRequest)
                    .padding(horizontal = 28.dp, vertical = 24.dp)
                    .testTag("detail-panel"),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "菜品详情",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = item.description,
                    color = Color(0xFFE4EAF4),
                )
                Text(
                    text = "标签：${item.tags.joinToString(separator = " / ")}",
                    color = Color(0xCCF4F7FB),
                )
                Text(
                    text = "点击详情面板任意区域返回浏览",
                    color = Color(0xCCF4F7FB),
                )
            }
        }
    }
}

/**
 * 渲染菜品徽章行。
 *
 * @param badges 徽章列表。
 */
@Composable
private fun BadgeRow(badges: List<DisplayBadge>) {
    if (badges.isEmpty()) {
        return
    }

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        badges.forEach { badge ->
            Text(
                text = badge.label,
                modifier = Modifier
                    .background(color = Color(0x332B3E58), shape = RoundedCornerShape(999.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                color = Color(0xFFFFD166),
            )
        }
    }
}

/**
 * 生成底部详情区价格文案。
 *
 * @param priceInfo 价格对象。
 * @return 用于大屏展示的价格字符串。
 */
private fun formatDockPrice(priceInfo: PriceInfo): String {
    val amount = priceInfo.amountMinor / 100.0
    return String.format(Locale.US, "¥%.2f", amount)
}
