package com.cooper.wheellog.utils

// used to be Java NIO Buffer
import okio.Buffer
import kotlin.math.max
import kotlin.math.min

object MathsUtil {
    var kmToMilesMultiplier = 0.62137119223733
    fun kmToMiles(km: Double): Double {
        return km * kmToMilesMultiplier
    }

    fun kmToMiles(km: Float): Float {
        return kmToMiles(km.toDouble()).toFloat()
    }

    fun celsiusToFahrenheit(temp: Double): Double {
        // celsius-to-fahrenheit.org
        return temp * 9.0 / 5.0 + 32
    }

//    fun getInt2(arr: ByteArray?, offset: Int): Int {
//        return ByteBuffer.wrap(arr, offset, 2).getShort().toInt()
//    }

    fun getInt2(arr: ByteArray?, offset: Int): Int {
        if (arr == null || offset + 2 > arr.size) {
            throw IllegalArgumentException("Invalid array or offset")
        }
        val buffer = Buffer()
        buffer.write(arr, offset, 2)
        return buffer.readShort().toInt()
    }

//    fun getInt2R(arr: ByteArray, offset: Int): Int {
//        return ByteBuffer.wrap(reverseEvery2(arr, offset, 2), 0, 2).getShort().toInt()
//    }

    fun getInt2R(arr: ByteArray, offset: Int): Int {
        val reversedArr = reverseEvery2(arr, offset, 2)
        val buffer = Buffer()
        buffer.write(reversedArr, 0, 2)
        return buffer.readShort().toInt()
    }

//    fun getInt4(arr: ByteArray?, offset: Int): Long {
//        return ByteBuffer.wrap(arr, offset, 4).getInt().toLong()
//    }

    fun getInt4(arr: ByteArray?, offset: Int): Long {
        if (arr == null || offset + 4 > arr.size) {
            throw IllegalArgumentException("Invalid array or offset")
        }
        val buffer = Buffer()
        buffer.write(arr, offset, 4)
        return buffer.readInt().toLong()
    }

//    fun getInt4R(arr: ByteArray, offset: Int): Int {
//        return ByteBuffer.wrap(reverseEvery2(arr, offset, 4), 0, 4).getInt()
//    }

    fun getInt4R(arr: ByteArray, offset: Int): Int {
        val reversedArr = reverseEvery2(arr, offset, 4)
        val buffer = Buffer()
        buffer.write(reversedArr, 0, 4)
        return buffer.readInt()
    }

//    fun getBytes(input: Short): ByteArray {
//        return ByteBuffer.allocate(2).putShort(input).array()
//    }

    fun getBytes(input: Short): ByteArray {
        val buffer = Buffer()
        buffer.writeShort(input.toInt())
        return buffer.readByteArray()
    }

//    fun getBytes(input: Int): ByteArray {
//        return ByteBuffer.allocate(4).putInt(input).array()
//    }

    fun getBytes(input: Int): ByteArray {
        val buffer = Buffer()
        buffer.writeInt(input)
        return buffer.readByteArray()
    }

//    @JvmOverloads
//    fun reverseEvery2(input: ByteArray, offset: Int = 0, len: Int = input.size): ByteArray {
//        val result = ByteArray(len)
//        System.arraycopy(input, offset, result, 0, len)
//        var i = 0
//        while (i < len - 1) {
//            val temp = result[i]
//            result[i] = result[i + 1]
//            result[i + 1] = temp
//            i += 2
//        }
//        return result
//    }

    fun reverseEvery2(arr: ByteArray, offset: Int = 0, length: Int = arr.size): ByteArray {
        val result = arr.copyOfRange(offset, offset + length)
        for (i in 0 until length step 2) {
            if (i + 1 < length) {
                val temp = result[i]
                result[i] = result[i + 1]
                result[i + 1] = temp
            }
        }
        return result
    }

