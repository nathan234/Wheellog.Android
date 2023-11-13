package com.cooper.wheellog.utils.inmotion

import com.cooper.wheellog.WheelData
import timber.log.Timber

class TimerUpdateUseCase(private val wd: WheelData) {
    fun extracted(
        updateStep: Int,
        stateCon: Int,
        settingCommandReady: Boolean,
        settingCommand: ByteArray,
        requestSettings: Boolean,
    ): TimerUpdateResult {
        var updateStep = updateStep
        var stateCon = stateCon
        var settingCommandReady = settingCommandReady
        var requestSettings = requestSettings
        if (updateStep == 0) {
            when {
                stateCon == 0 -> {
                    if (wd.bluetoothCmd(InmotionAdapterV2.Message.carType.writeBuffer())) {
                        Timber.i("Sent car type message")
                    } else {
                        updateStep = 35
                    }
                }
                stateCon == 1 -> {
                    if (wd.bluetoothCmd(InmotionAdapterV2.Message.serialNumber.writeBuffer())) {
                        Timber.i("Sent s/n message")
                    } else {
                        updateStep = 35
                    }
                }
                stateCon == 2 -> {
                    if (wd.bluetoothCmd(InmotionAdapterV2.Message.versions.writeBuffer())) {
                        stateCon += 1
                        Timber.i("Sent versions message")
                    } else {
                        updateStep = 35
                    }
                }
                settingCommandReady -> {
                    if (wd.bluetoothCmd(settingCommand)) {
                        settingCommandReady = false
                        requestSettings = true
                        Timber.i("Sent command message")
                    } else {
                        updateStep = 35 // after +1 and %10 = 0
                    }
                }
                (stateCon == 3) or requestSettings -> {
                    if (wd.bluetoothCmd(InmotionAdapterV2.Message.currentSettings.writeBuffer())) {
                        stateCon += 1
                        Timber.i("Sent unknown data message")
                    } else {
                        updateStep = 35
                    }
                }
                stateCon == 4 -> {
                    if (wd.bluetoothCmd(InmotionAdapterV2.Message.uselessData.writeBuffer())) {
                        Timber.i("Sent useless data message")
                        stateCon += 1
                    } else {
                        updateStep = 35
                    }
                }
                stateCon == 5 -> {
                    if (wd.bluetoothCmd(InmotionAdapterV2.Message.statistics.writeBuffer())) {
                        Timber.i("Sent statistics data message")
                        stateCon += 1
                    } else {
                        updateStep = 35
                    }
                }
                else -> {
                    if (wd.bluetoothCmd(InmotionAdapterV2.Message.realTimeData.writeBuffer())) {
                        Timber.i("Sent realtime data message")
                        stateCon = 5
                    } else {
                        updateStep = 35
                    }
                }
            }
        }
        updateStep += 1
        updateStep %= 10
        Timber.i("Step: %d", updateStep)
        return TimerUpdateResult(
            updateStep = updateStep,
            stateCon = stateCon,
            settingCommandReady = settingCommandReady,
            requestSettings = requestSettings,
        )
    }

    class TimerUpdateResult(
        val updateStep: Int,
        val stateCon: Int,
        val settingCommandReady: Boolean,
        val requestSettings: Boolean,
    )
}
