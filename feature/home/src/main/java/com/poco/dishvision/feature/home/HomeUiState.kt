/**
 * @file HomeUiState.kt
 * @author PopoY
 * @date 2026-03-18
 * @description 定义首页 attract mode（吸引模式）的 UI 状态。
 */
package com.poco.dishvision.feature.home

import com.poco.dishvision.core.model.menu.MenuItem

/**
 * 首页 UI 状态。
 *
 * @property heroTitle 主标题。
 * @property heroSubtitle 主副标题。
 * @property featuredItems 推荐菜品。
 * @property autoAdvanceEnabled 是否开启自动轮播。
 */
data class HomeUiState(
    val heroTitle: String,
    val heroSubtitle: String,
    val featuredItems: List<MenuItem>,
    val autoAdvanceEnabled: Boolean,
)
