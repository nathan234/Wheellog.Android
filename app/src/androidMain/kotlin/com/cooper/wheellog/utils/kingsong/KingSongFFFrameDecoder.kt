package com.cooper.wheellog.utils.kingsong

import com.cooper.wheellog.WheelData
import com.cooper.wheellog.utils.MathsUtil
import kotlin.math.roundToInt

class KingSongFFFrameDecoder(
    private val wd: WheelData,
    private val mathsUtil: MathsUtil = MathsUtil
) {
    fun decode(data: ByteArray) {
        val bmsnum = (data[16].toInt() and 255) - 0xF0
        val bms = if (bmsnum == 1) wd.bms1 else wd.bms2
        val pNum = data[17].toInt() and 255
        when (pNum) {
            0x00 -> {
                bms.voltage = mathsUtil.getInt2R(data, 2) / 100.0
                bms.current = mathsUtil.getInt2R(data, 4) / 100.0
                bms.remCap = mathsUtil.getInt2R(data, 6) * 10
                bms.factoryCap = mathsUtil.getInt2R(data, 8) * 10
                bms.fullCycles = mathsUtil.getInt2R(data, 10)
                bms.remPerc = (bms.remCap / (bms.factoryCap / 100.0)).roundToInt()
                if (bms.serialNumber == "") {
                    if (bmsnum == 1) {
                        requestBms1Serial()
                    } else {
                        requestBms2Serial()
                    }
                }
            }
            0x01 -> {
                bms.temp1 = (mathsUtil.getInt2R(data, 2) - 2730) / 10.0
                bms.temp2 = (mathsUtil.getInt2R(data, 4) - 2730) / 10.0
                bms.temp3 = (mathsUtil.getInt2R(data, 6) - 2730) / 10.0
                bms.temp4 = (mathsUtil.getInt2R(data, 8) - 2730) / 10.0
                bms.temp5 = (mathsUtil.getInt2R(data, 10) - 2730) / 10.0
                bms.temp6 = (mathsUtil.getInt2R(data, 12) - 2730) / 10.0
                bms.tempMos = (mathsUtil.getInt2R(data, 14) - 2730) / 10.0
            }
            0x02 -> {
                bms.cells[0] = mathsUtil.getInt2R(data, 2) / 1000.0
                bms.cells[1] = mathsUtil.getInt2R(data, 4) / 1000.0
                bms.cells[2] = mathsUtil.getInt2R(data, 6) / 1000.0
                bms.cells[3] = mathsUtil.getInt2R(data, 8) / 1000.0
                bms.cells[4] = mathsUtil.getInt2R(data, 10) / 1000.0
                bms.cells[5] = mathsUtil.getInt2R(data, 12) / 1000.0
                bms.cells[6] = mathsUtil.getInt2R(data, 14) / 1000.0
            }
            0x03 -> {
                bms.cells[7] = mathsUtil.getInt2R(data, 2) / 1000.0
                bms.cells[8] = mathsUtil.getInt2R(data, 4) / 1000.0
                bms.cells[9] = mathsUtil.getInt2R(data, 6) / 1000.0
                bms.cells[10] = mathsUtil.getInt2R(data, 8) / 1000.0
                bms.cells[11] = mathsUtil.getInt2R(data, 10) / 1000.0
                bms.cells[12] = mathsUtil.getInt2R(data, 12) / 1000.0
                bms.cells[13] = mathsUtil.getInt2R(data, 14) / 1000.0
            }
            0x04 -> {
                bms.cells[14] = mathsUtil.getInt2R(data, 2) / 1000.0
                bms.cells[15] = mathsUtil.getInt2R(data, 4) / 1000.0
                bms.cells[16] = mathsUtil.getInt2R(data, 6) / 1000.0
                bms.cells[17] = mathsUtil.getInt2R(data, 8) / 1000.0
                bms.cells[18] = mathsUtil.getInt2R(data, 10) / 1000.0
                bms.cells[19] = mathsUtil.getInt2R(data, 12) / 1000.0
                bms.cells[20] = mathsUtil.getInt2R(data, 14) / 1000.0
            }
            0x05 -> {
                bms.cells[21] = mathsUtil.getInt2R(data, 2) / 1000.0
                bms.cells[22] = mathsUtil.getInt2R(data, 4) / 1000.0
                bms.cells[23] = mathsUtil.getInt2R(data, 6) / 1000.0
                bms.cells[24] = mathsUtil.getInt2R(data, 8) / 1000.0
                bms.cells[25] = mathsUtil.getInt2R(data, 10) / 1000.0
                bms.cells[26] = mathsUtil.getInt2R(data, 12) / 1000.0
                bms.cells[27] = mathsUtil.getInt2R(data, 14) / 1000.0
            }
            0x06 -> {
                bms.cells[28] = mathsUtil.getInt2R(data, 2) / 1000.0
                bms.cells[29] = mathsUtil.getInt2R(data, 4) / 1000.0
                // bms.getCells()[30] = mathsUtil.getInt2R(data, 6)/1000.0;
                // bms.getCells()[31] = mathsUtil.getInt2R(data, 8)/1000.0;
                bms.tempMosEnv = (mathsUtil.getInt2R(data, 10) - 2730) / 10.0
                // bms.getCells()[5] = mathsUtil.getInt2R(data, 12)/1000.0;
                bms.minCell = bms.cells[29]
                bms.maxCell = bms.cells[29]
                for (i in 0..29) {
                    val cell = bms.cells[i]
                    if (cell > 0.0) {
                        if (bms.maxCell < cell) {
                            bms.maxCell = cell
                        }
                        if (bms.minCell > cell) {
                            bms.minCell = cell
                        }
                    }
                }
                bms.cellDiff = bms.maxCell - bms.minCell
                if (bms.versionNumber == "") {
                    if (bmsnum == 1) {
                        requestBms1Firmware()
                    } else {
                        requestBms2Firmware()
                    }
                }
            }
        }
    }

    private fun requestBms1Serial() {
        val data = KingsongUtils.getEmptyRequest()
        data[16] = 0xe1.toByte()
        data[17] = 0x00.toByte()
        data[18] = 0x00.toByte()
        data[19] = 0x00.toByte()
        wd.bluetoothCmd(data)
    }

    private fun requestBms2Serial() {
        val data = KingsongUtils.getEmptyRequest()
        data[16] = 0xe2.toByte()
        data[17] = 0x00.toByte()
        data[18] = 0x00.toByte()
        data[19] = 0x00.toByte()
        wd.bluetoothCmd(data)
    }

    private fun requestBms1Firmware() {
        val data = KingsongUtils.getEmptyRequest()
        data[16] = 0xe5.toByte()
        data[17] = 0x00.toByte()
        data[18] = 0x00.toByte()
        data[19] = 0x00.toByte()
        wd.bluetoothCmd(data)
    }

    private fun requestBms2Firmware() {
        val data = KingsongUtils.getEmptyRequest()
        data[16] = 0xe6.toByte()
        data[17] = 0x00.toByte()
        data[18] = 0x00.toByte()
        data[19] = 0x00.toByte()
        wd.bluetoothCmd(data)
    }
}
