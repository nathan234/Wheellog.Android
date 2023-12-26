package com.cooper.wheellog

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler
import android.os.IBinder
import androidx.core.content.ContextCompat
import com.cooper.wheellog.utils.Constants
import com.cooper.wheellog.utils.Constants.ALARM_TYPE
import com.cooper.wheellog.utils.Constants.PEBBLE_APP_SCREEN
import com.getpebble.android.kit.PebbleKit
import com.getpebble.android.kit.PebbleKit.PebbleAckReceiver
import com.getpebble.android.kit.PebbleKit.PebbleNackReceiver
import com.getpebble.android.kit.util.PebbleDictionary
import timber.log.Timber
import java.util.Calendar
import java.util.UUID

class PebbleService : Service() {
    private val mHandler = Handler()
    private var last_message_send_time: Long = 0
    var outgoingDictionary = PebbleDictionary()
    var lastSpeed = 0
    var lastBattery = 0
    var lastTemperature = 0
    var lastFanStatus = 0
    var lastRideTime = 0
    var lastDistance = 0
    var lastTopSpeed = 0
    var lastVoltage = 0
    var lastCurrent = 0
    var lastPWM = 0
    var lastConnectionState = false
    var vibe_alarm = -1
    var refreshAll = true
    var displayedScreen = PEBBLE_APP_SCREEN.GUI
    var ready = false
    var message_pending = false
    var data_available = false
    private val mSendPebbleData = Runnable {
        if (!ready) {
            outgoingDictionary.addInt32(KEY_READY, 0)
            ready = true
        }
        if (refreshAll) {
            outgoingDictionary.addInt32(KEY_USE_MPH, if (WheelLog.AppConfig.useMph) 1 else 0)
            outgoingDictionary.addInt32(KEY_MAX_SPEED, WheelLog.AppConfig.maxSpeed)
        }
        val data = WheelData.instance!! ?: return@Runnable
        when (displayedScreen) {
            PEBBLE_APP_SCREEN.GUI -> {
                if (refreshAll || lastSpeed != data.speed) {
                    lastSpeed = data.speed
                    outgoingDictionary.addInt32(KEY_SPEED, lastSpeed)
                }
                if (refreshAll || lastBattery != data.batteryLevel) {
                    lastBattery = data.batteryLevel
                    outgoingDictionary.addInt32(KEY_BATTERY, lastBattery)
                }
                if (refreshAll || lastTemperature != data.temperature) {
                    lastTemperature = data.temperature
                    outgoingDictionary.addInt32(KEY_TEMPERATURE, lastTemperature)
                }
                if (refreshAll || lastFanStatus != data.fanStatus) {
                    lastFanStatus = data.fanStatus
                    outgoingDictionary.addInt32(KEY_FAN_STATE, lastFanStatus)
                }
                if (refreshAll || lastConnectionState != data.isConnected) {
                    lastConnectionState = data.isConnected
                    outgoingDictionary.addInt32(KEY_BT_STATE, if (lastConnectionState) 1 else 0)
                }
                if (refreshAll || lastVoltage != data.voltage) {
                    lastVoltage = data.voltage
                    outgoingDictionary.addInt32(KEY_VOLTAGE, lastVoltage)
                }
                if (refreshAll || lastCurrent != data.current) {
                    lastCurrent = data.current
                    outgoingDictionary.addInt32(KEY_CURRENT, lastCurrent)
                }
                if (refreshAll || lastPWM != data.calculatedPwm.toInt()) {
                    lastPWM = data.calculatedPwm.toInt()
                    outgoingDictionary.addInt32(KEY_PWM, lastCurrent)
                }
            }

            PEBBLE_APP_SCREEN.DETAILS -> {
                if (refreshAll || lastRideTime != data.rideTime) {
                    lastRideTime = data.rideTime
                    outgoingDictionary.addInt32(KEY_RIDE_TIME, lastRideTime)
                }
                if (refreshAll || lastDistance != data.distance) {
                    lastDistance = data.distance
                    outgoingDictionary.addInt32(KEY_DISTANCE, lastDistance / 100)
                }
                if (refreshAll || lastTopSpeed != data.topSpeed) {
                    lastTopSpeed = data.topSpeed
                    outgoingDictionary.addInt32(KEY_TOP_SPEED, lastTopSpeed / 10)
                }
            }
        }
        if (vibe_alarm >= 0) {
            outgoingDictionary.addInt32(KEY_VIBE_ALERT, vibe_alarm)
            vibe_alarm = -1
        }
        if (outgoingDictionary.size() > 0) {
            message_pending = true
            PebbleKit.sendDataToPebble(applicationContext, APP_UUID, outgoingDictionary)
        }
        last_message_send_time = Calendar.getInstance().getTimeInMillis()
        data_available = false
        refreshAll = false
    }
    private val mBroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                Constants.ACTION_ALARM_TRIGGERED -> if (intent.hasExtra(Constants.INTENT_EXTRA_ALARM_TYPE)) {
                    // crutch to legacy pebble app, which does't know alarms other than 0 (speed) and 1 (current)
                    vibe_alarm = 0
                    when (intent.getSerializableExtra(Constants.INTENT_EXTRA_ALARM_TYPE) as ALARM_TYPE?) {
                        ALARM_TYPE.CURRENT, ALARM_TYPE.TEMPERATURE -> vibe_alarm = 1
                        ALARM_TYPE.SPEED1,
                        ALARM_TYPE.SPEED2,
                        ALARM_TYPE.SPEED3,
                        ALARM_TYPE.PWM,
                        ALARM_TYPE.BATTERY,
                        null -> {}
                    }
                }

