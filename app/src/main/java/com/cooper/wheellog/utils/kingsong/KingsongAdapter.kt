package com.cooper.wheellog.utils.kingsong

import com.cooper.wheellog.AppConfig
import com.cooper.wheellog.WheelData
import com.cooper.wheellog.WheelLog
import com.cooper.wheellog.utils.BaseAdapter
import com.cooper.wheellog.utils.MathsUtil
import com.cooper.wheellog.utils.kingsong.KingsongUtils.getEmptyRequest
import com.cooper.wheellog.utils.kingsong.KingsongUtils.is100vWheel
import com.cooper.wheellog.utils.kingsong.KingsongUtils.is126vWheel
import com.cooper.wheellog.utils.kingsong.KingsongUtils.is84vWheel
import timber.log.Timber
import java.util.Locale

class KingsongAdapter(
    private val wd: WheelData,
    private val appConfig: AppConfig,
    private val kingsongLiveDataDecoder: KingSongLiveDataDecoder,
    private val kingsongFFFrameDecoder: KingSongFFFrameDecoder
) : BaseAdapter() {
    private var mKSAlarm1Speed = 0
    private var mKSAlarm2Speed = 0
    private var mKSAlarm3Speed = 0
    private var mWheelMaxSpeed = 0
    private var m18Lkm = true
    var mode = 0
        private set
    var speedLimit = 0.0
        private set

    override fun decode(data: ByteArray?): Boolean {
        Timber.i("Decode KingSong")
        wd.resetRideTime()
        if (data!!.size >= 20) {
            val a1 = data[0].toInt() and 255
            val a2 = data[1].toInt() and 255
            if (a1 != 170 || a2 != 85) {
                return false
            }
            if (data[16].toInt() and 255 == 0xA9) {
                mode = kingsongLiveDataDecoder.decode(data, m18Lkm, mode)
                return true
            } else if (data[16].toInt() and 255 == 0xB9) { // Distance/Time/Fan Data
                decodeKingsongDistanceTimeFan(data)
                return false
            } else if (data[16].toInt() and 255 == 187) { // Name and Type data
                decodeKingSongNameAndTypeData(data)
                return false
            } else if (data[16].toInt() and 255 == 0xB3) { // Serial Number
                decodeKingSongSerialNumber(data)
                return false
            } else if (data[16].toInt() and 255 == 0xF5) { //cpu load
                decodeKingSongCpuLoad(data)
                return false
            } else if (data[16].toInt() and 255 == 0xF6) { //speed limit (PWM?)
                decodeKingSongSpeed(data)
                return false
            } else if (data[16].toInt() and 255 == 0xA4 || data[16].toInt() and 255 == 0xB5) { //max speed and alerts
                decodeKingSongMaxSpeedAndAlerts(data)
                return true
            } else if (data[16].toInt() and 255 == 0xF1 || data[16].toInt() and 255 == 0xF2) { // F1 - 1st BMS, F2 - 2nd BMS. F3 and F4 are also present but empty
                decodeFFrames(data)
            } else if (data[16].toInt() and 255 == 0xe1 || data[16].toInt() and 255 == 0xe2) { // e1 - 1st BMS, e2 - 2nd BMS.
                decodeE1E2Frames(data)
            } else if (data[16].toInt() and 255 == 0xe5 || data[16].toInt() and 255 == 0xe6) { // e5 - 1st BMS, e6 - 2nd BMS.
                deocdE5E6Frames(data)
            }
        }
        return false
    }

    private fun deocdE5E6Frames(data: ByteArray?) {
        val bmsnum = (data!![16].toInt() and 255) - 0xE4
        val bms = if (bmsnum == 1) wd.bms1 else wd.bms2
        val sndata = ByteArray(19)
        System.arraycopy(data, 2, sndata, 0, 14)
        System.arraycopy(data, 17, sndata, 14, 3)
        sndata[18] = 0.toByte()
        bms.versionNumber = String(sndata)
    }

    private fun decodeE1E2Frames(data: ByteArray?) {
        val bmsnum = (data!![16].toInt() and 255) - 0xE0
        val bms = if (bmsnum == 1) wd.bms1 else wd.bms2
        val sndata = ByteArray(18)
        System.arraycopy(data, 2, sndata, 0, 14)
        System.arraycopy(data, 17, sndata, 14, 3)
        sndata[17] = 0.toByte()
        bms.serialNumber = String(sndata)
    }

    private fun decodeFFrames(data: ByteArray?) {
        kingsongFFFrameDecoder.decode(data!!)
    }

    private fun decodeKingSongMaxSpeedAndAlerts(data: ByteArray?) {
        mWheelMaxSpeed = data!![10].toInt() and 255
        appConfig.wheelMaxSpeed = mWheelMaxSpeed
        mKSAlarm3Speed = data[8].toInt() and 255
        mKSAlarm2Speed = data[6].toInt() and 255
        mKSAlarm1Speed = data[4].toInt() and 255
        appConfig.wheelKsAlarm3 = mKSAlarm3Speed
        appConfig.wheelKsAlarm2 = mKSAlarm2Speed
        appConfig.wheelKsAlarm1 = mKSAlarm1Speed
        // after received 0xa4 send same repeat data[2] =0x01 data[16] = 0x98
        if (data[16].toInt() and 255 == 164) {
            data[16] = 0x98.toByte()
            wd.bluetoothCmd(data)
        }
    }

    private fun decodeKingSongSpeed(data: ByteArray?) {
        speedLimit = MathsUtil.getInt2R(data, 2) / 100.0
        wd.speedLimit = speedLimit
    }

    private fun decodeKingSongCpuLoad(data: ByteArray?) {
        wd.cpuLoad = data!![14].toInt()
        wd.output = data[15] * 100
    }

    private fun decodeKingSongSerialNumber(data: ByteArray?) {
        val sndata = ByteArray(18)
        System.arraycopy(data, 2, sndata, 0, 14)
        System.arraycopy(data, 17, sndata, 14, 3)
        sndata[17] = 0.toByte()
        wd.serial = String(sndata)
        updateKSAlarmAndSpeed()
    }

    private fun decodeKingSongNameAndTypeData(data: ByteArray?) {
        var end = 0
        var i = 0
        while (i < 14 && data!![i + 2].toInt() != 0) {
            end++
            i++
        }
        wd.name = String(data!!, 2, end).trim { it <= ' ' }
        wd.model = ""
        val ss = wd.name.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val model = StringBuilder()
        i = 0
        while (i < ss.size - 1) {
            if (i != 0) {
                model.append("-")
            }
            model.append(ss[i])
            i++
        }
        wd.model = model.toString()
        try {
            wd.version = String.format(Locale.US, "%.2f", ss[ss.size - 1].toInt() / 100.0)
        } catch (ignored: Exception) {
        }
    }

    private fun decodeKingsongDistanceTimeFan(data: ByteArray?) {
        val distance = MathsUtil.getInt4R(data, 2).toLong()
        wd.setWheelDistance(distance)
        wd.updateRideTime()
        wd.topSpeed = MathsUtil.getInt2R(data, 8)
        wd.fanStatus = data!![12].toInt()
        wd.chargingStatus = data[13].toInt()
        wd.temperature2 = MathsUtil.getInt2R(data, 14)
    }

    override val isReady: Boolean
        get() = (wd.model != "Unknown"
                && wd.voltage != 0)

    override fun updatePedalsMode(pedalsMode: Int) {
        val data = getEmptyRequest()
        data[2] = pedalsMode.toByte()
        data[3] = 0xE0.toByte()
        data[16] = 0x87.toByte()
        data[17] = 0x15.toByte()
        wd.bluetoothCmd(data)
    }

    override fun wheelCalibration() {
        val data = getEmptyRequest()
        data[16] = 0x89.toByte()
        wd.bluetoothCmd(data)
    }

    override fun switchFlashlight() {
        var lightMode = appConfig.lightMode.toInt() + 1
        if (lightMode > 2) {
            lightMode = 0
        }
        appConfig.lightMode = lightMode.toString()
        setLightMode(lightMode)
    }

    override fun setLightMode(lightMode: Int) {
        val data = getEmptyRequest()
        data[2] = (lightMode + 0x12).toByte()
        data[3] = 0x01.toByte()
        data[16] = 0x73.toByte()
        wd.bluetoothCmd(data)
    }

    override val cellsForWheel: Int
        get() {
            var cells = 16
            if (is84vWheel(wd)) {
                cells = 20
            } else if (is126vWheel(wd)) {
                cells = 30
            } else if (is100vWheel(wd)) {
                cells = 24
            }
            return cells
        }

    override fun updateMaxSpeed(maxSpeed: Int) {
        mWheelMaxSpeed = maxSpeed
        updateKSAlarmAndSpeed()
    }

    fun updateKSAlarmAndSpeed() {
        val data = getEmptyRequest()
        data[2] = mKSAlarm1Speed.toByte()
        data[4] = mKSAlarm2Speed.toByte()
        data[6] = mKSAlarm3Speed.toByte()
        data[8] = mWheelMaxSpeed.toByte()
        data[16] = 0x85.toByte()
        if (mWheelMaxSpeed or mKSAlarm3Speed or mKSAlarm2Speed or mKSAlarm1Speed == 0) {
            data[16] = 0x98.toByte() // request speed & alarm values from wheel
        }
        wd.bluetoothCmd(data)
    }

    fun updateKSAlarm1(wheelKSAlarm1: Int) {
        if (mKSAlarm1Speed != wheelKSAlarm1) {
            mKSAlarm1Speed = wheelKSAlarm1
            updateKSAlarmAndSpeed()
        }
    }

    fun updateKSAlarm2(wheelKSAlarm2: Int) {
        if (mKSAlarm2Speed != wheelKSAlarm2) {
            mKSAlarm2Speed = wheelKSAlarm2
            updateKSAlarmAndSpeed()
        }
    }

    fun updateKSAlarm3(wheelKSAlarm3: Int) {
        if (mKSAlarm3Speed != wheelKSAlarm3) {
            mKSAlarm3Speed = wheelKSAlarm3
            updateKSAlarmAndSpeed()
        }
    }

    fun set18Lkm(enabled: Boolean) {
        m18Lkm = enabled
        if (wd.model.compareTo("KS-18L") == 0 && !m18Lkm) {
            wd.totalDistance =
                Math.round(wd.totalDistance * KS18L_SCALER)
        }
    }

    override fun wheelBeep() {
        val data = getEmptyRequest()
        data[16] = 0x88.toByte()
        wd.bluetoothCmd(data)
    }

    fun requestNameData() {
        val data = getEmptyRequest()
        data[16] = 0x9B.toByte()
        wd.bluetoothCmd(data)
    }

    fun requestSerialData() {
        val data = getEmptyRequest()
        data[16] = 0x63
        wd.bluetoothCmd(data)
    }

    fun requestAlarmSettingsAndMaxSpeed() {
        val data = getEmptyRequest()
        data[16] = 0x98.toByte()
        wd.bluetoothCmd(data)
    }

    override fun powerOff() {
        val data = getEmptyRequest()
        data[16] = 0x40.toByte()
        wd.bluetoothCmd(data)
    }

    override fun updateLedMode(ledMode: Int) {
        val data = getEmptyRequest()
        data[2] = ledMode.toByte()
        data[16] = 0x6C.toByte()
        wd.bluetoothCmd(data)
    }

    override fun updateStrobeMode(strobeMode: Int) {
        val data = getEmptyRequest()
        data[2] = strobeMode.toByte()
        data[16] = 0x53.toByte()
        wd.bluetoothCmd(data)
    }

    fun setChargingUpTo(chargeUpTo: Int) { // 100 => 100%
        val data = getEmptyRequest()
        data[2] = 0x09.toByte()
        data[4] = chargeUpTo.toByte()
        data[16] = 0x8a.toByte()
        wd.bluetoothCmd(data)
    }

    fun setStandByDelay(standByDelay: Int) { //3600 => 60 min => 1 hour
        val data = getEmptyRequest()
        data[2] = 0x01.toByte()
        data[4] = (standByDelay and 0xFF).toByte()
        data[5] = (standByDelay shr 8 and 0xFF).toByte()
        data[16] = 0x3f.toByte()
        wd.bluetoothCmd(data)
    }

    fun setGyroSwitchOff(gyroSwitchOff: Int) { //501 => 50.1 degree
        val data = getEmptyRequest()
        data[2] = 0x03.toByte()
        data[4] = (gyroSwitchOff and 0xFF).toByte()
        data[5] = (gyroSwitchOff shr 8 and 0xFF).toByte()
        data[16] = 0x8a.toByte()
        wd.bluetoothCmd(data)
    }

    fun setGyroFrontAngle(gyroFrontAngle: Int) { //-32 => -3.2 degree
        val data = getEmptyRequest()
        data[2] = 0x01.toByte()
        data[4] = (gyroFrontAngle and 0xFF).toByte()
        data[5] = (gyroFrontAngle shr 8 and 0xFF).toByte()
        data[16] = 0x8a.toByte()
        wd.bluetoothCmd(data)
    }

    companion object {
        private var INSTANCE: KingsongAdapter? = null
        const val KS18L_SCALER = 0.83
        @JvmStatic
        val instance: KingsongAdapter?
            get() {
                Timber.i("Get instance")
                if (INSTANCE == null) {
                    val wd = WheelData.getInstance()
                    val appConfig = WheelLog.AppConfig
                    Timber.i("New instance")
                    INSTANCE = KingsongAdapter(
                        wd,
                        appConfig,
                        KingSongLiveDataDecoder(wd, KingSongBatteryCalculator(wd, appConfig)),
                        KingSongFFFrameDecoder(wd)
                    )
                }
                return INSTANCE
            }
    }
}