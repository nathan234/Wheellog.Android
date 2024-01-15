package com.cooper.wheellog.utils.gotway

import com.cooper.wheellog.AppConfig

/**
 * Scale voltage for Gotway wheels. Expecting ints between 0 and 4, corresponding to the
 * voltage setting in the app.
 */
class GotwayScaledVoltageCalculator(private val appConfig: AppConfig) {
    fun getScaledVoltage(value: Double): Double {
        var voltage = 0
        var scaler = 1.0
        if (appConfig.gotwayVoltage != "") {
            voltage = appConfig.gotwayVoltage.toInt()
        }
        when (voltage) {
            0 -> scaler = 1.0
            1 -> scaler = 1.25
            2 -> scaler = 1.5
            3 -> scaler = 1.7380952380952380952380952380952
            4 -> scaler = 2.0
        }
        return value * scaler
    }
}