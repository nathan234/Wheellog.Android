package com.cooper.wheellog.utils.gotway

import android.content.Intent
import android.os.Handler
import com.cooper.wheellog.AppConfig
import com.cooper.wheellog.wheeldata.WheelData
import com.cooper.wheellog.WheelLog
import com.cooper.wheellog.utils.BaseAdapter
import com.cooper.wheellog.models.Constants
import timber.log.Timber

class GotwayAdapter(
    private val appConfig: AppConfig,
    private val wd: WheelData,
    private val unpacker: GotwayUnpacker,
    private val gotwayFrameADecoder: GotwayFrameADecoder,
    private val gotwayFrameBDecoder: GotwayFrameBDecoder,
) : BaseAdapter() {
    private var model = ""
    private var imu = ""
    private var fw = ""
    private var attempt = 0
    private var lock_Changes = 0
    private val lightModeOff = 0
    private val lightModeOn = 1
    private val lightModeStrobe = 2
    private val alarmModeTwo = 0 // 30 + 35 (45) km/h + 80% PWM
    private val alarmModeOne = 1 // 35 (45) km/h + 80% PWM
    private val alarmModeOff = 2 // 80% PWM only
    private val alarmModeCF = 3 // PWM tiltback for custom firmware

    override fun decode(data: ByteArray?): Boolean {
        Timber.i("Decode Gotway/Begode")
        wd.resetRideTime()
        var newDataFound = false
        if (
            model.length == 0 || fw.length == 0
        ) { // IMU sent at the begining, so there is no sense to check it, we can't request it
            val dataS = String(data!!, 0, data.size).trim { it <= ' ' }
            if (dataS.startsWith("NAME")) {
                model = dataS.substring(5).trim { it <= ' ' }
                wd.model = model
            } else if (dataS.startsWith("GW")) {
                fw = dataS.substring(2).trim { it <= ' ' }
                wd.version = fw
                appConfig.hwPwm = false
            } else if (dataS.startsWith("CF")) {
                fw = dataS.substring(2).trim { it <= ' ' }
                wd.version = fw
                appConfig.hwPwm = true
            } else if (dataS.startsWith("MPU")) {
                imu = dataS.substring(1, 7).trim { it <= ' ' }
            }
        }
        for (c in data!!) {
            if (unpacker.addChar(c.toInt())) {
                val buff = unpacker.getBuffer()
                val useRatio = appConfig.useRatio
                val useBetterPercents = appConfig.useBetterPercents
                val gotwayNegative = appConfig.gotwayNegative.toInt()
                if (buff[18] == 0x00.toByte()) {
                    Timber.i("Begode frame A found (live data)")
                    gotwayFrameADecoder.decode(buff, useRatio, useBetterPercents, gotwayNegative)
                    newDataFound = true
                } else if (buff[18] == 0x04.toByte()) {
                    Timber.i("Begode frame B found (total distance and flags)")
                    val result = gotwayFrameBDecoder.decode(buff, useRatio, lock_Changes, fw)
                    lock_Changes = result.lock
                    val alert = result.alert
                    var alertLine = ""
                    if (alert and 0x01 == 1) alertLine += "HighPower "
                    if (alert shr 1 and 0x01 == 1) alertLine += "Speed2 "
                    if (alert shr 2 and 0x01 == 1) alertLine += "Speed1 "
                    if (alert shr 3 and 0x01 == 1) alertLine += "LowVoltage "
                    if (alert shr 4 and 0x01 == 1) alertLine += "OverVoltage "
                    if (alert shr 5 and 0x01 == 1) alertLine += "OverTemperature "
                    if (alert shr 6 and 0x01 == 1) alertLine += "errHallSensors "
                    if (alert shr 7 and 0x01 == 1) alertLine += "TransportMode"
                    wd.alert = (alertLine)
                    if (alertLine !== "" && context != null) {
                        Timber.i("News to send: %s, sending Intent", alertLine)
                        val intent = Intent(Constants.ACTION_WHEEL_NEWS_AVAILABLE)
                        intent.putExtra(Constants.INTENT_EXTRA_NEWS, alertLine)
                        context!!.sendBroadcast(intent)
                    }
                }
                if (attempt < 10) {
                    if (model == "") {
                        sendCommand("N", "", 0)
                    } else if (fw == "") {
                        sendCommand("V", "", 0)
                    }
                    attempt += 1
                } else {
                    if (model == "") {
                        model = "Begode"
                        wd.version = model
                    } else if (fw == "") {
                        fw = "-"
                        wd.version = fw
                        appConfig.hwPwm = false
                    }
                }
            }
        }
        return newDataFound
    }

    override val isReady: Boolean
        get() = WheelData.instance!!.voltage != 0

    private fun sendCommand(s: String, delayed: String = "b", timer: Int = 100) {
        sendCommand(s.toByteArray(), delayed.toByteArray(), timer)
    }

    private fun sendCommand(s: ByteArray, delayed: ByteArray, timer: Int) {
        WheelData.instance!!.bluetoothCmd(s)
        if (timer > 0) {
            Handler().postDelayed({ WheelData.instance!!.bluetoothCmd(delayed) }, timer.toLong())
        }
    }

    override fun updatePedalsMode(pedalsMode: Int) {
        val command =
            when (pedalsMode) {
                0 -> "h"
                1 -> "f"
                2 -> "s"
                else -> ""
            }
        lock_Changes = 2
        sendCommand(command)
    }

    override fun switchFlashlight() {
        var lightMode = appConfig.lightMode.toInt() + 1
        if (lightMode > lightModeStrobe) {
            lightMode = lightModeOff
        }
        // Strobe light not available on Freestyl3r firmware while pwm tiltback mode enabled.
        // For custom firmware with enabled tiltback available only light off and on.
        // Strobe is using for tiltback warning. Detect via specific for this firmware alarm mode.
        if (lightMode > lightModeOn && appConfig.alarmMode == alarmModeCF.toString()) {
            lightMode = lightModeOff
        }
        appConfig.lightMode = lightMode.toString()
        setLightMode(lightMode)
    }

    override fun setLightMode(lightMode: Int) {
        lock_Changes = 2
        val command =
            when (lightMode) {
                lightModeOff -> "E"
                lightModeOn -> "Q"
                lightModeStrobe -> "T"
                else -> "E"
            }
        sendCommand(command)
    }

    override fun setMilesMode(milesMode: Boolean) {
        var command = ""
        lock_Changes = 2
        command =
            if (milesMode) {
                "m"
            } else {
                "g"
            }
        sendCommand(command)
    }

    override fun setRollAngleMode(rollAngle: Int) {
        var command = ""
        lock_Changes = 2
        when (rollAngle) {
            0 -> command = ">"
            1 -> command = "="
            2 -> command = "<"
        }
        sendCommand(command)
    }

    override fun updateLedMode(ledMode: Int) {
        val param = ByteArray(1)
        lock_Changes = 5
        param[0] = (ledMode % 10 + 0x30).toByte()
        Handler().postDelayed({ sendCommand("W", "M") }, 100)
        Handler().postDelayed({ sendCommand(param, "b".toByteArray(), 100) }, 300)
    }

    override fun updateBeeperVolume(beeperVolume: Int) {
        val param = ByteArray(1)
        param[0] = (beeperVolume % 10 + 0x30).toByte()
        Handler().postDelayed({ sendCommand("W", "B") }, 100)
        Handler().postDelayed({ sendCommand(param, "b".toByteArray(), 100) }, 300)
    }

    override fun updateAlarmMode(alarmMode: Int) {
        val command =
            when (alarmMode) {
                alarmModeTwo -> "o"
                alarmModeOne -> "u"
                alarmModeOff -> "i"
                alarmModeCF -> "I"
                else -> ""
            }
        lock_Changes = 2
        sendCommand(command)
    }

    override fun wheelCalibration() {
        sendCommand("c", "y", 300)
    }

    override val cellsForWheel: Int
        get() =
            when (appConfig.gotwayVoltage) {
                "0" -> 16
                "1" -> 20
                "2" -> 24
                "3" -> 32
                "4" -> 32
                else -> 24
            }

    override fun wheelBeep() {
        WheelData.instance!!.bluetoothCmd("b".toByteArray())
    }

    override fun updateMaxSpeed(maxSpeed: Int) {
        val hhh = ByteArray(1)
        val lll = ByteArray(1)
        lock_Changes = 5
        if (maxSpeed != 0) {
            hhh[0] = (maxSpeed / 10 + 0x30).toByte()
            lll[0] = (maxSpeed % 10 + 0x30).toByte()
            WheelData.instance!!.bluetoothCmd("b".toByteArray())
            Handler().postDelayed({ sendCommand("W", "Y") }, 100)
            Handler().postDelayed({ sendCommand(hhh, lll, 100) }, 300)
            Handler().postDelayed({ sendCommand("b", "b") }, 500)
        } else {
            sendCommand("b", "\"")
            Handler().postDelayed({ sendCommand("b", "b") }, 200)
        }
    }

    companion object {
        private var INSTANCE: GotwayAdapter? = null
        const val RATIO_GW = 0.875

        @JvmStatic
        val instance: GotwayAdapter
            get() {
                if (INSTANCE == null) {
                    val wd = WheelData.instance!!
                    val appConfig = WheelLog.AppConfig
                    INSTANCE =
                        GotwayAdapter(
                            appConfig,
                            wd,
                            GotwayUnpacker(),
                            GotwayFrameADecoder(
                                wd,
                                GotwayScaledVoltageCalculator(appConfig),
                                GotwayBatteryCalculator(),
                            ),
                            GotwayFrameBDecoder(wd, appConfig),
                        )
                }
                return INSTANCE!!
            }
    }
}
