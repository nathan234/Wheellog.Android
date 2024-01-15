package com.cooper.wheellog

import android.bluetooth.BluetoothGattDescriptor
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import com.cooper.wheellog.WheelLog.Companion.appContext
import com.cooper.wheellog.utils.Alarms.checkAlarm
import com.cooper.wheellog.utils.BaseAdapter
import com.cooper.wheellog.utils.Calculator.pushPower
import com.cooper.wheellog.utils.Constants
import com.cooper.wheellog.utils.Constants.WHEEL_TYPE
import com.cooper.wheellog.utils.GotwayVirtualAdapter
import com.cooper.wheellog.utils.MathsUtil
import com.cooper.wheellog.utils.SmartBms
import com.cooper.wheellog.utils.StringUtil.getRawTextResource
import com.cooper.wheellog.utils.StringUtil.inArray
import com.cooper.wheellog.utils.gotway.GotwayAdapter
import com.cooper.wheellog.utils.inmotion.InMotionAdapter
import com.cooper.wheellog.utils.inmotion.InmotionAdapterV2
import com.cooper.wheellog.utils.inmotion.InmotionAdapterV2.Companion.proto
import com.cooper.wheellog.utils.kingsong.KingsongAdapter
import com.cooper.wheellog.utils.ninebot.NinebotAdapter
import com.cooper.wheellog.utils.ninebot.NinebotZAdapter
import com.cooper.wheellog.utils.veteran.VeteranAdapter
import org.json.JSONArray
import org.json.JSONException
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.Timer
import java.util.TimerTask
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.math.max
import kotlin.math.min

class WheelData: IWheelData {
    private var ridingTimerControl: Timer? = null
    var bluetoothService: BluetoothService? = null
    private var graph_last_update_time: Long = 0
    val xAxis = ArrayList<String>()
    val currentAxis = ArrayList<Float>()
    val speedAxis = ArrayList<Float>()

    // BMS
    val bms1 = SmartBms()
    val bms2 = SmartBms()

    //all
    private var mSpeed = 0
    var torque = 0.0
    var motorPower = 0.0
    var cpuTemp = 0
    var imuTemp = 0
    var speedLimit = 0.0
    var currentLimit = 0.0
    private var mTotalDistance: Long = 0
    private var mCurrent = 0
    private var mPower = 0
    var phaseCurrent = 0
    private var mTemperature = 0
    private var mMaxTemp = 0
    private var mMaxCurrent = 0.0
    private var mMaxPower = 0.0
    private var mTemperature2 = 0
    var cpuLoad = 0
    private var mOutput = 0
    var angle = 0.0
    var roll = 0.0
    var isWheelIsReady = false
        private set
    private var mBattery = 0
    private var mBatteryStart = -1
    var batteryLowestLevel = 101
        private set
    var voltage = 0
    var wheelDistance: Long = 0
    private var mUserDistance: Long = 0
    var rideTime = 0
        private set
    private var mRidingTime = 0
    private var mLastRideTime = 0
    private var mTopSpeed = 0
    private var mVoltageSag = 0
    var fanStatus = 0
    var chargingStatus = 0
        private set
    private var mConnectionState = false
    var name = "Unknown"
    private var mModel = "Unknown"
    var modeStr = "Unknown"
    private var mBtName = ""
    private var mAlert = StringBuilder()

    //    private int mVersion; # sorry King, but INT not good for Inmo
    private var mVersion = ""
    var serial = "Unknown"
    private var mWheelType = WHEEL_TYPE.Unknown
    private var rideStartTime: Long = 0
    private var mStartTotalDistance: Long = 0
    private var mCalculatedPwm = 0.0
    private var mMaxPwm = 0.0
    private var mLowSpeedMusicTime: Long = 0
    private var mBmsView = false
    var protoVer = ""
        private set
    private var timestamp_raw: Long = 0
    var timeStamp: Long = 0
        private set
    var lastLifeData: Long = -1
        private set
    val adapter: BaseAdapter?
        get() = when (mWheelType) {
            WHEEL_TYPE.GOTWAY_VIRTUAL -> GotwayVirtualAdapter.instance
            WHEEL_TYPE.GOTWAY -> GotwayAdapter.instance
            WHEEL_TYPE.VETERAN -> VeteranAdapter.instance
            WHEEL_TYPE.KINGSONG -> KingsongAdapter.instance
            WHEEL_TYPE.NINEBOT -> NinebotAdapter.instance
            WHEEL_TYPE.NINEBOT_Z -> NinebotZAdapter.instance
            WHEEL_TYPE.INMOTION -> InMotionAdapter.instance
            WHEEL_TYPE.INMOTION_V2 -> InmotionAdapterV2.instance
            else -> null
        }

    override fun bluetoothCmd(cmd: ByteArray?): Boolean {
        return if (bluetoothService == null) {
            false
        } else bluetoothService!!.writeWheelCharacteristic(cmd)
    }

