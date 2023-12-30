package com.cooper.wheellog.ui.theme

import androidx.compose.ui.graphics.Color

fun com.wheellog.shared.ui.Color.toColor(): Color {
    return Color(convertHexToLong(this.hex))
}

private fun convertHexToLong(hex: String): Long {
    val numericValue =
        hex.drop(1) // drop the leading #
            .fold(0L) { acc, char -> acc * 16 + char.digitToInt(16) }
    return (0xFF shl 24).toLong() or numericValue
}