    fun longFromBytesLE(bytes: ByteArray, starting: Int): Long {
        return if (bytes.size >= starting + 8) {
            ((((((((bytes[starting + 7].toInt() and 255).toLong() shl 8
                    or (bytes[starting + 6].toInt() and 255).toLong()) shl 8
                    or (bytes[starting + 5].toInt() and 255).toLong()) shl 8
                    or (bytes[starting + 4].toInt() and 255).toLong()) shl 8
                    or (bytes[starting + 3].toInt() and 255).toLong()) shl 8
                    or (bytes[starting + 2].toInt() and 255).toLong()) shl 8
                    or (bytes[starting + 1].toInt() and 255).toLong()) shl 8
                    or (bytes[starting].toInt() and 255).toLong())
        } else 0
    }

    fun signedIntFromBytesLE(bytes: ByteArray, starting: Int): Long {
        return if (bytes.size >= starting + 4) {
            (bytes[starting + 3].toInt() and 0xFF shl 24 or (bytes[starting + 2]
                .toInt() and 0xFF shl 16) or (bytes[starting + 1]
                .toInt() and 0xFF shl 8) or (bytes[starting].toInt() and 0xFF)).toLong()
        } else 0
    }

    fun intFromBytesRevLE(bytes: ByteArray, starting: Int): Long {
        return if (bytes.size >= starting + 4) {
            (bytes[starting + 1].toInt() and 0xFF shl 24 or (bytes[starting]
                .toInt() and 0xFF shl 16) or (bytes[starting + 3]
                .toInt() and 0xFF shl 8) or (bytes[starting + 2].toInt() and 0xFF)).toLong()
        } else 0
    }

    fun intFromBytesLE(bytes: ByteArray, starting: Int): Int {
        return if (bytes.size >= starting + 4) {
            bytes[starting + 3].toInt() and 0xFF shl 24 or (bytes[starting + 2]
                .toInt() and 0xFF shl 16) or (bytes[starting + 1]
                .toInt() and 0xFF shl 8) or (bytes[starting].toInt() and 0xFF)
        } else 0
    }

    fun intFromBytesRevBE(bytes: ByteArray, starting: Int): Int {
        return if (bytes.size >= starting + 4) {
            bytes[starting + 2].toInt() and 0xFF shl 24 or (bytes[starting + 3]
                .toInt() and 0xFF shl 16) or (bytes[starting]
                .toInt() and 0xFF shl 8) or (bytes[starting + 1].toInt() and 0xFF)
        } else 0
    }

    fun shortFromBytesLE(bytes: ByteArray, starting: Int): Int {
        return if (bytes.size >= starting + 2) {
            bytes[starting + 1].toInt() and 0xFF shl 8 or (bytes[starting]
                .toInt() and 0xFF)
        } else 0
    }

    fun shortFromBytesBE(bytes: ByteArray, starting: Int): Int {
        return if (bytes.size >= starting + 2) {
            bytes[starting].toInt() and 0xFF shl 8 or (bytes[starting + 1]
                .toInt() and 0xFF)
        } else 0
    }

    fun signedShortFromBytesBE(bytes: ByteArray, starting: Int): Int {
        return if (bytes.size >= starting + 2) {
            bytes[starting].toInt() shl 8 or (bytes[starting + 1].toInt() and 0xFF)
        } else 0
    }

    fun signedShortFromBytesLE(bytes: ByteArray, starting: Int): Int {
        return if (bytes.size >= starting + 2) {
            bytes[starting + 1].toInt() shl 8 or (bytes[starting].toInt() and 0xFF)
        } else 0
    }

    fun clamp(`val`: Double, min: Double, max: Double): Double {
        return max(min, min(max, `val`))
    }

    fun clamp(`val`: Float, min: Float, max: Float): Float {
        return max(min.toDouble(), min(max.toDouble(), `val`.toDouble())).toFloat()
    }

    fun clamp(`val`: Int, min: Int, max: Int): Int {
        return max(min.toDouble(), min(max.toDouble(), `val`.toDouble())).toInt()
    }
}
