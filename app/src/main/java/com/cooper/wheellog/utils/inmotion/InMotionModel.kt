package com.cooper.wheellog.utils.inmotion

import timber.log.Timber

enum class InMotionModel(val value: Int, val wheelName: String) {
    V11(6, "Inmotion V11"),
    V12(7, "Inmotion V12"),
    V13(8, "Inmotion V13"),
    UNKNOWN(
        0,
        "Inmotion Unknown",
    ), ;

    companion object {
        fun findById(id: Int): InMotionModel {
            Timber.i("Model %d", id)
            for (m in values()) {
                if (m.value == id) return m
            }
            return UNKNOWN
        }
    }
}
