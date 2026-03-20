/**
 * @file MenuPreviewCatalogContractTest.kt
 * @author PopoY
 * @date 2026-03-20
 * @description 锁定菜单 preview 数据与 Browse UI 约定，避免测试入口继续偏离真实菜单。
 */
package com.poco.dishvision.feature.menu

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * `previewMenuCategories` 契约测试。
 */
class MenuPreviewCatalogContractTest {

    @Test
    fun `preview categories keep menu document order and counts`() {
        val categories = buildMenuUiState(
            categories = previewMenuCategories(),
            interactionState = MenuInteractionState(),
        ).categories

        assertEquals(
            listOf(
                "招牌热炒",
                "香辣口味",
                "鱼鲜大菜",
                "家常土菜",
                "风味小菜",
            ),
            categories.map { category -> category.displayName },
        )
        assertEquals(44, categories.sumOf { category -> category.items.size })
        assertEquals(
            listOf(9, 9, 7, 10, 9),
            categories.map { category -> category.items.size },
        )
    }
}
