package com.cooper.wheellog.utils.ninebot

import com.cooper.wheellog.WheelData
import com.cooper.wheellog.utils.BaseAdapter
import com.cooper.wheellog.utils.MathsUtil
import com.cooper.wheellog.utils.StringUtil.toHexString
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.Arrays
import java.util.Locale
import java.util.Timer
import java.util.TimerTask
import kotlin.math.abs

/**
 * Created by palachzzz on Dec 2019.
 */
class NinebotAdapter : BaseAdapter() {
    private var keepAliveTimer: Timer? = null
    private var settingCommandReady = false
    private lateinit var settingCommand: ByteArray
    var unpacker = NinebotUnpacker()
    fun startKeepAliveTimer(protoVer: String) {
        Timber.i("Ninebot timer starting")
        if (protoVer.compareTo("S2") == 0) protoVersion = 1
        if (protoVer.compareTo("Mini") == 0) protoVersion = 2
        updateStep = 0
        stateCon = 0
        val timerTask: TimerTask = object : TimerTask() {
            override fun run() {
                if (updateStep == 0) {
                    if (stateCon == 0) {
                        if (WheelData.instance!!
                                .bluetoothCmd(CANMessage.serialNumber.writeBuffer())
                        ) {
                            Timber.i("Sent serial number message")
                        } else updateStep = 39
                    } else if (stateCon == 1) {
                        if (WheelData.instance!!
                                .bluetoothCmd(CANMessage.version.writeBuffer())
                        ) {
                            Timber.i("Sent serial version message")
                        } else updateStep = 39
                    } else if (settingCommandReady) {
                        if (WheelData.instance!!.bluetoothCmd(settingCommand)) {
                            settingCommandReady = false
                            Timber.i("Sent command message")
                        } else updateStep = 39
                    } else {
                        if (!WheelData.instance!!
                                .bluetoothCmd(CANMessage.liveData.writeBuffer())
                        ) {
                            Timber.i("Unable to send keep-alive message")
                            updateStep = 39
                        } else {
                            Timber.i("Sent keep-alive message")
                        }
                    }
                }
                updateStep += 1
                updateStep %= 5
                Timber.i("Step: %d", updateStep)
            }
        }
        Timber.i("Ninebot timer started")
        keepAliveTimer = Timer()
        keepAliveTimer!!.scheduleAtFixedRate(timerTask, 0, 25)
    }

    fun resetConnection() {
        stateCon = 0
        updateStep = 0
        gamma = byteArrayOf(
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
            0x00
        )
        stopTimer()
    }

    override val cellsForWheel: Int
        get() = 15

    override fun decode(data: ByteArray?): Boolean {
        Timber.i("Ninebot_decoding")
        val statuses = charUpdated(data)
        if (statuses.size < 1) {
            return false
        }
        val wd = WheelData.instance!!
        wd.resetRideTime()
        for (status in statuses) {
            Timber.i(status.toString())
            if (status is serialNumberStatus) {
                wd.serial = status.serialNumber
                wd.model = ("Ninebot " + wd.protoVer)
            } else if (status is versionStatus) {
                wd.version = status.version
            } else {
                val speed = status.speed
                val voltage = status.voltage
                val battery = status.batt
                wd.speed = speed
                wd.voltage = voltage
                wd.current = (status.current)
                wd.totalDistance = (status.distance.toLong())
                wd.temperature = status.temperature * 10
                wd.updateRideTime()
                wd.batteryLevel = (battery)
            }
        }
        return true
    }

    override val isReady: Boolean
        get() = (WheelData.instance!!.serial != ""
                && WheelData.instance!!.version != "" && WheelData.instance!!.voltage != 0)

    open class Status {
        val speed: Int
        val voltage: Int
        val batt: Int
        val current: Int
        val power: Int
        val distance: Int
        val temperature: Int

        internal constructor() {
            speed = 0
            voltage = 0
            batt = 0
            current = 0
            power = 0
            distance = 0
            temperature = 0
        }

        internal constructor(
            speed: Int,
            voltage: Int,
            batt: Int,
            current: Int,
            power: Int,
            distance: Int,
            temperature: Int
        ) {
            this.speed = speed
            this.voltage = voltage
            this.batt = batt
            this.current = current
            this.power = power
            this.distance = distance
            this.temperature = temperature
        }

        override fun toString(): String {
            return "Status{" +
                    "speed=" + speed +
                    ", voltage=" + voltage +
                    ", batt=" + batt +
                    ", current=" + current +
                    ", power=" + power +
                    ", distance=" + distance +
                    ", temperature=" + temperature +
                    '}'
        }
    }

    class serialNumberStatus internal constructor(val serialNumber: String) : Status() {

