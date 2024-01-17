package com.cooper.wheellog.app

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_UNSPECIFIED
import androidx.preference.PreferenceManager
import com.cooper.wheellog.R
import com.cooper.wheellog.utils.MiBandEnum
import com.cooper.wheellog.wheeldata.WheelData
import com.wheellog.shared.Constants
import com.wheellog.shared.WearPage
import com.wheellog.shared.WearPages
// import com.yandex.metrica.YandexMetrica
import timber.log.Timber

class AppConfig(var context: Context) {
    private val sharedPreferences: SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(context)
    private var specificPrefix: String = ""
    private val separator = ";"
    private val wd by lazy { WheelData.instance!! }

    init {
        // Clear all preferences if they are incompatible
        val version = getValue("versionSettings", -1)
        val currentVer = 1
        if (version < currentVer && sharedPreferences.edit()?.clear()?.commit() == true) {
            setValue("versionSettings", currentVer)
            PreferenceManager.setDefaultValues(
                context,
                R.xml.preferences, false
            )
        }
    }

    //region -=[ general settings ]=-    
    //region application
    var useEng: Boolean
        get() = getValue(Defaults.USE_ENG.res(), false)
        set(value) = setValue(Defaults.USE_ENG.res(), value)

    var appThemeInt: Int
        get() = getValue(Defaults.APP_THEME.res(), ThemeEnum.Original.value.toString()).toInt()
        set(value) = setValue(Defaults.APP_THEME.res(), value.toString())

    val appTheme: Int
        get() {
            val stringVal = getValue(Defaults.APP_THEME.res(), ThemeEnum.Original.value.toString())
            return ThemeEnum.fromInt(stringVal.toInt()).toStyle()
        }

    var dayNightThemeMode: Int
        get() = getValue(Defaults.DAY_NIGHT_THEME.res(), MODE_NIGHT_UNSPECIFIED.toString()).toInt()
        set(value) = setValue(Defaults.DAY_NIGHT_THEME.res(), value.toString())

    var useBetterPercents: Boolean
        get() = getValue(Defaults.USE_BETTER_PERCENTS.res(), false)
        set(value) = setValue(Defaults.USE_BETTER_PERCENTS.res(), value)

    var customPercents: Boolean
        get() = getValue(Defaults.CUSTOM_PERCENTS.res(), false)
        set(value) = setValue(Defaults.CUSTOM_PERCENTS.res(), value)

    var cellVoltageTiltback: Int
        get() = getValue(Defaults.CELL_VOLTAGE_TILTBACK.res(), 330)
        set(value) = setValue(Defaults.CELL_VOLTAGE_TILTBACK.res(), value)

    var useMph: Boolean
        get() = getValue(Defaults.USE_MPH.res(), false)
        set(value) = setValue(Defaults.USE_MPH.res(), value)

    var useFahrenheit: Boolean
        get() = getValue(Defaults.USE_FAHRENHEIT.res(), false)
        set(value) = setValue(Defaults.USE_FAHRENHEIT.res(), value)

    private var viewBlocksString: String?
        get() = getValue(Defaults.VIEW_BLOCKS_STRING.res(), null)
        set(value) = setValue(Defaults.VIEW_BLOCKS_STRING.res(), value)

    var viewBlocks: Array<String>
        get() = this.viewBlocksString?.split(separator)?.toTypedArray()
            ?: arrayOf(
                context.getString(Defaults.PWM.res()),
                context.getString(Defaults.MAX_PWM.res()),
                context.getString(Defaults.VOLTAGE.res()),
                context.getString(Defaults.AVG_RIDING.res()),
                context.getString(Defaults.RIDING_TIME.res()),
                context.getString(Defaults.TOP_SPEED.res()),
                context.getString(Defaults.DISTANCE.res()),
                context.getString(Defaults.TOTAL.res())
            )
        set(value) {
            this.viewBlocksString = value.joinToString(separator)
        }

    var usePipMode: Boolean
        get() = getValue(Defaults.USE_PIP_MODE.res(), true)
        set(value) = setValue(Defaults.USE_PIP_MODE.res(), value)

