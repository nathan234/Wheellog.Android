package com.cooper.wheellog.utils.ninebot

import com.cooper.wheellog.R
import com.cooper.wheellog.WheelData
import com.cooper.wheellog.WheelLog
import com.cooper.wheellog.utils.BaseAdapter
import com.cooper.wheellog.utils.MathsUtil
import com.cooper.wheellog.utils.StringUtil.toHexString
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.Arrays
import java.util.Timer
import java.util.TimerTask

/**
 * Created by palachzzz on 08/2018.
 */
class NinebotZAdapter : BaseAdapter() {
    private var keepAliveTimer: Timer? = null
    var settingCommandReady = false
    private var settingRequestReady = false
    lateinit var settingCommand: ByteArray
    private lateinit var settingRequest: ByteArray

    ///// wheel settings
    private var lockMode = 0
    private var limitedMode = 0
    private var limitModeSpeed = 0
    private var limitModeSpeed1Km = 0 // not sure (?)
    private val LimitModeSpeed = 0
    private var speakerVolume = 0
    private var alarms = 0
    private var alarm1Speed = 0
    private var alarm2Speed = 0
    private var alarm3Speed = 0
    private var ledMode = 0
    var ledColor1 = 0
        private set
    var ledColor2 = 0
        private set
    var ledColor3 = 0
        private set
    var ledColor4 = 0
        private set
    private var pedalSensivity = 0
    private var driveFlags = 0