        override fun toString(): String {
            return "Infos{" +
                    "serialNumber='" + serialNumber + '\'' +
                    '}'
        }
    }

    class versionStatus internal constructor(val version: String) : Status() {

        override fun toString(): String {
            return "Infos{" +
                    "version='" + version + '\'' +
                    '}'
        }
    }

    class activationStatus internal constructor(val version: String) : Status() {

        override fun toString(): String {
            return "Infos{" +
                    "activation='" + version + '\'' +
                    '}'
        }
    }

    class CANMessage {
        internal enum class Addr(
            private val value_def: Int,
            private val value_s2: Int,
            private val value_mini: Int
        ) {
            Controller(0x01, 0x01, 0x01),
            KeyGenerator(0x16, 0x16, 0x16),
            App(0x09, 0x11, 0x0A);

            val value: Int
                get() = if (protoVersion.toInt() == 1) {
                    value_s2
                } else if (protoVersion.toInt() == 2) {
                    value_mini
                } else {
                    value_def
                }
        }

        internal enum class Comm(val value: Int) {
            Read(0x01),
            Write(0x03),
            Get(0x04),
            GetKey(0x5b)

        }

        internal enum class Param(val value: Int) {
            SerialNumber(0x10),
            SerialNumber2(0x13),
            SerialNumber3(0x16),
            Firmware(0x1a),
            Angles(0x61),
            BatteryLevel(0x22),
            ActivationDate(0x69),
            LiveData(0xb0),
            LiveData2(0xb3),
            LiveData3(0xb6),
            LiveData4(0xb9),
            LiveData5(0xbc),
            LiveData6(0xbf)

        }

        var len = 0
        var source = 0
        var destination = 0
        var parameter = 0
        lateinit var data: ByteArray
        var crc = 0

        internal constructor(bArr: ByteArray) {
            if (bArr.size < 7) return
            len = bArr[0].toInt() and 0xff
            source = bArr[1].toInt() and 0xff
            destination = bArr[2].toInt() and 0xff
            parameter = bArr[3].toInt() and 0xff
            data = Arrays.copyOfRange(bArr, 4, bArr.size - 2)
            crc = bArr[bArr.size - 1].toInt() shl 8 + bArr[bArr.size - 2]
        }

        private constructor()