    var pipBlock: String
        get() = getValue(Defaults.PIP_BLOCK.res(), "")
        set(value) = setValue(Defaults.PIP_BLOCK.res(), value)

    private var notificationButtonsString: String?
        get() = getValue(Defaults.NOTIFICATION_BUTTONS.res(), null)
        set(value) {
            setValue(Defaults.NOTIFICATION_BUTTONS.res(), value)
            WheelLog.Notifications.update()
        }

    var notificationButtons: Array<String>
        get() = this.notificationButtonsString?.split(separator)?.toTypedArray()
            ?: arrayOf(
                context.getString(Defaults.ICON_CONNECTION.res()),
                context.getString(Defaults.ICON_LOGGING.res()),
                context.getString(Defaults.ICON_WATCH.res())
            )
        set(value) {
            this.notificationButtonsString = value.joinToString(separator)
        }

    var maxSpeed: Int
        get() = getValue(Defaults.MAX_SPEED.res(), 50)
        set(value) = setValue(Defaults.MAX_SPEED.res(), value)

    var currentOnDial: Boolean
        get() = getValue(Defaults.CURRENT_ON_DIAL.res(), false)
        set(value) {
            setValue(Defaults.CURRENT_ON_DIAL.res(), value)
            Timber.i("Change dial type to %b", value)
        }

    var pageGraph: Boolean
        get() = getValue(Defaults.SHOW_PAGE_GRAPH.res(), true)
        set(value) = setValue(Defaults.SHOW_PAGE_GRAPH.res(), value)

    var pageEvents: Boolean
        get() = getValue(Defaults.SHOW_PAGE_EVENTS.res(), false)
        set(value) = setValue(Defaults.SHOW_PAGE_EVENTS.res(), value)

    var pageTrips: Boolean
        get() = getValue(Defaults.SHOW_PAGE_TRIPS.res(), true)
        set(value) = setValue(Defaults.SHOW_PAGE_TRIPS.res(), value)

    var connectionSound: Boolean
        get() = getValue(Defaults.CONNECTION_SOUND.res(), false)
        set(value) = setValue(Defaults.CONNECTION_SOUND.res(), value)

    var noConnectionSound: Int
        get() = getValue(Defaults.NO_CONNECTION_SOUND.res(), 5)
        set(value) = setValue(Defaults.NO_CONNECTION_SOUND.res(), value)

    var useStopMusic: Boolean
        get() = getValue(Defaults.USE_STOP_MUSIC.res(), false)
        set(value) = setValue(Defaults.USE_STOP_MUSIC.res(), value)

    var showUnknownDevices: Boolean
        get() = getValue(Defaults.SHOW_UNKNOWN_DEVICES.res(), false)
        set(value) = setValue(Defaults.SHOW_UNKNOWN_DEVICES.res(), value)

    var useBeepOnSingleTap: Boolean
        get() = getValue(Defaults.BEEP_ON_SINGLE_TAP.res(), false)
        set(value) = setValue(Defaults.BEEP_ON_SINGLE_TAP.res(), value)

    var useBeepOnVolumeUp: Boolean
        get() = getValue(Defaults.BEEP_ON_VOLUME_UP.res(), false)
        set(value) {
            setValue(Defaults.BEEP_ON_VOLUME_UP.res(), value)
            WheelLog.VolumeKeyController.setActive(wd.isConnected && value)
        }

    var beepByWheel: Boolean
        get() = getValue(Defaults.BEEP_BY_WHEEL.res(), false)
        set(value) = setValue(Defaults.BEEP_BY_WHEEL.res(), value)

    var useCustomBeep: Boolean
        get() = getValue(Defaults.CUSTOM_BEEP.res(), false)
        set(value) = setValue(Defaults.CUSTOM_BEEP.res(), value)

    var beepFile: Uri
        get() = Uri.parse(getValue(Defaults.BEEP_FILE.res(), ""))
        set(value) = setValue(Defaults.BEEP_FILE.res(), value.toString())

    var customBeepTimeLimit: Float
        get() = getValue("custom_beep_time_limit", 2.0f)
        set(value) = setValue("custom_beep_time_limit", value)

