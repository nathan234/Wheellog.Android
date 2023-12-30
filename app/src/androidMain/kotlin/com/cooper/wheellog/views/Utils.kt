package com.cooper.wheellog.views

import android.content.Context
import android.util.TypedValue

object Utils {
    fun dpToPx(context: Context, dp: Int): Int {
        return Math.round(
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp.toFloat(),
                context.resources.displayMetrics
            )
        )
    }
}