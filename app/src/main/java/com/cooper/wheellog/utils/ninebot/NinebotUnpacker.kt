package com.cooper.wheellog.utils.ninebot

import timber.log.Timber
import java.io.ByteArrayOutputStream

class NinebotUnpacker {
    enum class UnpackerState {
        UNKNOWN,
        STARTED,
        COLLECTING,
        DONE,
    }

    private var buffer = ByteArrayOutputStream()
    var oldc = 0
    var len = 0
    var state = UnpackerState.UNKNOWN
    fun getBuffer(): ByteArray {
        return buffer.toByteArray()
    }

    fun addChar(c: Int, updateStep: Int): Pair<Boolean, Int> {
        var step = updateStep
        when (state) {
            UnpackerState.COLLECTING -> {
                buffer.write(c)
                if (buffer.size() == len + 6) {
                    state = UnpackerState.DONE
                    step = 0
                    Timber.i("Len %d", len)
                    Timber.i("Step reset")
                    return Pair(true, step)
                }
            }

            UnpackerState.STARTED -> {
                buffer.write(c)
                len = c and 0xff
                state = UnpackerState.COLLECTING
            }

            else -> {
                if (c == 0xAA.toByte().toInt() && oldc == 0x55.toByte().toInt()) {
                    Timber.i("Find start")
                    buffer = ByteArrayOutputStream()
                    buffer.write(0x55)
                    buffer.write(0xAA)
                    state = UnpackerState.STARTED
                }
                oldc = c
            }
        }
        return Pair(false, step)
    }

    fun reset() {
        buffer = ByteArrayOutputStream()
        oldc = 0
        state = UnpackerState.UNKNOWN
    }
}