    var mibandMode: MiBandEnum
        get() = MiBandEnum.fromInt(getValue(Defaults.MIBAND_MODE.res(), MiBandEnum.Min.value))
        set(value) = setValue(Defaults.MIBAND_MODE.res(), value.value)

    var useReconnect: Boolean
        get() = getValue(Defaults.USE_RECONNECT.res(), false)
        set(value) {
            setValue(Defaults.USE_RECONNECT.res(), value)
            if (value)
                wd.bluetoothService?.startReconnectTimer()
            else
                wd.bluetoothService?.stopReconnectTimer()
        }

    var detectBatteryOptimization: Boolean
        get() = getValue(Defaults.USE_DETECT_BATTERY_OPTIMIZATION.res(), true)
        set(value) = setValue(Defaults.USE_DETECT_BATTERY_OPTIMIZATION.res(), value)

    var privatePolicyAccepted: Boolean
        get() = getValue(Defaults.PRIVATE_POLICY_ACCEPTED.res(), false)
        set(value) = setValue(Defaults.PRIVATE_POLICY_ACCEPTED.res(), value)

    var yandexMetricaAccepted: Boolean
        get() = getValue(Defaults.YANDEX_METRIСA_ACCEPTED.res(), false)
        set(value) {
            setValue(Defaults.YANDEX_METRIСA_ACCEPTED.res(), value)
//            YandexMetrica.setStatisticsSending(
//                context,
//                WheelLog.AppConfig.yandexMetricaAccepted
//            )
        }
    //endregion

    //region logs
    var autoLog: Boolean
        get() = getValue(Defaults.AUTO_LOG.res(), false)
        set(value) {
            setValue(Defaults.AUTO_LOG.res(), value)
        }

    var autoWatch: Boolean
        get() = getValue(Defaults.AUTO_WATCH.res(), false)
        set(value) = setValue(Defaults.AUTO_WATCH.res(), value)

    var autoUploadEc: Boolean
        get() = getValue(Defaults.AUTO_UPLOAD_EC.res(), false)
        set(value) = setValue(Defaults.AUTO_UPLOAD_EC.res(), value)

    var logLocationData: Boolean
        get() = getValue(Defaults.LOG_LOCATION_DATA.res(), false)
        set(value) = setValue(Defaults.LOG_LOCATION_DATA.res(), value)

    var ecUserId: String?
        get() = getValue(Defaults.EC_USER_ID.res(), null)
        set(value) = setValue(Defaults.EC_USER_ID.res(), value)

    var ecToken: String?
        get() = getValue(Defaults.EC_TOKEN.res(), null)
        set(value) = setValue(Defaults.EC_TOKEN.res(), value)

    var ecGarage: String?
        get() = getValue(Defaults.EC_GARAGE.res(), null)
        set(value) = setValue(Defaults.EC_GARAGE.res(), value)

    var enableRawData: Boolean
        get() = getValue(Defaults.USE_RAW_DATA.res(), false)
        set(value) = setValue(Defaults.USE_RAW_DATA.res(), value)

    var startAutoLoggingWhenIsMovingMore: Float
        get() = getValue(Defaults.AUTO_LOG_WHEN_MOVING_MORE.res(), 7f)
        set(value) = setValue(Defaults.AUTO_LOG_WHEN_MOVING_MORE.res(), value)

    var continueThisDayLog: Boolean
        get() = getValue(Defaults.CONTINUE_THIS_DAY_LOG.res(), false)
        set(value) = setValue(Defaults.CONTINUE_THIS_DAY_LOG.res(), value)

    var continueThisDayLogMacException: String
        get() = getValue(Defaults.CONTINUE_THIS_DAY_LOG_EXCEPTION.res(), "")
        set(value) = setValue(Defaults.CONTINUE_THIS_DAY_LOG_EXCEPTION.res(), value)
    //endregion

    //region watch
    var hornMode: Int
        get() = getValue(Defaults.HORN_MODE.res(), 0)
        set(value) = setValue(Defaults.HORN_MODE.res(), value)

    var garminConnectIqEnable: Boolean
        get() = getValue(Defaults.GARMIN_CONNECTIQ_ENABLE.res(), false)
        set(value) = setValue(Defaults.GARMIN_CONNECTIQ_ENABLE.res(), value)

