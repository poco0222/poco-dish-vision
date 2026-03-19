/**
 * @file HomeUiState.kt
 * @author PopoY
 * @date 2026-03-19
 * @description 定义首页 attract mode（吸引模式）专用展示模型。
 */
package com.poco.dishvision.feature.home

/**
 * 首页 UI 状态。
 *
 * @property brandName 品牌名。
 * @property brandSubtitle 品牌副标题。
 * @property seasonBadgeText 右上角季节标签。
 * @property categoryChips 首屏标签组。
 * @property showcaseItems 首屏 5 张展示卡。
 * @property autoAdvanceEnabled 是否开启自动轮播。
 * @property autoAdvanceIntervalMs 自动轮播间隔。
 * @property autoResumeAfterInteractionMs 手动交互后的自动恢复时间。
 */
data class HomeUiState(
    val brandName: String,
    val brandSubtitle: String,
    val seasonBadgeText: String,
    val categoryChips: List<String>,
    val showcaseItems: List<HomeShowcaseItem>,
    val autoAdvanceEnabled: Boolean,
    val autoAdvanceIntervalMs: Long = 5_000L,
    val autoResumeAfterInteractionMs: Long = 10_000L,
)
