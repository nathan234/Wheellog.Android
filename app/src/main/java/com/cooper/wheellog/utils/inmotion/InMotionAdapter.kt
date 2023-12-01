package com.cooper.wheellog.utils.inmotion

import android.content.Intent
import com.cooper.wheellog.AppConfig
import com.cooper.wheellog.R
import com.cooper.wheellog.WheelData
import com.cooper.wheellog.WheelLog
import com.cooper.wheellog.utils.BaseAdapter
import com.cooper.wheellog.utils.Constants
import timber.log.Timber
import java.util.Timer
import java.util.TimerTask

class InMotionAdapter(
    private val appConfig: AppConfig,
) : BaseAdapter() {
    private var keepAliveTimer: Timer? = null
    private var passwordSent = 0
    private var needSlowData = true
    protected var settingCommandReady = false
    protected lateinit var settingCommand: ByteArray

    override fun decode(data: ByteArray?): Boolean {
        for (c in data!!) {
            val addCharResult = unpacker.addChar(c.toInt(), updateStep)
            updateStep = addCharResult.second
            if (!addCharResult.first) {
                continue
            }
            val result = CANMessage.verify(unpacker.getBuffer()) ?: continue
            // data OK
            var idValue: IDValue? = IDValue.NoOp
            for (id in IDValue.entries) {
                if (id.value == result.id) {
                    idValue = id
                    break
                }
            }
            when (idValue) {
                IDValue.GetFastInfo -> return result.parseFastInfoMessage(model)
                IDValue.Alert -> return result.parseAlertInfoMessage()
                IDValue.GetSlowInfo -> {
                    if (result.isValid) {
                        needSlowData = false
                    }
                    val result = result.parseSlowInfoMessage(model)
                    setModel(result.second)
                    return result.first
                }
                IDValue.PinCode -> passwordSent = Int.MAX_VALUE
                IDValue.NoOp,
                IDValue.RideMode,
                IDValue.RemoteControl,
                IDValue.Calibration,
                IDValue.Light,
                IDValue.HandleButton,
                IDValue.SpeakerVolume,
                IDValue.PlaySound,
                null,
                -> {
                    // no op todo
                }
            }
            if (context != null) {
                val news =
                    when (idValue) {
                        IDValue.Calibration ->
                            if (result.data[0].toInt() == 1) {
                                context!!.getString(R.string.calibration_success)
                            } else {
                                context!!.getString(R.string.calibration_fail)
                            }
                        IDValue.RideMode ->
                            if (result.data[0].toInt() == 1) {
                                context!!.getString(R.string.ridemode_success)
                            } else {
                                context!!.getString(R.string.ridemode_fail)
                            }
                        IDValue.RemoteControl ->
                            if (result.data[0].toInt() == 1) {
                                context!!.getString(R.string.remotecontrol_success)
                            } else {
                                context!!.getString(R.string.remotecontrol_fail)
                            }
                        IDValue.Light ->
                            if (result.data[0].toInt() == 1) {
                                context!!.getString(R.string.light_success)
                            } else {
                                context!!.getString(R.string.light_fail)
                            }
                        IDValue.HandleButton ->
                            if (result.data[0].toInt() == 1) {
                                context!!.getString(R.string.handlebutton_success)
                            } else {
                                context!!.getString(R.string.handlebutton_fail)
                            }
                        IDValue.SpeakerVolume ->
                            if (result.data[0].toInt() == 1) {
                                context!!.getString(R.string.speakervolume_success)
                            } else {
                                context!!.getString(R.string.speakervolume_fail)
                            }
                        else -> null
                    }
                if (news != null) {
                    Timber.i("News to send: %s, sending Intent", news)
                    val intent = Intent(Constants.ACTION_WHEEL_NEWS_AVAILABLE)
                    intent.putExtra(Constants.INTENT_EXTRA_NEWS, news)
                    context!!.sendBroadcast(intent)
                }
            }
        }
        return false
    }

    override val isReady: Boolean
        get() = model != Model.UNKNOWN && WheelData.getInstance().serial != ""

    enum class Mode(val value: Int) {
        ROOKIE(0),
        GENERAL(1),
        SMOOTHLY(2),
        UN_BOOT(3),
        OLD_C(4),
        FOC(5),
    }

    val maxSpeed: Int
        get() {
            when (model) {
                Model.V5,
                Model.V5PLUS,
                Model.V5F,
                Model.V5D,
                -> return 25
                Model.V8,
                Model.Glide3,
                -> return 35
                Model.V8F,
                Model.V8S,
                Model.V10S,
                Model.V10SF,
                Model.V10,
                Model.V10F,
                Model.V10T,
                Model.V10FT,
                -> return 45

                Model.R1N,
                Model.R1S,
                Model.R1CF,
                Model.R1AP,
                Model.R1EX,
                Model.R1Sample,
                Model.R1T,
                Model.R10,
                Model.V3,
                Model.V3C,
                Model.V3PRO,
                Model.V3S,
                Model.R2N,
                Model.R2S,
                Model.R2Sample,
                Model.R2,
                Model.R2EX,
                Model.R0,
                Model.L6,
                Model.Lively,
                Model.UNKNOWN,
                -> {
                    // todo model enum includes non-InMotion wheels
                }
            }
            return 70
        }

    val ledThere: Boolean
        get() =
            when (model) {
                Model.Glide3,
                Model.V8,
                Model.V8F,
                Model.V8S,
                Model.V10S,
                Model.V10SF,
                Model.V10T,
                Model.V10,
                Model.V10F,
                Model.V10FT,
                -> true
                else -> false
            }

    val wheelModesWheel: Boolean
        get() =
            when (model) {
                Model.V8F,
                Model.V8S,
                Model.V10S,
                Model.V10SF,
                Model.V10T,
                Model.V10,
                Model.V10F,
                Model.V10FT,
                -> true
                else -> false
            }

    enum class Model(val value: String, val speedCalculationFactor: Double) {
        R1N("0", 3812.0),
        R1S("1", 1000.0),
        R1CF("2", 3812.0),
        R1AP("3", 3812.0),
        R1EX("4", 3812.0),
        R1Sample("5", 1000.0),
        R1T("6", 3810.0),
        R10("7", 3812.0),
        V3("10", 3812.0),
        V3C("11", 3812.0),
        V3PRO("12", 3812.0),
        V3S("13", 3812.0),
        R2N("21", 3812.0),
        R2S("22", 3812.0),
        R2Sample("23", 3812.0),
        R2("20", 3812.0),
        R2EX("24", 3812.0),
        R0("30", 1000.0),
        L6("60", 3812.0),
        Lively("61", 3812.0),
        V5("50", 3812.0),
        V5PLUS("51", 3812.0),
        V5F("52", 3812.0),
        V5D("53", 3812.0),
        V8("80", 3812.0),
        V8F("86", 3812.0),
        V8S("87", 3812.0),
        Glide3("85", 3812.0),
        V10S("100", 3812.0),
        V10SF("101", 3812.0),
        V10("140", 3812.0),
        V10F("141", 3812.0),
        V10T("142", 3812.0),
        V10FT("143", 3812.0),
        UNKNOWN("x", 3812.0),
        ;

        fun belongToInputType(type: String): Boolean {
            return if ("0" == type) {
                value.length == 1
            } else {
                value.substring(0, 1) == type && value.length == 2
            }
        }

        companion object {
            fun findById(id: String): Model {
                Timber.i("Model %s", id)
                for (m in entries) {
                    if (m.value == id) return m
                }
                return UNKNOWN
            }

            fun findByBytes(data: ByteArray?): Model {
                val stringBuffer = StringBuilder()
                if (data!!.size >= 108) {
                    if (data[107] > 0.toByte()) {
                        stringBuffer.append(data[107].toInt())
                    }
                    stringBuffer.append(data[104].toInt())
                }
                return findById(stringBuffer.toString())
            }
        }
    }

    enum class WorkMode(val value: Int) {
        IDLE(0),
        DRIVE(1),
        ZERO(2),
        LARGE_ANGLE(3),
        CHECK_C(4),
        LOCK(5),
        ERROR(6),
        CARRY(7),
        REMOTE_CONTROL(8),
        SHUTDOWN(9),
        POM_STOP(10),
        UNKNOWN(11),
        UNLOCK(12),
    }

    var unpacker = InMotionUnpacker()

    private fun setModel(value: Model) {
        model = value
    }

    fun startKeepAliveTimer(password: String) {
        val timerTask: TimerTask =
            object : TimerTask() {
                override fun run() {
                    if (updateStep == 0) {
                        if (passwordSent < 6) {
                            if (
                                WheelData.getInstance()
                                    .bluetoothCmd(CANMessage.getPassword(password).writeBuffer())
                            ) {
                                Timber.i("Sent password message")
                                passwordSent++
                            } else {
                                updateStep = 5
                            }
                        } else if ((model == Model.UNKNOWN) or needSlowData) {
                            if (
                                WheelData.getInstance()
                                    .bluetoothCmd(CANMessage.slowData.writeBuffer())
                            ) {
                                Timber.i("Sent infos message")
                            } else {
                                updateStep = 5
                            }
                        } else if (settingCommandReady) {
                            if (WheelData.getInstance().bluetoothCmd(settingCommand)) {
                                needSlowData = true
                                settingCommandReady = false
                                Timber.i("Sent command message")
                            } else {
                                updateStep = 5 // after +1 and %10 = 0
                            }
                        } else {
                            if (
                                !WheelData.getInstance()
                                    .bluetoothCmd(CANMessage.standardMessage().writeBuffer())
                            ) {
                                Timber.i("Unable to send keep-alive message")
                                updateStep = 5
                            } else {
                                Timber.i("Sent keep-alive message")
                            }
                        }
                    }
                    updateStep++
                    updateStep %= 10
                    Timber.i("Step: %d", updateStep)
                }
            }
        keepAliveTimer = Timer()
        keepAliveTimer!!.scheduleAtFixedRate(timerTask, 200, 25)
    }

    override fun switchFlashlight() {
        val light = !appConfig.lightEnabled
        appConfig.lightEnabled = light
        setLightState(light)
    }

    override fun setLightState(lightEnable: Boolean) {
        settingCommand = CANMessage.setLight(lightEnable).writeBuffer()
        settingCommandReady = true
    }

    override fun setLedState(ledEnable: Boolean) {
        settingCommand = CANMessage.setLed(ledEnable).writeBuffer()
        settingCommandReady = true
    }

    override fun setHandleButtonState(handleButtonEnable: Boolean) {
        settingCommand = CANMessage.setHandleButton(handleButtonEnable).writeBuffer()
        settingCommandReady = true
    }

    override fun updateMaxSpeed(maxSpeed: Int) {
        settingCommand = CANMessage.setMaxSpeed(maxSpeed).writeBuffer()
        settingCommandReady = true
    }

    override fun setSpeakerVolume(speakerVolume: Int) {
        settingCommand = CANMessage.setSpeakerVolume(speakerVolume).writeBuffer()
        settingCommandReady = true
    }

    override fun setPedalTilt(angle: Int) {
        settingCommand = CANMessage.setTiltHorizon(angle).writeBuffer()
        settingCommandReady = true
    }

    override fun setPedalSensivity(sensivity: Int) {
        settingCommand = CANMessage.setPedalSensivity(sensivity).writeBuffer()
        settingCommandReady = true
    }

    override fun setRideMode(rideMode: Boolean) {
        settingCommand = CANMessage.setRideMode(rideMode).writeBuffer()
        settingCommandReady = true
    }

    override fun powerOff() {
        settingCommand = CANMessage.powerOff().writeBuffer()
        settingCommandReady = true
    }

    override fun wheelCalibration() {
        settingCommand = CANMessage.wheelCalibration().writeBuffer()
        settingCommandReady = true
    }

    override fun wheelBeep() {
        settingCommand =
            if (wheelModesWheel) {
                CANMessage.wheelBeep().writeBuffer()
            } else {
                CANMessage.playSound(4.toByte())
                    .writeBuffer() // old wheels like V8 and V5F don't have beep command, so let's
            }
        // play sound instead
        settingCommandReady = true
    }

    fun wheelSound(soundNumber: Byte) {
        settingCommand = CANMessage.playSound(soundNumber).writeBuffer()
        settingCommandReady = true
    }

    override val cellsForWheel: Int
        get() = 20

    companion object {
        private var INSTANCE: InMotionAdapter? = null
        private var updateStep = 0
        private var model = Model.UNKNOWN

        fun intToMode(mode: Int): Mode {
            return if (mode and 16 != 0) {
                Mode.ROOKIE
            } else if (mode and 32 != 0) {
                Mode.GENERAL
            } else if (mode and 64 == 0 || mode and 128 == 0) {
                Mode.UN_BOOT
            } else {
                Mode.SMOOTHLY
            }
        }

        fun intToModeWithL6(mode: Int): Mode {
            return if (mode and 15 != 0) {
                Mode.OLD_C
            } else {
                Mode.FOC
            }
        }

        fun intToWorkModeWithL6(mode: Int): WorkMode {
            return if (mode and 240 != 0) {
                WorkMode.LOCK
            } else {
                WorkMode.UNLOCK
            }
        }

        fun intToWorkMode(mode: Int): WorkMode {
            val v = mode and 0xF
            return when (v) {
                0 -> WorkMode.IDLE
                1 -> WorkMode.DRIVE
                2 -> WorkMode.ZERO
                3 -> WorkMode.LARGE_ANGLE
                4 -> WorkMode.CHECK_C
                5 -> WorkMode.LOCK
                6 -> WorkMode.ERROR
                7 -> WorkMode.CARRY
                8 -> WorkMode.REMOTE_CONTROL
                9 -> WorkMode.SHUTDOWN
                16 -> WorkMode.POM_STOP
                else -> WorkMode.UNKNOWN
            }
        }

        fun batteryFromVoltage(volts_i: Int, model: Model): Int {
            val volts = volts_i.toDouble() / 100.0
            val batt: Double
            batt =
                if (model.belongToInputType("1") || model == Model.R0) {
                    if (volts >= 82.50) {
                        1.0
                    } else if (volts > 68.0) {
                        (volts - 68.0) / 14.50
                    } else {
                        0.0
                    }
                } else {
                    val useBetterPercents = WheelLog.AppConfig.useBetterPercents
                    if (
                        model.belongToInputType("5") ||
                        model == Model.V8 ||
                        model == Model.Glide3 ||
                        model == Model.V8F ||
                        model == Model.V8S
                    ) {
                        if (useBetterPercents) {
                            if (volts > 84.00) {
                                1.0
                            } else if (volts > 68.5) {
                                (volts - 68.5) / 15.5
                            } else {
                                0.0
                            }
                        } else {
                            if (volts > 82.50) {
                                1.0
                            } else if (volts > 68.0) {
                                (volts - 68.0) / 14.5
                            } else {
                                0.0
                            }
                        }
                    } else if (
                        model == Model.V10 ||
                        model == Model.V10F ||
                        model == Model.V10S ||
                        model == Model.V10SF ||
                        model == Model.V10T ||
                        model == Model.V10FT
                    ) {
                        if (useBetterPercents) {
                            if (volts > 83.50) {
                                1.00
                            } else if (volts > 68.00) {
                                (volts - 66.50) / 17
                            } else if (volts > 64.00) {
                                (volts - 64.00) / 45
                            } else {
                                0.0
                            }
                        } else {
                            if (volts > 82.50) {
                                1.0
                            } else if (volts > 68.0) {
                                (volts - 68.0) / 14.5
                            } else {
                                0.0
                            }
                        }
                    } else if (model.belongToInputType("6")) {
                        0.0
                    } else {
                        if (volts >= 82.00) {
                            1.0
                        } else if (volts > 77.8) {
                            (volts - 77.8) / 4.2 * 0.2 + 0.8
                        } else if (volts > 74.8) {
                            (volts - 74.8) / 3.0 * 0.2 + 0.6
                        } else if (volts > 71.8) {
                            (volts - 71.8) / 3.0 * 0.2 + 0.4
                        } else if (volts > 70.3) {
                            (volts - 70.3) / 1.5 * 0.2 + 0.2
                        } else if (volts > 68.0) {
                            (volts - 68.0) / 2.3 * 0.2
                        } else {
                            0.0
                        }
                    }
                }
            return (batt * 100.0).toInt()
        }

        fun getModelString(model: Model): String {
            return when (model.value) {
                "0" -> "Inmotion R1N"
                "1" -> "Inmotion R1S"
                "2" -> "Inmotion R1CF"
                "3" -> "Inmotion R1AP"
                "4" -> "Inmotion R1EX"
                "5" -> "Inmotion R1Sample"
                "6" -> "Inmotion R1T"
                "7" -> "Inmotion R10"
                "10" -> "Inmotion V3"
                "11" -> "Inmotion V3C"
                "12" -> "Inmotion V3PRO"
                "13" -> "Inmotion V3S"
                "21" -> "Inmotion R2N"
                "22" -> "Inmotion R2S"
                "23" -> "Inmotion R2Sample"
                "20" -> "Inmotion R2"
                "24" -> "Inmotion R2EX"
                "30" -> "Inmotion R0"
                "60" -> "Inmotion L6"
                "61" -> "Inmotion Lively"
                "50" -> "Inmotion V5"
                "51" -> "Inmotion V5PLUS"
                "52" -> "Inmotion V5F"
                "53" -> "Inmotion V5D"
                "80" -> "Inmotion V8"
                "85" -> "Solowheel Glide 3"
                "86" -> "Inmotion V8F"
                "87" -> "Inmotion V8S"
                "100" -> "Inmotion V10S"
                "101" -> "Inmotion V10SF"
                "140" -> "Inmotion V10"
                "141" -> "Inmotion V10F"
                "142" -> "Inmotion V10T"
                "143" -> "Inmotion V10FT"
                else -> "Unknown"
            }
        }

        @JvmStatic
        @get:Synchronized
        val instance: InMotionAdapter?
            get() {
                if (INSTANCE == null) {
                    Timber.i("New instance")
                    INSTANCE = InMotionAdapter(
                        WheelLog.AppConfig,
                    )
                } else {
                    Timber.i("Get instance")
                }
                return INSTANCE
            }

        @Synchronized
        fun newInstance() {
            if (INSTANCE != null && INSTANCE!!.keepAliveTimer != null) {
                INSTANCE!!.keepAliveTimer!!.cancel()
                INSTANCE!!.keepAliveTimer = null
            }
            Timber.i("New instance")
            INSTANCE = InMotionAdapter(
                WheelLog.AppConfig,
            )
        }

        @JvmStatic
        @Synchronized
        fun stopTimer() {
            if (INSTANCE != null && INSTANCE!!.keepAliveTimer != null) {
                INSTANCE!!.keepAliveTimer!!.cancel()
                INSTANCE!!.keepAliveTimer = null
            }
            Timber.i("Kill instance, stop timer")
            INSTANCE = null
        }
    }
}