    var useGarminBetaCompanion: Boolean
        get() = getValue(Defaults.GARMIN_CONNECTIQ_USE_BETA.res(), false)
        set(value) = setValue(Defaults.GARMIN_CONNECTIQ_USE_BETA.res(), value)

    var mainMenuButtons: Array<String>
        get() = getValue<String?>("main_menu_buttons", null)?.split(separator)?.toTypedArray()
            ?: arrayOf("watch")
        set(value) = setValue("main_menu_buttons", value.joinToString(separator))

    var showClock: Boolean
        get() = getValue("show_clock", true)
        set(value) = setValue("show_clock", value)

    var mibandFixRs: Boolean
        get() = getValue(Defaults.MIBAND_FIXRS_ENABLE.res(), false)
        set(value) {
            setValue(Defaults.MIBAND_FIXRS_ENABLE.res(), value)
            WheelLog.Notifications.updateKostilTimer()
        }

    var wearOsPages: WearPages
        get() = WearPage.deserialize(
            getValue(
                Constants.wearPages,
                WearPage.serialize(WearPage.Main and WearPage.Voltage)
            )
        )
        set(value) = setValue(Constants.wearPages, WearPage.serialize(value))
    //endregion

    var lastMac: String
        get() {
            specificPrefix = getValue(Defaults.LAST_MAC.res(), "")
            return specificPrefix
        }
        set(value) {
            specificPrefix = value
            setValue(Defaults.LAST_MAC.res(), value)
        }

    var useGps: Boolean
        get() = getValue(Defaults.USE_GPS.res(), false)
        set(value) = setValue(Defaults.USE_GPS.res(), value)

    var useShortPwm
        get() = getValue(Defaults.USE_SHORT_PWM.res(), false)
        set(value) = setValue(Defaults.USE_SHORT_PWM.res(), value)
    //endregion

    //region -=[ specific settings ]=-
    //region alarms
    var alarmsEnabled: Boolean
        get() = getValue(Defaults.ALARMS_ENABLED.res(), false)
        set(value) = setValue(Defaults.ALARMS_ENABLED.res(), value)

    var disablePhoneVibrate: Boolean
        get() = getValue(Defaults.DISABLE_PHONE_VIBRATE.res(), false)
        set(value) = setValue(Defaults.DISABLE_PHONE_VIBRATE.res(), value)

    var disablePhoneBeep: Boolean
        get() = getValue(Defaults.DISABLE_PHONE_BEEP.res(), false)
        set(value) = setValue(Defaults.DISABLE_PHONE_BEEP.res(), value)

    var useWheelBeepForAlarm: Boolean
        get() = getValue(Defaults.USE_WHEEL_BEEP_FOR_ALARM.res(), false)
        set(value) = setValue(Defaults.USE_WHEEL_BEEP_FOR_ALARM.res(), value)

    var pwmBasedAlarms: Boolean
        get() = getValue(Defaults.ALTERED_ALARMS.res(), true)
        set(value) = setValue(Defaults.ALTERED_ALARMS.res(), value)

    var alarm1Speed: Int
        get() = getValue(Defaults.ALARM_1_SPEED.res(), 29)
        set(value) = setValue(Defaults.ALARM_1_SPEED.res(), value)

    var alarm1Battery: Int
        get() = getValue(Defaults.ALARM_1_BATTERY.res(), 100)
        set(value) = setValue(Defaults.ALARM_1_BATTERY.res(), value)

    var alarm2Speed: Int
        get() = getValue(Defaults.ALARM_2_SPEED.res(), 0)
        set(value) = setValue(Defaults.ALARM_2_SPEED.res(), value)

    var alarm2Battery: Int
        get() = getValue(Defaults.ALARM_2_BATTERY.res(), 0)
        set(value) = setValue(Defaults.ALARM_2_BATTERY.res(), value)

    var alarm3Speed: Int
        get() = getValue(Defaults.ALARM_3_SPEED.res(), 0)
        set(value) = setValue(Defaults.ALARM_3_SPEED.res(), value)

