package com.cooper.wheellog

import com.cooper.wheellog.utils.Constants

interface IWheelData {
    fun bluetoothCmd(cmd: ByteArray?): Boolean
    fun startRidingTimerControl()
    fun startAlarmTest()
    val isHardwarePWM: Boolean
    var speed: Int
    var btName: String?
    fun updateLight(enabledLight: Boolean)
    fun updateLed(enabledLed: Boolean)
    fun updateTailLight(tailLight: Boolean)
    fun wheelBeep()
    fun updatePedalsMode(pedalsMode: Int)
    fun updateStrobe(strobeMode: Int)
    fun updateLedMode(ledMode: Int)
    fun updateAlarmMode(alarmMode: Int)
    fun wheelCalibration()
    fun powerOff()
    fun updateHandleButton(enabledButton: Boolean)
    fun updateBrakeAssistant(brakeAssist: Boolean)
    fun setLedColor(value: Int, ledNum: Int)
    fun updateAlarmEnabled(value: Boolean, num: Int)
    fun updateAlarmSpeed(value: Int, num: Int)
    fun updateLimitedModeEnabled(value: Boolean)
    fun updateLimitedSpeed(value: Int)
    fun updateMaxSpeed(wheelMaxSpeed: Int)
    fun updateSpeakerVolume(speakerVolume: Int)
    fun updatePedals(pedalAdjustment: Int)
    fun updatePedalSensivity(pedalSensivity: Int)
    fun updateRideMode(rideMode: Boolean)
    fun updateLockMode(enable: Boolean)
    fun updateTransportMode(enable: Boolean)
    fun updateDrl(enable: Boolean)
    fun updateGoHome(enable: Boolean)
    fun updateFancierMode(enable: Boolean)
    fun updateMute(enable: Boolean)
    fun updateFanQuiet(enable: Boolean)
    fun updateFanState(enable: Boolean)
    fun updateLightBrightness(brightness: Int)
    var temperature: Int
    var maxTemp: Int
    var temperature2: Int
    val maxCurrentDouble: Double
    val maxPowerDouble: Double
    var output: Int
    var batteryLevel: Int
    fun setChargingStatus(charging: Int): Int
    var isConnected: Boolean
    var version: String
    var wheelType: Constants.WHEEL_TYPE
    var model: String
    fun resetUserDistance()
    fun resetMaxValues()
    fun resetExtremumValues()
    fun resetVoltageSag()
    val distanceDouble: Double
    val totalDistanceDouble: Double
    var totalDistance: Long
}