        fun writeBuffer(): ByteArray {
            val canBuffer = bytes
            val out = ByteArrayOutputStream()
            out.write(0x55)
            out.write(0xAA)
            try {
                out.write(canBuffer)
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return out.toByteArray()
        }

        private val bytes: ByteArray
            private get() {
                val buff = ByteArrayOutputStream()
                buff.write(len)
                buff.write(source)
                buff.write(destination)
                buff.write(parameter)
                try {
                    buff.write(data)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                crc = computeCheck(buff.toByteArray())
                buff.write(crc and 0xff)
                buff.write(crc shr 8 and 0xff)
                return crypto(buff.toByteArray())
            }

        @Deprecated("")
        private fun parseKey(): ByteArray {
            val gammaTemp = Arrays.copyOfRange(data, 0, data.size)
            val gamma_text = StringBuilder()
            for (datum in data) {
                gamma_text.append(String.format("%02X", datum))
            }
            Timber.i("New key: %s", gamma_text.toString())
            return gammaTemp
        }

        fun parseSerialNumber(): serialNumberStatus {
            serialNum = String(data)
            Timber.i("Serial Number: %s", serialNum)
            return serialNumberStatus(serialNum)
        }

        fun parseSerialNumber2(): serialNumberStatus {
            serialNum = serialNum + String(data)
            Timber.i("Serial Number: %s", serialNum)
            return serialNumberStatus(serialNum)
        }

        fun parseVersionNumber(): versionStatus {
            var versionNumber = ""
            if (protoVersion.toInt() == 1) {
                versionNumber = String.format(
                    Locale.US,
                    "%d.%d.%d",
                    data[1].toInt() shr 4,
                    data[0].toInt() shr 4,
                    data[0].toInt() and 0xf
                )
            } else if (protoVersion.toInt() == 2) {
                versionNumber = String.format(
                    Locale.US,
                    "%d.%d.%d",
                    data[1].toInt() and 0xf,
                    data[0].toInt() shr 4,
                    data[0].toInt() and 0xf
                )
            }
            Timber.i("Version Number: %s", versionNumber)
            return versionStatus(versionNumber)
        }

        fun parseActivationDate(): activationStatus {
            val activationDate = MathsUtil.shortFromBytesLE(data, 0)
            val year = activationDate shr 9
            val mounth = activationDate shr 5 and 0x0f
            val day = activationDate and 0x1f
            val activationDateStr = String.format("%02d.%02d.20%02d", day, mounth, year)
            return activationStatus(activationDateStr)
        }

        fun parseLiveData(): Status {
            val batt = MathsUtil.shortFromBytesLE(data, 8)
            val speed: Int
            speed = if (protoVersion.toInt() == 1) {
                MathsUtil.shortFromBytesLE(data, 28) //speed up to 320.00 km/h
            } else {
                abs((MathsUtil.signedShortFromBytesLE(data, 10) / 10).toDouble())
                    .toInt() //speed up to 32.000 km/h
            }
            val distance = MathsUtil.intFromBytesLE(data, 14)
            val temperature = MathsUtil.shortFromBytesLE(data, 22)
            var voltage = MathsUtil.shortFromBytesLE(data, 24)
            if (protoVersion.toInt() == 2) {
                voltage = 0 // no voltage for mini
            }
            val current = MathsUtil.signedShortFromBytesLE(data, 26)
            val power = voltage * current
            return Status(speed, voltage, batt, current, power, distance, temperature)
        }

        fun parseLiveData2(): Status {
            batt = MathsUtil.shortFromBytesLE(data, 2)
            speed = MathsUtil.shortFromBytesLE(data, 4) / 10
            return Status(speed, voltage, batt, current, power, distance, temperature)
        }

        fun parseLiveData3(): Status {
            distance = MathsUtil.intFromBytesLE(data, 2)
            return Status(speed, voltage, batt, current, power, distance, temperature)
        }

        fun parseLiveData4(): Status {
            temperature = MathsUtil.shortFromBytesLE(data, 4)
            return Status(speed, voltage, batt, current, power, distance, temperature)
        }

        fun parseLiveData5(): Status {
            voltage = MathsUtil.shortFromBytesLE(data, 0)
            current = MathsUtil.signedShortFromBytesLE(data, 2)
            power = voltage * current
            return Status(speed, voltage, batt, current, power, distance, temperature)
        }

        companion object {
            var batt = 0
            var speed = 0
            var distance = 0
            var temperature = 0
            var voltage = 0
            var current = 0
            var power = 0
            var serialNum = ""
            private fun computeCheck(buffer: ByteArray): Int {
                var check = 0
                for (c in buffer) {
                    check = check + (c.toInt() and 0xff)
                }
                check = check xor 0xFFFF
                check = check and 0xFFFF
                return check
            }

            fun verify(buffer: ByteArray): CANMessage? {
                Timber.i("Verifying")
                var dataBuffer = Arrays.copyOfRange(buffer, 2, buffer.size)
                dataBuffer = crypto(dataBuffer)
                val check =
                    dataBuffer[dataBuffer.size - 1].toInt() shl 8 or (dataBuffer[dataBuffer.size - 2].toInt() and 0xff) and 0xffff
                val dataBufferCheck = Arrays.copyOfRange(dataBuffer, 0, dataBuffer.size - 2)
                val checkBuffer = computeCheck(dataBufferCheck)
                if (check == checkBuffer) {
                    Timber.i("Check OK")
                } else {
                    Timber.i("Check FALSE, packet: %02X, calc: %02X", check, checkBuffer)
                }
                return if (check == checkBuffer) CANMessage(dataBuffer) else null
            }

            fun crypto(buffer: ByteArray): ByteArray {
                val dataBuffer = Arrays.copyOfRange(buffer, 0, buffer.size)
                Timber.i("Initial packet: %s", toHexString(dataBuffer))
                for (j in 1 until dataBuffer.size) {
                    dataBuffer[j] = (dataBuffer[j].toInt() xor gamma[(j - 1) % 16].toInt()).toByte()
                }
                Timber.i("En/Decrypted packet: %s", toHexString(dataBuffer))
                return dataBuffer
            }

            val serialNumber: CANMessage
                get() {
                    val msg = CANMessage()
                    msg.source = Addr.App.value
                    msg.destination = Addr.Controller.value
                    msg.parameter = Param.SerialNumber.value
                    msg.data = byteArrayOf(0x0e)
                    msg.len = msg.data.size + 2
                    msg.crc = 0
                    return msg
                }
            val version: CANMessage
                get() {
                    val msg = CANMessage()
                    msg.source = Addr.App.value
                    msg.destination = Addr.Controller.value
                    msg.parameter = Param.Firmware.value
                    msg.data = byteArrayOf(0x02)
                    msg.len = msg.data.size + 2
                    msg.crc = 0
                    return msg
                }
            val activationDate: CANMessage
                get() {
                    val msg = CANMessage()
                    msg.source = Addr.App.value
                    msg.destination = Addr.Controller.value
                    //            msg.command = Comm.Read.getValue();
                    msg.parameter = Param.ActivationDate.value
                    msg.data = byteArrayOf(0x02)
                    msg.len = msg.data.size + 2
                    msg.crc = 0
                    return msg
                }
            val liveData: CANMessage
                get() {
                    val msg = CANMessage()
                    msg.source = Addr.App.value
                    msg.destination = Addr.Controller.value
                    msg.parameter = Param.LiveData.value
                    msg.data = byteArrayOf(0x20)
                    msg.len = msg.data.size + 2
                    msg.crc = 0
                    return msg
                }
        }
    }

    fun charUpdated(data: ByteArray?): ArrayList<Status> {
        val outValues = ArrayList<Status>()
        Timber.i("Got data ")
        for (c in data!!) {
            if (unpacker.addChar(c.toInt())) {
                Timber.i("Starting verification")
                val result = CANMessage.verify(unpacker.getBuffer())
                if (result != null) { // data OK
                    Timber.i("Verification successful, command %02X", result.parameter)
                    if (result.parameter == CANMessage.Param.SerialNumber.value) {
                        Timber.i("Get serial number")
                        val infos = result.parseSerialNumber()
                        stateCon = 1
                        if (result.len - 2 == 14) {
                            if (infos != null) outValues.add(infos)
                        }
                    } else if (result.parameter == CANMessage.Param.SerialNumber2.value) {
                        Timber.i("Get serial number2")
                        val infos = result.parseSerialNumber2()
                    } else if (result.parameter == CANMessage.Param.SerialNumber3.value) {
                        Timber.i("Get serial number3")
                        val infos = result.parseSerialNumber2()
                        if (infos != null) outValues.add(infos)
                    } else if (result.parameter == CANMessage.Param.Firmware.value) {
                        Timber.i("Get version number")
                        val infos = result.parseVersionNumber()
                        stateCon = 2
                        if (infos != null) outValues.add(infos)
                    } else if (result.parameter == CANMessage.Param.LiveData.value) {
                        Timber.i("Get life data1")
                        if (result.len - 2 == 32) {
                            val status = result.parseLiveData()
                            if (status != null) {
                                outValues.add(status)
                            }
                        }
                    } else if (result.parameter == CANMessage.Param.LiveData2.value) {
                        Timber.i("Get life data2")
                        result.parseLiveData2()
                    } else if (result.parameter == CANMessage.Param.LiveData3.value) {
                        Timber.i("Get life data3")
                        result.parseLiveData3()
                    } else if (result.parameter == CANMessage.Param.LiveData4.value) {
                        Timber.i("Get life data4")
                        result.parseLiveData4()
                    } else if (result.parameter == CANMessage.Param.LiveData5.value) {
                        Timber.i("Get life data5")
                        val status = result.parseLiveData5()
                        if (status != null) {
                            outValues.add(status)
                        }
                    } else if (result.parameter == CANMessage.Param.LiveData6.value) {
                        Timber.i("Get life data")
                    }
                }
            }
        }
        return outValues
    }

    class NinebotUnpacker {
        enum class UnpackerState {
            unknown,
            started,
            collecting,
            done
        }

        var buffer = ByteArrayOutputStream()
        var oldc = 0
        var len = 0
        var state = UnpackerState.unknown
        fun getBuffer(): ByteArray {
            return buffer.toByteArray()
        }

        fun addChar(c: Int): Boolean {
            when (state) {
                UnpackerState.collecting -> {
                    buffer.write(c)
                    if (buffer.size() == len + 6) {
                        state = UnpackerState.done
                        updateStep = 0
                        Timber.i("Len %d", len)
                        Timber.i("Step reset")
                        return true
                    }
                }

                UnpackerState.started -> {
                    buffer.write(c)
                    len = c and 0xff
                    state = UnpackerState.collecting
                }

                else -> {
                    if (c == 0xAA.toByte().toInt() && oldc == 0x55.toByte().toInt()) {
                        Timber.i("Find start")
                        buffer = ByteArrayOutputStream()
                        buffer.write(0x55)
                        buffer.write(0xAA)
                        state = UnpackerState.started
                    }
                    oldc = c
                }
            }
            return false
        }

        fun reset() {
            buffer = ByteArrayOutputStream()
            oldc = 0
            state = UnpackerState.unknown
        }
    }

    companion object {
        private var INSTANCE: NinebotAdapter? = null
        private var updateStep = 0
        private var gamma = ByteArray(16)
        private var stateCon = 0
        private var protoVersion: Byte = 0
        @JvmStatic
        val instance: NinebotAdapter?
            get() {
                Timber.i("Get instance")
                if (INSTANCE == null) {
                    Timber.i("New instance")
                    INSTANCE = NinebotAdapter()
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
            INSTANCE = NinebotAdapter()
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
