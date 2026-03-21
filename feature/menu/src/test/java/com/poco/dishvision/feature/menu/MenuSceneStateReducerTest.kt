/**
 * @file MenuSceneStateReducerTest.kt
 * @author PopoY
 * @date 2026-03-20
 * @description 验证菜单场景状态（scene state）的纯函数规约，锁定 Browse 单事实源下的分类切换与恢复语义。
 */
package com.poco.dishvision.feature.menu

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * `MenuInteractionState` reducer（归约器）测试。
 */
class MenuSceneStateReducerTest {

    @Test
    fun `selecting a browse category resets the grid row anchor to the top`() {
        val categories = previewMenuCategories()
        val initialState = MenuInteractionState(
            selectedCategoryId = "hot-stir-fry",
            browseFocusedItemId = "hot-gizzard",
            categoryBrowseStates = mapOf(
                "hot-stir-fry" to CategoryBrowseState(
                    focusedItemId = "hot-gizzard",
                    rowIndex = 2,
                ),
            ),
        )

        val nextState = selectBrowseCategory(
            currentState = initialState,
            categories = categories,
            categoryId = "home-style",
        )
        val uiState = buildMenuUiState(
            categories = categories,
            interactionState = nextState,
        )

        assertEquals("home-style", uiState.selectedCategoryId)
        assertEquals("home-pickled-pork", uiState.browseSceneState.focusedItemId)
        assertEquals(0, uiState.browseSceneState.viewportRequest?.rowIndex)
        assertEquals(0, uiState.browseSceneState.viewportRequest?.firstVisibleItemIndex)
        assertEquals(0, uiState.browseSceneState.viewportRequest?.firstVisibleItemScrollOffset)
        assertNull(uiState.browseSceneState.focusRequest)
    }

    @Test
    fun `recording browse viewport stores row-level anchor`() {
        val categories = previewMenuCategories()
        val selectedState = selectBrowseCategory(
            currentState = MenuInteractionState(),
            categories = categories,
            categoryId = "hot-stir-fry",
        )
        val focusedState = recordBrowseItemFocus(
            currentState = selectedState,
            categories = categories,
            itemId = "hot-gizzard",
        )
        val nextState = recordBrowseViewport(
            currentState = focusedState,
            categories = categories,
            rowIndex = 2,
        )
        val uiState = buildMenuUiState(
            categories = categories,
            interactionState = nextState,
        )

        assertEquals("hot-stir-fry", uiState.selectedCategoryId)
        assertEquals("hot-gizzard", uiState.browseSceneState.focusedItemId)
        assertEquals(2, nextState.categoryBrowseStates["hot-stir-fry"]?.rowIndex)
        assertEquals(6, nextState.categoryBrowseStates["hot-stir-fry"]?.firstVisibleItemIndex)
        assertEquals(0, nextState.categoryBrowseStates["hot-stir-fry"]?.firstVisibleItemScrollOffset)
        // 仅记录滚动锚点时不应额外生成恢复请求，避免无意义 UI 副作用。
        assertNull(uiState.browseSceneState.viewportRequest)
    }

    @Test
    fun `browse focus restoration ignores category rail fallback while pending`() {
        val categories = previewMenuCategories()
        val selectedAndFocusedState = recordBrowseItemFocus(
            currentState = selectBrowseCategory(
                currentState = MenuInteractionState(),
                categories = categories,
                categoryId = "home-style",
            ),
            categories = categories,
            itemId = "home-preserved-egg-pepper",
        )
        val viewportState = recordBrowseViewport(
            currentState = selectedAndFocusedState,
            categories = categories,
            rowIndex = 3,
        )
        val restorationPendingState = requestBrowseItemFocus(
            currentState = viewportState,
            categories = categories,
            categoryId = "home-style",
        )

        val nextState = handleCategoryRailFocus(
            currentState = restorationPendingState,
            categories = categories,
            categoryId = "hot-stir-fry",
        )

        assertEquals("home-style", nextState.selectedCategoryId)
        assertEquals("home-preserved-egg-pepper", nextState.pendingFocusRequest?.targetItemId)
        assertEquals(3, nextState.categoryBrowseStates["home-style"]?.rowIndex)
    }

    @Test
    fun `browse viewport request resolves index and offset from row index`() {
        val request = BrowseViewportRequest(
            requestId = 7L,
            rowIndex = 4,
        )

        assertEquals(4, request.rowIndex)
        assertEquals(12, request.firstVisibleItemIndex)
        assertEquals(0, request.firstVisibleItemScrollOffset)
    }
}
