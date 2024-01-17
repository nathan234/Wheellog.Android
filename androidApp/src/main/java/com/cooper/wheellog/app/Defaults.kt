package com.cooper.wheellog.app

import com.cooper.wheellog.R

enum class Defaults {
    SPEAKER_MUTE,
    FAN_ENABLED,
    FAN_QUIET_ENABLE,
    LIGHT_BRIGHTNESS,
    WHEEL_ALARM_3,
    WHEEL_ALARM_3_ENABLED,
    WHEEL_ALARM_2,
    WHEEL_ALARM_2_ENABLED,
    WHEEL_ALARM_1,
    WHEEL_ALARM_1_ENABLED,
    WHEEL_LIMITED_MODE_ENABLED,
    WHEEL_LIMITED_SPEED,
    NB_LED_COLOR1,
    NB_LED_COLOR2,
    NB_LED_COLOR3,
    NB_LED_COLOR4,
    BRAKE_ASSISTANT_ENABLED,
    USE_ENG,
    APP_THEME,
    DAY_NIGHT_THEME,
    USE_BETTER_PERCENTS,
    CUSTOM_PERCENTS,
    CELL_VOLTAGE_TILTBACK,
    USE_MPH,
    USE_FAHRENHEIT,
    VIEW_BLOCKS_STRING,
    PWM,
    MAX_PWM,
    VOLTAGE,
    AVG_RIDING,
    RIDING_TIME,
    TOP_SPEED,
    DISTANCE,
    TOTAL,
    USE_PIP_MODE,
    PIP_BLOCK,
    NOTIFICATION_BUTTONS,
    MAX_SPEED,
    CURRENT_ON_DIAL,
    SHOW_PAGE_GRAPH,
    SHOW_PAGE_EVENTS,
    SHOW_PAGE_TRIPS,
    CONNECTION_SOUND,
    NO_CONNECTION_SOUND,
    USE_STOP_MUSIC,
    SHOW_UNKNOWN_DEVICES,
    BEEP_ON_SINGLE_TAP,
    BEEP_ON_VOLUME_UP,
    BEEP_BY_WHEEL,
    CUSTOM_BEEP,
    BEEP_FILE,
    MIBAND_MODE,
    USE_RECONNECT,
    USE_DETECT_BATTERY_OPTIMIZATION,
    PRIVATE_POLICY_ACCEPTED,
    YANDEX_METRIСA_ACCEPTED,
    AUTO_LOG,
    AUTO_WATCH,
    AUTO_UPLOAD_EC,
    LOG_LOCATION_DATA,
    EC_USER_ID,
    EC_TOKEN,
    EC_GARAGE,
    USE_RAW_DATA,
    AUTO_LOG_WHEN_MOVING_MORE,
    CONTINUE_THIS_DAY_LOG,
    CONTINUE_THIS_DAY_LOG_EXCEPTION,
    HORN_MODE,
    GARMIN_CONNECTIQ_ENABLE,
    GARMIN_CONNECTIQ_USE_BETA,
    MIBAND_FIXRS_ENABLE,
    LAST_MAC,
    USE_GPS,
    USE_SHORT_PWM,
    ALARMS_ENABLED,
    DISABLE_PHONE_VIBRATE,
    DISABLE_PHONE_BEEP,
    USE_WHEEL_BEEP_FOR_ALARM,
    ALTERED_ALARMS,
    ALARM_1_SPEED,
    ALARM_1_BATTERY,
    ALARM_2_SPEED,
    ALARM_2_BATTERY,
    ALARM_3_SPEED,
    ALARM_3_BATTERY,
    ALARM_TEMPERATURE,
    ROTATION_SPEED,
    ROTATION_VOLTAGE,
    ROTATION_SET,
    POWER_FACTOR,
    ALARM_FACTOR1,
    ALARM_FACTOR2,
    ALARM_FACTOR3,
    WARNING_SPEED,
    WARNING_PWM,
    WARNING_SPEED_PERIOD,
    ALARM_CURRENT,
    ALARM_BATTERY,
    LED_ENABLED,
    DRL_ENABLED,
    TAILLIGHT_ENABLED,
    HANDLE_BUTTON_DISABLED,
    SPEAKER_VOLUME,
    BEEPER_VOLUME,
    PEDALS_ADJUSTMENT,
    PEDAL_SENSIVITY,
    LIGHT_MODE,
    STROBE_MODE,
    LED_MODE,
    PEDALS_MODE,
    ROLL_ANGLE,
    RIDE_MODE,
    LOCK_MODE,
    TRANSPORT_MODE,
    GO_HOME_MODE,
    FANCIER_MODE,
    ALARM_MODE,
    WHEEL_MAX_SPEED,
    WHEEL_KS_ALARM1,
    WHEEL_KS_ALARM2,
    WHEEL_KS_ALARM3,
    KS18L_SCALER,
    USE_RATIO,
    GW_IN_MILES,
    GOTWAY_VOLTAGE,
    GOTWAY_NEGATIVE,
    HW_PWM,
    CONNECT_BEEP,
    LIGHT_ENABLED,
    PROFILE_NAME,
    BATTERY_CAPACITY,
    CHARGING_POWER,
    ICON_CONNECTION,
    ICON_LOGGING,
    ICON_WATCH;

