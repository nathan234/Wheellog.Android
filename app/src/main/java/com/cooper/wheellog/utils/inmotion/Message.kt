package com.cooper.wheellog.utils.inmotion

import android.content.Context
import android.content.Intent
import com.cooper.wheellog.WheelData
import com.cooper.wheellog.WheelLog
import com.cooper.wheellog.utils.Constants
import com.cooper.wheellog.utils.MathsUtil
import com.cooper.wheellog.utils.StringUtil
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.Arrays
import java.util.Locale

class Message(
    private val wd: WheelData,
) {
    internal enum class Flag(val value: Int) {
        NoOp(0), Initial(0x11), Default(0x14)
    }

    internal enum class Command(val value: Int) {
        NoOp(0), MainVersion(0x01), MainInfo(0x02), Diagnistic(0x03), RealTimeInfo(0x04), BatteryRealTimeInfo(
            0x05,
        ),
        Something1(0x10), TotalStats(0x11), Settings(0x20), Control(0x60)
    }

    var flags = Flag.NoOp.value
    var len = 0
    var command = 0
    lateinit var data: ByteArray

    internal constructor(bArr: ByteArray) : this() {
        if (bArr.size < 5) return
        flags = bArr[2].toInt()
        len = bArr[3].toInt()
        command = bArr[4].toInt() and 0x7F
        if (len > 1) {
            data = Arrays.copyOfRange(bArr, 5, len + 4)
        }
    }

    private constructor() : this(WheelData.getInstance())

    fun parseMainData(stateCon: Int): ParseMainDataResult {
        Timber.i("Parse main data")
        wd.resetRideTime()
        var stateCon = stateCon
        when {
            data[0] == 0x01.toByte() && len >= 6 -> {
                stateCon += 1
                Timber.i("Parse car type")
                // 020601010100 -v11
                // 020701010100 -v12
                // 020801010100 -v13
                val mainSeries = data[1].toInt() // 02
                val series = data[2].toInt() // 06
                val type = data[3].toInt() // 01
                val batch = data[4].toInt() // 02
                val feature = data[5].toInt() // 01
                val reverse = data[6].toInt() // 00
                InmotionAdapterV2.inMotionModel = InMotionModel.findById(series)
                wd.model = InmotionAdapterV2.inMotionModel.wheelName
                wd.version = String.format(Locale.ENGLISH, "-") // need to find how to parse
            }
            data[0] == 0x02.toByte() && len >= 17 -> {
                stateCon += 1
                Timber.i("Parse serial num")
                val serialNumber = String(data, 1, 16)
                wd.serial = serialNumber
            }
            data[0] == 0x06.toByte() && len >= 24 -> {
                Timber.i("Parse versions")
                InmotionAdapterV2.protoVer = 0
                val DriverBoard3 = MathsUtil.shortFromBytesLE(data, 2)
                val DriverBoard2 = data[4].toInt()
                val DriverBoard1 = data[5].toInt()
                val DriverBoard =
                    String.format(Locale.US, "%d.%d.%d", DriverBoard1, DriverBoard2, DriverBoard3)
                val smth13 = MathsUtil.shortFromBytesLE(data, 6)
                val smth12 = data[8].toInt()
                val smth11 = data[9].toInt()
                val smth1 = String.format(Locale.US, "%d.%d.%d", smth11, smth12, smth13)
                val MainBoard3 = MathsUtil.shortFromBytesLE(data, 11)
                val MainBoard2 = data[13].toInt()
                val MainBoard1 = data[14].toInt()
                val MainBoard =
                    String.format(Locale.US, "%d.%d.%d", MainBoard1, MainBoard2, MainBoard3)
                val smth23 = MathsUtil.shortFromBytesLE(data, 16)
                val smth22 = data[18].toInt()
                val smth21 = data[19].toInt()
                val smth2 = String.format(Locale.US, "%d.%d.%d", smth21, smth22, smth23)
                val Ble3 = MathsUtil.shortFromBytesLE(data, 20)
                val Ble2 = data[22].toInt()
                val Ble1 = data[23].toInt()
                val Ble = String.format(Locale.US, "%d.%d.%d", Ble1, Ble2, Ble3)
                val smth33 = MathsUtil.shortFromBytesLE(data, 16)
                val smth32 = data[18].toInt()
                val smth31 = data[19].toInt()
                val smth3 = String.format(Locale.US, "%d.%d.%d", smth31, smth32, smth33)
                val vers =
                    String.format(Locale.US, "Main:%s Drv:%s BLE:%s", MainBoard, DriverBoard, Ble)
                wd.version = vers
                if (InmotionAdapterV2.inMotionModel == InMotionModel.V11) {
                    if (MainBoard1 < 2 && MainBoard2 < 4) { // main board ver before 1.4
                        InmotionAdapterV2.protoVer = 1
                    } else {
                        InmotionAdapterV2.protoVer = 2 // main board 1.4+
                    }
                }
            }
        }
        return ParseMainDataResult(
            stateCon = stateCon,
            decodeResult = false,
        )
    }

    class ParseMainDataResult(
        val stateCon: Int,
        val decodeResult: Boolean
    )

    fun parseBatteryRealTimeInfo(): Boolean {
        val bat1Voltage = MathsUtil.shortFromBytesLE(data, 0)
        val bat1Temp = data[4].toInt()
        val bat1ValidStatus = data[5].toInt() and 1
        val bat1Enabled = data[5].toInt() shr 1 and 1
        val bat1WorkStatus1 = data[6].toInt() and 1
        val bat1WorkStatus2 = data[6].toInt() shr 1 and 1
        val bat2Voltage = MathsUtil.shortFromBytesLE(data, 8)
        val bat2Temp = data[12].toInt()
        val bat2ValidStatus = data[13].toInt() and 1
        val bat2Enabled = data[13].toInt() shr 1 and 1
        val bat2WorkStatus1 = data[14].toInt() and 1
        val bat2WorkStatus2 = data[14].toInt() shr 1 and 1
        val chargeVoltage = MathsUtil.shortFromBytesLE(data, 16)
        val chargeCurrent = MathsUtil.shortFromBytesLE(data, 18)
        return false
    }

    fun parseDiagnostic(): Boolean {
        var ok = true
        if (data.size > 7) {
            for (c in data) {
                if (c.toInt() != 0) {
                    ok = false
                    break
                }
            }
        }
        return false
    }

    fun parseSettings(): Boolean {
        Timber.i("Parse settings data")
        val i = 1
        val mSpeedLim = MathsUtil.shortFromBytesLE(data, i)
        val mPitchAngleZero = MathsUtil.signedShortFromBytesLE(data, i + 2)
        val mDriveMode = data[i + 4].toInt() and 0xF
        val mRideMode = data[i + 4].toInt() shr 4
        val mComfSens = data[i + 5].toInt()
        val mClassSens = data[i + 6].toInt()
        val mVolume = data[i + 7].toInt()
        val mAudioId = MathsUtil.intFromBytesLE(data, i + 8)
        val mStandByTime = MathsUtil.shortFromBytesLE(data, i + 12)
        val mDecorLightMode = data[i + 14].toInt()
        val mAutoLightLowThr = data[i + 15].toInt()
        val mAutoLightHighThr = data[i + 16].toInt()
        val mLightBr = data[i + 17].toInt()
        val mAudioState = data[i + 20].toInt() and 3
        val mDecorState = data[i + 20].toInt() shr 2 and 3
        val mLiftedState = data[i + 20].toInt() shr 4 and 3
        val mAutoLightState = data[i + 20].toInt() shr 6 and 3
        val mAutoLightBrState = data[i + 21].toInt() and 3
        val mLockState = data[i + 21].toInt() shr 2 and 3
        val mTranspMode = data[i + 21].toInt() shr 4 and 3
        val mLoadDetect = data[i + 21].toInt() shr 6 and 3
        val mNoLoadDetect = data[i + 22].toInt() and 3
        val mLowBat = data[i + 22].toInt() shr 2 and 3
        val mFanQuiet = data[i + 22].toInt() shr 4 and 3
        val mFan = data[i + 22].toInt() shr 6 and 3 // to test
        val mSome1 = data[i + 23].toInt() and 3 // to test
        val mSome2 = data[i + 23].toInt() shr 2 and 3 // to test
        val mSome3 = data[i + 23].toInt() shr 4 and 3 // to test
        val mSome4 = data[i + 23].toInt() shr 6 and 3 // to test
        WheelLog.AppConfig.pedalsAdjustment = mPitchAngleZero / 10
        WheelLog.AppConfig.wheelMaxSpeed = mSpeedLim / 100
        WheelLog.AppConfig.fancierMode = mRideMode != 0
        WheelLog.AppConfig.rideMode = mDriveMode != 0
        WheelLog.AppConfig.pedalSensivity = mComfSens
        WheelLog.AppConfig.speakerVolume = mVolume
        WheelLog.AppConfig.lightBrightness = mLightBr
        WheelLog.AppConfig.speakerMute = mAudioState == 0
        WheelLog.AppConfig.drlEnabled = mDecorState != 0
        WheelLog.AppConfig.handleButtonDisabled = mLiftedState == 0
        WheelLog.AppConfig.lockMode = mLockState != 0
        WheelLog.AppConfig.transportMode = mTranspMode != 0
        WheelLog.AppConfig.fanQuietEnabled = mFanQuiet != 0
        WheelLog.AppConfig.goHomeMode = mLowBat != 0
        return false
    }

    fun parseTotalStats(): Boolean {
        if (data.size < 20) {
            return false
        }
        Timber.i("Parse total stats data")
        val mTotal = MathsUtil.intFromBytesLE(data, 0).toLong()
        val mTotal2 = MathsUtil.getInt4(data, 0)
        val mDissipation = MathsUtil.intFromBytesLE(data, 4).toLong()
        val mRecovery = MathsUtil.intFromBytesLE(data, 8).toLong()
        val mRideTime = MathsUtil.intFromBytesLE(data, 12).toLong()
        var sec = (mRideTime % 60).toInt()
        var min = (mRideTime / 60 % 60).toInt()
        var hour = (mRideTime / 3600).toInt()
        val mRideTimeStr = String.format("%d:%02d:%02d", hour, min, sec)
        val mPowerOnTime = MathsUtil.intFromBytesLE(data, 16).toLong()
        sec = (mPowerOnTime % 60).toInt()
        min = (mPowerOnTime / 60 % 60).toInt()
        hour = (mPowerOnTime / 3600).toInt()
        val mPowerOnTimeStr = String.format("%d:%02d:%02d", hour, min, sec)
        wd.totalDistance = mTotal * 10
        return false
    }

    private fun getError(i: Int): String {
        var inmoError = ""
        if (data[i].toInt() and 0x01 == 1) inmoError += "err_iPhaseSensorState "
        if (data[i].toInt() shr 1 and 0x01 == 1) inmoError += "err_iBusSensorState "
        if (data[i].toInt() shr 2 and 0x01 == 1) inmoError += "err_motorHallState "
        if (data[i].toInt() shr 3 and 0x01 == 1) inmoError += "err_batteryState "
        if (data[i].toInt() shr 4 and 0x01 == 1) inmoError += "err_imuSensorState "
        if (data[i].toInt() shr 5 and 0x01 == 1) inmoError += "err_controllerCom1State "
        if (data[i].toInt() shr 6 and 0x01 == 1) inmoError += "err_controllerCom2State "
        if (data[i].toInt() shr 7 and 0x01 == 1) inmoError += "err_bleCom1State "
        if (data[i + 1].toInt() and 0x01 == 1) inmoError += "err_bleCom2State "
        if (data[i + 1].toInt() shr 1 and 0x01 == 1) inmoError += "err_mosTempSensorState "
        if (data[i + 1].toInt() shr 2 and 0x01 == 1) inmoError += "err_motorTempSensorState "
        if (data[i + 1].toInt() shr 3 and 0x01 == 1) inmoError += "err_batteryTempSensorState "
        if (data[i + 1].toInt() shr 4 and 0x01 == 1) inmoError += "err_boardTempSensorState "
        if (data[i + 1].toInt() shr 5 and 0x01 == 1) inmoError += "err_fanState "
        if (data[i + 1].toInt() shr 6 and 0x01 == 1) inmoError += "err_rtcState "
        if (data[i + 1].toInt() shr 7 and 0x01 == 1) inmoError += "err_externalRomState "
        if (data[i + 2].toInt() and 0x01 == 1) inmoError += "err_vBusSensorState "
        if (data[i + 2].toInt() shr 1 and 0x01 == 1) inmoError += "err_vBatterySensorState "
        if (data[i + 2].toInt() shr 2 and 0x01 == 1) inmoError += "err_canNotPowerOffState"
        if (data[i + 2].toInt() shr 3 and 0x01 == 1) inmoError += "err_notKnown1 "
        if (data[i + 3].toInt() and 0x01 == 1) inmoError += "err_underVoltageState "
        if (data[i + 3].toInt() shr 1 and 0x01 == 1) inmoError += "err_overVoltageState "
        if (data[i + 3].toInt() shr 2 and 0x03 > 0) inmoError += "err_overBusCurrentState-" + (data[43].toInt() shr 2 and 0x03).toString() + " "
        if (data[i + 3].toInt() shr 4 and 0x03 > 0) inmoError += "err_lowBatteryState-" + (data[43].toInt() shr 4 and 0x03).toString() + " "
        if (data[i + 3].toInt() shr 6 and 0x01 == 1) inmoError += "err_mosTempState "
        if (data[i + 3].toInt() shr 7 and 0x01 == 1) inmoError += "err_motorTempState "
        if (data[i + 4].toInt() and 0x01 == 1) inmoError += "err_batteryTempState "
        if (data[i + 4].toInt() shr 1 and 0x01 == 1) inmoError += "err_overBoardTempState "
        if (data[i + 4].toInt() shr 2 and 0x01 == 1) inmoError += "err_overSpeedState "
        if (data[i + 4].toInt() shr 3 and 0x01 == 1) inmoError += "err_outputSaturationState "
        if (data[i + 4].toInt() shr 4 and 0x01 == 1) inmoError += "err_motorSpinState "
        if (data[i + 4].toInt() shr 5 and 0x01 == 1) inmoError += "err_motorBlockState "
        if (data[i + 4].toInt() shr 6 and 0x01 == 1) inmoError += "err_postureState "
        if (data[i + 4].toInt() shr 7 and 0x01 == 1) inmoError += "err_riskBehaviourState "
        if (data[i + 5].toInt() and 0x01 == 1) inmoError += "err_motorNoLoadState "
        if (data[i + 5].toInt() shr 1 and 0x01 == 1) inmoError += "err_noSelfTestState "
        if (data[i + 5].toInt() shr 2 and 0x01 == 1) inmoError += "err_compatibilityState "
        if (data[i + 5].toInt() shr 3 and 0x01 == 1) inmoError += "err_powerKeyLongPressState "
        if (data[i + 5].toInt() shr 4 and 0x01 == 1) inmoError += "err_forceDfuState "
        if (data[i + 5].toInt() shr 5 and 0x01 == 1) inmoError += "err_deviceLockState "
        if (data[i + 5].toInt() shr 6 and 0x01 == 1) inmoError += "err_cpuOverTempState "
        if (data[i + 5].toInt() shr 7 and 0x01 == 1) inmoError += "err_imuOverTempState "
        if (data[i + 6].toInt() shr 1 and 0x01 == 1) inmoError += "err_hwCompatibilityState "
        if (data[i + 6].toInt() shr 2 and 0x01 == 1) inmoError += "err_fanLowSpeedState "
        if (data[i + 6].toInt() shr 3 and 0x01 == 1) inmoError += "err_notKnown2 "
        return inmoError
    }

    fun parseRealTimeInfoV11(sContext: Context?, lightSwitchCounter: Int): Pair<Int, Boolean> {
        var lightSwitchCounter = lightSwitchCounter
        Timber.i("Parse V11 realtime stats data")
        val mVoltage = MathsUtil.shortFromBytesLE(data, 0)
        val mCurrent = MathsUtil.signedShortFromBytesLE(data, 2)
        val mSpeed = MathsUtil.signedShortFromBytesLE(data, 4)
        val mTorque = MathsUtil.signedShortFromBytesLE(data, 6)
        val mBatPower = MathsUtil.signedShortFromBytesLE(data, 8)
        val mMotPower = MathsUtil.signedShortFromBytesLE(data, 10)
        val mMileage = MathsUtil.shortFromBytesLE(data, 12) * 10
        val mRemainMileage = MathsUtil.shortFromBytesLE(data, 14) * 10
        val mBatLevel = data[16].toInt() and 0x7f
        val mBatMode = data[16].toInt() shr 7 and 0x1
        val mMosTemp = (data[17].toInt() and 0xff) + 80 - 256
        val mMotTemp = (data[18].toInt() and 0xff) + 80 - 256
        val mBatTemp = (data[19].toInt() and 0xff) + 80 - 256
        val mBoardTemp = (data[20].toInt() and 0xff) + 80 - 256
        val mLampTemp = (data[21].toInt() and 0xff) + 80 - 256
        val mPitchAngle = MathsUtil.signedShortFromBytesLE(data, 22)
        val mPitchAimAngle = MathsUtil.signedShortFromBytesLE(data, 24)
        val mRollAngle = MathsUtil.signedShortFromBytesLE(data, 26)
        val mDynamicSpeedLimit = MathsUtil.shortFromBytesLE(data, 28)
        val mDynamicCurrentLimit = MathsUtil.shortFromBytesLE(data, 30)
        val mBrightness = data[32].toInt() and 0xff
        val mLightBrightness = data[33].toInt() and 0xff
        val mCpuTemp = (data[34].toInt() and 0xff) + 80 - 256
        val mImuTemp = (data[35].toInt() and 0xff) + 80 - 256
        val mPwm = MathsUtil.shortFromBytesLE(data, 36)
        wd.voltage = mVoltage
        wd.torque = mTorque.toDouble() / 100.0
        wd.motorPower = mMotPower.toDouble()
        wd.cpuTemp = mCpuTemp
        wd.imuTemp = mImuTemp
        wd.current = mCurrent
        wd.speed = mSpeed
        wd.currentLimit = mDynamicCurrentLimit.toDouble() / 100.0
        wd.speedLimit = mDynamicSpeedLimit.toDouble() / 100.0
        wd.batteryLevel = mBatLevel
        wd.temperature = mMosTemp * 100
        wd.temperature2 = mBoardTemp * 100
        wd.angle = mPitchAngle.toDouble() / 100.0
        wd.roll = mRollAngle.toDouble() / 100.0
        wd.output = mPwm
        wd.updateRideTime()
        wd.topSpeed = mSpeed
        wd.voltageSag = mVoltage
        wd.setPower(mBatPower * 100)
        wd.setWheelDistance(mMileage.toLong())
        // // state data
        val i = if (data.size < 49) 36 else 38
        val mPcMode = data[i].toInt() and 0x07 // lock, drive, shutdown, idle
        val mMcMode = data[i].toInt() shr 3 and 0x07
        val mMotState = data[i].toInt() shr 6 and 0x01
        val chrgState = data[i].toInt() shr 7 and 0x01
        val lightState = data[i + 1].toInt() and 0x01
        val decorLiState = data[i + 1].toInt() shr 1 and 0x01
        val liftedState = data[i + 1].toInt() shr 2 and 0x01
        val tailLiState = data[i + 1].toInt() shr 3 and 0x03
        val fanState = data[i + 1].toInt() shr 5 and 0x01
        var wmode = ""
        if (mMotState == 1) {
            wmode = wmode + "Active"
        }
        if (chrgState == 1) {
            wmode = "$wmode Charging"
        }
        if (liftedState == 1) {
            wmode = "$wmode Lifted"
        }
        wd.modeStr = wmode
        // WheelLog.AppConfig.setFanEnabled(fanState != 0); // bad behaviour
        if (WheelLog.AppConfig.lightEnabled != (lightState == 1)) {
            if (lightSwitchCounter > 3) {
                // WheelLog.AppConfig.setLightEnabled(lightState == 1); // bad behaviour
                lightSwitchCounter = 0
            } else {
                lightSwitchCounter += 1
            }
        } else {
            lightSwitchCounter = 0
        }

        // WheelLog.AppConfig.setDrlEnabled(decorLiState != 0); // too fast, bad behaviour

        // // errors data
        val inmoError = getError(i + 5)
        wd.setAlert(inmoError)
        if (inmoError !== "" && sContext != null) {
            Timber.i("News to send: %s, sending Intent", inmoError)
            val intent = Intent(Constants.ACTION_WHEEL_NEWS_AVAILABLE)
            intent.putExtra(Constants.INTENT_EXTRA_NEWS, inmoError)
            sContext.sendBroadcast(intent)
        }
        //            System.out.println(String.format(Locale.US,"\nVolt: %.2f, Amp: %.2f, Km/h: %.2f, N*m: %.2f, Bat Wt: %d, Mot Wt: %d, PWM: %.2f, PitchAim: %.2f, Pith: %.2f, Roll: %.2f, \nTrip Km: %.2f, Rem Km: %.3f, Bat: %.2f, Lim km/h: %.2f, Lim A: %.2f, \nMos t: %d, Mot t: %d, Bat t: %d, Board t: %d, CPU t: %d, IMU t: %d, Lamp t: %d",
//                    mVoltage/100.0, mCurrent/100.0, mSpeed/100.0, mTorque/100.0, mBatPower,mMotPower, mPwm/100.0, mPitchAimAngle/100.0, mPitchAngle/100.0,  mRollAngle/100.0, mMileage/10.0, mRemainMileage/1000.0, mBatLevel/100.0, mDynamicSpeedLimit/100.0, mDynamicCurrentLimit/100.0, mMosTemp, mMotTemp, mBatTemp, mBoardTemp, mCpuTemp, mImuTemp, mLampTemp));

//            if (!(wmode.equals("Active") || wmode.equals(""))) System.out.println(String.format(Locale.US,"State: %s", wmode));
//            if (!inmoError.equals("")) System.out.println(String.format(Locale.US,"Err: %s", inmoError));
        return Pair(lightSwitchCounter, true)
    }

    fun parseRealTimeInfoV11_1_4(sContext: Context?, lightSwitchCounter: Int): Pair<Int, Boolean> {
        var lightSwitchCounter = lightSwitchCounter
        Timber.i("Parse V11 1.4+ realtime stats data")
        val mVoltage = MathsUtil.shortFromBytesLE(data, 0)
        val mCurrent = MathsUtil.signedShortFromBytesLE(data, 2)
        val mSpeed = MathsUtil.signedShortFromBytesLE(data, 4)
        val mTorque = MathsUtil.signedShortFromBytesLE(data, 6)
        val mPwm = MathsUtil.signedShortFromBytesLE(data, 8)
        val mBatPower = MathsUtil.signedShortFromBytesLE(data, 10)
        val mMotPower = MathsUtil.signedShortFromBytesLE(data, 12)
        val mXz = MathsUtil.signedShortFromBytesLE(data, 14) // always 0
        val mPitchAngle = MathsUtil.signedShortFromBytesLE(data, 16)
        val mPitchAimAngle = MathsUtil.signedShortFromBytesLE(data, 18)
        val mRollAngle = MathsUtil.signedShortFromBytesLE(data, 20)
        val mSomething1 = MathsUtil.shortFromBytesLE(data, 22)
        val mSomething2 = MathsUtil.shortFromBytesLE(data, 24)
        val mMileage = MathsUtil.shortFromBytesLE(data, 26) * 10
        val mBatLevel = MathsUtil.shortFromBytesLE(data, 28)
        val mRemainMileage = MathsUtil.shortFromBytesLE(data, 30) * 10
        val mSomeThing120 = MathsUtil.shortFromBytesLE(data, 32)
        val mDynamicSpeedLimit = MathsUtil.shortFromBytesLE(data, 34)
        val mDynamicCurrentLimit = MathsUtil.shortFromBytesLE(data, 36)
        val mSomething3 = MathsUtil.shortFromBytesLE(data, 38)
        val mSomething4 = MathsUtil.shortFromBytesLE(data, 40)
        val mMosTemp = (data[42].toInt() and 0xff) + 80 - 256
        val mMotTemp = (data[43].toInt() and 0xff) + 80 - 256
        val mBatTemp = (data[44].toInt() and 0xff) + 80 - 256 // 0
        val mBoardTemp = (data[45].toInt() and 0xff) + 80 - 256
        val mCpuTemp = (data[46].toInt() and 0xff) + 80 - 256
        val mImuTemp = (data[47].toInt() and 0xff) + 80 - 256
        val mLampTemp = (data[48].toInt() and 0xff) + 80 - 256 // 0
        val mBrightness = data[49].toInt() and 0xff
        val mLightBrightness = data[50].toInt() and 0xff
        //            System.out.println(String.format(Locale.US,"\nVolt: %.2f, Amp: %.2f, Km/h: %.2f, N*m: %.2f, Bat Wt: %d, Mot Wt: %d, XZ: %d, PWM: %.2f, PitchAim: %.2f, Pith: %.2f, Roll: %.2f, \nTrip Km: %.2f, Rem Km: %.3f, Bat: %.2f, Something: %.2f, Lim km/h: %.2f, Lim A: %.2f, \nMos t: %d, Mot t: %d, Bat t: %d, Board t: %d, CPU t: %d, IMU t: %d, Lamp t: %d",
//                    mVoltage/100.0, mCurrent/100.0, mSpeed/100.0, mTorque/100.0, mBatPower,mMotPower, mXz, mPwm/100.0, mPitchAimAngle/100.0, mPitchAngle/100.0,  mRollAngle/100.0, mMileage/10.0, mRemainMileage/1000.0, mBatLevel/100.0, mSomeThing180/100.0, mDynamicSpeedLimit/100.0, mDynamicCurrentLimit/100.0, mMosTemp, mMotTemp, mBatTemp, mBoardTemp, mCpuTemp, mImuTemp, mLampTemp));
        wd.voltage = mVoltage
        wd.torque = mTorque.toDouble() / 100.0
        wd.motorPower = mMotPower.toDouble()
        wd.cpuTemp = mCpuTemp
        wd.imuTemp = mImuTemp
        wd.current = mCurrent
        wd.speed = mSpeed
        wd.currentLimit = mDynamicCurrentLimit.toDouble() / 100.0
        wd.speedLimit = mDynamicSpeedLimit.toDouble() / 100.0
        wd.batteryLevel = Math.round(mBatLevel / 100.0).toInt()
        wd.temperature = mMosTemp * 100
        wd.temperature2 = mBoardTemp * 100
        wd.output = mPwm
        // wd.setMotorTemp(mMotTemp * 100); not existed in WD
        wd.angle = mPitchAngle.toDouble() / 100.0
        wd.roll = mRollAngle.toDouble() / 100.0
        wd.updateRideTime()
        wd.topSpeed = mSpeed
        wd.voltageSag = mVoltage
        wd.setPower(mBatPower * 100)
        wd.setWheelDistance(mMileage.toLong())
        // // state data
        val mPcMode = data[56].toInt() and 0x07 // lock, drive, shutdown, idle
        val mMcMode = data[56].toInt() shr 3 and 0x07
        val mMotState = data[56].toInt() shr 6 and 0x01
        val chrgState = data[56].toInt() shr 7 and 0x01
        val lowLightState = data[57].toInt() and 0x01
        val highLightState = data[57].toInt() shr 1 and 0x01
        val liftedState = data[57].toInt() shr 2 and 0x01
        val tailLiState = data[57].toInt() shr 3 and 0x03
        val fwUpdateState = data[57].toInt() shr 5 and 0x01
        var wmode = ""
        if (mMotState == 1) {
            wmode = wmode + "Active"
        }
        if (chrgState == 1) {
            wmode = "$wmode Charging"
        }
        if (liftedState == 1) {
            wmode = "$wmode Lifted"
        }
        // if (!(wmode.equals("Active") || wmode.equals(""))) System.out.println(String.format(Locale.US,"State: %s", wmode));
        wd.modeStr = wmode
        if (WheelLog.AppConfig.lightEnabled != (lowLightState == 1)) {
            if (lightSwitchCounter > 3) {
                // WheelLog.AppConfig.setLightEnabled(lightState == 1); // bad behaviour
                lightSwitchCounter = 0
            } else {
                lightSwitchCounter += 1
            }
        } else {
            lightSwitchCounter = 0
        }

        // // errors data
        val inmoError = getError(61)
        // if (!inmoError.equals("")) System.out.println(String.format(Locale.US,"Err: %s", inmoError));
        wd.setAlert(inmoError)
        if (inmoError !== "" && sContext != null) {
            Timber.i("News to send: %s, sending Intent", inmoError)
            val intent = Intent(Constants.ACTION_WHEEL_NEWS_AVAILABLE)
            intent.putExtra(Constants.INTENT_EXTRA_NEWS, inmoError)
            sContext.sendBroadcast(intent)
        }
        return Pair(lightSwitchCounter, true)
    }

    fun parseRealTimeInfoV12(sContext: Context?, lightSwitchCounter: Int): Pair<Int, Boolean> {
        var lightSwitchCounter = lightSwitchCounter
        Timber.i("Parse V12 realtime stats data")
        val mVoltage = MathsUtil.shortFromBytesLE(data, 0)
        val mCurrent = MathsUtil.signedShortFromBytesLE(data, 2)
        val mSpeed = MathsUtil.signedShortFromBytesLE(data, 4)
        val mTorque = MathsUtil.signedShortFromBytesLE(data, 6)
        val mPwm = MathsUtil.signedShortFromBytesLE(data, 8)
        val mBatPower = MathsUtil.signedShortFromBytesLE(data, 10)
        val mMotPower = MathsUtil.signedShortFromBytesLE(data, 12)
        val mXz = MathsUtil.signedShortFromBytesLE(data, 14) // always 0
        val mPitchAngle = MathsUtil.signedShortFromBytesLE(data, 16)
        val mPitchAimAngle = MathsUtil.signedShortFromBytesLE(data, 18)
        val mRollAngle = MathsUtil.signedShortFromBytesLE(data, 20)
        val mMileage = MathsUtil.shortFromBytesLE(data, 22) * 10
        val mBatLevel = MathsUtil.shortFromBytesLE(data, 24)
        val mRemainMileage = MathsUtil.shortFromBytesLE(data, 26) * 10
        val mSomeThing180 = MathsUtil.shortFromBytesLE(data, 28) // always 18000
        val mDynamicSpeedLimit = MathsUtil.shortFromBytesLE(data, 30)
        val mDynamicCurrentLimit = MathsUtil.shortFromBytesLE(data, 32)
        val mMosTemp = (data[40].toInt() and 0xff) + 80 - 256
        val mMotTemp = (data[41].toInt() and 0xff) + 80 - 256
        val mBatTemp = (data[42].toInt() and 0xff) + 80 - 256 // 0
        val mBoardTemp = (data[43].toInt() and 0xff) + 80 - 256
        val mCpuTemp = (data[44].toInt() and 0xff) + 80 - 256
        val mImuTemp = (data[45].toInt() and 0xff) + 80 - 256
        val mLampTemp = (data[46].toInt() and 0xff) + 80 - 256 // 0
        // don't remove
//            int mBrightness = data[48]& 0xff;
//            int mLightBrightness = data[49]& 0xff;
//            System.out.println(String.format(Locale.US,"\nVolt: %.2f, Amp: %.2f, Km/h: %.2f, N*m: %.2f, Bat Wt: %d, Mot Wt: %d, XZ: %d, PWM: %.2f, PitchAim: %.2f, Pith: %.2f, Roll: %.2f, \nTrip Km: %.2f, Rem Km: %.3f, Bat: %.2f, Something: %.2f, Lim km/h: %.2f, Lim A: %.2f, \nMos t: %d, Mot t: %d, Bat t: %d, Board t: %d, CPU t: %d, IMU t: %d, Lamp t: %d",
//                    mVoltage/100.0, mCurrent/100.0, mSpeed/100.0, mTorque/100.0, mBatPower,mMotPower, mXz, mPwm/100.0, mPitchAimAngle/100.0, mPitchAngle/100.0,  mRollAngle/100.0, mMileage/10.0, mRemainMileage/1000.0, mBatLevel/100.0, mSomeThing180/100.0, mDynamicSpeedLimit/100.0, mDynamicCurrentLimit/100.0, mMosTemp, mMotTemp, mBatTemp, mBoardTemp, mCpuTemp, mImuTemp, mLampTemp));
        wd.voltage = mVoltage
        wd.torque = mTorque.toDouble() / 100.0
        wd.motorPower = mMotPower.toDouble()
        wd.cpuTemp = mCpuTemp
        wd.imuTemp = mImuTemp
        wd.current = mCurrent
        wd.speed = mSpeed
        wd.currentLimit = mDynamicCurrentLimit.toDouble() / 100.0
        wd.speedLimit = mDynamicSpeedLimit.toDouble() / 100.0
        wd.batteryLevel = Math.round(mBatLevel / 100.0).toInt()
        wd.temperature = mMosTemp * 100
        wd.temperature2 = mMotTemp * 100
        wd.output = mPwm
        // wd.setMotorTemp(mMotTemp * 100); not existed in WD
        wd.angle = mPitchAngle.toDouble() / 100.0
        wd.roll = mRollAngle.toDouble() / 100.0
        wd.updateRideTime()
        wd.topSpeed = mSpeed
        wd.voltageSag = mVoltage
        wd.setPower(mBatPower * 100)
        wd.setWheelDistance(mMileage.toLong())
        // // state data
        val mPcMode = data[54].toInt() and 0x07 // lock, drive, shutdown, idle
        val mMcMode = data[54].toInt() shr 3 and 0x07
        val mMotState = data[54].toInt() shr 6 and 0x01
        val chrgState = data[54].toInt() shr 7 and 0x01
        val lowLightState = data[55].toInt() and 0x01
        val highLightState = data[55].toInt() shr 1 and 0x01
        val liftedState = data[55].toInt() shr 2 and 0x01
        val tailLiState = data[55].toInt() shr 3 and 0x03
        val fwUpdateState = data[55].toInt() shr 5 and 0x01
        var wmode = ""
        if (mMotState == 1) {
            wmode = wmode + "Active"
        }
        if (chrgState == 1) {
            wmode = "$wmode Charging"
        }
        if (liftedState == 1) {
            wmode = "$wmode Lifted"
        }
        // if (!(wmode.equals("Active") || wmode.equals(""))) System.out.println(String.format(Locale.US,"State: %s", wmode));
        wd.modeStr = wmode
        if (WheelLog.AppConfig.lightEnabled != (lowLightState == 1)) {
            if (lightSwitchCounter > 3) {
                // WheelLog.AppConfig.setLightEnabled(lightState == 1); // bad behaviour
                lightSwitchCounter = 0
            } else {
                lightSwitchCounter += 1
            }
        } else {
            lightSwitchCounter = 0
        }

        // // errors data
        val inmoError = getError(59)
        // if (!inmoError.equals("")) System.out.println(String.format(Locale.US,"Err: %s", inmoError));
        wd.setAlert(inmoError)
        if (inmoError !== "" && sContext != null) {
            Timber.i("News to send: %s, sending Intent", inmoError)
            val intent = Intent(Constants.ACTION_WHEEL_NEWS_AVAILABLE)
            intent.putExtra(Constants.INTENT_EXTRA_NEWS, inmoError)
            sContext.sendBroadcast(intent)
        }
        return Pair(lightSwitchCounter, true)
    }

    fun parseRealTimeInfoV13(lightSwitchCounter: Int): Pair<Int, Boolean> {
        var lightSwitchCounter = lightSwitchCounter
        Timber.i("Parse V13 realtime stats data")
        val mVoltage = MathsUtil.shortFromBytesLE(data, 0)
        val mCurrent = MathsUtil.signedShortFromBytesLE(data, 2)
        // int mSpeed = MathsUtil.signedShortFromBytesLE(data, 4);
        val mSomeThing2 = MathsUtil.signedShortFromBytesLE(data, 4)
        val mPitchAngle = MathsUtil.signedShortFromBytesLE(data, 6) // not sure
        val mSpeed = MathsUtil.signedShortFromBytesLE(data, 8)
        // int mSomething0 = MathsUtil.signedShortFromBytesLE(data, 10);
        val mMileage = MathsUtil.intFromBytesRevLE(data, 10) // not sure
        val mPwm = MathsUtil.signedShortFromBytesLE(data, 14)
        val mBatPower = MathsUtil.signedShortFromBytesLE(data, 16)
        val mTorque = MathsUtil.signedShortFromBytesLE(data, 18) // not sure
        val mPitchAimAngle = MathsUtil.signedShortFromBytesLE(data, 20) // not sure
        val mMotPower = MathsUtil.signedShortFromBytesLE(data, 22) // not sure
        val mRollAngle = MathsUtil.signedShortFromBytesLE(data, 24) // not sure

        // int mRemainMileage = MathsUtil.shortFromBytesLE(data, 26) * 10;
        // int mSomeThing180 = MathsUtil.shortFromBytesLE(data, 28); // always 18000
        // int mDynamicSpeedLimit = MathsUtil.shortFromBytesLE(data, 30);
        // int mDynamicCurrentLimit = MathsUtil.shortFromBytesLE(data, 32);
        val mBatLevel1 = MathsUtil.shortFromBytesLE(data, 34)
        val mBatLevel2 = MathsUtil.shortFromBytesLE(data, 36)
        val mSomeThing200_1 = MathsUtil.shortFromBytesLE(data, 38)
        val mDynamicSpeedLimit = MathsUtil.shortFromBytesLE(data, 40)
        val x5 = MathsUtil.shortFromBytesLE(data, 42)
        val x6 = MathsUtil.shortFromBytesLE(data, 44)
        val x7 = MathsUtil.shortFromBytesLE(data, 46)
        val mSomeThing200_2 = MathsUtil.shortFromBytesLE(data, 48)
        val mDynamicCurrentLimit = MathsUtil.shortFromBytesLE(data, 50)
        val mSomeThing380 = MathsUtil.shortFromBytesLE(data, 52)
        val mMosTemp = (data[58].toInt() and 0xff) + 80 - 256
        val mMotTemp = (data[59].toInt() and 0xff) + 80 - 256
        val mBatTemp = (data[60].toInt() and 0xff) + 80 - 256 // 0
        val mBoardTemp = (data[61].toInt() and 0xff) + 80 - 256
        val mCpuTemp = (data[62].toInt() and 0xff) + 80 - 256
        val mImuTemp = (data[63].toInt() and 0xff) + 80 - 256
        val mLampTemp = (data[64].toInt() and 0xff) + 80 - 256 // 0

// don't remove
//            int mBrightness = data[48]& 0xff;
//            int mLightBrightness = data[49]& 0xff;
//            System.out.println(String.format(Locale.US,"\nVolt: %.2f, Amp: %.2f, Km/h: %.2f, N*m: %.2f, Bat Wt: %d, Mot Wt: %d, XZ: %d, PWM: %.2f, PitchAim: %.2f, Pith: %.2f, Roll: %.2f, \nTrip Km: %.2f, Rem Km: %.3f, Bat: %.2f, Something: %.2f, Lim km/h: %.2f, Lim A: %.2f, \nMos t: %d, Mot t: %d, Bat t: %d, Board t: %d, CPU t: %d, IMU t: %d, Lamp t: %d",
//                    mVoltage/100.0, mCurrent/100.0, mSpeed/100.0, mTorque/100.0, mBatPower,mMotPower, mXz, mPwm/100.0, mPitchAimAngle/100.0, mPitchAngle/100.0,  mRollAngle/100.0, mMileage/10.0, mRemainMileage/1000.0, mBatLevel/100.0, mSomeThing180/100.0, mDynamicSpeedLimit/100.0, mDynamicCurrentLimit/100.0, mMosTemp, mMotTemp, mBatTemp, mBoardTemp, mCpuTemp, mImuTemp, mLampTemp));
        wd.voltage = mVoltage
        wd.torque = mTorque.toDouble() / 100.0
        wd.motorPower = mMotPower.toDouble()
        wd.cpuTemp = mCpuTemp
        wd.imuTemp = mImuTemp
        wd.current = mCurrent
        wd.speed = mSpeed
        wd.currentLimit = mDynamicCurrentLimit.toDouble() / 100.0
        wd.speedLimit = mDynamicSpeedLimit.toDouble() / 100.0
        wd.batteryLevel = Math.round((mBatLevel1 + mBatLevel2) / 200.0).toInt()
        wd.temperature = mMosTemp * 100
        wd.temperature2 = mMotTemp * 100
        wd.output = mPwm
        // wd.setMotorTemp(mMotTemp * 100); not existed in WD
        wd.angle = mPitchAngle.toDouble() / 100.0
        wd.roll = mRollAngle.toDouble() / 100.0
        wd.updateRideTime()
        wd.topSpeed = mSpeed
        wd.voltageSag = mVoltage
        wd.setPower(mBatPower * 100)
        wd.setWheelDistance(mMileage)
        // // state data
        val mPcMode = data[74].toInt() and 0x07 // lock, drive, shutdown, idle
        val mMcMode = data[74].toInt() shr 3 and 0x07
        val mMotState = data[74].toInt() shr 6 and 0x01
        val chrgState = data[74].toInt() shr 7 and 0x01
        val lowLightState = data[75].toInt() and 0x01
        val highLightState = data[75].toInt() shr 1 and 0x01
        val liftedState = data[75].toInt() shr 2 and 0x01
        val tailLiState = data[75].toInt() shr 3 and 0x03
        val fwUpdateState = data[75].toInt() shr 5 and 0x01
        var wmode = ""
        if (mMotState == 1) {
            wmode = wmode + "Active"
        }
        if (chrgState == 1) {
            wmode = "$wmode Charging"
        }
        if (liftedState == 1) {
            wmode = "$wmode Lifted"
        }
        // if (!(wmode.equals("Active") || wmode.equals(""))) System.out.println(String.format(Locale.US,"State: %s", wmode));
        wd.modeStr = wmode
        if (WheelLog.AppConfig.lightEnabled != (lowLightState == 1)) {
            if (lightSwitchCounter > 3) {
                // WheelLog.AppConfig.setLightEnabled(lightState == 1); // bad behaviour
                lightSwitchCounter = 0
            } else {
                lightSwitchCounter += 1
            }
        } else {
            lightSwitchCounter = 0
        }

        // // errors data
        val inmoError = getError(76)
        if (inmoError != "") println(String.format(Locale.US, "Err: %s", inmoError))
        wd.setAlert(inmoError)
        /*
        if ((inmoError != "") && (sContext != null)) {
            Timber.i("News to send: %s, sending Intent", inmoError);
            Intent intent = new Intent(Constants.ACTION_WHEEL_NEWS_AVAILABLE);
            intent.putExtra(Constants.INTENT_EXTRA_NEWS, inmoError);
            sContext.sendBroadcast(intent);
        }
        */return Pair(lightSwitchCounter, true)
    }

    fun writeBuffer(): ByteArray {
        val buffer = bytes
        val check = calcCheck(buffer)
        val out = ByteArrayOutputStream()
        out.write(0xAA)
        out.write(0xAA)
        try {
            out.write(escape(buffer))
        } catch (e: IOException) {
            e.printStackTrace()
        }
        out.write(check.toInt())
        return out.toByteArray()
    }

    private val bytes: ByteArray
        get() {
            val buff = ByteArrayOutputStream()
            buff.write(flags)
            buff.write(data.size + 1)
            buff.write(command)
            try {
                buff.write(data)
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return buff.toByteArray()
        }

    private fun escape(buffer: ByteArray): ByteArray {
        val out = ByteArrayOutputStream()
        for (c in buffer) {
            if (c == 0xAA.toByte() || c == 0xA5.toByte()) {
                out.write(0xA5)
            }
            out.write(c.toInt())
        }
        return out.toByteArray()
    }

    companion object {
        val carType: Message
            get() {
                val msg = Message()
                msg.flags = Flag.Initial.value
                msg.command = Command.MainInfo.value
                msg.data = byteArrayOf(0x01.toByte())
                return msg
            }
        val mainVersion: Message
            get() {
                val msg = Message()
                msg.flags = Flag.Default.value
                msg.command = Command.MainVersion.value
                msg.data = ByteArray(0)
                return msg
            }

        fun wheelOffFirstStage(): Message {
            val msg = Message()
            msg.flags = Flag.Initial.value
            msg.command = Command.Diagnistic.value
            msg.data = byteArrayOf(0x81.toByte(), 0x00.toByte())
            return msg
        }

        fun wheelOffSecondStage(): Message {
            val msg = Message()
            msg.flags = Flag.Initial.value
            msg.command = Command.Diagnistic.value
            msg.data = byteArrayOf(0x82.toByte())
            return msg
        }

        val serialNumber: Message
            get() {
                val msg = Message()
                msg.flags = Flag.Initial.value
                msg.command = Command.MainInfo.value
                msg.data = byteArrayOf(0x02.toByte())
                return msg
            }
        val versions: Message
            get() {
                val msg = Message()
                msg.flags = Flag.Initial.value
                msg.command = Command.MainInfo.value
                msg.data = byteArrayOf(0x06.toByte())
                return msg
            }
        val currentSettings: Message
            get() {
                val msg = Message()
                msg.flags = Flag.Default.value
                msg.command = Command.Settings.value
                msg.data = byteArrayOf(0x20.toByte())
                return msg
            }
        val uselessData: Message
            get() {
                val msg = Message()
                msg.flags = Flag.Default.value
                msg.command = Command.Something1.value
                msg.data = byteArrayOf(0x00.toByte(), 0x01.toByte())
                return msg
            }
        val batteryData: Message
            get() {
                val msg = Message()
                msg.flags = Flag.Default.value
                msg.command = Command.BatteryRealTimeInfo.value
                msg.data = ByteArray(0)
                return msg
            }
        val diagnostic: Message
            get() {
                val msg = Message()
                msg.flags = Flag.Default.value
                msg.command = Command.Diagnistic.value
                msg.data = ByteArray(0)
                return msg
            }
        val statistics: Message
            get() {
                val msg = Message()
                msg.flags = Flag.Default.value
                msg.command = Command.TotalStats.value
                msg.data = ByteArray(0)
                return msg
            }
        val realTimeData: Message
            get() {
                val msg = Message()
                msg.flags = Flag.Default.value
                msg.command = Command.RealTimeInfo.value
                msg.data = ByteArray(0)
                return msg
            }

        fun playSound(number: Int): Message {
            val value = (number and 0xFF).toByte()
            val msg = Message()
            msg.flags = Flag.Default.value
            msg.command = Command.Control.value
            msg.data = byteArrayOf(0x41, value, 0x01)
            return msg
        }

        fun wheelCalibration(): Message {
            val msg = Message()
            msg.flags = Flag.Default.value
            msg.command = Command.Control.value
            msg.data = byteArrayOf(0x42, 0x01, 0x00, 0x01)
            return msg
        }

        fun setLight(on: Boolean): Message {
            var enable: Byte = 0
            if (on) enable = 1
            val msg = Message()
            msg.flags = Flag.Default.value
            msg.command = Command.Control.value
            msg.data = byteArrayOf(0x40, enable)
            return msg
        }

        fun setLightBrightness(brightness: Int): Message {
            val value = (brightness and 0xFF).toByte()
            val msg = Message()
            msg.flags = Flag.Default.value
            msg.command = Command.Control.value
            msg.data = byteArrayOf(0x2b, value)
            return msg
        }

        fun setVolume(volume: Int): Message {
            val value = (volume and 0xFF).toByte()
            val msg = Message()
            msg.flags = Flag.Default.value
            msg.command = Command.Control.value
            msg.data = byteArrayOf(0x26, value)
            return msg
        }

        fun setDrl(on: Boolean): Message {
            var enable: Byte = 0
            if (on) enable = 1
            val msg = Message()
            msg.flags = Flag.Default.value
            msg.command = Command.Control.value
            msg.data = byteArrayOf(0x2d, enable)
            return msg
        }

        fun setHandleButton(on: Boolean): Message {
            var enable: Byte = 0
            if (!on) enable = 1
            val msg = Message()
            msg.flags = Flag.Default.value
            msg.command = Command.Control.value
            msg.data = byteArrayOf(0x2e, enable)
            return msg
        }

        fun setFan(on: Boolean): Message {
            var enable: Byte = 0
            if (on) enable = 1
            val msg = Message()
            msg.flags = Flag.Default.value
            msg.command = Command.Control.value
            msg.data = byteArrayOf(0x43, enable)
            return msg
        }

        fun setQuietMode(on: Boolean): Message {
            var enable: Byte = 0
            if (on) enable = 1
            val msg = Message()
            msg.flags = Flag.Default.value
            msg.command = Command.Control.value
            msg.data = byteArrayOf(0x38, enable)
            return msg
        }

        fun setFancierMode(on: Boolean): Message {
            var enable: Byte = 0
            if (on) enable = 1
            val msg = Message()
            msg.flags = Flag.Default.value
            msg.command = Command.Control.value
            msg.data = byteArrayOf(0x24, enable)
            return msg
        }

        fun setMaxSpeed(maxSpeed: Int): Message {
            val value = MathsUtil.getBytes((maxSpeed * 100).toShort())
            val msg = Message()
            msg.flags = Flag.Default.value
            msg.command = Command.Control.value
            msg.data = byteArrayOf(0x21, value[1], value[0])
            return msg
        }

        fun setPedalSensivity(sensivity: Int): Message {
            val value = (sensivity and 0xFF).toByte()
            val msg = Message()
            msg.flags = Flag.Default.value
            msg.command = Command.Control.value
            msg.data = byteArrayOf(0x25, value, 0x64)
            return msg
        }

        fun setClassicMode(on: Boolean): Message {
            var enable: Byte = 0
            if (on) enable = 1
            val msg = Message()
            msg.flags = Flag.Default.value
            msg.command = Command.Control.value
            msg.data = byteArrayOf(0x23, enable)
            return msg
        }

        fun setGoHome(on: Boolean): Message {
            var enable: Byte = 0
            if (on) enable = 1
            val msg = Message()
            msg.flags = Flag.Default.value
            msg.command = Command.Control.value
            msg.data = byteArrayOf(0x37, enable)
            return msg
        }

        fun setTransportMode(on: Boolean): Message {
            var enable: Byte = 0
            if (on) enable = 1
            val msg = Message()
            msg.flags = Flag.Default.value
            msg.command = Command.Control.value
            msg.data = byteArrayOf(0x32, enable)
            return msg
        }

        fun setLock(on: Boolean): Message {
            var enable: Byte = 0
            if (on) enable = 1
            val msg = Message()
            msg.flags = Flag.Default.value
            msg.command = Command.Control.value
            msg.data = byteArrayOf(0x31, enable)
            return msg
        }

        fun setMute(on: Boolean): Message {
            var enable: Byte = 1
            if (on) enable = 0
            val msg = Message()
            msg.flags = Flag.Default.value
            msg.command = Command.Control.value
            msg.data = byteArrayOf(0x2c, enable)
            return msg
        }

        fun setPedalTilt(angle: Int): Message {
            val value = MathsUtil.getBytes((angle * 10).toShort())
            val msg = Message()
            msg.flags = Flag.Default.value
            msg.command = Command.Control.value
            msg.data = byteArrayOf(0x22, value[1], value[0])
            return msg
        }

        private fun calcCheck(buffer: ByteArray): Byte {
            var check = 0
            for (c in buffer) {
                check = check xor c.toInt() and 0xFF
            }
            return check.toByte()
        }

        fun verify(buffer: ByteArray): Message? {
            Timber.i("Verify: %s", StringUtil.toHexString(buffer))
            val dataBuffer = Arrays.copyOfRange(buffer, 0, buffer.size - 1)
            val check = calcCheck(dataBuffer)
            val bufferCheck = buffer[buffer.size - 1]
            if (check == bufferCheck) {
                Timber.i("Check OK")
            } else {
                Timber.i("Check FALSE, calc: %02X, packet: %02X", check, bufferCheck)
            }
            return if (check == bufferCheck) Message(dataBuffer) else null
        }
    }
}