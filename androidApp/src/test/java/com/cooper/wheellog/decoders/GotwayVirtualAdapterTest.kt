package com.cooper.wheellog.decoders

import android.content.Context
import com.cooper.wheellog.BluetoothService
import com.cooper.wheellog.app.AppConfig
import com.cooper.wheellog.app.WheelLog
import com.cooper.wheellog.models.Constants
import com.cooper.wheellog.utils.Utils.Companion.hexToByteArray
import com.cooper.wheellog.wheeldata.WheelData
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockkClass
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.spyk
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Before
import org.junit.Test

class GotwayVirtualAdapterTest {

    private var adapter: GotwayVirtualAdapter = GotwayVirtualAdapter()
    private lateinit var data: WheelData

    @Before
    fun setUp() {
        mockkObject(WheelLog)
        every { WheelLog.appContext } returns mockkClass(Context::class, relaxed = true)
        data = spyk(WheelData())
        data.wheelType = Constants.WHEEL_TYPE.GOTWAY_VIRTUAL
        WheelLog.AppConfig = mockkClass(AppConfig::class, relaxed = true)
        WheelData.instance = data
        mockkStatic(WheelData::class)
        mockkStatic(BluetoothService::class)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `switch to gotway and decode`() {
        // Arrange.
        val byteArray1 = "55AA19C1000000000000008CF0000001FFF80018".hexToByteArray()

        // Act.
        adapter.decode(byteArray1)

        // Assert.
        assertThat(data.wheelType).isEqualTo(Constants.WHEEL_TYPE.GOTWAY)
    }

    @Test
    fun `switch to veteran`() {
        // Arrange.
        val byteArray1 = "DC5A5C20238A0112121A00004D450005064611F2".hexToByteArray()

        // Act.
        adapter.decode(byteArray1)

        // Assert.
        assertThat(data.wheelType).isEqualTo(Constants.WHEEL_TYPE.VETERAN)
    }

    @Test
    fun `switch to veteran shermans`() {
        // Arrange.
        val byteArray1 = "DC5A5C22266200000084000017A2000000000C35".hexToByteArray()

        // Act.
        adapter.decode(byteArray1)

        // Assert.
        assertThat(data.wheelType).isEqualTo(Constants.WHEEL_TYPE.VETERAN)
    }
}
