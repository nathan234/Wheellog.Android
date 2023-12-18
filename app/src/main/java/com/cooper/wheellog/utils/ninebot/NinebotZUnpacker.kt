package com.cooper.wheellog.utils.ninebot

import java.io.ByteArrayOutputStream
import timber.log.Timber

class NinebotZUnpacker {
    internal enum class UnpackerState {
        UNKNOWN,
        STARTED,
        COLLECTING,
        DONE
    }

    private var buffer = ByteArrayOutputStream()
    var oldc = 0
    var len = 0
    private var state = UnpackerState.UNKNOWN

    fun getBuffer(): ByteArray {
        return buffer.toByteArray()
    }

    fun addChar(c: Int, updateStep: Int): Pair<Boolean, Int> {
        var updateStep = updateStep
        when (state) {
            UnpackerState.COLLECTING -> {
                buffer.write(c)
                if (buffer.size() == len + 9) {
                    state = UnpackerState.DONE
                    updateStep = 0
                    Timber.i("Len %d", len)
                    Timber.i("Step reset")
                    return Pair(true, updateStep)
                }
            }
            UnpackerState.STARTED -> {
                buffer.write(c)
                len = c and 0xff
                state = UnpackerState.COLLECTING
            }
            else -> {
                if (c == 0xA5.toByte().toInt() && oldc == 0x5A.toByte().toInt()) {
                    Timber.i("Find start")
                    buffer = ByteArrayOutputStream()
                    buffer.write(0x5A)
                    buffer.write(0xA5)
                    state = UnpackerState.STARTED
                }
                oldc = c
            }
        }
        return Pair(false, updateStep)
    }
}
