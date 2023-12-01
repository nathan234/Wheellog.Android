package com.cooper.wheellog.utils.inmotion

import com.cooper.wheellog.WheelData
import com.cooper.wheellog.WheelLog
import com.cooper.wheellog.utils.MathsUtil
import com.cooper.wheellog.utils.StringUtil
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.Arrays
import java.util.Locale
import kotlin.math.abs

class CANMessage() {
    internal enum class CanFormat(val value: Int) {
        StandardFormat(0),
        ExtendedFormat(1),
    }

    internal enum class CanFrame(val value: Int) {
        DataFrame(0),
        RemoteFrame(1),
    }

    var id = IDValue.NoOp.value
    var data = ByteArray(8)
    var len = 0
    var ch = 0
    var format = CanFormat.StandardFormat.value
    var type = CanFrame.DataFrame.value
    var ex_data: ByteArray? = null

    internal constructor(bArr: ByteArray) : this() {
        if (bArr.size < 16) return
        id = ((bArr[3] * 256 + bArr[2]) * 256 + bArr[1]) * 256 + bArr[0]
        data = Arrays.copyOfRange(bArr, 4, 12)
        len = bArr[12].toInt()
        ch = bArr[13].toInt()
        format =
            if (bArr[14].toInt() == 0) {
                CanFormat.StandardFormat.value
            } else {
                CanFormat.ExtendedFormat.value
            }
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

    fun parseFastInfoMessage(model: InMotionAdapter.Model): Boolean {
        if (!isValid) return false
        val angle = MathsUtil.intFromBytesLE(ex_data, 0).toDouble() / 65536.0
        var roll = MathsUtil.intFromBytesLE(ex_data, 72).toDouble() / 90.0
        var speed =
            (
                MathsUtil.intFromBytesLE(ex_data, 12).toDouble() +
                    MathsUtil.intFromBytesLE(ex_data, 16).toDouble()
                ) /
                (model.speedCalculationFactor * 2.0)
        speed = abs(speed)
        val voltage = MathsUtil.intFromBytesLE(ex_data, 24)
        val current = MathsUtil.signedIntFromBytesLE(ex_data, 20).toInt()
        val temperature = ex_data!![32].toInt()
        val temperature2 = ex_data!![34].toInt()
        val batt = InMotionAdapter.batteryFromVoltage(voltage, model)
        val totalDistance: Long
        val distance: Long
        totalDistance =
            if (
                model.belongToInputType("1") ||
                model.belongToInputType("5") ||
                model == InMotionAdapter.Model.V8 ||
                model == InMotionAdapter.Model.Glide3 ||
                model == InMotionAdapter.Model.V10 ||
                model == InMotionAdapter.Model.V10F ||
                model == InMotionAdapter.Model.V10S ||
                model == InMotionAdapter.Model.V10SF ||
                model == InMotionAdapter.Model.V10T ||
                model == InMotionAdapter.Model.V10FT ||
                model == InMotionAdapter.Model.V8F ||
                model == InMotionAdapter.Model.V8S
            ) {
                MathsUtil.intFromBytesLE(ex_data, 44)
                    .toLong() // /// V10F 48 byte - trip distance
            } else if (model == InMotionAdapter.Model.R0) {
                MathsUtil.longFromBytesLE(ex_data, 44)
            } else if (model == InMotionAdapter.Model.L6) {
                MathsUtil.longFromBytesLE(ex_data, 44) * 100
            } else {
                Math.round(MathsUtil.longFromBytesLE(ex_data, 44) / 5.711016379455429E7)
            }
        distance = MathsUtil.intFromBytesLE(ex_data, 48).toLong()
        val workMode: String
        val workModeInt = MathsUtil.intFromBytesLE(ex_data, 60)
        if (
            model == InMotionAdapter.Model.V8F ||
            model == InMotionAdapter.Model.V8S ||
            model == InMotionAdapter.Model.V10 ||
            model == InMotionAdapter.Model.V10F ||
            model == InMotionAdapter.Model.V10FT ||
            model == InMotionAdapter.Model.V10S ||
            model == InMotionAdapter.Model.V10SF ||
            model == InMotionAdapter.Model.V10T
        ) {
            roll = 0.0
            workMode = getWorkModeString(workModeInt)
        } else {
            workMode = getLegacyWorkModeString(workModeInt)
        }
        val wd = WheelData.getInstance()
        wd.angle = angle
        wd.roll = roll
        wd.speed = (speed * 360.0).toInt()
        wd.voltage = voltage
        wd.setBatteryLevel(batt)
        wd.setCurrent(current)
        wd.setTotalDistance(totalDistance)
        wd.setWheelDistance(distance)
        wd.temperature = temperature * 100
        wd.temperature2 = temperature2 * 100
        wd.modeStr = workMode
        return true
    }

    fun parseAlertInfoMessage(): Boolean {
        val alertId = data[0].toInt()
        val alertValue = (data[3] * 256 or (data[2].toInt() and 0xFF)).toDouble()
        val alertValue2 =
            (
                data[7] * 256 * 256 * 256 or
                    (data[6].toInt() and 0xFF) * 256 * 256 or
                    (data[5].toInt() and 0xFF) * 256 or
                    (data[4].toInt() and 0xFF)
                )
                .toDouble()
        val a_speed = abs(alertValue2 / 3812.0 * 3.6)
        val fullText: String
        val hex = StringBuilder("[")
        for (c in data) {
            hex.append(String.format("%02X", (c.toInt() and 0xFF)))
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
                        hex.toString(),
                    )
                0x06 ->
                    String.format(
                        Locale.ENGLISH,
                        "Tiltback at speed %.2f at limit %.2f %s",
                        a_speed,
                        alertValue / 1000.0,
                        hex.toString(),
                    )
                0x19 -> String.format(Locale.ENGLISH, "Fall Down %s", hex.toString())
                0x20 ->
                    String.format(
                        Locale.ENGLISH,
                        "Low battery at voltage %.2f %s",
                        alertValue2 / 100.0,
                        hex.toString(),
                    )
                0x21 ->
                    String.format(
                        Locale.ENGLISH,
                        "Speed cut-off at speed %.2f and something %.2f %s",
                        a_speed,
                        alertValue / 10.0,
                        hex.toString(),
                    )
                0x26 ->
                    String.format(
                        Locale.ENGLISH,
                        "High load at speed %.2f and current %.2f %s",
                        a_speed,
                        alertValue / 1000.0,
                        hex.toString(),
                    )
                0x1d ->
                    String.format(
                        Locale.ENGLISH,
                        "Please repair: bad battery cell found. At voltage %.2f %s",
                        alertValue2 / 100.0,
                        hex.toString(),
                    )
                else ->
                    String.format(
                        Locale.ENGLISH,
                        "Unknown Alert %.2f %.2f, please contact palachzzz, hex %s",
                        alertValue,
                        alertValue2,
                        hex.toString(),
                    )
            }
        val wd = WheelData.getInstance()
        wd.setAlert(fullText)
        return true
    }

