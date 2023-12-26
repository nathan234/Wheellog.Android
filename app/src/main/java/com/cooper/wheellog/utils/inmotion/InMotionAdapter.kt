package com.cooper.wheellog.utils.inmotion

import android.content.Intent
import com.cooper.wheellog.R
import com.cooper.wheellog.WheelData
import com.cooper.wheellog.WheelLog
import com.cooper.wheellog.utils.BaseAdapter
import com.cooper.wheellog.utils.Constants
import com.cooper.wheellog.utils.MathsUtil
import com.cooper.wheellog.utils.StringUtil.toHexString
import com.cooper.wheellog.utils.inmotion.InMotionAdapter.CANMessage.IDValue
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.Arrays
import java.util.Locale
import java.util.Timer
import java.util.TimerTask
import kotlin.experimental.and
import kotlin.math.abs

class InMotionAdapter : BaseAdapter() {
    private var keepAliveTimer: Timer? = null
    private var passwordSent = 0
    private var needSlowData = true
    var settingCommandReady = false
    var settingCommand: ByteArray? = null

    override fun decode(data: ByteArray?): Boolean {
        for (c in data!!) {
            if (!unpacker.addChar(c.toInt())) {
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
                    return result.parseSlowInfoMessage()
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
                null -> {}
            }
            if (context != null) {
                var news: String? = null
                when (idValue) {
                    IDValue.Calibration ->
                        news =
                            if (result.data[0].toInt() == 1)
                                context!!.getString(R.string.calibration_success)
                            else context!!.getString(R.string.calibration_fail)
                    IDValue.RideMode ->
                        news =
                            if (result.data[0].toInt() == 1)
                                context!!.getString(R.string.ridemode_success)
                            else context!!.getString(R.string.ridemode_fail)
                    IDValue.RemoteControl ->
                        news =
                            if (result.data[0].toInt() == 1)
                                context!!.getString(R.string.remotecontrol_success)
                            else context!!.getString(R.string.remotecontrol_fail)
                    IDValue.Light ->
                        news =
                            if (result.data[0].toInt() == 1)
                                context!!.getString(R.string.light_success)
                            else context!!.getString(R.string.light_fail)
                    IDValue.HandleButton ->
                        news =
                            if (result.data[0].toInt() == 1)
                                context!!.getString(R.string.handlebutton_success)
                            else context!!.getString(R.string.handlebutton_fail)
                    IDValue.SpeakerVolume ->
                        news =
                            if (result.data[0].toInt() == 1)
                                context!!.getString(R.string.speakervolume_success)
                            else context!!.getString(R.string.speakervolume_fail)
                    IDValue.NoOp,
                    IDValue.GetFastInfo,
                    IDValue.GetSlowInfo,
                    IDValue.PinCode,
                    IDValue.PlaySound,
                    IDValue.Alert,
                    null -> {}
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
        get() = model != Model.UNKNOWN && WheelData.instance!!.serial != ""

    enum class Mode(val value: Int) {
        rookie(0),
        general(1),
        smoothly(2),
        unBoot(3),
        bldc(4),
        foc(5)
    }

    val maxSpeed: Int
        get() {
            when (model) {
                Model.V5,
                Model.V5PLUS,
                Model.V5F,
                Model.V5D -> return 25
                Model.V8,
                Model.Glide3 -> return 35
                Model.V8F,
                Model.V8S,
                Model.V10S,
                Model.V10SF,
                Model.V10,
                Model.V10F,
                Model.V10T,
                Model.V10FT -> return 45
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
                Model.UNKNOWN -> {}
            }
            return 70
        }

    val ledThere: Boolean
        get() {
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
                Model.V10FT -> return true
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
                Model.V5,
                Model.V5PLUS,
                Model.V5F,
                Model.V5D,
                Model.UNKNOWN -> {}
            }
            return false
        }

    val wheelModesWheel: Boolean
        get() {
            when (model) {
                Model.V8F,
                Model.V8S,
                Model.V10S,
                Model.V10SF,
                Model.V10T,
                Model.V10,
                Model.V10F,
                Model.V10FT -> return true
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
                Model.V5,
                Model.V5PLUS,
                Model.V5F,
                Model.V5D,
                Model.V8,
                Model.Glide3,
                Model.UNKNOWN -> {}
            }
            return false
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
        UNKNOWN("x", 3812.0);

        fun belongToInputType(type: String): Boolean {
            return if ("0" == type) {
                value.length == 1
            } else value.substring(0, 1) == type && value.length == 2
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
        idle(0),
        drive(1),
        zero(2),
        largeAngle(3),
        checkc(4),
        lock(5),
        error(6),
        carry(7),
        remoteControl(8),
        shutdown(9),
        pomStop(10),
        unknown(11),
        unlock(12)
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
                                WheelData.instance!!
                                    .bluetoothCmd(CANMessage.getPassword(password).writeBuffer())
                            ) {
                                Timber.i("Sent password message")
                                passwordSent++
                            } else {
                                updateStep = 5
                            }
                        } else if ((model == Model.UNKNOWN) or needSlowData) {
                            if (
                                WheelData.instance!!
                                    .bluetoothCmd(CANMessage.slowData.writeBuffer())
                            ) {
                                Timber.i("Sent infos message")
                            } else {
                                updateStep = 5
                            }
                        } else if (settingCommandReady) {
                            if (WheelData.instance!!.bluetoothCmd(settingCommand)) {
                                needSlowData = true
                                settingCommandReady = false
                                Timber.i("Sent command message")
                            } else {
                                updateStep = 5 // after +1 and %10 = 0
                            }
                        } else {
                            if (
                                !WheelData.instance!!
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
        val light = !WheelLog.AppConfig.lightEnabled
        WheelLog.AppConfig.lightEnabled = light
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
            if (wheelModesWheel) CANMessage.wheelBeep().writeBuffer()
            else
                CANMessage.playSound(4.toByte())
                    .writeBuffer() // old wheels like V8 and V5F don't have beep command, so let's
                                   // play sound instead
        settingCommandReady = true
    }

    fun wheelSound(soundNumber: Byte) {
        settingCommand = CANMessage.playSound(soundNumber).writeBuffer()
        settingCommandReady = true
    }

    class CANMessage {
        internal enum class CanFormat(val value: Int) {
            StandardFormat(0),
            ExtendedFormat(1)
        }

        internal enum class CanFrame(val value: Int) {
            DataFrame(0),
            RemoteFrame(1)
        }

        internal enum class IDValue(val value: Int) {
            NoOp(0),
            GetFastInfo(0x0F550113),
            GetSlowInfo(0x0F550114),
            RideMode(0x0F550115),
            RemoteControl(0x0F550116),
            Calibration(0x0F550119),
            PinCode(0x0F550307),
            Light(0x0F55010D),
            HandleButton(0x0F55012E),
            SpeakerVolume(0x0F55060A),
            PlaySound(0x0F550609),
            Alert(0x0F780101)
        }

        var id = IDValue.NoOp.value
        var data = ByteArray(8)
        var len = 0
        var ch = 0
        var format = CanFormat.StandardFormat.value
        var type = CanFrame.DataFrame.value
        var ex_data: ByteArray? = null

        internal constructor(bArr: ByteArray) {
            if (bArr.size < 16) return
            id = ((bArr[3] * 256 + bArr[2]) * 256 + bArr[1]) * 256 + bArr[0]
            data = Arrays.copyOfRange(bArr, 4, 12)
            len = bArr[12].toInt()
            ch = bArr[13].toInt()
            format =
                if (bArr[14].toInt() == 0) CanFormat.StandardFormat.value
                else CanFormat.ExtendedFormat.value
            type =
                if (bArr[15].toInt() == 0) CanFrame.DataFrame.value else CanFrame.RemoteFrame.value
            if (len == 0xFE.toByte().toInt()) {
                val ldata = MathsUtil.intFromBytesLE(data, 0)
                if (ldata == bArr.size - 16) {
                    ex_data = Arrays.copyOfRange(bArr, 16, 16 + ldata)
                }
            }
        }

        val isValid: Boolean
            get() = ex_data != null

        private constructor()

        fun writeBuffer(): ByteArray {
            val canBuffer = bytes
            val check = computeCheck(canBuffer)
            val out = ByteArrayOutputStream()
            out.write(0xAA)
            out.write(0xAA)
            try {
                out.write(escape(canBuffer))
            } catch (e: IOException) {
                e.printStackTrace()
            }
            out.write(check.toInt())
            out.write(0x55)
            out.write(0x55)
            return out.toByteArray()
        }

        private val bytes: ByteArray
            private get() {
                val buff = ByteArrayOutputStream()
                val b3 = id / (256 * 256 * 256)
                val b2 = (id - b3 * 256 * 256 * 256) / (256 * 256)
                val b1 = (id - b3 * 256 * 256 * 256 - b2 * 256 * 256) / 256
                val b0 = id % 256
                buff.write(b0)
                buff.write(b1)
                buff.write(b2)
                buff.write(b3)
                try {
                    buff.write(data)
                    buff.write(len)
                    buff.write(ch)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                buff.write(if (format == CanFormat.StandardFormat.value) 0 else 1)
                buff.write(if (type == CanFrame.DataFrame.value) 0 else 1)
                if (len == 0xFE.toByte().toInt()) {
                    try {
                        buff.write(ex_data)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
                return buff.toByteArray()
            }

        fun clearData() {
            data = ByteArray(data.size)
        }

        private fun escape(buffer: ByteArray): ByteArray {
            val out = ByteArrayOutputStream()
            for (c in buffer) {
                if (c == 0xAA.toByte() || c == 0x55.toByte() || c == 0xA5.toByte()) {
                    out.write(0xA5)
                }
                out.write(c.toInt())
            }
            return out.toByteArray()
        }

        fun parseFastInfoMessage(model: Model): Boolean {
            if (!isValid) return false
            val angle = MathsUtil.intFromBytesLE(ex_data, 0).toDouble() / 65536.0
            var roll = MathsUtil.intFromBytesLE(ex_data, 72).toDouble() / 90.0
            var speed =
                (MathsUtil.intFromBytesLE(ex_data, 12).toDouble() +
                    MathsUtil.intFromBytesLE(ex_data, 16).toDouble()) /
                    (model.speedCalculationFactor * 2.0)
            speed = abs(speed)
            val voltage = MathsUtil.intFromBytesLE(ex_data, 24)
            val current = MathsUtil.signedIntFromBytesLE(ex_data, 20).toInt()
            val temperature = ex_data!![32].toInt()
            val temperature2 = ex_data!![34].toInt()
            val batt = batteryFromVoltage(voltage, model)
            val totalDistance: Long
            val distance: Long
            totalDistance =
                if (
                    model.belongToInputType("1") ||
                        model.belongToInputType("5") ||
                        model == Model.V8 ||
                        model == Model.Glide3 ||
                        model == Model.V10 ||
                        model == Model.V10F ||
                        model == Model.V10S ||
                        model == Model.V10SF ||
                        model == Model.V10T ||
                        model == Model.V10FT ||
                        model == Model.V8F ||
                        model == Model.V8S
                ) {
                    MathsUtil.intFromBytesLE(ex_data, 44)
                        .toLong() ///// V10F 48 byte - trip distance
                } else if (model == Model.R0) {
                    MathsUtil.longFromBytesLE(ex_data, 44)
                } else if (model == Model.L6) {
                    MathsUtil.longFromBytesLE(ex_data, 44) * 100
                } else {
                    Math.round(MathsUtil.longFromBytesLE(ex_data, 44) / 5.711016379455429E7)
                }
            distance = MathsUtil.intFromBytesLE(ex_data, 48).toLong()
            val workMode: String
            val workModeInt = MathsUtil.intFromBytesLE(ex_data, 60)
            if (
                model == Model.V8F ||
                    model == Model.V8S ||
                    model == Model.V10 ||
                    model == Model.V10F ||
                    model == Model.V10FT ||
                    model == Model.V10S ||
                    model == Model.V10SF ||
                    model == Model.V10T
            ) {
                roll = 0.0
                workMode = getWorkModeString(workModeInt)
            } else {
                workMode = getLegacyWorkModeString(workModeInt)
            }
            val wd = WheelData.instance!!
            wd.angle = angle
            wd.roll = roll
            wd.speed = (speed * 360.0).toInt()
            wd.voltage = voltage
            wd.batteryLevel = (batt)
            wd.current = (current)
            wd.totalDistance = (totalDistance)
            wd.wheelDistance = (distance)
            wd.temperature = temperature * 100
            wd.temperature2 = temperature2 * 100
            wd.modeStr = workMode
            return true
        }

        fun parseAlertInfoMessage(): Boolean {
            val alertId = data[0].toInt()
            val alertValue = (data[3] * 256 or (data[2].toInt() and 0xFF)).toDouble()
            val alertValue2 =
                (data[7] * 256 * 256 * 256 or
                        (data[6].toInt() and 0xFF) * 256 * 256 or
                        (data[5].toInt() and 0xFF) * 256 or
                        (data[4].toInt() and 0xFF))
                    .toDouble()
            val a_speed = abs(alertValue2 / 3812.0 * 3.6)
            val fullText: String
            val hex = StringBuilder("[")
            for (c in data) {
                hex.append(String.format("%02X", c and 0xFF.toByte()))
            }
            hex.append("]")
            fullText =
                when (alertId) {
                    0x05 ->
                        String.format(
                            Locale.ENGLISH,
                            "Start from tilt angle %.2f at speed %.2f %s",
                            alertValue / 100.0,
                            a_speed,
                            hex.toString()
                        )
                    0x06 ->
                        String.format(
                            Locale.ENGLISH,
                            "Tiltback at speed %.2f at limit %.2f %s",
                            a_speed,
                            alertValue / 1000.0,
                            hex.toString()
                        )
                    0x19 -> String.format(Locale.ENGLISH, "Fall Down %s", hex.toString())
                    0x20 ->
                        String.format(
                            Locale.ENGLISH,
                            "Low battery at voltage %.2f %s",
                            alertValue2 / 100.0,
                            hex.toString()
                        )
                    0x21 ->
                        String.format(
                            Locale.ENGLISH,
                            "Speed cut-off at speed %.2f and something %.2f %s",
                            a_speed,
                            alertValue / 10.0,
                            hex.toString()
                        )
                    0x26 ->
                        String.format(
                            Locale.ENGLISH,
                            "High load at speed %.2f and current %.2f %s",
                            a_speed,
                            alertValue / 1000.0,
                            hex.toString()
                        )
                    0x1d ->
                        String.format(
                            Locale.ENGLISH,
                            "Please repair: bad battery cell found. At voltage %.2f %s",
                            alertValue2 / 100.0,
                            hex.toString()
                        )
                    else ->
                        String.format(
                            Locale.ENGLISH,
                            "Unknown Alert %.2f %.2f, please contact palachzzz, hex %s",
                            alertValue,
                            alertValue2,
                            hex.toString()
                        )
                }
            val wd = WheelData.instance!!
            wd.alert = (fullText)
            return true
        }

        fun parseSlowInfoMessage(): Boolean {
            if (!isValid) return false
            var lmodel = Model.findByBytes(ex_data) // CarType is just model.rawValue
            if (lmodel == Model.UNKNOWN) lmodel = Model.V8
            val v0 = ex_data!![27].toInt() and 0xFF
            val v1 = ex_data!![26].toInt() and 0xFF
            val v2 = (ex_data!![25].toInt() and 0xFF) * 256 or (ex_data!![24].toInt() and 0xFF)
            val version = String.format(Locale.ENGLISH, "%d.%d.%d", v0, v1, v2)
            val serialNumber = StringBuilder()
            val maxspeed: Int
            var speakervolume = 0
            val light = ex_data!![80].toInt() == 1
            var led = false
            var handlebutton = false
            var rideMode = false
            var pedalHardness = 100
            val pedals = Math.round(MathsUtil.intFromBytesLE(ex_data, 56) / 6553.6).toInt()
            maxspeed =
                ((ex_data!![61].toInt() and 0xFF) * 256 or (ex_data!![60].toInt() and 0xFF)) / 1000
            if (ex_data!!.size > 126) {
                speakervolume =
                    ((ex_data!![126].toInt() and 0xFF) * 256 or (ex_data!![125].toInt() and 0xFF)) /
                        100
            }
            if (ex_data!!.size > 130) {
                led = ex_data!![130].toInt() == 1
            }
            if (ex_data!!.size > 129) {
                handlebutton = ex_data!![129].toInt() != 1
            }
            if (ex_data!!.size > 132) {
                rideMode = ex_data!![132].toInt() == 1
            }
            if (ex_data!!.size > 124) {
                pedalHardness =
                    ex_data!![124] - 28 and 0xFF // 0x80 = 128 = 100% -maximum, 0x20 = 32 - minimum
            }
            for (j in 0..7) {
                serialNumber.append(String.format("%02X", ex_data!![7 - j]))
            }
            val wd = WheelData.instance!!
            wd.serial = serialNumber.toString()
            wd.model = (getModelString(lmodel))
            wd.version = version
            WheelLog.AppConfig.lightEnabled = light
            WheelLog.AppConfig.ledEnabled = led
            WheelLog.AppConfig.handleButtonDisabled = handlebutton
            WheelLog.AppConfig.wheelMaxSpeed = maxspeed
            WheelLog.AppConfig.speakerVolume = speakervolume
            WheelLog.AppConfig.pedalsAdjustment = pedals
            WheelLog.AppConfig.rideMode = rideMode
            WheelLog.AppConfig.pedalSensivity = pedalHardness
            instance!!.setModel(lmodel)
            return false
        }

        companion object {
            private fun computeCheck(buffer: ByteArray): Byte {
                var check = 0
                for (c in buffer) {
                    check = check + c and 0xFF
                }
                return check.toByte()
            }

            fun verify(buffer: ByteArray): CANMessage? {
                if (
                    buffer[0] != 0xAA.toByte() ||
                        buffer[1] != 0xAA.toByte() ||
                        buffer[buffer.size - 1] != 0x55.toByte() ||
                        buffer[buffer.size - 2] != 0x55.toByte()
                ) {
                    return null // Header and tail not correct
                }
                Timber.i("Before escape %s", toHexString(buffer))
                val len = buffer.size - 3
                val dataBuffer = Arrays.copyOfRange(buffer, 2, len)
                Timber.i("After escape %s", toHexString(dataBuffer))
                val check = computeCheck(dataBuffer)
                val bufferCheck = buffer[len]
                if (check == bufferCheck) {
                    Timber.i("Check OK")
                } else {
                    Timber.i("Check FALSE, calc: %02X, packet: %02X", check, bufferCheck)
                }
                return if (check == bufferCheck) CANMessage(dataBuffer) else null
            }

            fun standardMessage(): CANMessage {
                val msg = CANMessage()
                msg.len = 8
                msg.id = IDValue.GetFastInfo.value
                msg.ch = 5
                msg.data = byteArrayOf(-1, -1, -1, -1, -1, -1, -1, -1)
                return msg
            }

            val fastData: CANMessage
                get() {
                    val msg = CANMessage()
                    msg.len = 8
                    msg.id = IDValue.GetFastInfo.value
                    msg.ch = 5
                    msg.data =
                        byteArrayOf(
                            0xFF.toByte(),
                            0xFF.toByte(),
                            0xFF.toByte(),
                            0xFF.toByte(),
                            0xFF.toByte(),
                            0xFF.toByte(),
                            0xFF.toByte(),
                            0xFF.toByte()
                        )
                    return msg
                }

            val slowData: CANMessage
                get() {
                    val msg = CANMessage()
                    msg.len = 8
                    msg.id = IDValue.GetSlowInfo.value
                    msg.ch = 5
                    msg.type = CanFrame.RemoteFrame.value
                    msg.data =
                        byteArrayOf(
                            0xFF.toByte(),
                            0xFF.toByte(),
                            0xFF.toByte(),
                            0xFF.toByte(),
                            0xFF.toByte(),
                            0xFF.toByte(),
                            0xFF.toByte(),
                            0xFF.toByte()
                        )
                    return msg
                }

            fun setLight(on: Boolean): CANMessage {
                val msg = CANMessage()
                var enable: Byte = 0
                if (on) {
                    enable = 1
                }
                msg.len = 8
                msg.id = IDValue.Light.value
                msg.ch = 5
                msg.type = CanFrame.DataFrame.value
                msg.data =
                    byteArrayOf(
                        enable,
                        0x00.toByte(),
                        0x00.toByte(),
                        0x00.toByte(),
                        0x00.toByte(),
                        0x00.toByte(),
                        0x00.toByte(),
                        0x00.toByte()
                    )
                return msg
            }

            fun setLed(on: Boolean): CANMessage {
                val msg = CANMessage()
                var enable: Byte = 0x10
                if (on) {
                    enable = 0x0F
                }
                msg.len = 8
                msg.id = IDValue.RemoteControl.value
                msg.ch = 5
                msg.type = CanFrame.DataFrame.value
                msg.data =
                    byteArrayOf(
                        0xB2.toByte(),
                        0x00.toByte(),
                        0x00.toByte(),
                        0x00.toByte(),
                        enable,
                        0x00.toByte(),
                        0x00.toByte(),
                        0x00.toByte()
                    )
                return msg
            }

            fun wheelBeep(): CANMessage {
                val msg = CANMessage()
                msg.len = 8
                msg.id = IDValue.RemoteControl.value
                msg.ch = 5
                msg.type = CanFrame.DataFrame.value
                msg.data =
                    byteArrayOf(
                        0xB2.toByte(),
                        0x00.toByte(),
                        0x00.toByte(),
                        0x00.toByte(),
                        0x11.toByte(),
                        0x00.toByte(),
                        0x00.toByte(),
                        0x00.toByte()
                    )
                return msg
            }

            fun wheelCalibration(): CANMessage {
                val msg = CANMessage()
                msg.len = 8
                msg.id = IDValue.Calibration.value
                msg.ch = 5
                msg.type = CanFrame.DataFrame.value
                msg.data =
                    byteArrayOf(
                        0x32.toByte(),
                        0x54.toByte(),
                        0x76.toByte(),
                        0x98.toByte(),
                        0x00.toByte(),
                        0x00.toByte(),
                        0x00.toByte(),
                        0x00.toByte()
                    )
                return msg
            }

            fun powerOff(): CANMessage {
                val msg = CANMessage()
                msg.len = 8
                msg.id = IDValue.RemoteControl.value
                msg.ch = 5
                msg.type = CanFrame.DataFrame.value
                msg.data = byteArrayOf(0xB2.toByte(), 0, 0, 0, 5, 0, 0, 0)
                return msg
            }

            fun setHandleButton(on: Boolean): CANMessage {
                val msg = CANMessage()
                var enable: Byte = 1
                if (on) {
                    enable = 0
                }
                msg.len = 8
                msg.id = IDValue.HandleButton.value
                msg.ch = 5
                msg.type = CanFrame.DataFrame.value
                msg.data =
                    byteArrayOf(
                        enable,
                        0x00.toByte(),
                        0x00.toByte(),
                        0x00.toByte(),
                        0x00.toByte(),
                        0x00.toByte(),
                        0x00.toByte(),
                        0x00.toByte()
                    )
                return msg
            }

            fun setMaxSpeed(maxSpeed: Int): CANMessage {
                val msg = CANMessage()
                val value = MathsUtil.getBytes((maxSpeed * 1000).toShort())
                msg.len = 8
                msg.id = IDValue.RideMode.value
                msg.ch = 5
                msg.type = CanFrame.DataFrame.value
                msg.data = byteArrayOf(1, 0, 0, 0, value[1], value[0], 0, 0)
                return msg
            }

            fun playSound(soundNumber: Byte): CANMessage {
                val msg = CANMessage()
                msg.len = 8
                msg.id = IDValue.PlaySound.value
                msg.ch = 5
                msg.type = CanFrame.DataFrame.value
                msg.data = byteArrayOf(soundNumber, 0, 0, 0, 0, 0, 0, 0)
                return msg
            }

            fun setRideMode(rideMode: Boolean): CANMessage {
                /// rideMode =0 -Comfort, =1 -Classic
                var classic: Byte = 0
                if (rideMode) {
                    classic = 1
                }
                val msg = CANMessage()
                msg.len = 8
                msg.id = IDValue.RideMode.value
                msg.ch = 5
                msg.type = CanFrame.DataFrame.value
                msg.data =
                    byteArrayOf(
                        0x0a.toByte(),
                        0x00.toByte(),
                        0x00.toByte(),
                        0x00.toByte(),
                        classic,
                        0x00.toByte(),
                        0x00.toByte(),
                        0x00.toByte()
                    )
                return msg
            }

            fun setPedalSensivity(sensivity: Int): CANMessage {
                val value = MathsUtil.getBytes((sensivity + 28 shl 5).toShort())
                val msg = CANMessage()
                msg.len = 8
                msg.id = IDValue.RideMode.value
                msg.ch = 5
                msg.type = CanFrame.DataFrame.value
                msg.data =
                    byteArrayOf(
                        0x06.toByte(),
                        0x00.toByte(),
                        0x00.toByte(),
                        0x00.toByte(),
                        value[1],
                        value[0],
                        0x00.toByte(),
                        0x00.toByte()
                    )
                return msg
            }

            fun setSpeakerVolume(speakerVolume: Int): CANMessage {
                val msg = CANMessage()
                val lowByte = speakerVolume * 100 and 0xFF
                val highByte = speakerVolume * 100 / 0x100 and 0xFF
                msg.len = 8
                msg.id = IDValue.SpeakerVolume.value
                msg.ch = 5
                msg.type = CanFrame.DataFrame.value
                msg.data =
                    byteArrayOf(
                        lowByte.toByte(),
                        highByte.toByte(),
                        0x00.toByte(),
                        0x00.toByte(),
                        0x00.toByte(),
                        0x00.toByte(),
                        0x00.toByte(),
                        0x00.toByte()
                    )
                return msg
            }

            fun setTiltHorizon(tiltHorizon: Int): CANMessage {
                val msg = CANMessage()
                val tilt = tiltHorizon * 65536 / 10
                val t = MathsUtil.getBytes(tilt)
                msg.len = 8
                msg.id = IDValue.RideMode.value
                msg.ch = 5
                msg.type = CanFrame.DataFrame.value
                msg.data = byteArrayOf(0, 0, 0, 0, t[3], t[2], t[1], t[0])
                return msg
            }

            val batteryLevelsdata: CANMessage
                get() {
                    val msg = CANMessage()
                    msg.len = 8
                    msg.id = IDValue.GetSlowInfo.value
                    msg.ch = 5
                    msg.type = CanFrame.RemoteFrame.value
                    msg.data = byteArrayOf(0, 0, 0, 15, 0, 0, 0, 0)
                    return msg
                }

            val version: CANMessage
                get() {
                    val msg = CANMessage()
                    msg.len = 8
                    msg.id = IDValue.GetSlowInfo.value
                    msg.ch = 5
                    msg.type = CanFrame.RemoteFrame.value
                    msg.data = byteArrayOf(32, 0, 0, 0, 0, 0, 0, 0)
                    return msg
                }

            fun getPassword(password: String): CANMessage {
                val msg = CANMessage()
                msg.len = 8
                msg.id = IDValue.PinCode.value
                msg.ch = 5
                msg.type = CanFrame.DataFrame.value
                val pass = password.toByteArray()
                msg.data = byteArrayOf(pass[0], pass[1], pass[2], pass[3], pass[4], pass[5], 0, 0)
                return msg
            }

            fun setMode(mode: Int): CANMessage {
                val msg = CANMessage()
                msg.len = 8
                msg.id = IDValue.NoOp.value
                msg.ch = 5
                msg.type = CanFrame.DataFrame.value
                msg.data = byteArrayOf(0xB2.toByte(), 0, 0, 0, mode.toByte(), 0, 0, 0)
                return msg
            }
        }
    }

    class InMotionUnpacker {
        enum class UnpackerState {
            unknown,
            collecting,
            done
        }

        var buffer = ByteArrayOutputStream()
        var oldc = 0

        // there are two types of packets, basic and extended, if it is extended packet,
        // then len field should be 0xFE, and len of extended data should be in first data byte
        // of usual packet
        var len_p = 0 // basic packet len
        var len_ex = 0 // extended packet len
        var state = UnpackerState.unknown

        fun getBuffer(): ByteArray {
            return buffer.toByteArray()
        }

        fun addChar(c: Int): Boolean {
            if (c != 0xA5.toByte().toInt() || oldc == 0xA5.toByte().toInt()) {
                if (state == UnpackerState.collecting) {
                    buffer.write(c)
                    val sz = buffer.size()
                    if (sz == 7) len_ex = c and 0xFF else if (sz == 15) len_p = c and 0xFF
                    if (sz > len_ex + 21 && len_p == 0xFE) {
                        reset() // longer than expected
                        return false
                    }
                    if (
                        c == 0x55.toByte().toInt() &&
                            oldc == 0x55.toByte().toInt() &&
                            (sz == len_ex + 21 || len_p != 0xFE)
                    ) { // 18 header + 1 crc + 2 footer
                        state = UnpackerState.done
                        updateStep = 0
                        oldc = 0
                        Timber.i("Step reset")
                        return true
                    }
                } else {
                    if (c == 0xAA.toByte().toInt() && oldc == 0xAA.toByte().toInt()) {
                        buffer = ByteArrayOutputStream()
                        buffer.write(0xAA)
                        buffer.write(0xAA)
                        state = UnpackerState.collecting
                    }
                }
            }
            oldc = c
            return false
        }

        fun reset() {
            buffer = ByteArrayOutputStream()
            oldc = 0
            len_p = 0
            len_ex = 0
            state = UnpackerState.unknown
        }
    }

    override val cellsForWheel: Int
        get() = 20

    companion object {
        private var INSTANCE: InMotionAdapter? = null
        private var updateStep = 0
        private var model = Model.UNKNOWN

        fun intToMode(mode: Int): Mode {
            return if (mode and 16 != 0) {
                Mode.rookie
            } else if (mode and 32 != 0) {
                Mode.general
            } else if (mode and 64 == 0 || mode and 128 == 0) {
                Mode.unBoot
            } else {
                Mode.smoothly
            }
        }

        fun intToModeWithL6(mode: Int): Mode {
            return if (mode and 15 != 0) {
                Mode.bldc
            } else {
                Mode.foc
            }
        }

        fun intToWorkModeWithL6(mode: Int): WorkMode {
            return if (mode and 240 != 0) {
                WorkMode.lock
            } else {
                WorkMode.unlock
            }
        }

        fun intToWorkMode(mode: Int): WorkMode {
            val v = mode and 0xF
            return when (v) {
                0 -> WorkMode.idle
                1 -> WorkMode.drive
                2 -> WorkMode.zero
                3 -> WorkMode.largeAngle
                4 -> WorkMode.checkc
                5 -> WorkMode.lock
                6 -> WorkMode.error
                7 -> WorkMode.carry
                8 -> WorkMode.remoteControl
                9 -> WorkMode.shutdown
                16 -> WorkMode.pomStop
                else -> WorkMode.unknown
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

        private fun getLegacyWorkModeString(value: Int): String {
            return when (value and 0xF) {
                0 -> "Idle"
                1 -> "Drive"
                2 -> "Zero"
                3 -> "LargeAngle"
                4 -> "Check"
                5 -> "Lock"
                6 -> "Error"
                7 -> "Carry"
                8 -> "RemoteControl"
                9 -> "Shutdown"
                10 -> "pomStop"
                12 -> "Unlock"
                else -> "Unknown"
            }
        }

        private fun getWorkModeString(value: Int): String {
            val hValue = value shr 4
            var result: String
            result =
                when (hValue) {
                    1 -> "Shutdown"
                    2 -> "Drive"
                    3 -> "Charging"
                    else -> "Unknown code $hValue"
                }
            if (value and 0xF == 1) {
                result += " - Engine off"
            }
            return result
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
        val instance: InMotionAdapter
            get() {
                if (INSTANCE == null) {
                    Timber.i("New instance")
                    INSTANCE = InMotionAdapter()
                } else {
                    Timber.i("Get instance")
                }
                return INSTANCE!!
            }

        @Synchronized
        fun newInstance() {
            if (INSTANCE != null && INSTANCE!!.keepAliveTimer != null) {
                INSTANCE!!.keepAliveTimer!!.cancel()
                INSTANCE!!.keepAliveTimer = null
            }
            Timber.i("New instance")
            INSTANCE = InMotionAdapter()
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
