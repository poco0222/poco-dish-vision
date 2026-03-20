/**
 * @file MenuSceneStateReducerTest.kt
 * @author PopoY
 * @date 2026-03-20
 * @description 验证菜单场景状态（scene state）的纯函数规约，锁定分类切换与 FocusScene 返回恢复行为。
 */
package com.poco.dishvision.feature.menu

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * `MenuInteractionState` reducer（归约器）测试。
 */
class MenuSceneStateReducerTest {

    @Test
    fun `selecting a browse category resets the grid viewport to the top`() {
        val categories = previewMenuCategories()
        val initialState = MenuInteractionState(
            selectedCategoryId = "hot-stir-fry",
            browseFocusedItemId = "hot-gizzard",
            categoryBrowseStates = mapOf(
                "hot-stir-fry" to CategoryBrowseState(
                    focusedItemId = "hot-gizzard",
                    firstVisibleItemIndex = 6,
                    firstVisibleItemScrollOffset = 24,
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

        assertEquals(MenuScene.Browse, uiState.scene)
        assertEquals("home-style", uiState.selectedCategoryId)
        assertEquals("home-pickled-pork", uiState.browseSceneState.focusedItemId)
        assertEquals(0, uiState.browseSceneState.viewportRequest?.firstVisibleItemIndex)
        assertEquals(0, uiState.browseSceneState.viewportRequest?.firstVisibleItemScrollOffset)
        assertNull(uiState.browseSceneState.focusRequest)
    }

    @Test
    fun `dismissing focus scene restores the saved browse viewport and focus item`() {
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
        val viewportState = recordBrowseViewport(
            currentState = focusedState,
            categories = categories,
            firstVisibleItemIndex = 6,
            firstVisibleItemScrollOffset = 24,
        )
        val focusSceneState = enterFocusScene(
            currentState = viewportState,
            categories = categories,
        )

        val nextState = exitFocusScene(
            currentState = focusSceneState,
            categories = categories,
        )
        val uiState = buildMenuUiState(
            categories = categories,
            interactionState = nextState,
        )

        assertEquals(MenuScene.Browse, uiState.scene)
        assertEquals("hot-stir-fry", uiState.selectedCategoryId)
        assertEquals("hot-gizzard", uiState.browseSceneState.focusedItemId)
        assertEquals(6, uiState.browseSceneState.viewportRequest?.firstVisibleItemIndex)
        assertEquals(24, uiState.browseSceneState.viewportRequest?.firstVisibleItemScrollOffset)
        assertEquals("hot-gizzard", uiState.browseSceneState.focusRequest?.targetItemId)
        assertEquals(7, uiState.browseSceneState.focusRequest?.targetItemIndex)
        assertNotNull(uiState.browseSceneState.focusRequest?.requestId)
    }

    @Test
    fun `focus scene navigation keeps the browse anchor for back restoration`() {
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
        val viewportState = recordBrowseViewport(
            currentState = focusedState,
            categories = categories,
            firstVisibleItemIndex = 6,
            firstVisibleItemScrollOffset = 24,
        )
        val focusSceneState = enterFocusScene(
            currentState = viewportState,
            categories = categories,
        )
        val movedFocusSceneState = recordFocusSceneItemFocus(
            currentState = focusSceneState,
            categories = categories,
            itemId = "hot-beef",
        )

        val nextState = exitFocusScene(
            currentState = movedFocusSceneState,
            categories = categories,
        )
        val uiState = buildMenuUiState(
            categories = categories,
            interactionState = nextState,
        )

        assertEquals("hot-gizzard", uiState.browseSceneState.focusedItemId)
        assertEquals("hot-gizzard", uiState.browseSceneState.focusRequest?.targetItemId)
        assertEquals(7, uiState.browseSceneState.focusRequest?.targetItemIndex)
    }

    @Test
    fun `category rail fallback focus does not override pending browse restoration`() {
        val categories = previewMenuCategories()
        val restoredBrowseState = exitFocusScene(
            currentState = enterFocusScene(
                currentState = recordBrowseViewport(
                    currentState = recordBrowseItemFocus(
                        currentState = selectBrowseCategory(
                            currentState = MenuInteractionState(),
                            categories = categories,
                            categoryId = "home-style",
                        ),
                        categories = categories,
                        itemId = "home-preserved-egg-pepper",
                    ),
                    categories = categories,
                    firstVisibleItemIndex = 9,
                    firstVisibleItemScrollOffset = 0,
                ),
                categories = categories,
            ),
            categories = categories,
        )

        val nextState = handleCategoryRailFocus(
            currentState = restoredBrowseState,
            categories = categories,
            categoryId = "hot-stir-fry",
        )

        assertEquals("home-style", nextState.selectedCategoryId)
        assertEquals("home-preserved-egg-pepper", nextState.pendingFocusRequest?.targetItemId)
        assertEquals(9, nextState.pendingViewportRequest?.firstVisibleItemIndex)
    }
}