    override fun startRidingTimerControl() {
        val timerTask: TimerTask = object : TimerTask() {
            override fun run() {
                if (mConnectionState && mSpeed > RIDING_SPEED) mRidingTime += 1
            }
        }
        ridingTimerControl = Timer()
        ridingTimerControl!!.schedule(timerTask, 0, 1000)
    }

    ///// test purpose, please let it be
    override fun startAlarmTest() {
        Timer().schedule(object : TimerTask() {
            override fun run() {
                val mContext = appContext
                checkAlarm(mCalculatedPwm, mContext!!)
            }
        }, 1000, 200000)

//        mCalculatedPwm = 70 / 100.0;
//        mSpeed = 50_00;
//        mBattery = 10;
//        mCurrent = 100_00;
        mTemperature = 6000
        WheelLog.AppConfig.alarmTemperature = 10
        //        WheelLog.AppConfig.setAlarmCurrent(10);

//        WheelLog.AppConfig.setAlarm1Speed(1);
//        WheelLog.AppConfig.setAlarm1Battery(70);
//        WheelLog.AppConfig.setAlarmFactor1(10_00);
        WheelLog.AppConfig.pwmBasedAlarms = false
    }

    override val isHardwarePWM: Boolean
        get() = when (wheelType) {
            WHEEL_TYPE.KINGSONG, WHEEL_TYPE.Unknown -> true
            WHEEL_TYPE.INMOTION_V2 -> proto >= 2
            WHEEL_TYPE.VETERAN -> VeteranAdapter.instance!!.ver >= 2 // 2+
            else -> false
        }
    override var speed: Int
        get() = Math.round(mSpeed / 10.0).toInt()
        set(speed) {
            mSpeed = speed
        }
    override var btName: String?
        get() = mBtName
        set(btName) {
            if (btName != null) {
                mBtName = btName
            }
        }

    override fun updateLight(enabledLight: Boolean) {
        if (adapter != null) {
            adapter!!.setLightState(enabledLight)
        }
    }

    override fun updateLed(enabledLed: Boolean) {
        if (adapter != null) {
            adapter!!.setLedState(enabledLed)
        }
    }

    override fun updateTailLight(tailLight: Boolean) {
        if (adapter != null) {
            adapter!!.setTailLightState(tailLight)
        }
    }

    override fun wheelBeep() {
        if (adapter != null) {
            adapter!!.wheelBeep()
        }
    }

    override fun updatePedalsMode(pedalsMode: Int) {
        if (adapter != null) {
            adapter!!.updatePedalsMode(pedalsMode)
        }
    }

    override fun updateStrobe(strobeMode: Int) {
        if (adapter != null) {
            adapter!!.updateStrobeMode(strobeMode)
        }
    }

    override fun updateLedMode(ledMode: Int) {
        if (adapter != null) {
            adapter!!.updateLedMode(ledMode)
        }
    }

    override fun updateAlarmMode(alarmMode: Int) {
        if (adapter != null) {
            adapter!!.updateAlarmMode(alarmMode)
        }
    }

    override fun wheelCalibration() {
        if (adapter != null) {
            adapter!!.wheelCalibration()
        }
    }

    override fun powerOff() {
        if (adapter != null) {
            adapter!!.powerOff()
        }
    }

    override fun updateHandleButton(enabledButton: Boolean) {
        if (adapter != null) {
            adapter!!.setHandleButtonState(enabledButton)
        }
    }

    override fun updateBrakeAssistant(brakeAssist: Boolean) {
        if (adapter != null) {
            adapter!!.setBrakeAssist(brakeAssist)
        }
    }

    override fun setLedColor(value: Int, ledNum: Int) {
        if (adapter != null) {
            adapter!!.setLedColor(value, ledNum)
        }
    }

    override fun updateAlarmEnabled(value: Boolean, num: Int) {
        if (adapter != null) {
            adapter!!.setAlarmEnabled(value, num)
        }
    }

    override fun updateAlarmSpeed(value: Int, num: Int) {
        if (adapter != null) {
            adapter!!.setAlarmSpeed(value, num)
        }
    }

    override fun updateLimitedModeEnabled(value: Boolean) {
        if (adapter != null) {
            adapter!!.setLimitedModeEnabled(value)
        }
    }

    override fun updateLimitedSpeed(value: Int) {
        if (adapter != null) {
            adapter!!.setLimitedSpeed(value)
        }
    }

    override fun updateMaxSpeed(wheelMaxSpeed: Int) {
        if (adapter != null) {
            adapter!!.updateMaxSpeed(wheelMaxSpeed)
        }
    }

    override fun updateSpeakerVolume(speakerVolume: Int) {
        if (adapter != null) {
            adapter!!.setSpeakerVolume(speakerVolume)
        }
    }

    override fun updatePedals(pedalAdjustment: Int) {
        if (adapter != null) {
            adapter!!.setPedalTilt(pedalAdjustment)
        }
    }

    override fun updatePedalSensivity(pedalSensivity: Int) {
        if (adapter != null) {
            adapter!!.setPedalSensivity(pedalSensivity)
        }
    }

