package ru.pepega.meterapp3

import androidx.compose.ui.graphics.Color

fun getColorByName(colorName: String): Color {
    return availableColors.find { it.first == colorName }?.second ?: Color(0xFF2196F3)
}