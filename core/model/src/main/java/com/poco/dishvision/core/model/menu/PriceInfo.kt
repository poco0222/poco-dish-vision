/**
 * @file PriceInfo.kt
 * @author PopoY
 * @date 2026-03-18
 * @description 定义价格信息（Price Info）领域模型。
 */
package com.poco.dishvision.core.model.menu

import kotlinx.serialization.Serializable

/**
 * 价格信息（Price Info），采用最小货币单位（minor unit）避免浮点误差。
 *
 * @property currencyCode ISO-4217 货币代码，例如 CNY。
 * @property amountMinor 当前价格，单位为分（cent）。
 * @property originalAmountMinor 原价，若无折扣则可与 amountMinor 相同。
 * @property unitLabel 计量单位文案，例如 份/杯。
 */
@Serializable
data class PriceInfo(
    val currencyCode: String,
    val amountMinor: Int,
    val originalAmountMinor: Int,
    val unitLabel: String,
)
