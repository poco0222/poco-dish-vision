/**
 * @file AvailabilityWindow.kt
 * @author PopoY
 * @date 2026-03-18
 * @description 定义可售时段（Availability Window）领域模型。
 */
package com.poco.dishvision.core.model.menu

import kotlinx.serialization.Serializable

/**
 * 菜品可售时段（Availability Window）。
 *
 * @property daysOfWeek 生效星期列表，使用英文全大写（MONDAY...SUNDAY）。
 * @property startTime 开始时间（24h，HH:mm）。
 * @property endTime 结束时间（24h，HH:mm）。
 */
@Serializable
data class AvailabilityWindow(
    val daysOfWeek: List<String>,
    val startTime: String,
    val endTime: String,
)