    override fun updateRideMode(rideMode: Boolean) {
        if (adapter != null) {
            adapter!!.setRideMode(rideMode)
        }
    }

    override fun updateLockMode(enable: Boolean) {
        if (adapter != null) {
            adapter!!.setLockMode(enable)
        }
    }

    override fun updateTransportMode(enable: Boolean) {
        if (adapter != null) {
            adapter!!.setTransportMode(enable)
        }
    }

    override fun updateDrl(enable: Boolean) {
        if (adapter != null) {
            adapter!!.setDrl(enable)
        }
    }

    override fun updateGoHome(enable: Boolean) {
        if (adapter != null) {
            adapter!!.setGoHomeMode(enable)
        }
    }

    override fun updateFancierMode(enable: Boolean) {
        if (adapter != null) {
            adapter!!.setFancierMode(enable)
        }
    }

    override fun updateMute(enable: Boolean) {
        if (adapter != null) {
            adapter!!.setMute(enable)
        }
    }

    override fun updateFanQuiet(enable: Boolean) {
        if (adapter != null) {
            adapter!!.setFanQuiet(enable)
        }
    }

    override fun updateFanState(enable: Boolean) {
        if (adapter != null) {
            adapter!!.setFan(enable)
        }
    }

    override fun updateLightBrightness(brightness: Int) {
        if (adapter != null) {
            adapter!!.setLightBrightness(brightness)
        }
    }

    override var temperature: Int
        get() = mTemperature / 100
        set(value) {
            mTemperature = value
        }
    override var maxTemp: Int
        get() = mMaxTemp / 100
        set(temp) {
            if (temp > mMaxTemp && temp > 0) mMaxTemp = temp
        }
    override var temperature2: Int
        get() = mTemperature2 / 100
        set(value) {
            mTemperature2 = value
        }
    override val maxCurrentDouble: Double
        get() = mMaxCurrent / 100
    override val maxPowerDouble: Double
        get() = mMaxPower / 100
    override var output: Int
        get() = mOutput / 100
        set(value) {
            mOutput = value
        }
    override var batteryLevel: Int
        get() = mBattery
        set(battery) {
            var battery = battery
            if (WheelLog.AppConfig.customPercents) {
                val maxVoltage = maxVoltageForWheel
                val minVoltage = voltageTiltbackForWheel
                val voltagePercentStep = (maxVoltage - minVoltage) / 100.0
                if (voltagePercentStep != 0.0) {
                    battery = MathsUtil.clamp(
                        ((voltageDouble - minVoltage) / voltagePercentStep).toInt(),
                        0,
                        100
                    )
                }
            }
            batteryLowestLevel = min(batteryLowestLevel.toDouble(), battery.toDouble())
                .toInt()
            if (mBatteryStart == -1) {
                mBatteryStart = battery
            }
            mBattery = battery
        }

    override fun setChargingStatus(charging: Int): Int {
        return charging.also { chargingStatus = it }
    }

