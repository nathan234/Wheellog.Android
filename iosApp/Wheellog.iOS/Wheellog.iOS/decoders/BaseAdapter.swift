//
//  BaseAdapter.swift
//  Wheellog.iOS
//
//  Created by nathan retta on 1/14/24.
//

import Foundation

import Foundation

protocol BaseAdapter {
    // TODO
//    var context: Context? { get }
    func decode(data: Data?) -> Bool
    func updatePedalsMode(pedalsMode: Int)
    func setLightMode(lightMode: Int)
    func setRollAngleMode(rollAngleMode: Int)
    func updateBeeperVolume(beeperVolume: Int)
    func setMilesMode(milesMode: Bool)
    func setLightState(on: Bool)
    func setLedState(on: Bool)
    func setTailLightState(on: Bool)
    func setHandleButtonState(on: Bool)
    func setBrakeAssist(on: Bool)
    func setLedColor(value: Int, ledNum: Int)
    func setAlarmEnabled(on: Bool, num: Int)
    var ledModeString: String? { get }
    func getLedIsAvailable(ledNum: Int) -> Bool
    func setLimitedModeEnabled(on: Bool)
    func setLimitedSpeed(value: Int)
    func setAlarmSpeed(value: Int, num: Int)
    func setRideMode(on: Bool)
    func setLockMode(on: Bool)
    func setTransportMode(on: Bool)
    func setDrl(on: Bool)
    func setGoHomeMode(on: Bool)
    func setFancierMode(on: Bool)
    func setMute(on: Bool)
    func setFanQuiet(on: Bool)
    func setFan(on: Bool)
    func setLightBrightness(value: Int)
    func powerOff()
    func switchFlashlight()
    func wheelBeep()
    func updateMaxSpeed(wheelMaxSpeed: Int)
    func setSpeakerVolume(speakerVolume: Int)
    func setPedalTilt(angle: Int)
    func setPedalSensivity(sensivity: Int)
    func wheelCalibration()
    func updateLedMode(ledMode: Int)
    func updateStrobeMode(strobeMode: Int)
    func updateAlarmMode(alarmMode: Int)
    var cellsForWheel: Int { get }
    var isReady: Bool { get }
}
