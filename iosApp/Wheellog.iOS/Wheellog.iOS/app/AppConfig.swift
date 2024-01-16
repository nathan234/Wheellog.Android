//
//  AppConfig.swift
//  Wheellog.iOS
//
//  Created by nathan retta on 1/14/24.
//

import Foundation
class AppConfig {

    private let userDefaults = UserDefaults.standard
    private let separator = ";"

    // Application Settings
    var useEng: Bool {
        get { userDefaults.bool(forKey: "useEng") }
        set { userDefaults.set(newValue, forKey: "useEng") }
    }

    var appThemeInt: Int {
        get { userDefaults.integer(forKey: "appThemeInt") }
        set { userDefaults.set(newValue, forKey: "appThemeInt") }
    }

    var dayNightThemeMode: Int {
        get { userDefaults.integer(forKey: "dayNightThemeMode") }
        set { userDefaults.set(newValue, forKey: "dayNightThemeMode") }
    }

    var useBetterPercents: Bool {
        get { userDefaults.bool(forKey: "useBetterPercents") }
        set { userDefaults.set(newValue, forKey: "useBetterPercents") }
    }

    var customPercents: Bool {
        get { userDefaults.bool(forKey: "customPercents") }
        set { userDefaults.set(newValue, forKey: "customPercents") }
    }

    var cellVoltageTiltback: Int {
        get { userDefaults.integer(forKey: "cellVoltageTiltback") }
        set { userDefaults.set(newValue, forKey: "cellVoltageTiltback") }
    }

    var useMph: Bool {
        get { userDefaults.bool(forKey: "useMph") }
        set { userDefaults.set(newValue, forKey: "useMph") }
    }

    var useFahrenheit: Bool {
        get { userDefaults.bool(forKey: "useFahrenheit") }
        set { userDefaults.set(newValue, forKey: "useFahrenheit") }
    }
    // todo string translations
    

    // Custom Methods
    private func setArray(_ value: [String], forKey key: String) {
        let joined = value.joined(separator: separator)
        userDefaults.set(joined, forKey: key)
    }

    private func getArray(forKey key: String) -> [String] {
        let value = userDefaults.string(forKey: key) ?? ""
        return value.components(separatedBy: separator)
    }

    // Specific settings (example)
    var alarmsEnabled: Bool {
        get { userDefaults.bool(forKey: "alarmsEnabled") }
        set { userDefaults.set(newValue, forKey: "alarmsEnabled") }
    }

    // ... and so on for each property

    // Utility methods for specific and general settings
    // Implement as needed, similar to the Kotlin version
}
