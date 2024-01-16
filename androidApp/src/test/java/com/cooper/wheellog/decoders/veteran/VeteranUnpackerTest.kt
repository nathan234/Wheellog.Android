package com.cooper.wheellog.decoders.veteran

import org.junit.Assert
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.zip.CRC32

class VeteranUnpackerTest {
    private val unpacker = VeteranUnpacker(ByteArrayOutputStream())

    @Test
    @Throws(IOException::class)
    fun testAddCharCollectingDataVerificationFails() {
        unpacker.state = VeteranUnpacker.UnpackerState.COLLECTING
        unpacker.buffer.write(ByteArray(22)) // Buffer size is now 22
        val result = unpacker.addChar(1) // Should fail as data verification does not pass
        Assert.assertFalse(result)
        Assert.assertEquals(
            VeteranUnpacker.UnpackerState.UNKNOWN,
            unpacker.state,
        ) // State should reset to UNKNOWN
    }

    //    @Test
    fun testAddCharCollectingCRC32CheckPasses() {
        // Prepare the Unpacker to reach the COLLECTING state
        unpacker.reset()
        unpacker.addChar(0xDC.toByte().toInt())
        unpacker.addChar(0x5A.toByte().toInt())
        unpacker.addChar(0x5C.toByte().toInt())
        unpacker.addChar(39) // This should be the length of the packet + 3

        // Simulate adding 39 bytes of data (length+3) to the buffer
        for (i in 0..38) {
            unpacker.addChar(0xFF)
        }

        // Calculate the CRC32 of the dummy data to append at the end
        val crc = CRC32()
        crc.update(ByteArray(39)) // Assuming the data is all 0xFF, update with actual data if different
        val crcValue = crc.value

        // Add the CRC32 checksum to the buffer
        unpacker.addChar((crcValue and 0xFFL).toInt())
        unpacker.addChar((crcValue shr 8 and 0xFFL).toInt())
        unpacker.addChar((crcValue shr 16 and 0xFFL).toInt())
        unpacker.addChar((crcValue shr 24 and 0xFFL).toInt())

        // The next call to addChar should return true if CRC check passes
        Assert.assertTrue(unpacker.addChar(0)) // This could be any value; it should trigger the CRC check
    }

    @Test
    fun testAddCharLensSearchUpdatesLengthAndState() {
        unpacker.state = VeteranUnpacker.UnpackerState.LENS_SEARCH
        val testLength = 20
        unpacker.addChar(testLength)
        Assert.assertEquals(testLength.toLong(), unpacker.len.toLong())
        Assert.assertEquals(VeteranUnpacker.UnpackerState.COLLECTING, unpacker.state)
    }

    @Test
    fun testAddCharUnknownToLensSearchTransition() {
        unpacker.old1 = 0x5A.toByte().toInt()
        unpacker.old2 = 0xDC.toByte().toInt()
        unpacker.addChar(0x5C.toByte().toInt())
        Assert.assertEquals(VeteranUnpacker.UnpackerState.LENS_SEARCH, unpacker.state)
    }

    @Test
    fun testResetFunctionality() {
        unpacker.old1 = 1
        unpacker.old2 = 1
        unpacker.state = VeteranUnpacker.UnpackerState.DONE
        unpacker.reset()
        Assert.assertEquals(0, unpacker.old1.toLong())
        Assert.assertEquals(0, unpacker.old2.toLong())
        Assert.assertEquals(VeteranUnpacker.UnpackerState.UNKNOWN, unpacker.state)
    }
}