                Constants.ACTION_PEBBLE_APP_READY -> {
                    displayedScreen = PEBBLE_APP_SCREEN.GUI
                    refreshAll = true
                }

                Constants.ACTION_PEBBLE_AFFECTING_PREFERENCE_CHANGED -> refreshAll = true
                Constants.ACTION_PEBBLE_APP_SCREEN -> if (intent.hasExtra(Constants.INTENT_EXTRA_PEBBLE_DISPLAYED_SCREEN)) {
                    val screen =
                        intent.getIntExtra(Constants.INTENT_EXTRA_PEBBLE_DISPLAYED_SCREEN, 0)
                    if (screen == 0) displayedScreen =
                        PEBBLE_APP_SCREEN.GUI else if (screen == 1) displayedScreen =
                        PEBBLE_APP_SCREEN.DETAILS
                    refreshAll = true
                }
            }

            // There's something new to send, start the check
            if (message_pending &&
                last_message_send_time + MESSAGE_TIMEOUT >= Calendar.getInstance().getTimeInMillis()
            ) data_available = true else mHandler.post(mSendPebbleData)
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        instance = this
        ContextCompat.registerReceiver(
            this,
            ackReceiver,
            IntentFilter(com.getpebble.android.kit.Constants.INTENT_APP_RECEIVE_ACK),
            ContextCompat.RECEIVER_EXPORTED
        )
        ContextCompat.registerReceiver(
            this,
            nackReceiver,
            IntentFilter(com.getpebble.android.kit.Constants.INTENT_APP_RECEIVE_NACK),
            ContextCompat.RECEIVER_EXPORTED
        )
        PebbleKit.startAppOnPebble(this, APP_UUID)
        val intentFilter = IntentFilter()
        intentFilter.addAction(Constants.ACTION_BLUETOOTH_CONNECTION_STATE)
        intentFilter.addAction(Constants.ACTION_WHEEL_DATA_AVAILABLE)
        intentFilter.addAction(Constants.ACTION_ALARM_TRIGGERED)
        intentFilter.addAction(Constants.ACTION_PEBBLE_APP_READY)
        intentFilter.addAction(Constants.ACTION_PEBBLE_APP_SCREEN)
        intentFilter.addAction(Constants.ACTION_PEBBLE_AFFECTING_PREFERENCE_CHANGED)
        ContextCompat.registerReceiver(
            this,
            mBroadcastReceiver,
            intentFilter,
            ContextCompat.RECEIVER_EXPORTED
        )
        val serviceStartedIntent = Intent(Constants.ACTION_PEBBLE_SERVICE_TOGGLED)
            .putExtra(Constants.INTENT_EXTRA_IS_RUNNING, true)
        sendBroadcast(serviceStartedIntent)
        mHandler.post(mSendPebbleData)
        WheelLog.Notifications.update()
        startForeground(Constants.MAIN_NOTIFICATION_ID, WheelLog.Notifications.notification)
        Timber.d("PebbleConnectivity Started")
        return START_STICKY
    }

    override fun onDestroy() {
        try {
            unregisterReceiver(mBroadcastReceiver)
            unregisterReceiver(ackReceiver)
            unregisterReceiver(nackReceiver)
        } catch (exception: Exception) {
            // ignored
        }
        mHandler.removeCallbacksAndMessages(null)
        instance = null
        PebbleKit.closeAppOnPebble(this, APP_UUID)
        val serviceStartedIntent = Intent(Constants.ACTION_PEBBLE_SERVICE_TOGGLED)
        serviceStartedIntent.putExtra(Constants.INTENT_EXTRA_IS_RUNNING, false)
        sendBroadcast(serviceStartedIntent)
        stopForeground(false)
        Timber.i("PebbleConnectivity Stopped")
    }

    private val ackReceiver: PebbleAckReceiver = object : PebbleAckReceiver(APP_UUID) {
        override fun receiveAck(context: Context, transactionId: Int) {
            outgoingDictionary = PebbleDictionary()
            if (data_available) mHandler.post(mSendPebbleData) else message_pending = false
        }
    }
    private val nackReceiver: PebbleNackReceiver = object : PebbleNackReceiver(APP_UUID) {
        override fun receiveNack(context: Context, transactionId: Int) {
            mHandler.post(mSendPebbleData)
        }
    }

    companion object {
        private val APP_UUID = UUID.fromString("185c8ae9-7e72-451a-a1c7-8f1e81df9a3d")
        private const val MESSAGE_TIMEOUT = 500 // milliseconds
        const val KEY_SPEED = 0
        const val KEY_BATTERY = 1
        const val KEY_TEMPERATURE = 2
        const val KEY_FAN_STATE = 3
        const val KEY_BT_STATE = 4
        const val KEY_VIBE_ALERT = 5
        const val KEY_USE_MPH = 6
        const val KEY_MAX_SPEED = 7
        const val KEY_RIDE_TIME = 8
        const val KEY_DISTANCE = 9
        const val KEY_TOP_SPEED = 10
        const val KEY_READY = 11
        const val KEY_VOLTAGE = 12
        const val KEY_CURRENT = 13
        const val KEY_PWM = 20
        private var instance: PebbleService? = null
        val isInstanceCreated: Boolean
            get() = instance != null
    }
}
