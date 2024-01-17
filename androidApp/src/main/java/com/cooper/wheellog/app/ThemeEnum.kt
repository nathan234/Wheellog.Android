package com.cooper.wheellog.app

import com.cooper.wheellog.R

enum class ThemeEnum(val value: Int) {
    Original(0),
    AJDM(1);

    fun toStyle() = when (this) {
        Original -> R.style.OriginalTheme
        AJDM -> R.style.AJDMTheme
    }

    companion object {
        fun fromInt(value: Int) = values().first { it.value == value }
    }
}