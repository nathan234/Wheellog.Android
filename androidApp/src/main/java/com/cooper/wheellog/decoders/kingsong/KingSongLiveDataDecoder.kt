package com.cooper.wheellog.decoders.kingsong

import com.cooper.wheellog.wheeldata.WheelData
import com.cooper.wheellog.utils.MathsUtil
import java.util.Locale
import kotlin.math.roundToLong

class KingSongLiveDataDecoder(
    private val wd: WheelData,
    private val kingSongBatteryCalculator: KingSongBatteryCalculator,
) {
    fun decode(data: ByteArray, m18Lkm: Boolean, mode: Int): Int {
        var mMode = mode
        // Live data
        val voltage = MathsUtil.getInt2R(data, 2)
        wd.voltage = voltage
        wd.speed = MathsUtil.getInt2R(data, 4)
        wd.totalDistance = MathsUtil.getInt4R(data, 6).toLong()
        if (wd.model.compareTo("KS-18L") == 0 && !m18Lkm) {
            wd.totalDistance = (wd.totalDistance * KingsongAdapter.KS18L_SCALER).roundToLong()
        }
        wd.current = (data[10].toInt() and 0xFF) + (data[11].toInt() shl 8)
        wd.temperature = MathsUtil.getInt2R(data, 12)
        wd.voltageSag = voltage
        if (data[15].toInt() and 255 == 224) {
            mMode = data[14].toInt()
            wd.modeStr = String.format(Locale.US, "%d", mMode)
        }
        kingSongBatteryCalculator.calculateAndStoreBatteryLevel(voltage)
        return mMode
    }
}
