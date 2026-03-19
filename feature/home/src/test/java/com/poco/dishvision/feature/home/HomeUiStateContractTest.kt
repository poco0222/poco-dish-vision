/**
 * @file HomeUiStateContractTest.kt
 * @author PopoY
 * @date 2026-03-19
 * @description 锁定首页展示模型（presentation model）的固定契约。
 */
package com.poco.dishvision.feature.home

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 首页 preview 状态契约测试。
 */
class HomeUiStateContractTest {

    @Test
    fun `preview state exposes xinhua brand chips and five showcase items`() {
        val uiState = previewHomeUiState()

        assertEquals("新华饭店", uiState.brandName)
        assertEquals("剁椒热炒 · 腊味土菜 · 今日现炒", uiState.brandSubtitle)
        assertEquals("老牌湘菜 · 热锅上桌", uiState.seasonBadgeText)
        assertEquals(
            listOf("热锅现炒", "剁椒头牌", "腊味土菜"),
            uiState.categoryChips,
        )
        assertEquals(5, uiState.showcaseItems.size)
        assertTrue(uiState.autoAdvanceEnabled)
        assertEquals(5_000L, uiState.autoAdvanceIntervalMs)
        assertEquals(10_000L, uiState.autoResumeAfterInteractionMs)
        assertEquals("茶油炒鸡", uiState.showcaseItems.first().heroTitlePrimary)
        assertEquals("鲜辣上桌", uiState.showcaseItems.first().heroTitleSecondary)
    }
}
