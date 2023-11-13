package com.cooper.wheellog.utils.inmotion

import com.cooper.wheellog.AppConfig
import com.cooper.wheellog.WheelData
import com.cooper.wheellog.WheelLog
import com.cooper.wheellog.utils.BaseAdapter
import timber.log.Timber
import java.util.Timer
import java.util.TimerTask

class InMotionAdapterV2(
    private val unPacker: InMotionUnpackerV2,
    private val timerUpdateUseCase: TimerUpdateUseCase,
    private val appConfig: AppConfig,
) : BaseAdapter() {
    private var keepAliveTimer: Timer? = null
    private var settingCommandReady = false
    private var requestSettings = false
    private var turningOff = false
    private lateinit var settingCommand: ByteArray

    override fun decode(data: ByteArray?): Boolean {
        for (c in data!!) {
            if (unPacker.addChar(c.toInt())) {
                updateStep = 0
                val result = Message.verify(unPacker.getBuffer())
                if (result != null) {
                    Timber.i("Get new data, command: %02X", result.command)
                    when (result.flags) {
                        Message.Flag.Initial.value -> {
                            if (result.command == Message.Command.MainInfo.value) {
                                val parseMainResult = result.parseMainData(stateCon = stateCon)
                                stateCon = parseMainResult.stateCon
                                return parseMainResult.decodeResult
                            } else if (result.command == Message.Command.Diagnistic.value && turningOff) {
                                settingCommand = Message.wheelOffSecondStage().writeBuffer()
                                turningOff = false
                                settingCommandReady = true
                                return false
                            }
                        }
                        Message.Flag.Default.value -> {
                            when (result.command) {
                                Message.Command.Settings.value -> {
                                    requestSettings = false
                                    return when (inMotionModel) {
                                        InMotionModel.V12 -> {
                                            false
                                        }

                                        InMotionModel.V13 -> {
                                            false
                                        }

                                        else -> {
                                            result.parseSettings()
                                        }
                                    }
                                }

                                Message.Command.Diagnistic.value -> {
                                    return result.parseDiagnostic()
                                }

                                Message.Command.BatteryRealTimeInfo.value -> {
                                    return result.parseBatteryRealTimeInfo()
                                }

                                Message.Command.TotalStats.value -> {
                                    return result.parseTotalStats()
                                }

                                Message.Command.RealTimeInfo.value -> {
                                    return when {
                                        inMotionModel == InMotionModel.V12 -> {
                                            val info = result.parseRealTimeInfoV12(context, lightSwitchCounter)
                                            lightSwitchCounter = info.first
                                            info.second
                                        }

                                        inMotionModel == InMotionModel.V13 -> {
                                            val info = result.parseRealTimeInfoV13(lightSwitchCounter)
                                            lightSwitchCounter = info.first
                                            info.second
                                        }

                                        protoVer < 2 -> {
                                            val info = result.parseRealTimeInfoV11(context, lightSwitchCounter)
                                            lightSwitchCounter = info.first
                                            info.second
                                        }

                                        else -> {
                                            val info = result.parseRealTimeInfoV11version1dot4(context, lightSwitchCounter)
                                            lightSwitchCounter = info.first
                                            info.second
                                        }
                                    }
                                }

                                else -> {
                                    Timber.i("Get unknown command: %02X", result.command)
                                }
                            }
                        }
                    }
                }
            }
        }
        return false
    }

    override val isReady: Boolean
        get() = inMotionModel != InMotionModel.UNKNOWN && protoVer != 0
    val maxSpeed: Int
        get() {
            return when (inMotionModel) {
                InMotionModel.V11 -> 60
                InMotionModel.V12 -> 70
                InMotionModel.V13 -> 100
                InMotionModel.UNKNOWN -> 100
            }
        }

    fun startKeepAliveTimer() {
        updateStep = 0
        stateCon = 0
        val timerTask: TimerTask = object : TimerTask() {
            override fun run() {
                val result = timerUpdateUseCase.extracted(
                    updateStep = updateStep,
                    stateCon = stateCon,
                    settingCommandReady = settingCommandReady,
                    settingCommand = settingCommand,
                    requestSettings = requestSettings,
                )
                updateStep = result.updateStep
                stateCon = result.stateCon
                settingCommandReady = result.settingCommandReady
                requestSettings = result.requestSettings
            }
        }
        keepAliveTimer = Timer()
        keepAliveTimer!!.scheduleAtFixedRate(timerTask, 100, 25)
    }

    override fun wheelBeep() {
        settingCommand = Message.playSound(0x18).writeBuffer()
        settingCommandReady = true
    }

    override fun switchFlashlight() {
        val light = !appConfig.lightEnabled
        appConfig.lightEnabled = light
        setLightState(light)
    }

    override fun setLightState(on: Boolean) {
        settingCommand = Message.setLight(on).writeBuffer()
        settingCommandReady = true
    }

    override fun setHandleButtonState(on: Boolean) {
        settingCommand = Message.setHandleButton(on).writeBuffer()
        settingCommandReady = true
    }

    override fun setRideMode(on: Boolean) {
        settingCommand = Message.setClassicMode(on).writeBuffer()
        settingCommandReady = true
    }

    override fun setSpeakerVolume(speakerVolume: Int) {
        settingCommand = Message.setVolume(speakerVolume).writeBuffer()
        settingCommandReady = true
    }

    override fun setPedalTilt(angle: Int) {
        settingCommand = Message.setPedalTilt(angle).writeBuffer()
        settingCommandReady = true
    }

    override fun setPedalSensivity(sensivity: Int) {
        settingCommand = Message.setPedalSensivity(sensivity).writeBuffer()
        settingCommandReady = true
    }

    override fun wheelCalibration() {
        settingCommand = Message.wheelCalibration().writeBuffer()
        settingCommandReady = true
    }

    override fun setLockMode(on: Boolean) {
        settingCommand = Message.setLock(on).writeBuffer()
        settingCommandReady = true
    }

    override fun setTransportMode(on: Boolean) {
        settingCommand = Message.setTransportMode(on).writeBuffer()
        settingCommandReady = true
    }

    override fun setDrl(on: Boolean) {
        settingCommand = Message.setDrl(on).writeBuffer()
        settingCommandReady = true
    }

    override fun setGoHomeMode(on: Boolean) {
        settingCommand = Message.setGoHome(on).writeBuffer()
        settingCommandReady = true
    }

    override fun setFancierMode(on: Boolean) {
        settingCommand = Message.setFancierMode(on).writeBuffer()
        settingCommandReady = true
    }

    override fun setMute(on: Boolean) {
        settingCommand = Message.setMute(on).writeBuffer()
        settingCommandReady = true
    }

    override fun setFanQuiet(on: Boolean) {
        settingCommand = Message.setQuietMode(on).writeBuffer()
        settingCommandReady = true
    }

    override fun setFan(on: Boolean) {
        settingCommand = Message.setFan(on).writeBuffer()
        settingCommandReady = true
    }

    override fun setLightBrightness(value: Int) {
        settingCommand = Message.setLightBrightness(value).writeBuffer()
        settingCommandReady = true
    }

    override fun updateMaxSpeed(wheelMaxSpeed: Int) {
        settingCommand = Message.setMaxSpeed(wheelMaxSpeed).writeBuffer()
        settingCommandReady = true
    }

    override fun powerOff() {
        settingCommand = Message.wheelOffFirstStage().writeBuffer()
        turningOff = true
        settingCommandReady = true
    }

    fun setModel(m: InMotionModel) {
        inMotionModel = m
    }

    fun setProto(proto: Int) {
        protoVer = proto
    }

    override val cellsForWheel: Int
        get() {
            if (inMotionModel == InMotionModel.V12) {
                return 24
            }
            return if (inMotionModel == InMotionModel.V13) {
                30
            } else {
                20
            }
        }

    companion object {
        private var INSTANCE: InMotionAdapterV2? = null
        private var updateStep = 0
        private var stateCon = 0
        private var lightSwitchCounter = 0

        @JvmField
        var inMotionModel = InMotionModel.UNKNOWN

        @JvmField
        var protoVer: Int = 0

        @JvmStatic
        val proto: Int
            get() {
                return protoVer
            }

        @JvmStatic
        val instance: InMotionAdapterV2?
            get() {
                if (INSTANCE == null) {
                    Timber.i("New instance")
                    INSTANCE = InMotionAdapterV2(
                        InMotionUnpackerV2(),
                        TimerUpdateUseCase(WheelData.getInstance()),
                        WheelLog.AppConfig,
                    )
                }
                Timber.i("Get instance")
                return INSTANCE
            }

        @Synchronized
        fun newInstance() {
            if (INSTANCE != null && INSTANCE!!.keepAliveTimer != null) {
                INSTANCE!!.keepAliveTimer!!.cancel()
                INSTANCE!!.keepAliveTimer = null
            }
            Timber.i("New instance")
            INSTANCE = InMotionAdapterV2(
                InMotionUnpackerV2(),
                TimerUpdateUseCase(WheelData.getInstance()),
                WheelLog.AppConfig,
            )
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