    ///// end of wheel settings
    var unpacker = NinebotZUnpacker()
    fun startKeepAliveTimer() {
        Timber.i("Ninebot Z timer starting")
        updateStep = 0
        stateCon = 0
        val timerTask: TimerTask = object : TimerTask() {
            override fun run() {
                if (updateStep == 0) {
                    Timber.i("State connection %d", stateCon)
                    if (stateCon == 0) {
                        if (WheelData.instance!!
                                .bluetoothCmd(CANMessage.bleVersion.writeBuffer())
                        ) {
                            Timber.i("Sent start message")
                        } else Timber.i("Unable to send start message")
                    } else if (stateCon == 1) {
                        if (WheelData.instance!!.bluetoothCmd(CANMessage.key.writeBuffer())) {
                            Timber.i("Sent getkey message")
                        } else Timber.i("Unable to send getkey message")
                    } else if (stateCon == 2) {
                        if (WheelData.instance!!
                                .bluetoothCmd(CANMessage.serialNumber.writeBuffer())
                        ) {
                            Timber.i("Sent serial number message")
                        } else Timber.i("Unable to send serial number message")
                    } else if (stateCon == 3) {
                        if (WheelData.instance!!
                                .bluetoothCmd(CANMessage.version.writeBuffer())
                        ) {
                            Timber.i("Sent version message")
                        } else Timber.i("Unable to send version message")
                    } else if (stateCon == 4) {
                        if (WheelData.instance!!
                                .bluetoothCmd(CANMessage.params1.writeBuffer())
                        ) {
                            Timber.i("Sent getParams1 message")
                        } else Timber.i("Unable to send getParams1 message")
                    } else if (stateCon == 5) {
                        if (WheelData.instance!!
                                .bluetoothCmd(CANMessage.params2.writeBuffer())
                        ) {
                            Timber.i("Sent getParams2 message")
                        } else Timber.i("Unable to send getParams2 message")
                    } else if (stateCon == 6) {
                        if (WheelData.instance!!
                                .bluetoothCmd(CANMessage.params3.writeBuffer())
                        ) {
                            Timber.i("Sent getParams3 message")
                        } else Timber.i("Unable to send getParams2 message")
                    } else if (stateCon == 7) {
                        if (WheelData.instance!!.bluetoothCmd(CANMessage.bms1Sn.writeBuffer())) {
                            Timber.i("Sent BMS1 SN message")
                        } else Timber.i("Unable to send BMS1 SN message")
                    } else if (stateCon == 8) {
                        if (WheelData.instance!!
                                .bluetoothCmd(CANMessage.bms1Life.writeBuffer())
                        ) {
                            Timber.i("Sent BMS1 life message")
                        } else Timber.i("Unable to send BMS1 life message")
                    } else if (stateCon == 9) {
                        if (WheelData.instance!!
                                .bluetoothCmd(CANMessage.bms1Cells.writeBuffer())
                        ) {
                            Timber.i("Sent BMS1 cells message")
                        } else Timber.i("Unable to send BMS1 cells message")
                    } else if (stateCon == 10) {
                        if (WheelData.instance!!.bluetoothCmd(CANMessage.bms2Sn.writeBuffer())) {
                            Timber.i("Sent BMS2 SN message")
                        } else Timber.i("Unable to send BMS2 SN message")
                    } else if (stateCon == 11) {
                        if (WheelData.instance!!
                                .bluetoothCmd(CANMessage.bms2Life.writeBuffer())
                        ) {
                            Timber.i("Sent BMS2 life message")
                        } else Timber.i("Unable to send BMS2 life message")
                    } else if (stateCon == 12) {
                        if (WheelData.instance!!
                                .bluetoothCmd(CANMessage.bms2Cells.writeBuffer())
                        ) {
                            Timber.i("Sent BMS2 cells message")
                        } else Timber.i("Unable to send BMS2 cells message")
                    } else if (settingCommandReady) {
                        if (WheelData.instance!!.bluetoothCmd(settingCommand)) {
                            settingCommandReady = false
                            Timber.i("Sent command message")
                        } else Timber.i("Unable to send command message")
                    } else if (settingRequestReady) {
                        if (WheelData.instance!!.bluetoothCmd(settingRequest)) {
                            settingRequestReady = false
                            Timber.i("Sent settings request message")
                        } else Timber.i("Unable to send settings request message")
                    } else {
                        if (!WheelData.instance!!
                                .bluetoothCmd(CANMessage.liveData.writeBuffer())
                        ) {
                            Timber.i("Unable to send keep-alive message")
                        } else {
                            Timber.i("Sent keep-alive message")
                        }
                    }
                }
                updateStep += 1
                if (updateStep == 5 && stateCon > 6 && stateCon < 13) {
                    stateCon += 1
                    Timber.i("Change state to %d 1", stateCon)
                    if (stateCon > 12) stateCon = 7
                }
                if (bmsMode && stateCon == 13) {
                    stateCon = 7
                    Timber.i("Change state to %d 2", stateCon)
                }
                if (!bmsMode && stateCon > 6 && stateCon < 13) {
                    stateCon = 13
                    Timber.i("Change state to %d 3", stateCon)
                }
                updateStep %= 5
                Timber.i("Step: %d", updateStep)
            }
        }
        Timber.i("Ninebot Z timer started")
        keepAliveTimer = Timer()
        keepAliveTimer!!.scheduleAtFixedRate(timerTask, 200, 25)
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

    val wheelAlarmMax: Int
        //// mocks
        get() = 100
    val wheelLimitedSpeed: Int
        get() = 500

    fun getPedalSensivity(): Int {
        return pedalSensivity
    }

    fun getLedMode(): String {
        return ledMode.toString()
    }

    fun getSpeakerVolume(): Int {
        return speakerVolume
    }

    /// end of mocks
    fun setBmsReadingMode(mode: Boolean) {
        bmsMode = mode
    }

    override val cellsForWheel: Int
        get() = 14
    override val ledModeString: String
        get() = when (WheelLog.AppConfig.ledMode) {
            "0" -> context!!.getString(R.string.off)
            "1" -> context!!.getString(R.string.led_type1)
            "2" -> context!!.getString(R.string.led_type2)
            "3" -> context!!.getString(R.string.led_type3)
            "4" -> context!!.getString(R.string.led_type4)
            "5" -> context!!.getString(R.string.led_type5)
            "6" -> context!!.getString(R.string.led_type6)
            "7" -> context!!.getString(R.string.led_type7)
            else -> context!!.getString(R.string.led_mode_nb_description)
        }

    override fun getLedIsAvailable(ledNum: Int): Boolean {
        return when (WheelLog.AppConfig.ledMode) {
            "1", "4", "5" -> ledNum == 1
            "2" -> ledNum < 3
            "3" -> true
            else -> false
        }
    }

    override fun decode(data: ByteArray?): Boolean {
        Timber.i("Ninebot_z decoding")
        val wd = WheelData.instance!!
        setBmsReadingMode(wd.bmsView)
        var retResult = false
        for (c in data!!) {
            if (unpacker.addChar(c.toInt())) {
                Timber.i("Starting verification")
                val result = CANMessage.verify(unpacker.getBuffer())
                if (result != null) { // data OK
                    Timber.i("Verification successful, command %02X", result.parameter)
                    if (result.parameter == CANMessage.Param.BleVersion.value && result.source == CANMessage.Addr.Controller.value) {
                        Timber.i("Get start answer")
                        stateCon = 2
                    } else if (result.parameter == CANMessage.Param.GetKey.value && result.source == CANMessage.Addr.KeyGenerator.value) {
                        Timber.i("Get encryption key")
                        gamma = result.parseKey()
                        stateCon = 2
                        retResult = false
                    } else if (result.parameter == CANMessage.Param.SerialNumber.value && result.source == CANMessage.Addr.Controller.value) {
                        Timber.i("Get serial number")
                        result.parseSerialNumber()
                        stateCon = 3
                    } else if (result.parameter == CANMessage.Param.LockMode.value && result.source == CANMessage.Addr.Controller.value) {
                        Timber.i("Get param1 number")
                        result.parseParams1()
                        stateCon = 5
                    } else if (result.parameter == CANMessage.Param.LedMode.value && result.source == CANMessage.Addr.Controller.value) {
                        Timber.i("Get param2 number")
                        result.parseParams2()
                        stateCon = 6
                    } else if (result.parameter == CANMessage.Param.SpeakerVolume.value && result.source == CANMessage.Addr.Controller.value) {
                        Timber.i("Get param3 number")
                        result.parseParams3()
                        stateCon = 13
                    } else if (result.parameter == CANMessage.Param.Firmware.value && result.source == CANMessage.Addr.Controller.value) {
                        Timber.i("Get version number")
                        result.parseVersionNumber()
                        stateCon = 4
                    } else if (result.parameter == CANMessage.Param.LiveData.value && result.source == CANMessage.Addr.Controller.value) {
                        Timber.i("Get life data")
                        result.parseLiveData()
                        retResult = true
                    } else if (result.source == CANMessage.Addr.BMS1.value) {
                        Timber.i("Get info from BMS1")
                        if (result.parameter == 0x10) {
                            result.parseBmsSn(1)
                            stateCon = 8
                        }
                        if (result.parameter == 0x30) {
                            result.parseBmsLife(1)
                            stateCon = 9
                        }
                        if (result.parameter == 0x40) {
                            result.parseBmsCells(1)
                            stateCon = 10
                        }
                    } else if (result.source == CANMessage.Addr.BMS2.value) {
                        Timber.i("Get info from BMS2")
                        if (result.parameter == 0x10) {
                            result.parseBmsSn(2)
                            stateCon = 11
                        }
                        if (result.parameter == 0x30) {
                            result.parseBmsLife(2)
                            stateCon = 12
                        }
                        if (result.parameter == 0x40) {
                            result.parseBmsCells(2)
                            stateCon = 13
                        }
                    }
                }
            }
        }
        wd.resetRideTime()
        return retResult
    }

    override val isReady: Boolean
        get() = (WheelData.instance!!.serial != ""
                && WheelData.instance!!.version != "" && WheelData.instance!!.voltage != 0)

    override fun setDrl(drl: Boolean) {
        // ToDo check if it is the same as old value
        driveFlags = driveFlags and 0xFFFE or if (drl) 1 else 0 // need to have driveflags before
        settingRequest = CANMessage.params2.writeBuffer()
        settingRequestReady = true
        settingCommand = CANMessage.setDriveFlags(driveFlags).writeBuffer()
        settingCommandReady = true
    }

    override fun setLightState(lightEnable: Boolean) { //not working yet, need more tests
        // ToDo check if it is the same as old value
        driveFlags =
            driveFlags and 0xFFFB or ((if (lightEnable) 1 else 0) shl 2) // need to have driveflags before
        //driveFlags = (driveFlags & 0xFF7F) | ((lightEnable ? 1 : 0) << 7) ; // need to have driveflags before
        settingRequest = CANMessage.params2.writeBuffer()
        settingRequestReady = true
        settingCommand = CANMessage.setDriveFlags(driveFlags).writeBuffer()
        settingCommandReady = true
    }

    override fun setTailLightState(drl: Boolean) {
        // ToDo check if it is the same as old value
        driveFlags =
            driveFlags and 0xFFFD or ((if (drl) 1 else 0) shl 1) // need to have driveflags before
        settingRequest = CANMessage.params2.writeBuffer()
        settingRequestReady = true
        settingCommand = CANMessage.setDriveFlags(driveFlags).writeBuffer()
        settingCommandReady = true
    }

    override fun setHandleButtonState(handleButtonEnable: Boolean) {
        // ToDo check if it is the same as old value
        driveFlags =
            driveFlags and 0xFFF7 or ((if (handleButtonEnable) 0 else 1) shl 3) // need to have driveflags before
        settingRequest = CANMessage.params2.writeBuffer()
        settingRequestReady = true
        settingCommand = CANMessage.setDriveFlags(driveFlags).writeBuffer()
        settingCommandReady = true
    }

    override fun setBrakeAssist(brakeAssist: Boolean) {
        // ToDo check if it is the same as old value
        driveFlags =
            driveFlags and 0xFFEF or ((if (brakeAssist) 0 else 1) shl 4) // need to have driveflags before
        settingRequest = CANMessage.params2.writeBuffer()
        settingRequestReady = true
        settingCommand = CANMessage.setDriveFlags(driveFlags).writeBuffer()
        settingCommandReady = true
    }

    override fun setLedColor(value: Int, ledNum: Int) {
        settingRequest = CANMessage.params2.writeBuffer()
        settingRequestReady = true
        settingCommand = CANMessage.setLedColor(value, ledNum).writeBuffer()
        settingCommandReady = true
    }

    override fun setAlarmEnabled(value: Boolean, num: Int) {
        alarms =
            if (num == 1) alarms and 0xFFFE or (if (value) 1 else 0) else if (num == 2) alarms and 0xFFFD or ((if (value) 1 else 0) shl 1) else alarms and 0xFFFB or ((if (value) 1 else 0) shl 2)
        settingRequest = CANMessage.params1.writeBuffer()
        settingRequestReady = true
        settingCommand = CANMessage.setAlarms(alarms).writeBuffer()
        settingCommandReady = true
    }

    override fun setAlarmSpeed(value: Int, num: Int) {
        if (alarm1Speed != value) {
            settingRequest = CANMessage.params1.writeBuffer()
            settingRequestReady = true
            settingCommand = CANMessage.setAlarmSpeed(value, num).writeBuffer()
            settingCommandReady = true
        }
    }

    override fun setLimitedModeEnabled(value: Boolean) {
        if (limitedMode == 1 != value) {
            settingRequest = CANMessage.params1.writeBuffer()
            settingRequestReady = true
            settingCommand = CANMessage.setLimitedMode(value).writeBuffer()
            settingCommandReady = true
        }
    }

    override fun setLimitedSpeed(value: Int) {
        if (limitModeSpeed != value) {
            settingRequest = CANMessage.params1.writeBuffer()
            settingRequestReady = true
            settingCommand = CANMessage.setLimitedSpeed(value).writeBuffer()
            settingCommandReady = true
        }
    }

    override fun setPedalSensivity(value: Int) {
        if (pedalSensivity != value) {
            settingRequest = CANMessage.params2.writeBuffer()
            settingRequestReady = true
            settingCommand = CANMessage.setPedalSensivity(value).writeBuffer()
            settingCommandReady = true
        }
    }

    override fun updateLedMode(value: Int) {
        if (ledMode != value) {
            settingRequest = CANMessage.params2.writeBuffer()
            settingRequestReady = true
            settingCommand = CANMessage.setLedMode(value).writeBuffer()
            settingCommandReady = true
        }
    }

    override fun setSpeakerVolume(value: Int) {
        if (speakerVolume != value) {
            settingRequest = CANMessage.params1.writeBuffer()
            settingRequestReady = true
            settingCommand = CANMessage.setSpeakerVolume(value shl 3).writeBuffer()
            settingCommandReady = true
        }
    }

    override fun setLockMode(value: Boolean) {
        if (lockMode == 1 != value) {
            settingRequest = CANMessage.params1.writeBuffer()
            settingRequestReady = true
            settingCommand = CANMessage.setLockMode(value).writeBuffer()
            settingCommandReady = true
        }
    }

    override fun wheelCalibration() {
        settingCommand = CANMessage.runCalibration(true).writeBuffer()
        settingCommandReady = true
    }

    class CANMessage {
        internal enum class Addr(val value: Int) {
            BMS1(0x11),
            BMS2(0x12),
            Controller(0x14),
            KeyGenerator(0x16),
            App(0x3e)

        }

        internal enum class Comm(val value: Int) {
            Read(0x01),
            Write(0x03),
            Get(0x04),
            GetKey(0x5b)

        }

        internal enum class Param(val value: Int) {
            GetKey(0x00),
            SerialNumber(0x10),
            Firmware(0x1a),
            BatteryLevel(0x22),
            Angles(0x61),
            Bat1Fw(0x66),
            Bat2Fw(0x67),
            BleVersion(0x68),
            ActivationDate(0x69),
            LockMode(0x70),
            LimitedMode(0x72),
            LimitModeSpeed1Km(0x73),

            // not sure (?)
            LimitModeSpeed(0x74),
            Calibration(0x75),
            Alarms(0x7c),
            Alarm1Speed(0x7d),
            Alarm2Speed(0x7e),
            Alarm3Speed(0x7f),
            LiveData(0xb0),
            LedMode(0xc6),
            LedColor1(0xc8),
            LedColor2(0xca),
            LedColor3(0xcc),
            LedColor4(0xce),
            PedalSensivity(0xd2),
            DriveFlags(0xd3),

            // 1bit - Light(DRL?), 2bit - Taillight, 3bit- Light(???), 4bit - StrainGuage, 5bit - BrakeAssist, ,
            SpeakerVolume(0xf5)

        }

        var len = 0
        var source = 0
        var destination = 0
        var command = 0
        var parameter = 0
        lateinit var data: ByteArray
        var crc = 0

        internal constructor(bArr: ByteArray) {
            if (bArr.size < 7) return
            len = bArr[0].toInt() and 0xff
            source = bArr[1].toInt() and 0xff
            destination = bArr[2].toInt() and 0xff
            command = bArr[3].toInt() and 0xff
            parameter = bArr[4].toInt() and 0xff
            data = Arrays.copyOfRange(bArr, 5, bArr.size - 2)
            crc = bArr[bArr.size - 1].toInt() shl 8 + bArr[bArr.size - 2]
        }

        private constructor()

        fun writeBuffer(): ByteArray {
            val canBuffer = bytes
            val out = ByteArrayOutputStream()
            out.write(0x5A)
            out.write(0xA5)
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
                buff.write(command)
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

        fun parseKey(): ByteArray {
            val gammaTemp = Arrays.copyOfRange(data, 0, data.size)
            val gamma_text = StringBuilder()
            for (datum in data) {
                gamma_text.append(String.format("%02X", datum))
            }
            Timber.i("New key: %s", gamma_text.toString())
            return gammaTemp
        }

        fun parseSerialNumber() {
            val serialNumber = String(data)
            val wd = WheelData.instance!!
            wd.serial = serialNumber
            wd.model = ("Ninebot Z")
        }

        fun parseParams1() {
            instance!!.lockMode = MathsUtil.shortFromBytesLE(data, 0)
            instance!!.limitedMode = MathsUtil.shortFromBytesLE(data, 4)
            instance!!.limitModeSpeed1Km = MathsUtil.shortFromBytesLE(data, 6) / 100
            instance!!.limitModeSpeed = MathsUtil.shortFromBytesLE(data, 8) / 100
            instance!!.alarms = MathsUtil.shortFromBytesLE(data, 24)
            instance!!.alarm1Speed = MathsUtil.shortFromBytesLE(data, 26) / 100
            instance!!.alarm2Speed = MathsUtil.shortFromBytesLE(data, 28) / 100
            instance!!.alarm3Speed = MathsUtil.shortFromBytesLE(data, 30) / 100
            WheelLog.AppConfig.lockMode = instance!!.lockMode == 1
            WheelLog.AppConfig.wheelLimitedModeEnabled = instance!!.limitedMode == 1
            WheelLog.AppConfig.wheelLimitedModeSpeed = instance!!.limitModeSpeed
            WheelLog.AppConfig.wheelAlarm1Speed = instance!!.alarm1Speed
            WheelLog.AppConfig.wheelAlarm2Speed = instance!!.alarm2Speed
            WheelLog.AppConfig.wheelAlarm3Speed = instance!!.alarm3Speed
            WheelLog.AppConfig.wheelAlarm1Enabled = instance!!.alarms and 0x0001 == 1
            WheelLog.AppConfig.wheelAlarm2Enabled = instance!!.alarms shr 1 and 0x0001 == 1
            WheelLog.AppConfig.wheelAlarm3Enabled = instance!!.alarms shr 2 and 0x0001 == 1
        }

        fun parseParams2() {
            instance!!.ledMode = MathsUtil.shortFromBytesLE(data, 0)
            instance!!.ledColor1 = MathsUtil.intFromBytesLE(data, 4) shr 16 and 0xFF
            instance!!.ledColor2 = MathsUtil.intFromBytesLE(data, 8) shr 16 and 0xFF
            instance!!.ledColor3 = MathsUtil.intFromBytesLE(data, 12) shr 16 and 0xFF
            instance!!.ledColor4 = MathsUtil.intFromBytesLE(data, 16) shr 16 and 0xFF
            instance!!.pedalSensivity = MathsUtil.shortFromBytesLE(data, 24)
            instance!!.driveFlags = MathsUtil.shortFromBytesLE(data, 26)
            WheelLog.AppConfig.ledMode = instance!!.ledMode.toString()
            WheelLog.AppConfig.pedalSensivity = instance!!.pedalSensivity
            WheelLog.AppConfig.lightEnabled = instance!!.driveFlags shr 2 and 0x0001 == 1
            WheelLog.AppConfig.taillightEnabled = instance!!.driveFlags shr 1 and 0x0001 == 1
            WheelLog.AppConfig.drlEnabled = instance!!.driveFlags and 0x0001 == 1
            WheelLog.AppConfig.handleButtonDisabled = instance!!.driveFlags shr 3 and 0x0001 == 0
            WheelLog.AppConfig.brakeAssistantEnabled = instance!!.driveFlags shr 4 and 0x0001 == 1
        }

        fun parseParams3() {
            instance!!.speakerVolume = MathsUtil.shortFromBytesLE(data, 0) shr 3
            WheelLog.AppConfig.speakerVolume = instance!!.speakerVolume
        }

        fun parseVersionNumber() {
            var versionNumber = ""
            val wd = WheelData.instance!!
            versionNumber += String.format("%X.", data[1].toInt() and 0x0f)
            versionNumber += String.format("%1X.", data[0].toInt() shr 4 and 0x0f)
            versionNumber += String.format("%1X", data[0].toInt() and 0x0f)
            wd.version = versionNumber
        }

        fun parseActivationDate() { ////// ToDo: add to wheeldata
            val wd = WheelData.instance!!
            val activationDate = MathsUtil.shortFromBytesLE(data, 0)
            val year = activationDate shr 9
            val mounth = activationDate shr 5 and 0x0f
            val day = activationDate and 0x1f
            val activationDateStr = String.format("%02d.%02d.20%02d", day, mounth, year)
            //wd.setActivationDate(activationDateStr); fixme
        }

        fun parseLiveData() {
            val wd = WheelData.instance!!
            val errorcode = MathsUtil.shortFromBytesLE(data, 0)
            val alarmcode = MathsUtil.shortFromBytesLE(data, 2)
            val escstatus = MathsUtil.shortFromBytesLE(data, 4)
            val batt = MathsUtil.shortFromBytesLE(data, 8)
            val speed = MathsUtil.shortFromBytesLE(data, 10)
            val avgspeed = MathsUtil.shortFromBytesLE(data, 12)
            val distance = MathsUtil.intFromBytesLE(data, 14)
            val tripdistance = MathsUtil.shortFromBytesLE(data, 18) * 10
            val operatingtime = MathsUtil.shortFromBytesLE(data, 20)
            val temperature = MathsUtil.signedShortFromBytesLE(data, 22)
            val voltage = MathsUtil.shortFromBytesLE(data, 24)
            val current = MathsUtil.signedShortFromBytesLE(data, 26)
            //int speed = MathsUtil.shortFromBytesLE(data, 28); //the same as speed
            //int avgspeed = MathsUtil.shortFromBytesLE(data, 30); //the same as avgspeed
            val power = voltage * current / 100
            var alert: String
            //alert = String.format(Locale.ENGLISH, "error: %04X, warn: %04X, status: %04X", errorcode, alarmcode, escstatus);
            wd.speed = speed
            wd.voltage = voltage
            wd.current = (current)
            wd.totalDistance = (distance.toLong())
            wd.temperature = temperature * 10
            //wd.alert = (alert);
            wd.updateRideTime()
            wd.batteryLevel = (batt)
            wd.voltageSag = voltage
            wd.setPower(power)
        }

        fun parseBmsSn(bmsnum: Int) {
            val wd = WheelData.instance!!
            val serialNumber = String(data, 0, 14)
            var versionNumber = ""
            versionNumber += String.format("%X.", data[15])
            versionNumber += String.format("%1X.", data[14].toInt() shr 4 and 0x0f)
            versionNumber += String.format("%1X", data[14].toInt() and 0x0f)
            val factoryCap = MathsUtil.shortFromBytesLE(data, 16)
            val actualCap = MathsUtil.shortFromBytesLE(data, 18)
            val fullCycles = MathsUtil.shortFromBytesLE(data, 22)
            val chargeCount = MathsUtil.shortFromBytesLE(data, 24)
            val mfgDate = MathsUtil.shortFromBytesLE(data, 32)
            val year = mfgDate shr 9
            val mounth = mfgDate shr 5 and 0x0f
            val day = mfgDate and 0x1f
            val mfgDateStr = String.format("%02d.%02d.20%02d", day, mounth, year)
            val bms = if (bmsnum == 1) wd.bms1 else wd.bms2
            bms.serialNumber = serialNumber
            bms.versionNumber = versionNumber
            bms.factoryCap = factoryCap
            bms.actualCap = actualCap
            bms.fullCycles = fullCycles
            bms.chargeCount = chargeCount
            bms.mfgDateStr = mfgDateStr
        }

        fun parseBmsLife(bmsnum: Int) {
            val wd = WheelData.instance!!
            val bmsStatus = MathsUtil.shortFromBytesLE(data, 0)
            val remCap = MathsUtil.shortFromBytesLE(data, 2)
            val remPerc = MathsUtil.shortFromBytesLE(data, 4)
            val current = MathsUtil.signedShortFromBytesLE(data, 6)
            val voltage = MathsUtil.shortFromBytesLE(data, 8)
            val temp1 = data[10] - 20
            val temp2 = data[11] - 20
            val balanceMap = MathsUtil.shortFromBytesLE(data, 12)
            val health = MathsUtil.shortFromBytesLE(data, 22)
            val bms = if (bmsnum == 1) wd.bms1 else wd.bms2
            bms.status = bmsStatus
            bms.remCap = remCap
            bms.remPerc = remPerc
            bms.current = current / 100.0
            bms.voltage = voltage / 100.0
            bms.temp1 = temp1.toDouble()
            bms.temp2 = temp2.toDouble()
            bms.balanceMap = balanceMap
            bms.health = health
        }

        fun parseBmsCells(bmsnum: Int) {
            val wd = WheelData.instance!!
            val cell1 = MathsUtil.shortFromBytesLE(data, 0)
            val cell2 = MathsUtil.shortFromBytesLE(data, 2)
            val cell3 = MathsUtil.shortFromBytesLE(data, 4)
            val cell4 = MathsUtil.shortFromBytesLE(data, 6)
            val cell5 = MathsUtil.shortFromBytesLE(data, 8)
            val cell6 = MathsUtil.shortFromBytesLE(data, 10)
            val cell7 = MathsUtil.shortFromBytesLE(data, 12)
            val cell8 = MathsUtil.shortFromBytesLE(data, 14)
            val cell9 = MathsUtil.shortFromBytesLE(data, 16)
            val cell10 = MathsUtil.shortFromBytesLE(data, 18)
            val cell11 = MathsUtil.shortFromBytesLE(data, 20)
            val cell12 = MathsUtil.shortFromBytesLE(data, 22)
            val cell13 = MathsUtil.shortFromBytesLE(data, 24)
            val cell14 = MathsUtil.shortFromBytesLE(data, 26)
            val cell15 = MathsUtil.shortFromBytesLE(data, 28)
            val cell16 = MathsUtil.shortFromBytesLE(data, 30)
            val bms = if (bmsnum == 1) wd.bms1 else wd.bms2
            bms.cells[0] = cell1 / 1000.0
            bms.cells[1] = cell2 / 1000.0
            bms.cells[2] = cell3 / 1000.0
            bms.cells[3] = cell4 / 1000.0
            bms.cells[4] = cell5 / 1000.0
            bms.cells[5] = cell6 / 1000.0
            bms.cells[6] = cell7 / 1000.0
            bms.cells[7] = cell8 / 1000.0
            bms.cells[8] = cell9 / 1000.0
            bms.cells[9] = cell10 / 1000.0
            bms.cells[10] = cell11 / 1000.0
            bms.cells[11] = cell12 / 1000.0
            bms.cells[12] = cell13 / 1000.0
            bms.cells[13] = cell14 / 1000.0
            bms.cells[14] = cell15 / 1000.0
            bms.cells[15] = cell16 / 1000.0
            bms.minCell = bms.cells[0]
            for (i in 0..15) {
                val cell = bms.cells[i]
                if (cell > 0.0) {
                    if (bms.maxCell < cell) {
                        bms.maxCell = cell
                    }
                    if (bms.minCell > cell) {
                        bms.minCell = cell
                    }
                }
            }
            bms.cellDiff = bms.maxCell - bms.minCell
        }

        companion object {
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

            val bleVersion: CANMessage
                get() {
                    val msg = CANMessage()
                    msg.source = Addr.App.value
                    msg.destination = Addr.Controller.value
                    msg.command = Comm.Read.value
                    msg.parameter = Param.BleVersion.value
                    msg.data = byteArrayOf(0x02)
                    msg.len = msg.data.size
                    msg.crc = 0
                    return msg
                }
            val key: CANMessage
                get() {
                    val msg = CANMessage()
                    msg.source = Addr.App.value
                    msg.destination = Addr.KeyGenerator.value
                    msg.command = Comm.GetKey.value
                    msg.parameter = Param.GetKey.value
                    msg.data = byteArrayOf()
                    msg.len = msg.data.size
                    msg.crc = 0
                    return msg
                }
            val serialNumber: CANMessage
                get() {
                    val msg = CANMessage()
                    msg.source = Addr.App.value
                    msg.destination = Addr.Controller.value
                    msg.command = Comm.Read.value
                    msg.parameter = Param.SerialNumber.value
                    msg.data = byteArrayOf(0x0e)
                    msg.len = msg.data.size
                    msg.crc = 0
                    return msg
                }
            val version: CANMessage
                get() {
                    val msg = CANMessage()
                    msg.source = Addr.App.value
                    msg.destination = Addr.Controller.value
                    msg.command = Comm.Read.value
                    msg.parameter = Param.Firmware.value
                    msg.data = byteArrayOf(0x02)
                    msg.len = msg.data.size
                    msg.crc = 0
                    return msg
                }
            val activationDate: CANMessage
                get() {
                    val msg = CANMessage()
                    msg.source = Addr.App.value
                    msg.destination = Addr.Controller.value
                    msg.command = Comm.Read.value
                    msg.parameter = Param.ActivationDate.value
                    msg.data = byteArrayOf(0x02)
                    msg.len = msg.data.size
                    msg.crc = 0
                    return msg
                }
            val liveData: CANMessage
                get() {
                    val msg = CANMessage()
                    msg.source = Addr.App.value
                    msg.destination = Addr.Controller.value
                    msg.command = Comm.Read.value
                    msg.parameter = Param.LiveData.value
                    msg.data = byteArrayOf(0x20)
                    msg.len = msg.data.size
                    msg.crc = 0
                    return msg
                }
            val params1: CANMessage
                get() {
                    val msg = CANMessage()
                    msg.source = Addr.App.value
                    msg.destination = Addr.Controller.value
                    msg.command = Comm.Read.value
                    msg.parameter = Param.LockMode.value
                    msg.data = byteArrayOf(0x20)
                    msg.len = msg.data.size
                    msg.crc = 0
                    return msg
                }
            val params2: CANMessage
                get() {
                    val msg = CANMessage()
                    msg.source = Addr.App.value
                    msg.destination = Addr.Controller.value
                    msg.command = Comm.Read.value
                    msg.parameter = Param.LedMode.value
                    msg.data = byteArrayOf(0x1c)
                    msg.len = msg.data.size
                    msg.crc = 0
                    return msg
                }
            val params3: CANMessage
                get() {
                    val msg = CANMessage()
                    msg.source = Addr.App.value
                    msg.destination = Addr.Controller.value
                    msg.command = Comm.Read.value
                    msg.parameter = Param.SpeakerVolume.value
                    msg.data = byteArrayOf(0x02)
                    msg.len = msg.data.size
                    msg.crc = 0
                    return msg
                }

            fun setLimitedMode(on: Boolean): CANMessage {
                val value = if (on) 1.toByte() else 0
                val msg = CANMessage()
                msg.source = Addr.App.value
                msg.destination = Addr.Controller.value
                msg.command = Comm.Write.value
                msg.parameter = Param.LimitedMode.value
                msg.data = byteArrayOf(value)
                msg.len = msg.data.size
                msg.crc = 0
                return msg
            }

            val bms1Sn: CANMessage
                get() {
                    val msg = CANMessage()
                    msg.source = Addr.App.value
                    msg.destination = Addr.BMS1.value
                    msg.command = Comm.Read.value
                    msg.parameter = 0x10
                    msg.data = byteArrayOf(0x22)
                    msg.len = msg.data.size
                    msg.crc = 0
                    return msg
                }
            val bms1Life: CANMessage
                get() {
                    val msg = CANMessage()
                    msg.source = Addr.App.value
                    msg.destination = Addr.BMS1.value
                    msg.command = Comm.Read.value
                    msg.parameter = 0x30
                    msg.data = byteArrayOf(0x18)
                    msg.len = msg.data.size
                    msg.crc = 0
                    return msg
                }
            val bms1Cells: CANMessage
                get() {
                    val msg = CANMessage()
                    msg.source = Addr.App.value
                    msg.destination = Addr.BMS1.value
                    msg.command = Comm.Read.value
                    msg.parameter = 0x40
                    msg.data = byteArrayOf(0x20)
                    msg.len = msg.data.size
                    msg.crc = 0
                    return msg
                }
            val bms2Sn: CANMessage
                get() {
                    val msg = CANMessage()
                    msg.source = Addr.App.value
                    msg.destination = Addr.BMS2.value
                    msg.command = Comm.Read.value
                    msg.parameter = 0x10
                    msg.data = byteArrayOf(0x22)
                    msg.len = msg.data.size
                    msg.crc = 0
                    return msg
                }
            val bms2Life: CANMessage
                get() {
                    val msg = CANMessage()
                    msg.source = Addr.App.value
                    msg.destination = Addr.BMS2.value
                    msg.command = Comm.Read.value
                    msg.parameter = 0x30
                    msg.data = byteArrayOf(0x18)
                    msg.len = msg.data.size
                    msg.crc = 0
                    return msg
                }
            val bms2Cells: CANMessage
                get() {
                    val msg = CANMessage()
                    msg.source = Addr.App.value
                    msg.destination = Addr.BMS2.value
                    msg.command = Comm.Read.value
                    msg.parameter = 0x40
                    msg.data = byteArrayOf(0x20)
                    msg.len = msg.data.size
                    msg.crc = 0
                    return msg
                }

            fun setDriveFlags(drFl: Int): CANMessage {
                val msg = CANMessage()
                msg.source = Addr.App.value
                msg.destination = Addr.Controller.value
                msg.command = Comm.Write.value
                msg.parameter = Param.DriveFlags.value
                msg.data = byteArrayOf((drFl and 0xFF).toByte(), (drFl shr 8 and 0xFF).toByte())
                msg.len = msg.data.size
                msg.crc = 0
                return msg
            }

            fun setLedColor(value: Int, ledNum: Int): CANMessage {
                val msg = CANMessage()
                msg.source = Addr.App.value
                msg.destination = Addr.Controller.value
                msg.command = Comm.Write.value
                msg.parameter = Param.LedColor1.value + (ledNum - 1) * 2
                if (value < 256) {
                    msg.data = byteArrayOf(0xF0.toByte(), (value and 0xFF).toByte(), 0x00, 0x00)
                } else {
                    msg.data = byteArrayOf(0x00.toByte(), 0x00.toByte(), 0x00, 0x00)
                }
                msg.len = msg.data.size
                msg.crc = 0
                return msg
            }

            fun setAlarms(value: Int): CANMessage {
                val msg = CANMessage()
                msg.source = Addr.App.value
                msg.destination = Addr.Controller.value
                msg.command = Comm.Write.value
                msg.parameter = Param.Alarms.value
                msg.data = byteArrayOf((value and 0xFF).toByte(), (value shr 8 and 0xFF).toByte())
                msg.len = msg.data.size
                msg.crc = 0
                return msg
            }

            fun setAlarmSpeed(value: Int, alarmNum: Int): CANMessage {
                val speed = value * 100
                val msg = CANMessage()
                msg.source = Addr.App.value
                msg.destination = Addr.Controller.value
                msg.command = Comm.Write.value
                when (alarmNum) {
                    1 -> msg.parameter = Param.Alarm1Speed.value
                    2 -> msg.parameter = Param.Alarm2Speed.value
                    3 -> msg.parameter = Param.Alarm3Speed.value
                }
                msg.data = byteArrayOf((speed and 0xFF).toByte(), (speed shr 8 and 0xFF).toByte())
                msg.len = msg.data.size
                msg.crc = 0
                return msg
            }

            fun setLimitedSpeed(value: Int): CANMessage {
                val speed = value * 100
                val msg = CANMessage()
                msg.source = Addr.App.value
                msg.destination = Addr.Controller.value
                msg.command = Comm.Write.value
                msg.parameter = Param.LimitModeSpeed.value
                msg.data = byteArrayOf((speed and 0xFF).toByte(), (speed shr 8 and 0xFF).toByte())
                msg.len = msg.data.size
                msg.crc = 0
                return msg
            }

            fun setPedalSensivity(value: Int): CANMessage {
                val msg = CANMessage()
                msg.source = Addr.App.value
                msg.destination = Addr.Controller.value
                msg.command = Comm.Write.value
                msg.parameter = Param.PedalSensivity.value
                msg.data = byteArrayOf((value and 0xFF).toByte(), (value shr 8 and 0xFF).toByte())
                msg.len = msg.data.size
                msg.crc = 0
                return msg
            }

            fun setLedMode(value: Int): CANMessage {
                val msg = CANMessage()
                msg.source = Addr.App.value
                msg.destination = Addr.Controller.value
                msg.command = Comm.Write.value
                msg.parameter = Param.LedMode.value
                msg.data = byteArrayOf((value and 0xFF).toByte(), (value shr 8 and 0xFF).toByte())
                msg.len = msg.data.size
                msg.crc = 0
                return msg
            }

            fun setSpeakerVolume(value: Int): CANMessage {
                val msg = CANMessage()
                msg.source = Addr.App.value
                msg.destination = Addr.Controller.value
                msg.command = Comm.Write.value
                msg.parameter = Param.SpeakerVolume.value
                msg.data = byteArrayOf((value and 0xFF).toByte(), (value shr 8 and 0xFF).toByte())
                msg.len = msg.data.size
                msg.crc = 0
                return msg
            }

            fun setLockMode(on: Boolean): CANMessage {
                val value = if (on) 1.toByte() else 0
                val msg = CANMessage()
                msg.source = Addr.App.value
                msg.destination = Addr.Controller.value
                msg.command = Comm.Write.value
                msg.parameter = Param.LockMode.value
                msg.data = byteArrayOf(value, 0x00)
                msg.len = msg.data.size
                msg.crc = 0
                return msg
            }

            fun runCalibration(on: Boolean): CANMessage {
                val value = if (on) 1.toByte() else 0
                val msg = CANMessage()
                msg.source = Addr.App.value
                msg.destination = Addr.Controller.value
                msg.command = Comm.Write.value
                msg.parameter = Param.Calibration.value
                msg.data = byteArrayOf(value, 0x00)
                msg.len = msg.data.size
                msg.crc = 0
                return msg
            }
        }
    }

    class NinebotZUnpacker {
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
                    if (buffer.size() == len + 9) {
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
                    if (c == 0xA5.toByte().toInt() && oldc == 0x5A.toByte().toInt()) {
                        Timber.i("Find start")
                        buffer = ByteArrayOutputStream()
                        buffer.write(0x5A)
                        buffer.write(0xA5)
                        state = UnpackerState.started
                    }
                    oldc = c
                }
            }
            return false
        }
    }

    companion object {
        private var INSTANCE: NinebotZAdapter? = null
        private var updateStep = 0
        private var gamma = ByteArray(16)
        private var stateCon = 0
        private var bmsMode = false
        @JvmStatic
        val instance: NinebotZAdapter?
            get() {
                Timber.i("Get instance")
                if (INSTANCE == null) {
                    Timber.i("New instance")
                    INSTANCE = NinebotZAdapter()
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
            INSTANCE = NinebotZAdapter()
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
