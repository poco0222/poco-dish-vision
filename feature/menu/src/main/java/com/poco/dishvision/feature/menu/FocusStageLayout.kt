/**
 * @file FocusStageLayout.kt
 * @author PopoY
 * @date 2026-03-20
 * @description 聚焦舞台主布局组件，匹配设计稿 npcH9 中的 focusStage 区域。
 *
 * 使用 Box + offset 绝对定位（与 HomeRoute 相同模式），
 * 中央 FocusMidCard（纯展示） + 周围 FocusSmallCard × N（可聚焦，D-pad 导航）。
 *
 * 设计稿参数：容器 w=1544, h=794；中央卡 x=432,y=24；
 * 周围 8 个卡槽的绝对坐标见 ScreenProportions.focusSlotPositions。
 */
package com.poco.dishvision.feature.menu

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.testTag
import com.poco.dishvision.core.ui.theme.LocalScreenProportions

/**
 * 聚焦舞台中固定卡槽 ID 列表，顺序与 [ScreenProportions.focusSlotPositions] 对应。
 *
 * 布局：
 * - Row1: A1(左上), [focusMid居中], A3(右上)
 * - Row2: B1(左中),                   B3(右中)
 * - Row3: C1(左下), C2(中下), C3(中右下), A2(右下)
 */
val SLOT_IDS = listOf("A1", "A3", "B1", "B3", "C1", "C2", "C3", "A2")

/**
 * D-pad 导航映射表，基于设计稿卡槽的物理位置定义四方向跳转关系。
 * Key = 源卡槽 ID, Value = 各方向目标卡槽 ID（null 表示该方向不可跳转）。
 */
private data class NavLinks(
    val up: String? = null,
    val down: String? = null,
    val left: String? = null,
    val right: String? = null,
)

/**
 * 设计稿各行小卡默认透明度（非聚焦态）：
 * Row1(A1,A3)=0.94, Row2(B1,B3)=0.92, Row3(C1,C2,C3,A2)=0.90。
 */
private val SLOT_ROW_ALPHA = mapOf(
    "A1" to 0.94f, "A3" to 0.94f,
    "B1" to 0.92f, "B3" to 0.92f,
    "C1" to 0.90f, "C2" to 0.90f, "C3" to 0.90f, "A2" to 0.90f,
)

/** 完整导航表（8 个卡槽） */
private val FULL_NAV_MAP = mapOf(
    "A1" to NavLinks(right = "A3", down = "B1"),
    "A3" to NavLinks(left = "A1", down = "B3"),
    "B1" to NavLinks(up = "A1", right = "B3", down = "C1"),
    "B3" to NavLinks(up = "A3", left = "B1", down = "A2"),
    "C1" to NavLinks(up = "B1", right = "C2"),
    "C2" to NavLinks(left = "C1", right = "C3", up = "B1"),
    "C3" to NavLinks(left = "C2", right = "A2", up = "B3"),
    "A2" to NavLinks(left = "C3", up = "B3"),
)

/**
 * 聚焦舞台主布局。
 *
 * 用户确认菜品后从 Grid 切换到此布局；按 Back 键返回 Grid。
 * 中央大卡展示当前聚焦菜品详情，周围小卡可通过 D-pad 导航切换焦点。
 *
 * @param stageState FocusScene 状态（中央菜品 + 周围卡槽列表）。
 * @param selectedCategoryName 当前分类名称，传递给 FocusMidCard 的 eyebrow 标签。
 * @param modifier 外层 Modifier。
 * @param onSmallCardFocused 小卡获焦回调，参数为菜品 itemId。
 * @param onDismiss 返回 Grid 浏览态回调（Back 键触发）。
 */
