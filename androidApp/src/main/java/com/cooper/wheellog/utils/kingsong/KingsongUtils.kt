package com.cooper.wheellog.utils.kingsong

import com.cooper.wheellog.wheeldata.WheelData
import com.cooper.wheellog.utils.StringUtil.inArray

object KingsongUtils {
    @JvmStatic
    fun is84vWheel(wd: WheelData): Boolean {
        return (
            inArray(
                wd.model,
                arrayOf("KS-18L", "KS-16X", "KS-16XF", "RW", "KS-18LH", "KS-18LY", "KS-S18"),
            ) || wd.name.startsWith("ROCKW") || wd.btName?.compareTo("RW") == 0
            )
    }

    @JvmStatic
    fun is126vWheel(wd: WheelData): Boolean {
        return inArray(wd.model, arrayOf("KS-S20", "KS-S22"))
    }

    @JvmStatic
    fun is100vWheel(wd: WheelData): Boolean {
        return inArray(wd.model, arrayOf("KS-S19"))
    }

    @JvmStatic
    fun getEmptyRequest(): ByteArray {
        return byteArrayOf(
            0xAA.toByte(),
            0x55,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x14,
            0x5A,
            0x5A,
        )
    }
}