    fun res(): Int{
        return when (this) {
            SPEAKER_MUTE -> R.string.speaker_mute
            FAN_ENABLED -> R.string.fan_enabled
            FAN_QUIET_ENABLE -> R.string.fan_quiet_enable
            LIGHT_BRIGHTNESS -> R.string.light_brightness
            WHEEL_ALARM_3 -> R.string.wheel_alarm3
            WHEEL_ALARM_3_ENABLED -> R.string.wheel_alarm3_enabled
            WHEEL_ALARM_2 -> R.string.wheel_alarm2
            WHEEL_ALARM_2_ENABLED -> R.string.wheel_alarm2_enabled
            WHEEL_ALARM_1 -> R.string.wheel_alarm1
            WHEEL_ALARM_1_ENABLED -> R.string.wheel_alarm1_enabled
            WHEEL_LIMITED_MODE_ENABLED -> R.string.wheel_limited_mode_enabled
            WHEEL_LIMITED_SPEED -> R.string.wheel_limited_speed
            NB_LED_COLOR1 -> R.string.nb_led_color1
            NB_LED_COLOR2 -> R.string.nb_led_color2
            NB_LED_COLOR3 -> R.string.nb_led_color3
            NB_LED_COLOR4 -> R.string.nb_led_color4
            BRAKE_ASSISTANT_ENABLED -> R.string.brake_assistant_enabled
            USE_ENG -> R.string.use_eng
            APP_THEME -> R.string.app_theme
            DAY_NIGHT_THEME -> R.string.day_night_theme
            USE_BETTER_PERCENTS -> R.string.use_better_percents
            CUSTOM_PERCENTS -> R.string.custom_percents
            CELL_VOLTAGE_TILTBACK -> R.string.cell_voltage_tiltback
            USE_MPH -> R.string.use_mph
            USE_FAHRENHEIT -> R.string.use_fahrenheit
            VIEW_BLOCKS_STRING -> R.string.view_blocks_string
            PWM -> R.string.pwm
            MAX_PWM -> R.string.max_pwm
            VOLTAGE -> R.string.voltage
            AVG_RIDING -> R.string.average_riding_speed
            RIDING_TIME -> R.string.riding_time
            TOP_SPEED -> R.string.top_speed
            DISTANCE -> R.string.distance
            TOTAL -> R.string.total
            USE_PIP_MODE -> R.string.use_pip_mode
            PIP_BLOCK -> R.string.pip_block
            NOTIFICATION_BUTTONS -> R.string.notification_buttons
            MAX_SPEED -> R.string.max_speed
            CURRENT_ON_DIAL -> R.string.current_on_dial
            SHOW_PAGE_GRAPH -> R.string.show_page_graph
            SHOW_PAGE_EVENTS -> R.string.show_page_events
            SHOW_PAGE_TRIPS -> R.string.show_page_trips
            CONNECTION_SOUND -> R.string.connection_sound
            NO_CONNECTION_SOUND -> R.string.no_connection_sound
            USE_STOP_MUSIC -> R.string.use_stop_music
            SHOW_UNKNOWN_DEVICES -> R.string.show_unknown_devices
            BEEP_ON_SINGLE_TAP -> R.string.beep_on_single_tap
            BEEP_ON_VOLUME_UP -> R.string.beep_on_volume_up
            BEEP_BY_WHEEL -> R.string.beep_by_wheel
            CUSTOM_BEEP -> R.string.custom_beep
            BEEP_FILE -> R.string.beep_file
            MIBAND_MODE -> R.string.miband_mode
            USE_RECONNECT -> R.string.use_reconnect
            USE_DETECT_BATTERY_OPTIMIZATION -> R.string.use_detect_battery_optimization
            PRIVATE_POLICY_ACCEPTED -> R.string.private_policy_accepted
            YANDEX_METRIСA_ACCEPTED -> R.string.yandex_metriсa_accepted
            AUTO_LOG -> R.string.auto_log
            AUTO_WATCH -> R.string.auto_watch
            AUTO_UPLOAD_EC -> R.string.auto_upload_ec
            LOG_LOCATION_DATA -> R.string.log_location_data
            EC_USER_ID -> R.string.ec_user_id
            EC_TOKEN -> R.string.ec_token
            EC_GARAGE -> R.string.ec_garage
            USE_RAW_DATA -> R.string.use_raw_data
            AUTO_LOG_WHEN_MOVING_MORE -> R.string.auto_log_when_moving_more
            CONTINUE_THIS_DAY_LOG -> R.string.continue_this_day_log
            CONTINUE_THIS_DAY_LOG_EXCEPTION -> R.string.continue_this_day_log_exception
            HORN_MODE -> R.string.horn_mode
            GARMIN_CONNECTIQ_ENABLE -> R.string.garmin_connectiq_enable
            GARMIN_CONNECTIQ_USE_BETA -> R.string.garmin_connectiq_use_beta
            MIBAND_FIXRS_ENABLE -> R.string.miband_fixrs_enable
            LAST_MAC -> R.string.last_mac
            USE_GPS -> R.string.use_gps
            USE_SHORT_PWM -> R.string.use_short_pwm
            ALARMS_ENABLED -> R.string.alarms_enabled
            DISABLE_PHONE_VIBRATE -> R.string.disable_phone_vibrate
            DISABLE_PHONE_BEEP -> R.string.disable_phone_beep
            USE_WHEEL_BEEP_FOR_ALARM -> R.string.use_wheel_beep_for_alarm
            ALTERED_ALARMS -> R.string.altered_alarms
            ALARM_1_SPEED -> R.string.alarm_1_speed
            ALARM_1_BATTERY -> R.string.alarm_1_battery
            ALARM_2_SPEED -> R.string.alarm_2_speed
            ALARM_2_BATTERY -> R.string.alarm_2_battery
            ALARM_3_SPEED -> R.string.alarm_3_speed
            ALARM_3_BATTERY -> R.string.alarm_3_battery
            ALARM_TEMPERATURE -> R.string.alarm_temperature
            ROTATION_SPEED -> R.string.rotation_speed
            ROTATION_VOLTAGE -> R.string.rotation_voltage
            ROTATION_SET -> R.string.rotation_set
            POWER_FACTOR -> R.string.power_factor
            ALARM_FACTOR1 -> R.string.alarm_factor1
            ALARM_FACTOR2 -> R.string.alarm_factor2
            ALARM_FACTOR3 -> R.string.alarm_factor3
            WARNING_SPEED -> R.string.warning_speed
            WARNING_PWM -> R.string.warning_pwm
            WARNING_SPEED_PERIOD -> R.string.warning_speed_period
            ALARM_CURRENT -> R.string.alarm_current
            ALARM_BATTERY -> R.string.alarm_battery
            LED_ENABLED -> R.string.led_enabled
            DRL_ENABLED -> R.string.drl_enabled
            TAILLIGHT_ENABLED -> R.string.taillight_enabled
            HANDLE_BUTTON_DISABLED -> R.string.handle_button_disabled
            SPEAKER_VOLUME -> R.string.speaker_volume
            BEEPER_VOLUME -> R.string.beeper_volume
            PEDALS_ADJUSTMENT -> R.string.pedals_adjustment
            PEDAL_SENSIVITY -> R.string.pedal_sensivity
            LIGHT_MODE -> R.string.light_mode
            STROBE_MODE -> R.string.strobe_mode
            LED_MODE -> R.string.led_mode
            PEDALS_MODE -> R.string.pedals_mode
            ROLL_ANGLE -> R.string.roll_angle
            RIDE_MODE -> R.string.ride_mode
            LOCK_MODE -> R.string.lock_mode
            TRANSPORT_MODE -> R.string.transport_mode
            GO_HOME_MODE -> R.string.go_home_mode
            FANCIER_MODE -> R.string.fancier_mode
            ALARM_MODE -> R.string.alarm_mode
            WHEEL_MAX_SPEED -> R.string.wheel_max_speed
            WHEEL_KS_ALARM1 -> R.string.wheel_ks_alarm1
            WHEEL_KS_ALARM2 -> R.string.wheel_ks_alarm2
            WHEEL_KS_ALARM3 -> R.string.wheel_ks_alarm3
            KS18L_SCALER -> R.string.ks18l_scaler
            USE_RATIO -> R.string.use_ratio
            GW_IN_MILES -> R.string.gw_in_miles
            GOTWAY_VOLTAGE -> R.string.gotway_voltage
            GOTWAY_NEGATIVE -> R.string.gotway_negative
            HW_PWM -> R.string.hw_pwm
            CONNECT_BEEP -> R.string.connect_beep
            LIGHT_ENABLED -> R.string.light_enabled
            PROFILE_NAME -> R.string.profile_name
            BATTERY_CAPACITY -> R.string.battery_capacity
            CHARGING_POWER -> R.string.charging_power
            ICON_CONNECTION -> R.string.icon_connection
            ICON_LOGGING -> R.string.icon_logging
            ICON_WATCH -> R.string.icon_watch
        }
    }
}