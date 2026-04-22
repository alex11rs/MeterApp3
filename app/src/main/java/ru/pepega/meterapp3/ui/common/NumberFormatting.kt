package ru.pepega.meterapp3.ui.common

import java.math.BigDecimal
import java.math.RoundingMode

fun formatPlainNumber(value: Float, maxFractionDigits: Int = 2): String {
    if (!value.isFinite()) return "0"

    return BigDecimal.valueOf(value.toDouble())
        .setScale(maxFractionDigits, RoundingMode.HALF_UP)
        .stripTrailingZeros()
        .toPlainString()
}
