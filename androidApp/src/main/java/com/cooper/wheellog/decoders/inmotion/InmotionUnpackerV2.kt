package com.cooper.wheellog.decoders.inmotion

import timber.log.Timber
import java.io.ByteArrayOutputStream

class InmotionUnpackerV2 {
    enum class UnpackerState {
        unknown,
        flagsearch,
        lensearch,
        collecting,
        done
    }

    var buffer = ByteArrayOutputStream()
    var oldc = 0
    var len = 0
    var flags = 0
    var state = UnpackerState.unknown
    fun getBuffer(): ByteArray {
        return buffer.toByteArray()
    }

    fun addChar(c: Int): Boolean {
        if (c != 0xA5.toByte().toInt() || oldc == 0xA5.toByte().toInt()) {
            when (state) {
                UnpackerState.collecting -> {
                    buffer.write(c)
                    if (buffer.size() == len + 5) {
                        state = UnpackerState.done
                        //                        updateStep = 0;
                        oldc = 0
                        Timber.i("Len %d", len)
                        Timber.i("Step reset")
                        return true
                    }
                }

                UnpackerState.lensearch -> {
                    buffer.write(c)
                    len = c and 0xff
                    state = UnpackerState.collecting
                    oldc = c
                }

                UnpackerState.flagsearch -> {
                    buffer.write(c)
                    flags = c and 0xff
                    state = UnpackerState.lensearch
                    oldc = c
                }

                else -> {
                    if (c == 0xAA.toByte().toInt() && oldc == 0xAA.toByte().toInt()) {
                        buffer = ByteArrayOutputStream()
                        buffer.write(0xAA)
                        buffer.write(0xAA)
                        state = UnpackerState.flagsearch
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
        state = UnpackerState.unknown
    }
}