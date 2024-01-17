//
//  Constants.swift
//  Wheellog.iOS
//
//  Created by nathan retta on 1/14/24.
//

import Foundation

struct Constants {
    static let ACTION_BLUETOOTH_CONNECTION_STATE = "com.cooper.wheellog.bluetoothConnectionState"
    static let ACTION_WHEEL_TYPE_CHANGED = "com.cooper.wheellog.wheelTypeChanged"
    static let ACTION_WHEEL_DATA_AVAILABLE = "com.cooper.wheellog.wheelDataAvailable"
    static let ACTION_WHEEL_NEWS_AVAILABLE = "com.cooper.wheellog.wheelNews"
    static let ACTION_PEBBLE_SERVICE_TOGGLED = "com.cooper.wheellog.pebbleServiceToggled"
    static let ACTION_LOGGING_SERVICE_TOGGLED = "com.cooper.wheellog.loggingServiceToggled"
    static let ACTION_PREFERENCE_RESET = "com.cooper.wheellog.preferenceReset"
    static let ACTION_PEBBLE_AFFECTING_PREFERENCE_CHANGED = "com.cooper.wheellog.pebblePreferenceChanged"
    static let ACTION_ALARM_TRIGGERED = "com.cooper.wheellog.alarmTriggered"
    static let ACTION_PEBBLE_APP_READY = "com.cooper.wheellog.pebbleAppReady"
    static let ACTION_PEBBLE_APP_SCREEN = "com.cooper.wheellog.pebbleAppScreen"
    static let ACTION_WHEEL_TYPE_RECOGNIZED = "com.cooper.wheellog.wheelTypeRecognized"
    static let ACTION_WHEEL_MODEL_CHANGED = "com.cooper.wheellog.wheelModelChanged"
    
    /**
     * The wheel has been successfully connected and all the necessary data for operation has already been received
     */
    static let ACTION_WHEEL_IS_READY = "com.cooper.wheellog.wheelIsReady"
    static let NOTIFICATION_BUTTON_CONNECTION = "com.cooper.wheellog.notificationConnectionButton"
    static let NOTIFICATION_BUTTON_LOGGING = "com.cooper.wheellog.notificationLoggingButton"
    static let NOTIFICATION_BUTTON_WATCH = "com.cooper.wheellog.notificationWatchButton"
    static let NOTIFICATION_BUTTON_BEEP = "com.cooper.wheellog.notificationBeepButton"
    static let NOTIFICATION_BUTTON_LIGHT = "com.cooper.wheellog.notificationLightButton"
    static let NOTIFICATION_BUTTON_MIBAND = "com.cooper.wheellog.notificationMiBandButton"
    static let NOTIFICATION_CHANNEL_ID_NOTIFICATION = "com.cooper.wheellog.Channel_Notification"
    static let notificationChannelName = "Notify"
    static let notificationChannelDescription = "Default Notify"
    
    static let KINGSONG_DESCRIPTER_UUID = UUID(uuidString:"00002902-0000-1000-8000-00805f9b34fb")
    
    static let KINGSONG_READ_CHARACTER_UUID = UUID(uuidString:"0000ffe1-0000-1000-8000-00805f9b34fb")
    
    static let KINGSONG_SERVICE_UUID = UUID(uuidString:"0000ffe0-0000-1000-8000-00805f9b34fb")
    
    static let GOTWAY_READ_CHARACTER_UUID = UUID(uuidString:"0000ffe1-0000-1000-8000-00805f9b34fb")
    
    static let GOTWAY_SERVICE_UUID = UUID(uuidString:"0000ffe0-0000-1000-8000-00805f9b34fb")
    
    static let INMOTION_DESCRIPTER_UUID = UUID(uuidString:"00002902-0000-1000-8000-00805f9b34fb")
    
    static let INMOTION_READ_CHARACTER_UUID = UUID(uuidString:"0000ffe4-0000-1000-8000-00805f9b34fb")
    
    static let INMOTION_SERVICE_UUID = UUID(uuidString:"0000ffe0-0000-1000-8000-00805f9b34fb")
    static let INMOTION_WRITE_CHARACTER_UUID = UUID(uuidString:"0000ffe9-0000-1000-8000-00805f9b34fb")
    static let INMOTION_WRITE_SERVICE_UUID = UUID(uuidString:"0000ffe5-0000-1000-8000-00805f9b34fb")
    
