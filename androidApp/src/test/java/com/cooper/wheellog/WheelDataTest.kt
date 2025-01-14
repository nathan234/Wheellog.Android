package com.cooper.wheellog

import android.content.Context
import com.cooper.wheellog.app.AppConfig
import com.cooper.wheellog.app.WheelLog
import com.cooper.wheellog.wheeldata.WheelData
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockkClass
import io.mockk.mockkStatic
import io.mockk.spyk
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Before
import org.junit.Test

class WheelDataTest {
    private lateinit var data: WheelData

    @Before
    fun setUp() {
        data = spyk(WheelData())
        WheelData.instance = data
        every { data.bluetoothService?.applicationContext } returns mockkClass(
            Context::class,
            relaxed = true
        )
        WheelLog.AppConfig = mockkClass(AppConfig::class, relaxed = true)
        mockkStatic(WheelData::class)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Battery per km | 95 to 94`() {
        // Arrange.
        data.batteryLevel = (95)
        data.totalDistance = 1_000

        // Act.
        data.batteryLevel = (94)
        data.totalDistance += 2_000

        // Assert.
        assertThat(data.batteryPerKm).isEqualTo(0.5)
        assertThat(data.remainingDistance).isEqualTo(188)
    }

    @Test
    fun `Battery per km | 50 to 0`() {
        // Arrange.
        data.batteryLevel = (50)
        data.totalDistance = 0

        // Act.
        data.batteryLevel = (0)
        data.totalDistance += 25_000

        // Assert.
        assertThat(data.batteryPerKm).isEqualTo(2)
        assertThat(data.remainingDistance).isEqualTo(0)
    }

    @Test
    fun `Max power`() {
        // Arrange.
        data.setPower(50)

        // Act.
        data.setPower(100)
        data.setPower(75)

        // Assert.
        assertThat(data.powerDouble).isEqualTo(0.75)
        assertThat(data.maxPowerDouble).isEqualTo(1)
    }

    @Test
    fun `Max current`() {
        // Arrange.
        data.current = 50

        // Act.
        data.current = 100
        data.current = 75

        // Assert.
        assertThat(data.currentDouble).isEqualTo(0.75)
        assertThat(data.maxCurrentDouble).isEqualTo(1)
    }
}