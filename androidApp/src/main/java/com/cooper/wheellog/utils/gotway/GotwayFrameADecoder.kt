package com.cooper.wheellog.utils.gotway

import com.cooper.wheellog.WheelData
import com.cooper.wheellog.utils.MathsUtil
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Decoding of frame A for Gotway wheels
 * Used for various real time data points, such as speed,
 * distance, current, temperature, voltage, battery, etc
 */
class GotwayFrameADecoder(
    private val wd: WheelData,
    private val gotwayScaledVoltageCalculator: GotwayScaledVoltageCalculator,
    private val gotwayBatteryCalculator: GotwayBatteryCalculator,
) {
    fun decode(
        buff: ByteArray,
        useRatio: Boolean,
        useBetterPercents: Boolean?,
        gotwayNegative: Int,
    ) {
        var voltage = MathsUtil.shortFromBytesBE(buff, 2)
        var speed = (MathsUtil.signedShortFromBytesBE(buff, 4) * 3.6).roundToInt()
        var distance = MathsUtil.shortFromBytesBE(buff, 8)
        var phaseCurrent = MathsUtil.signedShortFromBytesBE(buff, 10)
        val temperature =
            (
                (
                    MathsUtil.signedShortFromBytesBE(buff, 12)
                        .toFloat() / 340.0 + 36.53
                    ) * 100
                ).roundToInt()
                .toInt() // mpu6050
        // int temperature = (int) Math.round((((float) MathsUtil.signedShortFromBytesBE(buff, 12) / 333.87) + 21.00) * 100); // mpu6500
        var hwPwm = MathsUtil.signedShortFromBytesBE(buff, 14) * 10
        if (gotwayNegative == 0) {
            speed = abs(speed)
            phaseCurrent = abs(phaseCurrent)
            hwPwm = abs(hwPwm)
        } else {
            speed *= gotwayNegative
            phaseCurrent *= gotwayNegative
            hwPwm *= gotwayNegative
        }
        val battery = gotwayBatteryCalculator.getBattery(useBetterPercents!!, voltage)
        if (useRatio) {
            distance = (distance * GotwayAdapter.RATIO_GW).roundToInt()
            speed = (speed * GotwayAdapter.RATIO_GW).roundToInt()
        }
        voltage =
            Math.round(gotwayScaledVoltageCalculator.getScaledVoltage(voltage.toDouble())).toInt()
        wd.speed = speed
        wd.topSpeed = speed
        wd.wheelDistance = (distance.toLong())
        wd.temperature = temperature
        wd.phaseCurrent = phaseCurrent
        wd.voltage = voltage
        wd.voltageSag = voltage
        wd.batteryLevel = battery
        wd.updateRideTime()
        wd.output = hwPwm
    }
}