    static let NINEBOT_Z_SERVICE_UUID = UUID(uuidString:"6e400001-b5a3-f393-e0a9-e50e24dcca9e")
    static let NINEBOT_Z_WRITE_CHARACTER_UUID = UUID(uuidString:"6e400002-b5a3-f393-e0a9-e50e24dcca9e")
    
    static let NINEBOT_Z_READ_CHARACTER_UUID = UUID(uuidString:"6e400003-b5a3-f393-e0a9-e50e24dcca9e")
    
    static let NINEBOT_Z_DESCRIPTER_UUID = UUID(uuidString:"00002902-0000-1000-8000-00805f9b34fb")
    
    static let INMOTION_V2_SERVICE_UUID = UUID(uuidString:"6e400001-b5a3-f393-e0a9-e50e24dcca9e")
    static let INMOTION_V2_WRITE_CHARACTER_UUID = UUID(uuidString:"6e400002-b5a3-f393-e0a9-e50e24dcca9e")
    
    static let INMOTION_V2_READ_CHARACTER_UUID = UUID(uuidString:"6e400003-b5a3-f393-e0a9-e50e24dcca9e")
    
    static let INMOTION_V2_DESCRIPTER_UUID = UUID(uuidString:"00002902-0000-1000-8000-00805f9b34fb")
    
    static let NINEBOT_SERVICE_UUID = UUID(uuidString:"0000ffe0-0000-1000-8000-00805f9b34fb")
    static let NINEBOT_WRITE_CHARACTER_UUID = UUID(uuidString:"0000ffe1-0000-1000-8000-00805f9b34fb")
    
    static let NINEBOT_READ_CHARACTER_UUID = UUID(uuidString:"0000ffe1-0000-1000-8000-00805f9b34fb")
    
    static let NINEBOT_DESCRIPTER_UUID = UUID(uuidString:"00002902-0000-1000-8000-00805f9b34fb")
    
    static let PEBBLE_APP_UUID = UUID(uuidString:"185c8ae9-7e72-451a-a1c7-8f1e81df9a3d")
    static let PEBBLE_KEY_READY = 11
    static let PEBBLE_KEY_LAUNCH_APP = 10012
    static let PEBBLE_KEY_PLAY_HORN = 10013
    static let PEBBLE_KEY_DISPLAYED_SCREEN = 10014
    static let PEBBLE_APP_VERSION = 104
    static let INTENT_EXTRA_LAUNCHED_FROM_PEBBLE = "launched_from_pebble"
    static let INTENT_EXTRA_PEBBLE_APP_VERSION = "pebble_app_version"
    static let INTENT_EXTRA_PEBBLE_DISPLAYED_SCREEN = "pebble_displayed_Screen"
    static let INTENT_EXTRA_BLE_AUTO_CONNECT = "ble_auto_connect"
    static let INTENT_EXTRA_LOGGING_FILE_LOCATION = "logging_file_location"
    static let INTENT_EXTRA_IS_RUNNING = "is_running"
    static let INTENT_EXTRA_GRAPH_UPDATE_AVAILABLE = "graph_update_available"
    static let INTENT_EXTRA_CONNECTION_STATE = "connection_state"
    static let INTENT_EXTRA_WHEEL_SEARCH = "wheel_search"
    static let INTENT_EXTRA_DIRECT_SEARCH_FAILED = "direct_search_failed"
    static let INTENT_EXTRA_ALARM_TYPE = "alarm_type"
    static let INTENT_EXTRA_ALARM_VALUE = "alarm_value"
    static let INTENT_EXTRA_NEWS = "wheel_news"
    static let MAX_CELL_VOLTAGE = 4.2
    static let MAIN_NOTIFICATION_ID = 423411
    static let LOG_FOLDER_NAME = "WheelLog Logs"
    
    enum WheelType {
        case unknown
        case kingsong
        case gotway
        case ninebot
        case ninebotZ
        case inmotion
        case inmotionV2
        case veteran
        case gotwayVirtual
    }
    
    enum PebbleAppScreen: Int {
        case gui = 0
        case details = 1
    }
    
    enum AlarmType: Int {
        case speed1 = 1
        case speed2 = 2
        case speed3 = 3
        case current = 4
        case temperature = 5
        case pwm = 6
        case battery = 7
    }
}
