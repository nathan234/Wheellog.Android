package com.cooper.wheellog.utils.veteran

import com.cooper.wheellog.utils.MathsUtil
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.util.zip.CRC32

class VeteranUnpacker(
    var buffer: ByteArrayOutputStream,
) {
    enum class UnpackerState {
        UNKNOWN,
        COLLECTING,
        LENS_SEARCH,
        DONE,
    }

    var old1 = 0

    var old2 = 0

    var len = 0

    var state = UnpackerState.UNKNOWN

    fun getBuffer(): ByteArray {
        return buffer.toByteArray()
    }

    fun addChar(c: Int): Boolean {
        when (state) {
            UnpackerState.COLLECTING -> {
                val bsize = buffer.size()
                if (
                    (bsize == 22 || bsize == 30) && c != 0x00 ||
                    bsize == 23 && c and 0xFE != 0x00 ||
                    bsize == 31 && c and 0xFC != 0x00
                ) {
                    state = UnpackerState.DONE
                    Timber.i("Data verification failed")
                    reset()
                    return false
                }
                buffer.write(c)
                if (bsize == len + 3) {
                    state = UnpackerState.DONE
                    Timber.i("Len %d", len)
                    Timber.i("Step reset")
                    reset()
                    if (len > 38) { // new format with crc32
                        val crc = CRC32()
                        crc.update(getBuffer(), 0, len)
                        val calcCrc = crc.value
                        val providedCrc = MathsUtil.getInt4(getBuffer(), len)
                        return if (calcCrc == providedCrc) {
                            Timber.i("CRC32 ok")
                            true
                        } else {
                            Timber.i("CRC32 fail")
                            false
                        }
                    }
                    return true // old format without crc32
                }
            }
            UnpackerState.LENS_SEARCH -> {
                buffer.write(c)
                len = c and 0xff
                state = UnpackerState.COLLECTING
                old2 = old1
                old1 = c
            }
            UnpackerState.UNKNOWN,
            UnpackerState.DONE,
            -> {
                when {
                    c == 0x5C.toByte().toInt() &&
                        old1 == 0x5A.toByte().toInt() &&
                        old2 == 0xDC.toByte().toInt() -> {
                        buffer = ByteArrayOutputStream()
                        buffer.write(0xDC)
                        buffer.write(0x5A)
                        buffer.write(0x5C)
                        state = UnpackerState.LENS_SEARCH
                    }
                    c == 0x5A.toByte().toInt() && old1 == 0xDC.toByte().toInt() -> {
                        old2 = old1
                    }
                    else -> {
                        old2 = 0
                    }
                }
                old1 = c
            }
        }
        return false
    }

    fun reset() {
        old1 = 0
        old2 = 0
        state = UnpackerState.UNKNOWN
    }
}
