/**
 * @file HomeShowcaseItem.kt
 * @author PopoY
 * @date 2026-03-19
 * @description 定义首页主视觉与底部推荐卡共用的展示条目。
 */
package com.poco.dishvision.feature.home

/**
 * 首页单个展示条目。
 *
 * @property heroEyebrow 主视觉眉文。
 * @property heroTitlePrimary 主标题第一行。
 * @property heroTitleSecondary 主标题第二行。
 * @property heroDescription 主视觉描述。
 * @property cardTitle 底部卡片标题。
 * @property cardPriceLabel 底部卡片价格文案。
 * @property cardDescription 底部卡片说明。
 * @property cardPrompt 底部卡片提示语。
 * @property heroImageRes 主视觉本地图片资源。
 */
data class HomeShowcaseItem(
    val heroEyebrow: String,
    val heroTitlePrimary: String,
    val heroTitleSecondary: String,
    val heroDescription: String,
    val cardTitle: String,
    val cardPriceLabel: String,
    val cardDescription: String,
    val cardPrompt: String,
    val heroImageRes: Int,
)