    fun parseSlowInfoMessage(model: InMotionAdapter.Model): Pair<Boolean, InMotionAdapter.Model> {
        if (!isValid) return Pair(false, model)
        var lmodel = InMotionAdapter.Model.findByBytes(ex_data) // CarType is just model.rawValue
        if (lmodel == InMotionAdapter.Model.UNKNOWN) lmodel = InMotionAdapter.Model.V8
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
        val wd = WheelData.getInstance()
        wd.serial = serialNumber.toString()
        wd.setModel(InMotionAdapter.getModelString(lmodel))
        wd.version = version
        val appConfig = WheelLog.AppConfig
        appConfig.lightEnabled = light
        appConfig.ledEnabled = led
        appConfig.handleButtonDisabled = handlebutton
        appConfig.wheelMaxSpeed = maxspeed
        appConfig.speakerVolume = speakervolume
        appConfig.pedalsAdjustment = pedals
        appConfig.rideMode = rideMode
        appConfig.pedalSensivity = pedalHardness
        return Pair(true, lmodel)
    }
    private fun getWorkModeString(value: Int): String {
        val hValue = value shr 4
        var result =
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
            Timber.i("Before escape %s", StringUtil.toHexString(buffer))
            val len = buffer.size - 3
            val dataBuffer = Arrays.copyOfRange(buffer, 2, len)
            Timber.i("After escape %s", StringUtil.toHexString(dataBuffer))
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
                        0xFF.toByte(),
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
                        0xFF.toByte(),
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
                    0x00.toByte(),
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
                    0x00.toByte(),
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
                    0x00.toByte(),
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
                    0x00.toByte(),
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
                    0x00.toByte(),
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
            // / rideMode =0 -Comfort, =1 -Classic
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
                    0x00.toByte(),
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
                    0x00.toByte(),
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
                    0x00.toByte(),
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
