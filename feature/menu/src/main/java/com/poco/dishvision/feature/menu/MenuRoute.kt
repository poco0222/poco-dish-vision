/**
 * @file MenuRoute.kt
 * @author PopoY
 * @date 2026-03-19
 * @description 浏览页 Route，负责桥接 ViewModel 与 Browse mode UI。
 */
package com.poco.dishvision.feature.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.tv.material3.Text
import com.poco.dishvision.core.data.repository.MenuRepository

/**
 * 浏览页入口 Route。未注入仓储时使用 preview 数据，以便先完成 UI 与交互测试。
 *
 * @param menuRepository 菜单仓储；为空时进入 preview 模式。
 * @param modifier 外层 Modifier。
 */
@Composable
fun MenuRoute(
    menuRepository: MenuRepository? = null,
    onUserInteraction: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    if (menuRepository == null) {
        PreviewMenuRoute(
            modifier = modifier,
            onUserInteraction = onUserInteraction,
        )
        return
    }

    val menuViewModel: MenuViewModel = viewModel(
        factory = MenuViewModel.provideFactory(menuRepository),
    )
    val uiState by menuViewModel.uiState.collectAsStateWithLifecycle()

    MenuScreen(
        uiState = uiState,
        modifier = modifier,
        onUserInteraction = onUserInteraction,
        onCategorySelected = menuViewModel::onCategorySelected,
        onItemFocused = menuViewModel::onItemFocused,
        onItemConfirmed = { menuViewModel.onFocusedItemConfirmed() },
        onDismissDetail = menuViewModel::dismissDetailPanel,
    )
}

/**
 * Preview 模式下的浏览页 Route，直接在 Compose 内维护本地状态。
 *
 * @param modifier 外层 Modifier。
 */
@Composable
private fun PreviewMenuRoute(
    onUserInteraction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val categories = remember { previewMenuCategories() }
    var selectedCategoryId by rememberSaveable { mutableStateOf<String?>(null) }
    var focusedItemId by rememberSaveable { mutableStateOf<String?>(null) }
    var isDetailPanelVisible by rememberSaveable { mutableStateOf(false) }

    val uiState = remember(
        categories,
        selectedCategoryId,
        focusedItemId,
        isDetailPanelVisible,
    ) {
        buildMenuUiState(
            categories = categories,
            selectedCategoryId = selectedCategoryId,
            focusedItemId = focusedItemId,
            isDetailPanelVisible = isDetailPanelVisible,
        )
    }

    MenuScreen(
        uiState = uiState,
        modifier = modifier,
        onUserInteraction = onUserInteraction,
        onCategorySelected = { categoryId ->
            selectedCategoryId = categoryId
            focusedItemId = categories
                .firstOrNull { category -> category.categoryId == categoryId }
                ?.items
                ?.firstOrNull()
                ?.itemId
            isDetailPanelVisible = false
        },
        onItemFocused = { itemId ->
            focusedItemId = itemId
        },
        onItemConfirmed = {
            if (uiState.focusedItemId != null) {
                isDetailPanelVisible = true
            }
        },
        onDismissDetail = {
            isDetailPanelVisible = false
        },
    )
}

/**
 * 浏览页主界面，布局为左侧分类导轨 + 右侧菜品行 + 底部 detail dock。
 *
 * @param uiState 浏览页 UI 状态。
 * @param modifier 外层 Modifier。
 * @param onCategorySelected 分类变更回调。
 * @param onItemFocused 菜品聚焦回调。
 * @param onItemConfirmed 菜品确认回调。
 * @param onDismissDetail 关闭详情回调。
 */
@Composable
private fun MenuScreen(
    uiState: MenuUiState,
    modifier: Modifier = Modifier,
    onUserInteraction: () -> Unit,
    onCategorySelected: (String) -> Unit,
    onItemFocused: (String) -> Unit,
    onItemConfirmed: () -> Unit,
    onDismissDetail: () -> Unit,
) {
    val focusedItem = remember(uiState.categories, uiState.selectedCategoryId, uiState.focusedItemId) {
        resolveFocusedItem(
            categories = uiState.categories,
            selectedCategoryId = uiState.selectedCategoryId,
            focusedItemId = uiState.focusedItemId,
        )
    }
    val firstItemFocusRequester = remember(uiState.selectedCategoryId) { FocusRequester() }
    var pendingFocusTransferCategoryId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(
        pendingFocusTransferCategoryId,
        uiState.selectedCategoryId,
        uiState.visibleItems.size,
    ) {
        if (
            pendingFocusTransferCategoryId != null &&
            pendingFocusTransferCategoryId == uiState.selectedCategoryId &&
            uiState.visibleItems.isNotEmpty()
        ) {
            firstItemFocusRequester.requestFocus()
            pendingFocusTransferCategoryId = null
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onPreviewKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown && keyEvent.key.isBrowseInteractionKey()) {
                    onUserInteraction()
                }
                false
            }
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F1723),
                        Color(0xFF162336),
                        Color(0xFF0B1018),
                    ),
                ),
            )
            .testTag("browse-screen"),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 40.dp, end = 40.dp, top = 28.dp, bottom = 216.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Text(
                text = "POCO Dish Vision",
                color = Color(0xFFF4F7FB),
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = "按方向键切换分类，按确认键展开详情",
                color = Color(0xCCE4EAF4),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                CategoryRail(
                    categories = uiState.categories,
                    selectedCategoryId = uiState.selectedCategoryId,
                    onCategoryFocused = onCategorySelected,
                    onMoveFocusToItems = { categoryId ->
                        onCategorySelected(categoryId)
                        pendingFocusTransferCategoryId = categoryId
                    },
                )
                MenuItemRow(
                    selectedCategoryId = uiState.selectedCategoryId,
                    items = uiState.visibleItems,
                    firstItemFocusRequester = firstItemFocusRequester,
                    modifier = Modifier.weight(1f),
                    onItemFocused = onItemFocused,
                    onItemConfirmed = { onItemConfirmed() },
                )
            }
        }

        ItemDetailPanel(
            item = focusedItem,
            isExpanded = uiState.isDetailPanelVisible,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 40.dp, vertical = 28.dp),
            onDismissRequest = onDismissDetail,
        )
    }
}

/**
 * 定义 Browse mode 下会重置 idle timeout 的按键集合。
 *
 * 这里覆盖方向键、确认键与返回键，保证遥控器常用操作都能刷新活跃态。
 */
private fun Key.isBrowseInteractionKey(): Boolean {
    return this == Key.DirectionUp ||
        this == Key.DirectionDown ||
        this == Key.DirectionLeft ||
        this == Key.DirectionRight ||
        this == Key.DirectionCenter ||
        this == Key.Enter ||
        this == Key.NumPadEnter ||
        this == Key.Back
}