    var alarm3Battery: Int
        get() = getValue(Defaults.ALARM_3_BATTERY.res(), 0)
        set(value) = setValue(Defaults.ALARM_3_BATTERY.res(), value)

    var alarmTemperature
        get() = getValue(Defaults.ALARM_TEMPERATURE.res(), 0)
        set(value) = setValue(Defaults.ALARM_TEMPERATURE.res(), value)

    var rotationSpeed
        get() = getValue(Defaults.ROTATION_SPEED.res(), 500)
        set(value) = setValue(Defaults.ROTATION_SPEED.res(), value)

    var rotationVoltage
        get() = getValue(Defaults.ROTATION_VOLTAGE.res(), 840)
        set(value) = setValue(Defaults.ROTATION_VOLTAGE.res(), value)

    var rotationIsSet
        get() = getValue(Defaults.ROTATION_SET.res(), false)
        set(value) = setValue(Defaults.ROTATION_SET.res(), value)

    var powerFactor
        get() = getValue(Defaults.POWER_FACTOR.res(), 90)
        set(value) = setValue(Defaults.POWER_FACTOR.res(), value)

    var alarmFactor1
        get() = getValue(Defaults.ALARM_FACTOR1.res(), 80)
        set(value) = setValue(Defaults.ALARM_FACTOR1.res(), value)

    var alarmFactor2
        get() = getValue(Defaults.ALARM_FACTOR2.res(), 90)
        set(value) = setValue(Defaults.ALARM_FACTOR2.res(), value)

    var alarmFactor3
        get() = getValue(Defaults.ALARM_FACTOR3.res(), 95)
        set(value) = setValue(Defaults.ALARM_FACTOR3.res(), value)

    var warningSpeed
        get() = getValue(Defaults.WARNING_SPEED.res(), 0)
        set(value) = setValue(Defaults.WARNING_SPEED.res(), value)

    var warningPwm
        get() = getValue(Defaults.WARNING_PWM.res(), 0)
        set(value) = setValue(Defaults.WARNING_PWM.res(), value)

    var warningSpeedPeriod
        get() = getValue(Defaults.WARNING_SPEED_PERIOD.res(), 0)
        set(value) = setValue(Defaults.WARNING_SPEED_PERIOD.res(), value)

    var alarmCurrent
        get() = getValue(Defaults.ALARM_CURRENT.res(), 0)
        set(value) = setValue(Defaults.ALARM_CURRENT.res(), value)

    var alarmBattery
        get() = getValue(Defaults.ALARM_BATTERY.res(), 0)
        set(value) = setValue(Defaults.ALARM_BATTERY.res(), value)
    //endregion

    //region inmotion
    var ledEnabled: Boolean
        get() = getValue(Defaults.LED_ENABLED.res(), false)
        set(value) = setValue(Defaults.LED_ENABLED.res(), value)

    var drlEnabled: Boolean
        get() = getValue(Defaults.DRL_ENABLED.res(), false)
        set(value) = setValue(Defaults.DRL_ENABLED.res(), value)

    var taillightEnabled: Boolean
        get() = getValue(Defaults.TAILLIGHT_ENABLED.res(), false)
        set(value) = setValue(Defaults.TAILLIGHT_ENABLED.res(), value)

    var handleButtonDisabled: Boolean
        get() = getValue(Defaults.HANDLE_BUTTON_DISABLED.res(), false)
        set(value) = setValue(Defaults.HANDLE_BUTTON_DISABLED.res(), value)

    var speakerVolume: Int
        get() = getValue(Defaults.SPEAKER_VOLUME.res(), 0)
        set(value) = setValue(Defaults.SPEAKER_VOLUME.res(), value)

    var beeperVolume: Int
        get() = getValue(Defaults.BEEPER_VOLUME.res(), 0)
        set(value) = setValue(Defaults.BEEPER_VOLUME.res(), value)

    var pedalsAdjustment: Int
        get() = getValue(Defaults.PEDALS_ADJUSTMENT.res(), 0)
        set(value) = setValue(Defaults.PEDALS_ADJUSTMENT.res(), value)

