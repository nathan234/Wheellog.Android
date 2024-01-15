//
//  IWheelData.swift
//  Wheellog.iOS
//
//  Created by nathan retta on 1/14/24.
//

import Foundation

protocol IWheelData {
    func bluetoothCmd(cmd: [UInt8]?) -> Bool
    func startRidingTimerControl()
    func startAlarmTest()
    var isHardwarePWM: Bool { get }
    var speed: Int { get set }
    var btName: String? { get set }
    func updateLight(enabledLight: Bool)
    func updateLed(enabledLed: Bool)
    func updateTailLight(tailLight: Bool)
    func wheelBeep()
    func updatePedalsMode(pedalsMode: Int)
    func updateStrobe(strobeMode: Int)
    func updateLedMode(ledMode: Int)
    func updateAlarmMode(alarmMode: Int)
    func wheelCalibration()
    func powerOff()
    func updateHandleButton(enabledButton: Bool)
    func updateBrakeAssistant(brakeAssist: Bool)
    func setLedColor(value: Int, ledNum: Int)
    func updateAlarmEnabled(value: Bool, num: Int)
    func updateAlarmSpeed(value: Int, num: Int)
    func updateLimitedModeEnabled(value: Bool)
    func updateLimitedSpeed(value: Int)
    func updateMaxSpeed(wheelMaxSpeed: Int)
    func updateSpeakerVolume(speakerVolume: Int)
    func updatePedals(pedalAdjustment: Int)
    func updatePedalSensivity(pedalSensivity: Int)
    func updateRideMode(rideMode: Bool)
    func updateLockMode(enable: Bool)
    func updateTransportMode(enable: Bool)
    func updateDrl(enable: Bool)
    func updateGoHome(enable: Bool)
    func updateFancierMode(enable: Bool)
    func updateMute(enable: Bool)
    func updateFanQuiet(enable: Bool)
    func updateFanState(enable: Bool)
    func updateLightBrightness(brightness: Int)
    var temperature: Int { get set }
    var maxTemp: Int { get set }
    var temperature2: Int { get set }
    var maxCurrentDouble: Double { get }
    var maxPowerDouble: Double { get }
    var output: Int { get set }
    var batteryLevel: Int { get set }
    func setChargingStatus(charging: Int) -> Int
    var isConnected: Bool { get set }
    var version: String { get set }
    var wheelType: Constants.WheelType { get set }
    var model: String { get set }
    func resetUserDistance()
    func resetMaxValues()
    func resetExtremumValues()
    func resetVoltageSag()
    var distanceDouble: Double { get }
    var totalDistanceDouble: Double { get }
    var totalDistance: Int64 { get set }
}
