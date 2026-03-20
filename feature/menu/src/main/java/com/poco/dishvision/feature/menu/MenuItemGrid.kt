/**
 * @file MenuItemGrid.kt
 * @author PopoY
 * @date 2026-03-20
 * @description 提供浏览页菜品 3 列网格区域，匹配设计稿"湘味分类/招牌热炒"中的 hotGrid 布局。
 */
package com.poco.dishvision.feature.menu

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.testTag
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
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map

/** 网格列数，设计稿中每行 3 张卡片 */
private const val GRID_COLUMNS = 3

/** 本地食物图片 drawable 资源列表，作为远程图片的 fallback */
internal val LOCAL_FOOD_DRAWABLES = listOf(
    R.drawable.menu_food_1,
    R.drawable.menu_food_2,
    R.drawable.menu_food_3,
    R.drawable.menu_food_4,
    R.drawable.menu_food_5,
)

/**
 * 当前分类下的菜品网格区域。
 *
 * 设计稿：width=1544, 3 列 × N 行, gap=20, 卡片 clip+cornerRadius=24。
 * 第一张卡片（index=0）使用强调样式（金色边框 + 深色底），其余使用默认容器色。
 *
 * @param selectedCategoryId 当前选中分类 ID（key 变化时重置列表）。
 * @param items 可见菜品列表。
 * @param viewportRequest 待执行的网格视口恢复请求。
 * @param focusRequest 待执行的网格焦点恢复请求。
 * @param modifier 外层 Modifier。
 * @param onViewportChanged 网格滚动位置变化回调。
 * @param onItemFocused 菜品获得焦点回调。
 * @param onItemConfirmed 菜品确认回调。
 */
@Composable
fun MenuItemGrid(
    selectedCategoryId: String,
    items: List<MenuItem>,
    viewportRequest: BrowseViewportRequest?,
    focusRequest: BrowseFocusRequest?,
    modifier: Modifier = Modifier,
    onViewportChanged: (Int, Int) -> Unit = { _, _ -> },
    onItemFocused: (String) -> Unit,
    onItemConfirmed: (String) -> Unit,
) {
    val proportions = LocalScreenProportions.current
    val gridState = rememberLazyGridState()

    // 分类切换或 FocusScene 返回后，按请求恢复 BrowseScene 网格位置。
    LaunchedEffect(viewportRequest?.requestId, selectedCategoryId) {
        viewportRequest?.let { request ->
            gridState.scrollToItem(
                index = request.firstVisibleItemIndex,
                scrollOffset = request.firstVisibleItemScrollOffset,
            )
        }
    }

    // 持续同步网格当前的首屏可见位置，供 ViewModel 记录分类级滚动锚点。
    LaunchedEffect(gridState, selectedCategoryId) {
        snapshotFlow {
            gridState.firstVisibleItemIndex to gridState.firstVisibleItemScrollOffset
        }.map { (firstVisibleItemIndex, firstVisibleItemScrollOffset) ->
            firstVisibleItemIndex to firstVisibleItemScrollOffset
        }.distinctUntilChanged()
            .filter { items.isNotEmpty() }
            .collect { (firstVisibleItemIndex, firstVisibleItemScrollOffset) ->
                onViewportChanged(firstVisibleItemIndex, firstVisibleItemScrollOffset)
            }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(GRID_COLUMNS),
        state = gridState,
        modifier = modifier
            .fillMaxSize()
            .testTag("menu-item-grid"),
        horizontalArrangement = Arrangement.spacedBy(proportions.browseGridHorizontalGap),
        verticalArrangement = Arrangement.spacedBy(proportions.browseGridVerticalGap),
    ) {
        itemsIndexed(items = items) { index, item ->
            val focusRequester = remember(item.itemId) { FocusRequester() }
            BrowseGridCard(
                item = item,
                itemIndex = index,
                isHighlighted = index == 0,
                testTag = "menu-item-$selectedCategoryId-$index",
                modifier = Modifier.focusRequester(focusRequester),
                focusRequest = focusRequest,
                focusRequester = focusRequester,
                onFocused = { onItemFocused(item.itemId) },
                onClick = { onItemConfirmed(item.itemId) },
            )
        }
    }
}

/**
 * 浏览页网格卡片组件。
 *
 * 设计稿结构：顶部图片区（180px）+ 底部文本区（name + description）。
 * 聚焦时升级边框/背景对比度；[isHighlighted] 首张卡片常态即带金色边框。
 *
 * @param item 菜品数据。
 * @param itemIndex 菜品在列表中的索引，用于本地图片 fallback 循环。
 * @param isHighlighted 是否为高亮卡片（第一张，设计稿 cardA1 样式）。
 * @param testTag UI 测试标签。
 * @param focusRequest 待执行的网格焦点恢复请求。
 * @param focusRequester 当前卡片 FocusRequester（焦点请求器）。
 * @param modifier 外层 Modifier。
 * @param onFocused 聚焦回调。
 * @param onClick 确认/点击回调。
 */
@Composable
private fun BrowseGridCard(
    item: MenuItem,
    itemIndex: Int,
    isHighlighted: Boolean,
    testTag: String,
    focusRequest: BrowseFocusRequest?,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier,
    onFocused: () -> Unit = {},
    onClick: () -> Unit = {},
) {
    val proportions = LocalScreenProportions.current
    var isFocused by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val shape = RoundedCornerShape(Dimens.BrowseGridCardCorner)

    // 边框：聚焦态 → 金色；高亮首卡 → 金色；其余 → 暗边框
    val borderColor = when {
        isFocused -> ColorTokens.GlassBorderFocused
        isHighlighted -> ColorTokens.GlassBorderFocused
        else -> ColorTokens.GlassBorderSubtle
    }
    // 背景：聚焦态 → 强调面；高亮首卡 → 强调面；其余 → 默认卡面
    val containerColor = when {
        isFocused -> ColorTokens.GlassSurfaceStrong
        isHighlighted -> ColorTokens.GlassSurfaceStrong
        else -> ColorTokens.GlassSurface
    }

    // 目标卡片在进入视口后主动请求焦点，驱动分类切入或 FocusScene 返回恢复。
    LaunchedEffect(focusRequest?.requestId) {
        if (focusRequest?.targetItemId == item.itemId) {
            focusRequester.requestFocus()
        }
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
        contentSpacing = proportions.browseGridCardContentSpacing,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(proportions.browseCardImageHeight)
                .clip(
                    RoundedCornerShape(
                        topStart = Dimens.BrowseGridCardCorner,
                        topEnd = Dimens.BrowseGridCardCorner,
                    ),
                ),
        ) {
            PocoAsyncImage(
                model = item.imageUrl.takeIf { it.isNotBlank() }
                    ?: LOCAL_FOOD_DRAWABLES[itemIndex % LOCAL_FOOD_DRAWABLES.size],
                contentDescription = item.name,
                modifier = Modifier.fillMaxSize(),
            )
        }
        // 文本区：菜品名 + 描述
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = proportions.browseGridCardBodyPaddingHorizontal,
                    end = proportions.browseGridCardBodyPaddingHorizontal,
                    bottom = proportions.browseGridCardBodyPaddingBottom,
                ),
            verticalArrangement = Arrangement.spacedBy(proportions.browseGridCardBodySpacing),
        ) {
            Text(
                text = item.name,
                color = ColorTokens.TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = proportions.scaledSp(24f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = item.description,
                color = ColorTokens.TextSecondary,
                fontSize = proportions.scaledSp(15f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