    override var isConnected: Boolean
        get() = mConnectionState
        set(connected) {
            mConnectionState = connected
            Timber.i("State %b", connected)
        }
    override var version: String
        get() = if (mVersion == "") "Unknown" else mVersion
        set(value) {
            mVersion = value
        }
    override var wheelType: WHEEL_TYPE
        get() = mWheelType
        set(wheelType) {
            val isChanged = wheelType !== mWheelType
            mWheelType = wheelType
            if (isChanged) {
                val mContext = appContext
                val intent = Intent(Constants.ACTION_WHEEL_TYPE_CHANGED)
                mContext!!.sendBroadcast(intent)
            }
        }
    override var model: String
        get() = mModel
        set(model) {
            val isChanged = model !== mModel
            mModel = model
            if (isChanged) {
                val intent = Intent(Constants.ACTION_WHEEL_MODEL_CHANGED)
                appContext!!.sendBroadcast(intent)
            }
        }
    val maxVoltageForWheel: Double
        get() {
            val adapter = adapter ?: return 0.toDouble()
            return Constants.MAX_CELL_VOLTAGE * adapter.cellsForWheel
        }
    val voltageTiltbackForWheel: Double
        get() {
            val adapter = adapter ?: return 0.toDouble()
            return WheelLog.AppConfig.cellVoltageTiltback / 100.0 * adapter.cellsForWheel
        }
    val isVoltageTiltbackUnsupported: Boolean
        get() = mWheelType === WHEEL_TYPE.NINEBOT || mWheelType === WHEEL_TYPE.NINEBOT_Z
    val chargeTime: String
        get() {
            val maxVoltage = maxVoltageForWheel
            val minVoltage = voltageTiltbackForWheel
            val whInOneV = WheelLog.AppConfig.batteryCapacity / (maxVoltage - minVoltage)
            val needToMax = maxVoltage - voltageDouble
            val needToMaxInWh = needToMax * whInOneV
            val chargePower = maxVoltage * WheelLog.AppConfig.chargingPower / 10.0
            val chargeTime = (needToMaxInWh / chargePower * 60).toInt()
            return if (speed == 0) String.format(
                Locale.US,
                "~%d min",
                chargeTime
            ) else String.format(
                Locale.US, "~%d min *", chargeTime
            )
        }
    var alert: String?
        get() {
            val nAlert = mAlert.toString()
            mAlert = StringBuilder()
            return nAlert
        }
        set(value) {
            if (mAlert.length != 0) {
                if (mAlert.length > 1000) {
                    mAlert = StringBuilder("... | ")
                } else {
                    mAlert.append(" | ")
                }
            }
            mAlert.append(value)
        }
    val averageBatteryConsumption: Double
        get() = MathsUtil.clamp(mBatteryStart - mBattery, 0, 100).toDouble()
    val distanceFromStart: Double
        get() = if (mTotalDistance != 0L) {
            (mTotalDistance - mStartTotalDistance).toDouble()
        } else 0.toDouble()
    val batteryPerKm: Double
        get() {
            val distance = distanceFromStart
            return if (distance != 0.0) {
                averageBatteryConsumption * 1000 / distance
            } else {
                0.toDouble()
            }
        }
    val avgVoltagePerCell: Double
        get() {
            val adapter = adapter ?: return 0.0
            val cells = max(1.0, adapter.cellsForWheel.toDouble()).toInt()
            return voltage / (cells * 100.0)
        }
    val remainingDistance: Double
        get() {
            val batteryByKm = batteryPerKm
            return if (batteryByKm != 0.0) {
                mBattery / batteryByKm
            } else {
                0.toDouble()
            }
        }
    val averageSpeedDouble: Double
        get() = if (mTotalDistance != 0L && rideTime != 0) {
            // 3.6 = (60 sec * 60 mim) / 1000 meters.
            distanceFromStart * 3.6 / (rideTime + mLastRideTime)
        } else 0.0
    val averageRidingSpeedDouble: Double
        get() = if (mTotalDistance != 0L && mRidingTime != 0) {
            // 3.6 = (60 sec * 60 mim) / 1000 meters.
            distanceFromStart * 3.6 / mRidingTime
        } else 0.0
    val rideTimeString: String
        get() {
            val currentTime = rideTime + mLastRideTime
            val hours = TimeUnit.SECONDS.toHours(currentTime.toLong())
            val minutes = TimeUnit.SECONDS.toMinutes(currentTime.toLong()) -
                    TimeUnit.HOURS.toMinutes(TimeUnit.SECONDS.toHours(currentTime.toLong()))
            val seconds = TimeUnit.SECONDS.toSeconds(currentTime.toLong()) -
                    TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(currentTime.toLong()))
            return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
        }
    val ridingTimeString: String
        get() {
            val hours = TimeUnit.SECONDS.toHours(mRidingTime.toLong())
            val minutes = TimeUnit.SECONDS.toMinutes(mRidingTime.toLong()) -
                    TimeUnit.HOURS.toMinutes(TimeUnit.SECONDS.toHours(mRidingTime.toLong()))
            val seconds = TimeUnit.SECONDS.toSeconds(mRidingTime.toLong()) -
                    TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(mRidingTime.toLong()))
            return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
        }
    val speedDouble: Double
        get() = mSpeed / 100.0
    val voltageDouble: Double
        get() = voltage / 100.0
    val voltageSagDouble: Double
        get() = mVoltageSag / 100.0
    val powerDouble: Double
        get() = mPower / 100.0

    private fun setMaxPower(power: Int) {
        mMaxPower = max(mMaxPower, power.toDouble())
    }

    fun setPower(value: Int) {
        mPower = value
        setMaxPower(value)
        pushPower(powerDouble, distance)
    }

    val currentDouble: Double
        get() = mCurrent / 100.0

    private fun setMaxCurrent(value: Int) {
        mMaxCurrent = max(mMaxCurrent, value.toDouble())
    }

    var current: Int
        get() = mCurrent
        set(value) {
            mCurrent = value
            setMaxCurrent(value)
        }
    val phaseCurrentDouble: Double
        get() = phaseCurrent / 100.0
    val calculatedPwm: Double
        get() = mCalculatedPwm * 100.0
    var maxPwm: Double
        get() = mMaxPwm * 100.0
        set(currentPwm) {
            if (currentPwm > mMaxPwm && currentPwm > 0) mMaxPwm = currentPwm
        }
    var topSpeed: Int
        get() = mTopSpeed
        set(topSpeed) {
            if (topSpeed > mTopSpeed) mTopSpeed = topSpeed
        }
    val topSpeedDouble: Double
        get() = mTopSpeed / 100.0
    val distance: Int
        get() = (mTotalDistance - mStartTotalDistance).toInt()
    val wheelDistanceDouble: Double
        get() = wheelDistance / 1000.0
    val userDistanceDouble: Double
        get() {
            if (mUserDistance == 0L && mTotalDistance != 0L) {
                mUserDistance = WheelLog.AppConfig.userDistance
                if (mUserDistance == 0L) {
                    WheelLog.AppConfig.userDistance = mTotalDistance
                    mUserDistance = mTotalDistance
                }
            }
            return (mTotalDistance - mUserDistance) / 1000.0
        }
    val mac: String
        get() = if (bluetoothService != null) bluetoothService!!.wheelAddress else "default"

    override fun resetUserDistance() {
        if (mTotalDistance != 0L) {
            WheelLog.AppConfig.userDistance = mTotalDistance
            mUserDistance = mTotalDistance
        }
    }

    override fun resetMaxValues() {
        mTopSpeed = 0
        mMaxPwm = 0.0
        mMaxCurrent = 0.0
        mMaxPower = 0.0
    }

    override fun resetExtremumValues() {
        resetMaxValues()
        batteryLowestLevel = 101
    }

    override fun resetVoltageSag() {
        Timber.i("Sag WD")
        mVoltageSag = 20000
        if (bluetoothService != null) {
            bluetoothService!!.applicationContext.sendBroadcast(Intent(Constants.ACTION_PREFERENCE_RESET))
        }
    }

    override val distanceDouble: Double
        get() = (mTotalDistance - mStartTotalDistance) / 1000.0
    override val totalDistanceDouble: Double
        get() = mTotalDistance / 1000.0
    override var totalDistance: Long
        get() = mTotalDistance
        set(totalDistance) {
            if (mStartTotalDistance == 0L && mTotalDistance != 0L) mStartTotalDistance =
                mTotalDistance
            mTotalDistance = totalDistance
        }
    var bmsView: Boolean
        get() = mBmsView
        set(bmsView) {
            if (mBmsView != bmsView) resetBmsData()
            mBmsView = bmsView
        }

    fun resetBmsData() {
        bms1.reset()
        bms2.reset()
    }

    fun setCurrentTime(currentTime: Int) {
        if (rideTime > currentTime + TIME_BUFFER) mLastRideTime = rideTime
        rideTime = currentTime
    }

    var voltageSag: Int
        get() = mVoltageSag
        set(voltSag) {
            if (voltSag < mVoltageSag && voltSag > 0) mVoltageSag = voltSag
        }

    fun decodeResponse(data: ByteArray, mContext: Context) {
        timestamp_raw = System.currentTimeMillis() //new Date(); //sdf.format(new Date());
        val stringBuilder = StringBuilder(data.size)
        for (aData in data) stringBuilder.append(String.format(Locale.US, "%02X", aData))
        Timber.i("Received: %s", stringBuilder)
        if (protoVer !== "") {
            Timber.i("Decode, proto: %s", protoVer)
        }
        val new_data = adapter!!.decode(data)
        if (!new_data) return
        lastLifeData = System.currentTimeMillis()
        resetRideTime()
        updateRideTime()
        topSpeed = mSpeed
        voltageSag = voltage
        maxTemp = mTemperature
        mCalculatedPwm =
            if (mWheelType === WHEEL_TYPE.KINGSONG || mWheelType === WHEEL_TYPE.INMOTION_V2 || WheelLog.AppConfig.hwPwm) {
                mOutput.toDouble() / 10000.0
            } else {
                val rotationSpeed = WheelLog.AppConfig.rotationSpeed / 10.0
                val rotationVoltage = WheelLog.AppConfig.rotationVoltage / 10.0
                val powerFactor = WheelLog.AppConfig.powerFactor / 100.0
                mSpeed / (rotationSpeed / rotationVoltage * voltage * powerFactor)
            }
        maxPwm = mCalculatedPwm
        if (mWheelType === WHEEL_TYPE.GOTWAY || mWheelType === WHEEL_TYPE.VETERAN) {
            current = Math.round(mCalculatedPwm * phaseCurrent).toInt()
        }
        if (mWheelType !== WHEEL_TYPE.INMOTION_V2) {
            setPower(Math.round(currentDouble * voltage).toInt())
        }
        val intent = Intent(Constants.ACTION_WHEEL_DATA_AVAILABLE)
        if (graph_last_update_time + GRAPH_UPDATE_INTERVAL < Calendar.getInstance()
                .getTimeInMillis()
        ) {
            graph_last_update_time = Calendar.getInstance().getTimeInMillis()
            intent.putExtra(Constants.INTENT_EXTRA_GRAPH_UPDATE_AVAILABLE, true)
            currentAxis.add(currentDouble.toFloat())
            speedAxis.add(speedDouble.toFloat())
            xAxis.add(SimpleDateFormat("HH:mm:ss", Locale.US).format(Calendar.getInstance().time))
            if (speedAxis.size > 3600000 / GRAPH_UPDATE_INTERVAL) {
                speedAxis.removeAt(0)
                currentAxis.removeAt(0)
                xAxis.removeAt(0)
            }
        }
        timeStamp = timestamp_raw
        intent.putExtra("Speed", mSpeed)
        mContext.sendBroadcast(intent)
        if (!isWheelIsReady && adapter!!.isReady) {
            isWheelIsReady = true
            val isReadyIntent = Intent(Constants.ACTION_WHEEL_IS_READY)
            mContext.sendBroadcast(isReadyIntent)
        }
        checkMuteMusic()
    }

    private fun checkMuteMusic() {
        if (!WheelLog.AppConfig.useStopMusic) return
        val muteSpeedThreshold = 3.5
        val speed = speedDouble
        if (speed <= muteSpeedThreshold) {
            mLowSpeedMusicTime = 0
            MainActivity.audioManager.setStreamMute(AudioManager.STREAM_MUSIC, true)
        } else {
            if (mLowSpeedMusicTime == 0L) mLowSpeedMusicTime = System.currentTimeMillis()
            if (System.currentTimeMillis() - mLowSpeedMusicTime >= 1500) MainActivity.audioManager.setStreamMute(
                AudioManager.STREAM_MUSIC,
                false
            )
        }
    }

    fun resetRideTime() {
        if (rideStartTime == 0L) {
            rideStartTime = Calendar.getInstance().getTimeInMillis()
            mRidingTime = 0
        }
    }

    fun incrementRidingTime() {
        mRidingTime++
    }

    /*
        Only for restore from log
     */
    fun setStartParameters(rideStartTime: Long, startTotalDistance: Long) {
        mRidingTime = 0
        mLastRideTime = 0
        this.rideStartTime = rideStartTime
        mStartTotalDistance = startTotalDistance
    }

    fun updateRideTime() {
        val currentTime = (Calendar.getInstance().getTimeInMillis() - rideStartTime).toInt() / 1000
        setCurrentTime(currentTime)
    }

    fun full_reset() {
        if (mWheelType === WHEEL_TYPE.INMOTION) InMotionAdapter.stopTimer()
        if (mWheelType === WHEEL_TYPE.INMOTION_V2) InmotionAdapterV2.stopTimer()
        if (mWheelType === WHEEL_TYPE.NINEBOT_Z) NinebotZAdapter.stopTimer()
        if (mWheelType === WHEEL_TYPE.NINEBOT) NinebotAdapter.stopTimer()
        mWheelType = WHEEL_TYPE.Unknown
        //mWheelType = WHEEL_TYPE.GOTWAY; //test
        xAxis.clear()
        speedAxis.clear()
        currentAxis.clear()
        reset()
        resetBmsData()
    }

    fun reset() {
        mLowSpeedMusicTime = 0
        mSpeed = 0
        torque = 0.0
        motorPower = 0.0
        cpuTemp = 0
        imuTemp = 0
        speedLimit = 0.0
        currentLimit = 0.0
        mTotalDistance = 0
        mCurrent = 0
        mPower = 0
        mTemperature = 0
        mTemperature2 = 0
        cpuLoad = 0
        mOutput = 0
        angle = 0.0
        roll = 0.0
        mBattery = 0
        batteryLowestLevel = 101
        mBatteryStart = -1
        //mAverageBatteryCount = 0;
        mCalculatedPwm = 0.0
        mMaxPwm = 0.0
        mMaxTemp = 0
        voltage = 0
        mVoltageSag = 20000
        rideTime = 0
        mRidingTime = 0
        mTopSpeed = 0
        fanStatus = 0
        chargingStatus = 0
        wheelDistance = 0
        mUserDistance = 0
        name = ""
        mModel = ""
        modeStr = ""
        mVersion = ""
        serial = ""
        mBtName = ""
        rideStartTime = 0
        mStartTotalDistance = 0
        protoVer = ""
        isWheelIsReady = false
    }

    fun detectWheel(deviceAddress: String?, mContext: Context?, servicesResId: Int): Boolean {
        WheelLog.AppConfig.lastMac = deviceAddress!!
        val advData = WheelLog.AppConfig.advDataForWheel
        var adapterName = ""
        protoVer = ""
        if (inArray(advData, arrayOf("4e421300000000ec", "4e421302000000ea"))) {
            protoVer = "S2"
        } else if (inArray(
                advData,
                arrayOf("4e421400000000eb", "4e422000000000df", "4e422200000000dd", "4e4230cf")
            ) || advData.startsWith("5600")
        ) {
            protoVer = "Mini"
        }
        Timber.i("ProtoVer %s, adv: %s", protoVer, advData)
        var detected_wheel = false
        val text = getRawTextResource(mContext!!, servicesResId)
        if (bluetoothService == null) {
            Timber.wtf("[error] BluetoothService is null. The wheel could not be detected.")
            return false
        }
        val wheelServices = bluetoothService!!.getWheelServices() ?: return false
        try {
            val arr = JSONArray(text)
            var i = 0
            while (i < arr.length() && !detected_wheel) {
                val services = arr.getJSONObject(i)
                if (services.length() - 1 != wheelServices.size) {
                    Timber.i("Services len not corresponds, go to the next")
                    i++
                    continue
                }
                adapterName = services.getString("adapter")
                Timber.i("Searching for %s", adapterName)
                val iterator = services.keys()
                // skip adapter key
                iterator.next()
                var go_next_adapter = false
                while (iterator.hasNext()) {
                    val keyName = iterator.next()
                    Timber.i("Key name %s", keyName)
                    val s_uuid = UUID.fromString(keyName)
                    val service = bluetoothService!!.getWheelService(s_uuid)
                    if (service == null) {
                        Timber.i("No such service")
                        go_next_adapter = true
                        break
                    }
                    val service_uuid = services.getJSONArray(keyName)
                    if (service_uuid.length() != service.characteristics.size) {
                        Timber.i("Characteristics len not corresponds, go to the next")
                        go_next_adapter = true
                        break
                    }
                    for (j in 0 until service_uuid.length()) {
                        val c_uuid = UUID.fromString(service_uuid.getString(j))
                        Timber.i("UUid %s", service_uuid.getString(j))
                        val characteristic = service.getCharacteristic(c_uuid)
                        if (characteristic == null) {
                            Timber.i("UUid not found")
                            go_next_adapter = true
                            break
                        } else {
                            Timber.i("UUid found")
                        }
                    }
                    if (go_next_adapter) {
                        break
                    }
                }
                if (!go_next_adapter) {
                    Timber.i("Wheel Detected as %s", adapterName)
                    detected_wheel = true
                }
                i++
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        if (detected_wheel) {
            Timber.i("Protocol recognized as %s", adapterName)
            if (WHEEL_TYPE.GOTWAY.toString().equals(
                    adapterName,
                    ignoreCase = true
                ) && (mBtName == "RW" || name.startsWith("ROCKW"))
            ) {
                Timber.i("It seems to be RochWheel, force to Kingsong proto")
                adapterName = WHEEL_TYPE.KINGSONG.toString()
            }
            if (WHEEL_TYPE.KINGSONG.toString().equals(adapterName, ignoreCase = true)) {
                wheelType = WHEEL_TYPE.KINGSONG
                val targetService =
                    bluetoothService!!.getWheelService(Constants.KINGSONG_SERVICE_UUID)
                val notifyCharacteristic =
                    targetService!!.getCharacteristic(Constants.KINGSONG_READ_CHARACTER_UUID)
                bluetoothService!!.setCharacteristicNotification(notifyCharacteristic, true)
                val descriptor =
                    notifyCharacteristic.getDescriptor(Constants.KINGSONG_DESCRIPTER_UUID)
                bluetoothService!!.writeWheelDescriptor(
                    descriptor,
                    BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                )
                return true
            } else if (WHEEL_TYPE.GOTWAY.toString().equals(adapterName, ignoreCase = true)) {
                wheelType = WHEEL_TYPE.GOTWAY_VIRTUAL
                val targetService =
                    bluetoothService!!.getWheelService(Constants.GOTWAY_SERVICE_UUID)
                val notifyCharacteristic =
                    targetService!!.getCharacteristic(Constants.GOTWAY_READ_CHARACTER_UUID)
                bluetoothService!!.setCharacteristicNotification(notifyCharacteristic, true)
                // Let the user know it's working by making the wheel beep
                if (WheelLog.AppConfig.connectBeep) bluetoothService!!.writeWheelCharacteristic("b".toByteArray())
                return true
            } else if (WHEEL_TYPE.INMOTION.toString().equals(adapterName, ignoreCase = true)) {
                wheelType = WHEEL_TYPE.INMOTION
                val targetService =
                    bluetoothService!!.getWheelService(Constants.INMOTION_SERVICE_UUID)
                val notifyCharacteristic =
                    targetService!!.getCharacteristic(Constants.INMOTION_READ_CHARACTER_UUID)
                bluetoothService!!.setCharacteristicNotification(notifyCharacteristic, true)
                val descriptor =
                    notifyCharacteristic.getDescriptor(Constants.INMOTION_DESCRIPTER_UUID)
                bluetoothService!!.writeWheelDescriptor(
                    descriptor,
                    BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                )
                val inmotionPassword = WheelLog.AppConfig.passwordForWheel
                if (inmotionPassword.length > 0) {
                    InMotionAdapter.instance.startKeepAliveTimer(inmotionPassword)
                    return true
                }
                return false
            } else if (WHEEL_TYPE.INMOTION_V2.toString().equals(adapterName, ignoreCase = true)) {
                Timber.i("Trying to start Inmotion V2")
                wheelType = WHEEL_TYPE.INMOTION_V2
                val targetService =
                    bluetoothService!!.getWheelService(Constants.INMOTION_V2_SERVICE_UUID)
                Timber.i("service UUID")
                val notifyCharacteristic =
                    targetService!!.getCharacteristic(Constants.INMOTION_V2_READ_CHARACTER_UUID)
                Timber.i("read UUID")
                if (notifyCharacteristic == null) {
                    Timber.i("it seems that RX UUID doesn't exist")
                }
                bluetoothService!!.setCharacteristicNotification(notifyCharacteristic!!, true)
                Timber.i("notify UUID")
                val descriptor =
                    notifyCharacteristic.getDescriptor(Constants.INMOTION_V2_DESCRIPTER_UUID)
                Timber.i("descr UUID")
                if (descriptor == null) {
                    Timber.i("it seems that descr UUID doesn't exist")
                } else {
                    Timber.i("enable notify UUID")
                    bluetoothService!!.writeWheelDescriptor(
                        descriptor,
                        BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    )
                    Timber.i("write notify")
                }
                InmotionAdapterV2.instance!!.startKeepAliveTimer()
                Timber.i("starting Inmotion V2 adapter")
                return true
            } else if (WHEEL_TYPE.NINEBOT_Z.toString().equals(adapterName, ignoreCase = true)) {
                Timber.i("Trying to start Ninebot Z")
                if (protoVer.compareTo("") == 0) {
                    Timber.i("really Z")
                    wheelType = WHEEL_TYPE.NINEBOT_Z
                } else {
                    Timber.i("no, switch to NB")
                    wheelType = WHEEL_TYPE.NINEBOT
                }
                val targetService =
                    bluetoothService!!.getWheelService(Constants.NINEBOT_Z_SERVICE_UUID)
                Timber.i("service UUID")
                val notifyCharacteristic =
                    targetService!!.getCharacteristic(Constants.NINEBOT_Z_READ_CHARACTER_UUID)
                Timber.i("read UUID")
                if (notifyCharacteristic == null) {
                    Timber.i("it seems that RX UUID doesn't exist")
                }
                bluetoothService!!.setCharacteristicNotification(notifyCharacteristic!!, true)
                Timber.i("notify UUID")
                val descriptor =
                    notifyCharacteristic.getDescriptor(Constants.NINEBOT_Z_DESCRIPTER_UUID)
                Timber.i("descr UUID")
                if (descriptor == null) {
                    Timber.i("it seems that descr UUID doesn't exist")
                } else {
                    Timber.i("enable notify UUID")
                    bluetoothService!!.writeWheelDescriptor(
                        descriptor,
                        BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    )
                }
                Timber.i("write notify")
                if (protoVer.compareTo("S2") == 0 || protoVer.compareTo("Mini") == 0) {
                    NinebotAdapter.instance!!.startKeepAliveTimer(protoVer)
                    Timber.i("starting ninebot adapter, proto: %s", protoVer)
                } else {
                    NinebotZAdapter.instance!!.startKeepAliveTimer()
                    Timber.i("starting ninebot Z adapter")
                }
                return true
            } else if (WHEEL_TYPE.NINEBOT.toString().equals(adapterName, ignoreCase = true)) {
                Timber.i("Trying to start Ninebot")
                wheelType = WHEEL_TYPE.NINEBOT
                val targetService =
                    bluetoothService!!.getWheelService(Constants.NINEBOT_SERVICE_UUID)
                Timber.i("service UUID")
                val notifyCharacteristic =
                    targetService!!.getCharacteristic(Constants.NINEBOT_READ_CHARACTER_UUID)
                Timber.i("read UUID")
                if (notifyCharacteristic == null) {
                    Timber.i("it seems that RX UUID doesn't exist")
                }
                bluetoothService!!.setCharacteristicNotification(notifyCharacteristic!!, true)
                Timber.i("notify UUID")
                val descriptor =
                    notifyCharacteristic.getDescriptor(Constants.NINEBOT_DESCRIPTER_UUID)
                Timber.i("descr UUID")
                if (descriptor == null) {
                    Timber.i("it seems that descr UUID doesn't exist")
                } else {
                    Timber.i("enable notify UUID")
                    bluetoothService!!.writeWheelDescriptor(
                        descriptor,
                        BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    )
                    Timber.i("write notify")
                }
                NinebotAdapter.instance!!.startKeepAliveTimer(protoVer)
                Timber.i("starting ninebot adapter")
                return true
            }
        } else {
            WheelLog.AppConfig.lastMac = ""
            Timber.i("Protocol recognized as Unknown")
            for (service in wheelServices) {
                Timber.i("Service: %s", service.uuid.toString())
                for (characteristics in service.characteristics) {
                    Timber.i("Characteristics: %s", characteristics.uuid.toString())
                }
            }
        }
        return false
    }

    fun setCalculatedPwm(d: Double) {
        mCalculatedPwm = d
    }

    fun setConnectionState(b: Boolean) {
        mConnectionState = b
    }

    companion object {
        private const val TIME_BUFFER = 10

        /////
        var instance: WheelData? = null
        private const val GRAPH_UPDATE_INTERVAL = 1000 // milliseconds
        private const val RIDING_SPEED = 200 // 2km/h
        fun initiate() {
            if (instance == null) instance = WheelData() else {
                if (instance!!.ridingTimerControl != null) {
                    instance!!.ridingTimerControl!!.cancel()
                    instance!!.ridingTimerControl = null
                }
            }
            instance!!.full_reset()
            instance!!.startRidingTimerControl()
            // mInstance.startAlarmTest(); // test
        }
    }
}
