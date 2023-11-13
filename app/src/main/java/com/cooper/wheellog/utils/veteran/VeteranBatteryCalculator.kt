package com.cooper.wheellog.utils.veteran

import kotlin.math.roundToInt

/**
 * Computes the battery percentage for Veteran wheels.
 */
class VeteranBatteryCalculator {
    fun calculateBattery(voltage: Int, version: Int, useAccuratePercentages: Boolean): Int {
        return if (version < 4) {
            if (useAccuratePercentages) {
                calculateNonPattonAccurate(
                    voltage,
                )
            } else {
                calculateNonPattonStandard(voltage)
            }
        } else {
            if (useAccuratePercentages) {
                calculatePattonAccurate(
                    voltage,
                )
            } else {
                calculatePattonStandard(voltage)
            }
        }
    }

    companion object {
        // TODO add support for 151V wheels
        private const val VOLTAGE_THRESHOLD_PATTON = 12525
        private const val VOLTAGE_THRESHOLD_BETTER_PERCENTS = 10200
        private const val VOLTAGE_LOW_THRESHOLD = 9600
        private const val VOLTAGE_TO_PERCENT_HIGH = 25.5
        private const val VOLTAGE_TO_PERCENT_LOW = 67.5
        private fun calculateNonPattonAccurate(voltage: Int): Int {
            if (voltage > 10020) return 100
            if (voltage > 8160) return roundPercentage((voltage - 8070).toDouble(), 19.5)
            return if (voltage > 7935) {
                roundPercentage(
                    (voltage - 7935).toDouble(),
                    48.75,
                )
            } else {
                0
            }
        }

        private fun calculateNonPattonStandard(voltage: Int): Int {
            if (voltage <= 7935) return 0
            return if (voltage >= 9870) {
                100
            } else {
                roundPercentage(
                    (voltage - 7935).toDouble(),
                    19.5,
                )
            }
        }

        private fun calculatePattonAccurate(voltage: Int): Int {
            if (voltage > VOLTAGE_THRESHOLD_PATTON) return 100
            if (voltage > VOLTAGE_THRESHOLD_BETTER_PERCENTS) {
                return roundPercentage(
                    (voltage - 9975).toDouble(),
                    VOLTAGE_TO_PERCENT_HIGH,
                )
            }
            return if (voltage > VOLTAGE_LOW_THRESHOLD) {
                roundPercentage(
                    (voltage - 9600).toDouble(),
                    VOLTAGE_TO_PERCENT_LOW,
                )
            } else {
                0
            }
        }

        private fun calculatePattonStandard(voltage: Int): Int {
            if (voltage <= 9918) return 0
            return if (voltage >= 12337) {
                100
            } else {
                roundPercentage(
                    (voltage - 9918).toDouble(),
                    24.2,
                )
            }
        }

        private fun roundPercentage(value: Double, factor: Double): Int {
            return (value / factor).roundToInt()
        }
    }
}
