package com.cooper.wheellog.utils.inmotion

import timber.log.Timber
import java.io.ByteArrayOutputStream

class InmotionUnpackerV2 {
    enum class UnpackerState {
        UNKNOWN, FLAG_SEARCH, LENS_SEARCH, COLLECTING, DONE
    }

    var buffer = ByteArrayOutputStream()
    var oldc = 0
    var len = 0
    var flags = 0
    var state = UnpackerState.UNKNOWN
    fun getBuffer(): ByteArray {
        return buffer.toByteArray()
    }

    fun addChar(c: Int): Boolean {
        if (c != 0xA5.toByte().toInt() || oldc == 0xA5.toByte().toInt()) {
            when (state) {
                UnpackerState.COLLECTING -> {
                    buffer.write(c)
                    if (buffer.size() == len + 5) {
                        state = UnpackerState.DONE
                        oldc = 0
                        Timber.i("Len %d", len)
                        Timber.i("Step reset")
                        return true
                    }
                }

                UnpackerState.LENS_SEARCH -> {
                    buffer.write(c)
                    len = c and 0xff
                    state = UnpackerState.COLLECTING
                    oldc = c
                }

                UnpackerState.FLAG_SEARCH -> {
                    buffer.write(c)
                    flags = c and 0xff
                    state = UnpackerState.LENS_SEARCH
                    oldc = c
                }

                else -> {
                    if (c == 0xAA.toByte().toInt() && oldc == 0xAA.toByte().toInt()) {
                        buffer = ByteArrayOutputStream()
                        buffer.write(0xAA)
                        buffer.write(0xAA)
                        state = UnpackerState.FLAG_SEARCH
                    }
                    oldc = c
                }
            }
        } else {
            oldc = c
        }
        return false
    }

    fun reset() {
        buffer = ByteArrayOutputStream()
        oldc = 0
        state = UnpackerState.UNKNOWN
    }
}
