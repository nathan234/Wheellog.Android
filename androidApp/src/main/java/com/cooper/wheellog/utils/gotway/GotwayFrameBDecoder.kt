package com.cooper.wheellog.utils.gotway

import com.cooper.wheellog.AppConfig
import com.cooper.wheellog.WheelData
import com.cooper.wheellog.utils.MathsUtil
import kotlin.math.roundToLong

/**
 * Decoding of frame B for Gotway wheels
 * Used for total distance and flags
 */
class GotwayFrameBDecoder(private val wd: WheelData, private val appConfig: AppConfig) {
    fun decode(buff: ByteArray, useRatio: Boolean, lock: Int, fw: String): AlertResult {
        var _lock = lock
        val totalDistance = MathsUtil.getInt4(buff, 2).toInt()
        if (useRatio) {
            wd.totalDistance = (totalDistance * GotwayAdapter.RATIO_GW).roundToLong()
        } else {
            wd.totalDistance = totalDistance.toLong()
        }
        val settings = MathsUtil.shortFromBytesBE(buff, 6)
        val pedalsMode = settings shr 13 and 0x03
        val speedAlarms = settings shr 10 and 0x03
        val rollAngle = settings shr 7 and 0x03
        val inMiles = settings and 0x01
        val powerOffTime = MathsUtil.shortFromBytesBE(buff, 8)
        var tiltBackSpeed = MathsUtil.shortFromBytesBE(buff, 10)
        if (tiltBackSpeed >= 100) tiltBackSpeed = 0
        val alert = buff[12].toInt() and 0xFF
        val ledMode = buff[13].toInt() and 0xFF
        val lightMode = buff[15].toInt() and 0x03
        if (_lock == 0) {
            appConfig.pedalsMode = (2 - pedalsMode).toString()
            appConfig.alarmMode = speedAlarms.toString() // CheckMe
            appConfig.lightMode = lightMode.toString()
            appConfig.ledMode = ledMode.toString()
            if (fw != "-") {
                appConfig.gwInMiles = inMiles == 1
                appConfig.wheelMaxSpeed = tiltBackSpeed
                appConfig.rollAngle = rollAngle.toString()
            }
        } else {
            _lock -= 1
        }
        return AlertResult(alert, _lock)
    }

    class AlertResult(@JvmField var alert: Int, @JvmField var lock: Int)
}
