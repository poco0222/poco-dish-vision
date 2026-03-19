/**
 * @file SettingsUiState.kt
 * @author PopoY
 * @date 2026-03-19
 * @description 定义设置页（Settings Screen）的 UI 状态。
 */
package com.poco.dishvision.feature.settings

/**
 * 设置页状态。
 *
 * @property sourceModeLabel 当前数据源模式。
 * @property lastRefreshAt 最近刷新时间。
 * @property catalogVersion 当前目录 schema 版本。
 */
data class SettingsUiState(
    val sourceModeLabel: String,
    val lastRefreshAt: String?,
    val catalogVersion: String?,
)
