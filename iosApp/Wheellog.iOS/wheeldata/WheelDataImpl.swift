//
//  WheelData.swift
//  Wheellog.iOS
//
//  Created by nathan retta on 1/14/24.
//

import Foundation

class WheelDataImpl : WheelData {
    
    init(isHardwarePWM: Bool, speed: Int, btName: String? = nil, temperature: Int, maxTemp: Int, temperature2: Int, maxCurrentDouble: Double, maxPowerDouble: Double, output: Int, batteryLevel: Int, isConnected: Bool, version: String, wheelType: Constants.WheelType, model: String, distanceDouble: Double, totalDistanceDouble: Double, totalDistance: Int64) {
        self.isHardwarePWM = isHardwarePWM
        self.speed = speed
        self.btName = btName
        self.temperature = temperature
        self.maxTemp = maxTemp
        self.temperature2 = temperature2
        self.maxCurrentDouble = maxCurrentDouble
        self.maxPowerDouble = maxPowerDouble
        self.output = output
        self.batteryLevel = batteryLevel
        self.isConnected = isConnected
        self.version = version
        self.wheelType = wheelType
        self.model = model
        self.distanceDouble = distanceDouble
        self.totalDistanceDouble = totalDistanceDouble
        self.totalDistance = totalDistance
    }
    func bluetoothCmd(cmd: [UInt8]?) -> Bool {
        return false
    }
    
    func startRidingTimerControl() {
        
    }
    
    func startAlarmTest() {
        
    }
    
    var isHardwarePWM: Bool
    
    var speed: Int
    
    var btName: String?
    
    func updateLight(enabledLight: Bool) {
        
    }
    
    func updateLed(enabledLed: Bool) {
        
    }
    
    func updateTailLight(tailLight: Bool) {
        
    }
    
    func wheelBeep() {
        
    }
    
    func updatePedalsMode(pedalsMode: Int) {
        
    }
    
    func updateStrobe(strobeMode: Int) {
        
    }
    
    func updateLedMode(ledMode: Int) {
        
    }
    
    func updateAlarmMode(alarmMode: Int) {
        
    }
    
    func wheelCalibration() {
        
    }
    
    func powerOff() {
        
    }
    
    func updateHandleButton(enabledButton: Bool) {
        
    }
    
    func updateBrakeAssistant(brakeAssist: Bool) {
        
    }
    
    func setLedColor(value: Int, ledNum: Int) {
        
    }
    
    func updateAlarmEnabled(value: Bool, num: Int) {
        
    }
    
    func updateAlarmSpeed(value: Int, num: Int) {
        
    }
    
    func updateLimitedModeEnabled(value: Bool) {
        
    }
    
    func updateLimitedSpeed(value: Int) {
        
    }
    
    func updateMaxSpeed(wheelMaxSpeed: Int) {
        
    }
    
    func updateSpeakerVolume(speakerVolume: Int) {
        
    }
    
    func updatePedals(pedalAdjustment: Int) {
        
    }
    
    func updatePedalSensivity(pedalSensivity: Int) {
        
    }
    
    func updateRideMode(rideMode: Bool) {
        
    }
    
    func updateLockMode(enable: Bool) {
        
    }
    
    func updateTransportMode(enable: Bool) {
        
    }
    
    func updateDrl(enable: Bool) {
        
    }
    
    func updateGoHome(enable: Bool) {
        
    }
    
    func updateFancierMode(enable: Bool) {
        
    }
    
    func updateMute(enable: Bool) {
        
    }
    
    func updateFanQuiet(enable: Bool) {
        
    }
    
    func updateFanState(enable: Bool) {
        
    }
    
    func updateLightBrightness(brightness: Int) {
        
    }
    
    var temperature: Int
    
    var maxTemp: Int
    
    var temperature2: Int
    
    var maxCurrentDouble: Double
    
    var maxPowerDouble: Double
    
    var output: Int
    
    var batteryLevel: Int
    
    func setChargingStatus(charging: Int) -> Int {
        return -1
    }
    
    var isConnected: Bool
    
    var version: String
    
    var wheelType: Constants.WheelType
    
    var model: String
    
    func resetUserDistance() {
        
    }
    
    func resetMaxValues() {
        
    }
    
    func resetExtremumValues() {
        
    }
    
    func resetVoltageSag() {
        
    }
    
    var distanceDouble: Double
    
    var totalDistanceDouble: Double
    
    var totalDistance: Int64
    
}