    var pedalSensivity: Int
        get() = getValue(Defaults.PEDAL_SENSIVITY.res(), 100)
        set(value) = setValue(Defaults.PEDAL_SENSIVITY.res(), value)

    var rideMode: Boolean
        get() = getValue(Defaults.RIDE_MODE.res(), false)
        set(value) = setValue(Defaults.RIDE_MODE.res(), value)

    var lockMode: Boolean
        get() = getValue(Defaults.LOCK_MODE.res(), false)
        set(value) = setValue(Defaults.LOCK_MODE.res(), value)

    var transportMode: Boolean
        get() = getValue(Defaults.TRANSPORT_MODE.res(), false)
        set(value) = setValue(Defaults.TRANSPORT_MODE.res(), value)

    var goHomeMode: Boolean
        get() = getValue(Defaults.GO_HOME_MODE.res(), false)
        set(value) = setValue(Defaults.GO_HOME_MODE.res(), value)

    var fancierMode: Boolean
        get() = getValue(Defaults.FANCIER_MODE.res(), false)
        set(value) = setValue(Defaults.FANCIER_MODE.res(), value)

    var speakerMute: Boolean
        get() = getValue(Defaults.SPEAKER_MUTE.res(), false)
        set(value) = setValue(Defaults.SPEAKER_MUTE.res(), value)

    var fanQuietEnabled: Boolean
        get() = getValue(Defaults.FAN_QUIET_ENABLE.res(), false)
        set(value) = setValue(Defaults.FAN_QUIET_ENABLE.res(), value)

    var fanEnabled: Boolean
        get() = getValue(Defaults.FAN_ENABLED.res(), false)
        set(value) = setValue(Defaults.FAN_ENABLED.res(), value)

    var lightBrightness: Int
        get() = getValue(Defaults.LIGHT_BRIGHTNESS.res(), 0)
        set(value) = setValue(Defaults.LIGHT_BRIGHTNESS.res(), value)

    //endregion

    //region ninebotZ
    var wheelAlarm1Enabled: Boolean
        get() = getValue(Defaults.WHEEL_ALARM_1_ENABLED.res(), false)
        set(value) = setValue(Defaults.WHEEL_ALARM_1_ENABLED.res(), value)

    var wheelAlarm2Enabled: Boolean
        get() = getValue(Defaults.WHEEL_ALARM_2_ENABLED.res(), false)
        set(value) = setValue(Defaults.WHEEL_ALARM_2_ENABLED.res(), value)

    var wheelAlarm3Enabled: Boolean
        get() = getValue(Defaults.WHEEL_ALARM_3_ENABLED.res(), false)
        set(value) = setValue(Defaults.WHEEL_ALARM_3_ENABLED.res(), value)

    var wheelAlarm1Speed: Int
        get() = getValue(Defaults.WHEEL_ALARM_1.res(), 0)
        set(value) = setValue(Defaults.WHEEL_ALARM_1.res(), value)

    var wheelAlarm2Speed: Int
        get() = getValue(Defaults.WHEEL_ALARM_2.res(), 0)
        set(value) = setValue(Defaults.WHEEL_ALARM_2.res(), value)

    var wheelAlarm3Speed: Int
        get() = getValue(Defaults.WHEEL_ALARM_3.res(), 0)
        set(value) = setValue(Defaults.WHEEL_ALARM_3.res(), value)

    var wheelLimitedModeEnabled: Boolean
        get() = getValue(Defaults.WHEEL_LIMITED_MODE_ENABLED.res(), false)
        set(value) = setValue(Defaults.WHEEL_LIMITED_MODE_ENABLED.res(), value)

    var wheelLimitedModeSpeed: Int
        get() = getValue(Defaults.WHEEL_LIMITED_SPEED.res(), 10)
        set(value) = setValue(Defaults.WHEEL_LIMITED_SPEED.res(), value)

    var ledColor1: Int
        get() = getValue(Defaults.NB_LED_COLOR1.res(), 0)
        set(value) = setValue(Defaults.NB_LED_COLOR1.res(), value)

    var ledColor2: Int
        get() = getValue(Defaults.NB_LED_COLOR2.res(), 0)
        set(value) = setValue(Defaults.NB_LED_COLOR2.res(), value)

