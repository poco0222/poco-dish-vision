/**
 * @file FocusableMenuCard.kt
 * @author PopoY
 * @date 2026-03-19
 * @description 提供浏览页使用的可聚焦菜品卡片组件。
 */
package com.poco.dishvision.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Text
import com.poco.dishvision.core.model.menu.MenuItem
import com.poco.dishvision.core.model.menu.PriceInfo
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
    val shape = RoundedCornerShape(28.dp)
    val interactionSource = remember { MutableInteractionSource() }
    val borderColor = if (isFocused) Color(0xFFFFD166) else Color(0x33FFFFFF)
    val containerColor = if (isFocused) Color(0xFF263750) else Color(0xCC172130)

    Column(
        modifier = modifier
            .clip(shape)
            .background(containerColor)
            .border(width = 2.dp, color = borderColor, shape = shape)
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
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(176.dp)
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)),
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
                .padding(start = 20.dp, end = 20.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = item.name,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = formatPrice(item.priceInfo),
                color = Color(0xFFFFD166),
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = item.description,
                color = Color(0xFFE4EAF4),
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
