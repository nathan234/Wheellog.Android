package com.cooper.wheellog.utils.inmotion

import timber.log.Timber
import java.io.ByteArrayOutputStream

class InMotionUnpacker {
    enum class UnpackerState {
        UNKNOWN,
        COLLECTING,
        DONE,
    }

    var buffer = ByteArrayOutputStream()
    var oldc = 0

    // there are two types of packets, basic and extended, if it is extended packet,
    // then len field should be 0xFE, and len of extended data should be in first data byte
    // of usual packet
    var len_p = 0 // basic packet len
    var len_ex = 0 // extended packet len
    var state = UnpackerState.UNKNOWN

    fun getBuffer(): ByteArray {
        return buffer.toByteArray()
    }

    fun addChar(c: Int, updateStep: Int): Pair<Boolean, Int> {
        var updateStep = updateStep
        if (c != 0xA5.toByte().toInt() || oldc == 0xA5.toByte().toInt()) {
            if (state == UnpackerState.COLLECTING) {
                buffer.write(c)
                val sz = buffer.size()
                if (sz == 7) len_ex = c and 0xFF else if (sz == 15) len_p = c and 0xFF
                if (sz > len_ex + 21 && len_p == 0xFE) {
                    reset() // longer than expected
                    return Pair(false, updateStep)
                }
                if (
                    c == 0x55.toByte().toInt() &&
                    oldc == 0x55.toByte().toInt() &&
                    (sz == len_ex + 21 || len_p != 0xFE)
                ) { // 18 header + 1 crc + 2 footer
                    state = UnpackerState.DONE
                    updateStep = 0
                    oldc = 0
                    Timber.i("Step reset")
                    return Pair(true, updateStep)
                }
            } else {
                if (c == 0xAA.toByte().toInt() && oldc == 0xAA.toByte().toInt()) {
                    buffer = ByteArrayOutputStream()
                    buffer.write(0xAA)
                    buffer.write(0xAA)
                    state = UnpackerState.COLLECTING
                }
            }
        }
        oldc = c
        return Pair(false, updateStep)
    }

    fun reset() {
        buffer = ByteArrayOutputStream()
        oldc = 0
        len_p = 0
        len_ex = 0
        state = UnpackerState.UNKNOWN
    }
}
