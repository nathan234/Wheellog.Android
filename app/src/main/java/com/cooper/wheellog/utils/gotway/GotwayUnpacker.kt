package com.cooper.wheellog.utils.gotway

import timber.log.Timber
import java.io.ByteArrayOutputStream

class GotwayUnpacker {
    enum class UnpackerState {
        UNKNOWN, COLLECTING, DONE
    }

    private var buffer = ByteArrayOutputStream()
    var state = UnpackerState.UNKNOWN
    var oldc = -1
    fun getBuffer(): ByteArray {
        return buffer.toByteArray()
    }

    fun addChar(c: Int): Boolean {
        if (state == UnpackerState.COLLECTING) {
            buffer.write(c)
            oldc = c
            val size = buffer.size()
            if (size == 20 && c != 0x18.toByte()
                    .toInt() || size in 21..24 && c != 0x5A.toByte().toInt()
            ) {
                Timber.i("Invalid frame footer (expected 18 5A 5A 5A 5A)")
                state = UnpackerState.UNKNOWN
                return false
            }
            if (size == 24) {
                state = UnpackerState.DONE
                Timber.i("Valid frame received")
                return true
            }
        } else {
            if (c == 0xAA.toByte().toInt() && oldc == 0x55.toByte().toInt()) {
                Timber.i("Frame header found (55 AA), collecting data")
                buffer = ByteArrayOutputStream()
                buffer.write(0x55)
                buffer.write(0xAA)
                state = UnpackerState.COLLECTING
            }
            oldc = c
        }
        return false
    }
}