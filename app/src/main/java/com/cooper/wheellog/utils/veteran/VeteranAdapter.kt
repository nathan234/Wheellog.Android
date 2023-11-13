package com.cooper.wheellog.utils.veteran

import com.cooper.wheellog.AppConfig
import com.cooper.wheellog.WheelData
import com.cooper.wheellog.WheelLog
import com.cooper.wheellog.utils.BaseAdapter
import com.cooper.wheellog.utils.MathsUtil
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.util.Locale

/**
 * Adapter for Veteran wheels.
 * Responsible for decoding data from the wheel, and sending commands to the wheel.
 */
class VeteranAdapter(
    private val batteryCalculator: VeteranBatteryCalculator,
    private val unpacker: VeteranUnpacker,
    private val wd: WheelData,
    private val appConfig: AppConfig,
) : BaseAdapter() {
    private var time_old: Long = 0
    private var mVer = 0
    override fun decode(data: ByteArray?): Boolean {
        Timber.i("Decode Veteran")
        wd.resetRideTime()
        val time_new = System.currentTimeMillis()
        if (time_new - time_old > WAITING_TIME) {
            // need to reset state in case of packet loose
            unpacker.reset()
        }
        time_old = time_new
        var newDataFound = false
        for (c in data!!) {
            if (unpacker.addChar(c.toInt())) {
                val buff = unpacker.getBuffer()
                val useBetterPercents = appConfig.useBetterPercents
                val veteranNegative = appConfig.gotwayNegative.toInt()
                val voltage = MathsUtil.shortFromBytesBE(buff, 4)
                var speed = MathsUtil.signedShortFromBytesBE(buff, 6) * 10
                val distance = MathsUtil.intFromBytesRevBE(buff, 8)
                val totalDistance = MathsUtil.intFromBytesRevBE(buff, 12)
                var phaseCurrent = MathsUtil.signedShortFromBytesBE(buff, 16) * 10
                val temperature = MathsUtil.signedShortFromBytesBE(buff, 18)
                val autoOffSec = MathsUtil.shortFromBytesBE(buff, 20)
                val chargeMode = MathsUtil.shortFromBytesBE(buff, 22)
                val speedAlert = MathsUtil.shortFromBytesBE(buff, 24) * 10
                val speedTiltback = MathsUtil.shortFromBytesBE(buff, 26) * 10
                val ver = MathsUtil.shortFromBytesBE(buff, 28)
                mVer = ver / 1000
                val version = String.format(
                    Locale.US,
                    "%03d.%01d.%02d",
                    ver / 1000,
                    ver % 1000 / 100,
                    ver % 100,
                )
                val pedalsMode = MathsUtil.shortFromBytesBE(buff, 30)
                val pitchAngle = MathsUtil.signedShortFromBytesBE(buff, 32)
                val hwPwm = MathsUtil.shortFromBytesBE(buff, 34)
                val battery = batteryCalculator.calculateBattery(voltage, mVer, useBetterPercents)
                if (veteranNegative == 0) {
                    speed = Math.abs(speed)
                    phaseCurrent = Math.abs(phaseCurrent)
                } else {
                    speed = speed * veteranNegative
                    phaseCurrent = phaseCurrent * veteranNegative
                }
                wd.version = version
                wd.speed = speed
                wd.topSpeed = speed
                wd.setWheelDistance(distance.toLong())
                wd.totalDistance = totalDistance.toLong()
                wd.temperature = temperature
                wd.phaseCurrent = phaseCurrent
                wd.current = phaseCurrent
                wd.voltage = voltage
                wd.voltageSag = voltage
                wd.batteryLevel = battery
                wd.chargingStatus = chargeMode
                wd.angle = pitchAngle / 100.0
                wd.output = hwPwm
                wd.updateRideTime()
                newDataFound = true
            }
        }
        return newDataFound
    }

    override val isReady: Boolean
        get() = wd.voltage != 0 && mVer != 0

    fun resetTrip() {
        wd.bluetoothCmd("CLEARMETER".toByteArray())
    }

    override fun updatePedalsMode(pedalsMode: Int) {
        when (pedalsMode) {
            0 -> wd.bluetoothCmd("SETh".toByteArray())
            1 -> wd.bluetoothCmd("SETm".toByteArray())
            2 -> wd.bluetoothCmd("SETs".toByteArray())
        }
    }

    val ver: Int
        get() {
            if (mVer >= 2) {
                appConfig.hwPwm = true
            }
            return mVer
        }

    override fun switchFlashlight() {
        val light = !appConfig.lightEnabled
        appConfig.lightEnabled = light
        setLightState(light)
    }

    override fun setLightState(lightEnable: Boolean) {
        var command = ""
        command = if (lightEnable) {
            "SetLightON"
        } else {
            "SetLightOFF"
        }
        wd.bluetoothCmd(command.toByteArray())
    }

    override val cellsForWheel: Int
        get() = if (mVer > 3) {
            30
        } else {
            24
        }

    override fun wheelBeep() {
        wd.bluetoothCmd("b".toByteArray())
    }

    companion object {
        private var INSTANCE: VeteranAdapter? = null
        private const val WAITING_TIME = 100

        @JvmStatic
        val instance: VeteranAdapter?
            get() {
                if (INSTANCE == null) {
                    INSTANCE = VeteranAdapter(
                        VeteranBatteryCalculator(),
                        VeteranUnpacker(
                            ByteArrayOutputStream(),
                        ),
                        WheelData.getInstance(),
                        WheelLog.AppConfig,
                    )
                }
                return INSTANCE
            }
    }
}