@Composable
fun FocusStageLayout(
    stageState: FocusSceneState,
    selectedCategoryName: String,
    modifier: Modifier = Modifier,
    onSmallCardFocused: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val proportions = LocalScreenProportions.current
    val slots = stageState.surroundingSlots

    // 为每个卡槽分配 FocusRequester
    val focusRequesters = remember(slots.size) {
        List(slots.size) { FocusRequester() }
    }

    // 构建 slotId → FocusRequester 映射
    val slotFocusMap = remember(slots, focusRequesters) {
        slots.mapIndexed { index, slot -> slot.slotId to focusRequesters[index] }.toMap()
    }

    // 构建动态导航表（仅包含实际存在的卡槽）
    val activeSlotIds = remember(slots) { slots.map { it.slotId }.toSet() }
    val navMap = remember(activeSlotIds) {
        buildDynamicNavMap(activeSlotIds)
    }

    // 进入 FocusStage 时自动聚焦第一张小卡
    LaunchedEffect(Unit) {
        if (focusRequesters.isNotEmpty()) {
            focusRequesters[0].requestFocus()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("focus-scene"),
    ) {
        // ── 中央大卡（纯展示，不可聚焦），内容通过 Crossfade 平滑切换 ──
        Crossfade(
            targetState = stageState.focusedItem.itemId,
            animationSpec = tween(durationMillis = 250),
            label = "focus-mid-crossfade",
            modifier = Modifier
                .offset(x = proportions.focusMidX, y = proportions.focusMidY)
                .width(proportions.focusMidWidth),
        ) { _ ->
            // 查找 focusedItem 在 visibleItems 中的全局索引，用于图片 fallback
            val focusedIndex = slots.indexOfFirst { it.item.itemId == stageState.focusedItem.itemId }
                .takeIf { it >= 0 } ?: 0

            FocusMidCard(
                item = stageState.focusedItem,
                categoryName = selectedCategoryName,
                itemIndex = focusedIndex,
            )
        }

        // ── 周围小卡（可聚焦，D-pad 导航） ──
        slots.forEachIndexed { index, slot ->
            // 获取该卡槽在 SLOT_IDS 中的设计位置索引
            val slotIndex = SLOT_IDS.indexOf(slot.slotId).coerceAtLeast(0)
            val (offsetX, offsetY) = proportions.focusSlotPositions[slotIndex]

            FocusSmallCard(
                item = slot.item,
                itemIndex = index,
                baseAlpha = SLOT_ROW_ALPHA[slot.slotId] ?: 0.92f,
                focusRequester = focusRequesters[index],
                modifier = Modifier
                    .offset(x = offsetX, y = offsetY)
                    .width(proportions.focusSmallCardWidth)
                    // D-pad 方向键手动路由
                    .onPreviewKeyEvent { event ->
                        if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false

                        val links = navMap[slot.slotId] ?: return@onPreviewKeyEvent false
                        val targetSlotId = when (event.key) {
                            Key.DirectionUp -> links.up
                            Key.DirectionDown -> links.down
                            Key.DirectionLeft -> links.left
                            Key.DirectionRight -> links.right
                            else -> null
                        }

                        if (targetSlotId != null) {
                            slotFocusMap[targetSlotId]?.requestFocus()
                            true
                        } else {
                            false
                        }
                    },
                onFocused = { onSmallCardFocused(slot.item.itemId) },
            )
        }
    }
}

/**
 * 根据实际存在的卡槽动态生成导航映射表。
 *
 * 当菜品数量不足 8 个时，部分卡槽缺失，需要过滤掉指向不存在卡槽的导航链接。
 *
 * @param activeSlotIds 实际存在的卡槽 ID 集合。
 * @return 过滤后的导航映射表。
 */
private fun buildDynamicNavMap(activeSlotIds: Set<String>): Map<String, NavLinks> {
    return FULL_NAV_MAP
        .filterKeys { it in activeSlotIds }
        .mapValues { (_, links) ->
            NavLinks(
                up = links.up?.takeIf { it in activeSlotIds },
                down = links.down?.takeIf { it in activeSlotIds },
                left = links.left?.takeIf { it in activeSlotIds },
                right = links.right?.takeIf { it in activeSlotIds },
            )
        }
}