    var ledColor3: Int
        get() = getValue(Defaults.NB_LED_COLOR3.res(), 0)
        set(value) = setValue(Defaults.NB_LED_COLOR3.res(), value)

    var ledColor4: Int
        get() = getValue(Defaults.NB_LED_COLOR4.res(), 0)
        set(value) = setValue(Defaults.NB_LED_COLOR4.res(), value)

    var brakeAssistantEnabled: Boolean
        get() = getValue(Defaults.BRAKE_ASSISTANT_ENABLED.res(), false)
        set(value) = setValue(Defaults.BRAKE_ASSISTANT_ENABLED.res(), value)

    //end region

    //region kingsong
    var lightMode: String // ListPreference only works with string parameters and writes them as string
        get() = getValue(Defaults.LIGHT_MODE.res(), "0")
        set(value) = setValue(Defaults.LIGHT_MODE.res(), value)

    var strobeMode: String // ListPreference only works with string parameters and writes them as string
        get() = getValue(Defaults.STROBE_MODE.res(), "0")
        set(value) = setValue(Defaults.STROBE_MODE.res(), value)

    var ledMode: String // ListPreference only works with string parameters and writes them as string
        get() = getValue(Defaults.LED_MODE.res(), "0")
        set(value) = setValue(Defaults.LED_MODE.res(), value)

    var pedalsMode: String // ListPreference only works with string parameters and writes them as string
        get() = getValue(Defaults.PEDALS_MODE.res(), "0")
        set(value) = setValue(Defaults.PEDALS_MODE.res(), value)

    var rollAngle: String // ListPreference only works with string parameters and writes them as string
        get() = getValue(Defaults.ROLL_ANGLE.res(), "0")
        set(value) = setValue(Defaults.ROLL_ANGLE.res(), value)

    var wheelMaxSpeed: Int
        get() = getValue(Defaults.WHEEL_MAX_SPEED.res(), 0)
        set(value) = setValue(Defaults.WHEEL_MAX_SPEED.res(), value)

    var wheelKsAlarm1: Int
        get() = getValue(Defaults.WHEEL_KS_ALARM1.res(), 0)
        set(value) = setValue(Defaults.WHEEL_KS_ALARM1.res(), value)

    var wheelKsAlarm2: Int
        get() = getValue(Defaults.WHEEL_KS_ALARM2.res(), 0)
        set(value) = setValue(Defaults.WHEEL_KS_ALARM2.res(), value)

    var wheelKsAlarm3: Int
        get() = getValue(Defaults.WHEEL_KS_ALARM3.res(), 0)
        set(value) = setValue(Defaults.WHEEL_KS_ALARM3.res(), value)

    var ks18LScaler: Boolean
        get() = getValue(Defaults.KS18L_SCALER.res(), false)
        set(value) = setValue(Defaults.KS18L_SCALER.res(), value)
    //endregion

    //region begode
    var alarmMode: String // ListPreference only works with string parameters
        get() = getValue(Defaults.ALARM_MODE.res(), "0")
        set(value) = setValue(Defaults.ALARM_MODE.res(), value)

    var useRatio: Boolean
        get() = getValue(Defaults.USE_RATIO.res(), false)
        set(value) = setValue(Defaults.USE_RATIO.res(), value)

    var gwInMiles: Boolean
        get() = getValue(Defaults.GW_IN_MILES.res(), false)
        set(value) = setValue(Defaults.GW_IN_MILES.res(), value)

    var gotwayVoltage: String // ListPreference only works with string parameters
        get() = getValue(Defaults.GOTWAY_VOLTAGE.res(), "1")
        set(value) = setValue(Defaults.GOTWAY_VOLTAGE.res(), value)

    var gotwayNegative: String // ListPreference only works with string parameter
        get() = getValue(Defaults.GOTWAY_NEGATIVE.res(), "0")
        set(value) = setValue(Defaults.GOTWAY_NEGATIVE.res(), value)

    var hwPwm: Boolean
        get() = getValue(Defaults.HW_PWM.res(), false)
        set(value) = setValue(Defaults.HW_PWM.res(), value)

    var connectBeep: Boolean
        get() = getValue(Defaults.CONNECT_BEEP.res(), true)
        set(value) = setValue(Defaults.CONNECT_BEEP.res(), value)
    //endregion

    var lightEnabled: Boolean
        get() = getValue(Defaults.LIGHT_ENABLED.res(), false)
        set(value) = setValue(Defaults.LIGHT_ENABLED.res(), value)

    var profileName: String
        get() = getValue(Defaults.PROFILE_NAME.res(), "")
        set(value) = setValue(Defaults.PROFILE_NAME.res(), value)

    var batteryCapacity: Int
        get() = getValue(Defaults.BATTERY_CAPACITY.res(), 0)
        set(value) = setValue(Defaults.BATTERY_CAPACITY.res(), value)

    var chargingPower: Int
        get() = getValue(Defaults.CHARGING_POWER.res(), 0)
        set(value) = setValue(Defaults.CHARGING_POWER.res(), value)
    //endregion

    //region -=[ custom settings ]=-
    var passwordForWheel: String
        get() = getValue("wheel_password_$specificPrefix", "")
        set(value) {
            var password = value
            while (password.length < 6) {
                password = "0$password"
            }
            setValue("wheel_password_$specificPrefix", password)
        }

    var advDataForWheel: String
        get() = getValue("wheel_adv_data_$specificPrefix", "")
        set(value) = setValue("wheel_adv_data_$specificPrefix", value)

    var userDistance: Long
        get() = getValue("user_distance_$specificPrefix", 0L)
        set(value) = setValue("user_distance_$specificPrefix", value)
    //endregion

    var lastLocationLaltitude: Double
        get() = getValue("lastLocationLaltitude", 0.0)
        set(value) = setValue("lastLocationLaltitude", value)

    var lastLocationLongitude: Double
        get() = getValue("lastLocationLongitude", 0.0)
        set(value) = setValue("lastLocationLongitude", value)

    fun getResId(resName: String?): Int {
        return if (resName == null || resName === "") {
            -1
        } else try {
            context.resources.getIdentifier(resName, "string", context.packageName)
        } catch (e: Exception) {
            e.printStackTrace()
            -1
        }
    }

    private fun setSpecific(resId: Int, value: Any?) {
        setValue(specificPrefix + "_" + context.getString(resId), value)
    }

    fun setValue(resId: Int, value: Any?) {
        setValue(context.getString(resId), value)
    }

    fun setValue(key: String, value: Any?) {
        when (value) {
            is String? -> sharedPreferences.edit().putString(key, value).apply()
            is String -> sharedPreferences.edit().putString(key, value).apply()
            is Int -> sharedPreferences.edit().putInt(key, value).apply()
            is Float -> sharedPreferences.edit().putFloat(key, value).apply()
            is Double -> sharedPreferences.edit().putFloat(key, value.toFloat()).apply()
            is Boolean -> sharedPreferences.edit().putBoolean(key, value).apply()
            is Long -> sharedPreferences.edit().putLong(key, value).apply()
        }
    }

    private fun <T : Any?> getSpecific(resId: Int, defaultValue: T): T {
        return getValue(specificPrefix + "_" + context.getString(resId), defaultValue)
    }

    fun <T : Any?> getValue(resId: Int, defaultValue: T): T {
        return getValue(context.getString(resId), defaultValue)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any?> getValue(key: String, defaultValue: T): T {
        return try {
            when (defaultValue) {
                is String? -> sharedPreferences.getString(key, defaultValue) as T
                is String -> sharedPreferences.getString(key, defaultValue) as T
                is Int -> sharedPreferences.getInt(key, defaultValue) as T
                is Float -> sharedPreferences.getFloat(key, defaultValue) as T
                is Double -> sharedPreferences.getFloat(key, defaultValue.toFloat()).toDouble() as T
                is Boolean -> sharedPreferences.getBoolean(key, defaultValue) as T
                is Long -> sharedPreferences.getLong(key, defaultValue) as T
                else -> defaultValue
            }
        } catch (ex: ClassCastException) {
            defaultValue
        }
    }
}