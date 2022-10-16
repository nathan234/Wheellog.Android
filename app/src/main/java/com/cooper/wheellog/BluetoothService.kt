package com.cooper.wheellog

import android.app.Service
import android.bluetooth.*
import android.content.Intent
import android.os.*
import android.os.PowerManager.WakeLock
import com.cooper.wheellog.utils.*
import com.cooper.wheellog.utils.Constants.WHEEL_TYPE
import com.cooper.wheellog.utils.SomeUtil.Companion.playSound
import com.cooper.wheellog.utils.StringUtil.Companion.toHexStringRaw
import com.welie.blessed.*
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

class BluetoothService: Service() {
    private var mDisconnectTime: Date? = null
    private var wheelConnection: BluetoothPeripheral? = null
    private var reconnectTimer: Timer? = null

    private var disconnectRequested = false
    private var autoConnect = false

    private var beepTimer: Timer? = null
    private var timerTicks = 0

    private val mBinder: IBinder = LocalBinder()
    private var fileUtilRawData: FileUtil? = null
    private var mgr: PowerManager? = null
    private var wl: WakeLock? = null

    var wheelAddress: String = ""
        set(value) {
            if (value.isNotEmpty() && StringUtil.isCorrectMac(value)) {
                field = value
            }
        }

    private val sdf = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.US)
    private val sdf2 = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)
    private val wakeLogTag = "WheelLog:WakeLockTag"
    private val central: BluetoothCentralManager by lazy {
        BluetoothCentralManager(
            this,
            bluetoothCentralManagerCallback,
            Handler(Looper.getMainLooper())
        )
    }

    private val bluetoothCentralManagerCallback: BluetoothCentralManagerCallback =
        object : BluetoothCentralManagerCallback() {

            override fun onConnectedPeripheral(peripheral: BluetoothPeripheral) {
                super.onConnectedPeripheral(peripheral)
                wheelConnection = peripheral
                val connectionSound = WheelLog.AppConfig.connectionSound
                val noConnectionSound = WheelLog.AppConfig.noConnectionSound * 1000
                if (connectionSound) {
                    if (noConnectionSound > 0) {
                        stopBeepTimer()
                    }
                    wl?.release()
                    wl = mgr!!.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, wakeLogTag).apply {
                        acquire(5 * 60 * 1000L /*5 minutes*/)
                    }
                    playSound(applicationContext, R.raw.sound_connect)
                }
                mDisconnectTime = null
            }

            override fun onDisconnectedPeripheral(
                peripheral: BluetoothPeripheral,
                status: HciStatus
            ) {
                super.onDisconnectedPeripheral(peripheral, status)
                Timber.i("Disconnected from wheel.")
                mDisconnectTime = Calendar.getInstance().time
                val connectionSound = WheelLog.AppConfig.connectionSound
                val noConnectionSound = WheelLog.AppConfig.noConnectionSound * 1000
                if (connectionSound) {
                    playSound(applicationContext, R.raw.sound_disconnect)
                    if (wl != null) {
                        wl!!.release()
                        wl = null
                    }
                    if (noConnectionSound > 0) {
                        startBeepTimer()
                    }
                }
                if (!disconnectRequested && wheelConnection != null) {
                    Timber.i("Trying to reconnect")
                    when (WheelData.getInstance().wheelType) {
                        WHEEL_TYPE.INMOTION -> {
                            InMotionAdapter.stopTimer()
                            InmotionAdapterV2.stopTimer()
                            NinebotZAdapter.getInstance().resetConnection()
                            NinebotAdapter.getInstance().resetConnection()
                        }
                        WHEEL_TYPE.INMOTION_V2 -> {
                            InmotionAdapterV2.stopTimer()
                            NinebotZAdapter.getInstance().resetConnection()
                            NinebotAdapter.getInstance().resetConnection()
                        }
                        WHEEL_TYPE.NINEBOT_Z -> {
                            NinebotZAdapter.getInstance().resetConnection()
                            NinebotAdapter.getInstance().resetConnection()
                        }
                        WHEEL_TYPE.NINEBOT -> NinebotAdapter.getInstance().resetConnection()
                        else -> {}
                    }
                    if (!autoConnect) {
                        autoConnect = true
                        isWheelSearch = true
                        central.autoConnectPeripheral(wheelConnection!!, wheelCallback)
                    }
                    broadcastConnectionUpdate(true)
                } else {
                    Timber.i("Disconnected")
                    broadcastConnectionUpdate()
                }
            }
        }

    private val wheelCallback: BluetoothPeripheralCallback =
        object : BluetoothPeripheralCallback() {
            override fun onConnectionUpdated(
                peripheral: BluetoothPeripheral,
                interval: Int,
                latency: Int,
                timeout: Int,
                status: GattStatus
            ) {
                super.onConnectionUpdated(peripheral, interval, latency, timeout, status)
                if (connectionState == ConnectionState.CONNECTED || connectionState == ConnectionState.CONNECTING) {
                    isWheelSearch = false
                }
                broadcastConnectionUpdate()
            }

            override fun onServicesDiscovered(peripheral: BluetoothPeripheral) {
                super.onServicesDiscovered(peripheral)
                Timber.i("onServicesDiscovered called")
                val recognisedWheel = WheelData.getInstance().detectWheel(wheelAddress)
                WheelData.getInstance().isConnected = recognisedWheel
                if (recognisedWheel) {
                    sendBroadcast(Intent(Constants.ACTION_WHEEL_TYPE_RECOGNIZED))
                } else {
                    disconnect()
                }
            }

            override fun onCharacteristicWrite(
                peripheral: BluetoothPeripheral,
                value: ByteArray,
                characteristic: BluetoothGattCharacteristic,
                status: GattStatus
            ) {
                super.onCharacteristicWrite(peripheral, value, characteristic, status)
                if (status != GattStatus.SUCCESS) {
                    readData(characteristic, value)
                }
            }

            override fun onCharacteristicUpdate(
                peripheral: BluetoothPeripheral,
                value: ByteArray,
                characteristic: BluetoothGattCharacteristic,
                status: GattStatus
            ) {
                super.onCharacteristicUpdate(peripheral, value, characteristic, status)
                Timber.i("onCharacteristicChanged called %s", characteristic.uuid.toString())
                if (status == GattStatus.SUCCESS) {
                    readData(characteristic, value)
                }
            }

            override fun onDescriptorWrite(
                peripheral: BluetoothPeripheral,
                value: ByteArray,
                descriptor: BluetoothGattDescriptor,
                status: GattStatus
            ) {
                super.onDescriptorWrite(peripheral, value, descriptor, status)
                Timber.i("onDescriptorWrite %d", status)
            }
        }

    private fun readData(characteristic: BluetoothGattCharacteristic, value: ByteArray) {
        // RAW data
        if (WheelLog.AppConfig.enableRawData) {
            if (fileUtilRawData == null) {
                fileUtilRawData = FileUtil(applicationContext)
            }
            if (fileUtilRawData!!.isNull) {
                val fileNameForRawData = "RAW_" + sdf.format(Date()) + ".csv"
                fileUtilRawData!!.prepareFile(fileNameForRawData, WheelData.getInstance().mac)
            }
            fileUtilRawData!!.writeLine(
                String.format(
                    Locale.US, "%s,%s",
                    sdf2.format(System.currentTimeMillis()),
                    toHexStringRaw(value)
                )
            )
        } else if (fileUtilRawData != null && !fileUtilRawData!!.isNull) {
            fileUtilRawData!!.close()
        }
        val wd = WheelData.getInstance()
        when (wd.wheelType) {
            WHEEL_TYPE.KINGSONG -> if (characteristic.uuid == Constants.KINGSONG_READ_CHARACTER_UUID) {
                wd.decodeResponse(value, applicationContext)
                if (WheelData.getInstance().name.isEmpty()) {
                    KingsongAdapter.getInstance().requestNameData()
                } else if (WheelData.getInstance().serial.isEmpty()) {
                    KingsongAdapter.getInstance().requestSerialData()
                }
            }
            WHEEL_TYPE.GOTWAY, WHEEL_TYPE.GOTWAY_VIRTUAL, WHEEL_TYPE.VETERAN ->
                wd.decodeResponse(value, applicationContext)
            WHEEL_TYPE.INMOTION -> if (characteristic.uuid == Constants.INMOTION_READ_CHARACTER_UUID)
                wd.decodeResponse(value, applicationContext)
            WHEEL_TYPE.INMOTION_V2 -> if (characteristic.uuid == Constants.INMOTION_V2_READ_CHARACTER_UUID)
                wd.decodeResponse(value, applicationContext)
            WHEEL_TYPE.NINEBOT_Z -> {
                Timber.i("Ninebot Z reading")
                if (characteristic.uuid == Constants.NINEBOT_Z_READ_CHARACTER_UUID) {
                    wd.decodeResponse(value, applicationContext)
                }
            }
            WHEEL_TYPE.NINEBOT -> {
                Timber.i("Ninebot reading")
                if (characteristic.uuid == Constants.NINEBOT_READ_CHARACTER_UUID
                    || characteristic.uuid == Constants.NINEBOT_Z_READ_CHARACTER_UUID) {
                    // in case of S2 or Mini
                    Timber.i("Ninebot read cont")
                    wd.decodeResponse(value, applicationContext)
                }
            }
            else -> {}
        }
    }

    private fun broadcastConnectionUpdate(autoConnect: Boolean = false) {
        val intent = Intent(Constants.ACTION_BLUETOOTH_CONNECTION_STATE)
        intent.putExtra(Constants.INTENT_EXTRA_CONNECTION_STATE, connectionState.value)
        intent.putExtra(Constants.INTENT_EXTRA_WHEEL_SEARCH, isWheelSearch)
        if (autoConnect) {
            intent.putExtra(Constants.INTENT_EXTRA_BLE_AUTO_CONNECT, true)
        }
        sendBroadcast(intent)
    }

    var connectionState = ConnectionState.DISCONNECTED
        get() = wheelConnection?.state ?: ConnectionState.DISCONNECTED
        private set

    var isWheelSearch = false
        private set

    fun startReconnectTimer() {
        if (reconnectTimer != null) {
            stopReconnectTimer()
        }
        val wd = WheelData.getInstance()
        val magicPeriod = 15000
        reconnectTimer = Timer().apply {
            scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    if (connectionState == ConnectionState.CONNECTED
                        && wd.lastLifeData > 0
                        && (System.currentTimeMillis() - wd.lastLifeData) / 1000 > magicPeriod) {
                        toggleReconnectToWheel()
                    }
                }
            }, magicPeriod.toLong(), magicPeriod.toLong())
        }
    }

    fun stopReconnectTimer() {
        reconnectTimer?.cancel()
        reconnectTimer = null
    }

    override fun onBind(p0: Intent?): IBinder {
        mgr = this.getSystemService(POWER_SERVICE) as PowerManager
        startForeground(Constants.MAIN_NOTIFICATION_ID, WheelLog.Notifications.notification)
        if (WheelLog.AppConfig.useReconnect) {
            startReconnectTimer()
        }
        Timber.i("BluetoothService is started.")
        return mBinder
    }

    override fun onDestroy() {
        super.onDestroy()
        fileUtilRawData?.close()
        stopBeepTimer()
        stopReconnectTimer()
        WheelLog.Notifications.close()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        }
        central.connectedPeripherals.forEach { it.cancelConnection() }
        central.close()
        Timber.i("BluetoothService is destroyed.")
    }

    fun connect(): Boolean {
        disconnectRequested = false
        autoConnect = false
        mDisconnectTime = null
        if (!central.isBluetoothEnabled || wheelAddress.isEmpty()) {
            Timber.i("BluetoothAdapter not initialized or unspecified address.")
            return false
        }
        central.transport = Transport.LE
        if (wheelConnection?.address == wheelAddress) {
            Timber.i("Trying to use an existing wheelConnection for connection.")
            isWheelSearch = true
            central.autoConnectPeripheral(wheelConnection!!, wheelCallback)
            broadcastConnectionUpdate()
            return true
        }
        wheelConnection = central.getPeripheral(wheelAddress)
        if (wheelConnection?.state == ConnectionState.CONNECTING) {
            Timber.i("Device not found.  Unable to connect.")
            return false
        }

        isWheelSearch = true
        central.autoConnectPeripheral(wheelConnection!!, wheelCallback)
        Timber.i("Trying to create a new connection.")
        broadcastConnectionUpdate()
        return true
    }

    fun disconnect() {
        disconnectRequested = true
        if (!central.isBluetoothEnabled || (connectionState != ConnectionState.CONNECTED && !isWheelSearch)) {
            Timber.i("not connected.")
            return
        }

        if (wheelConnection != null) {
            central.cancelConnection(wheelConnection!!)
        }

        isWheelSearch = false
        broadcastConnectionUpdate()
    }

    fun toggleConnectToWheel() {
        Timber.i("toggleConnectToWheel. Current state: ${connectionState.name}")
        when (connectionState) {
            ConnectionState.DISCONNECTING -> Timber.i("Already disconnecting")
            ConnectionState.DISCONNECTED ->
                if (isWheelSearch) {
                    disconnect()
                } else {
                    connect()
                }
            ConnectionState.CONNECTING,
            ConnectionState.CONNECTED -> disconnect()
        }
    }

    private fun toggleReconnectToWheel() {
        if (connectionState == ConnectionState.CONNECTED) {
            Timber.wtf("Trying to reconnect")
            // After disconnect, the method onConnectionStateChange will automatically reconnect
            // because disconnectRequested is false
            disconnectRequested = false
            wheelConnection?.cancelConnection()
        }
    }

    fun setCharacteristicNotification(
        characteristic: BluetoothGattCharacteristic,
        enabled: Boolean
    ) {
        Timber.i("Set characteristic start")
        if (!central.isBluetoothEnabled || connectionState != ConnectionState.CONNECTED) {
            Timber.i("BluetoothAdapter not initialized")
            return
        }
        val success: Boolean = wheelConnection!!.setNotify(characteristic, enabled)
        Timber.i("Set characteristic %b", success)
    }

    private fun getServiceCharacteristic(
        serviceUUID: UUID,
        characteristicUUID: UUID
    ): BluetoothGattCharacteristic? {
        val service = wheelConnection!!.getService(serviceUUID)
        if (service == null) {
            Timber.i("writeBluetoothGattCharacteristic service == null")
            return null
        }
        val characteristic = service.getCharacteristic(characteristicUUID)
        if (characteristic == null) {
            Timber.i("writeBluetoothGattCharacteristic characteristic == null")
            return null
        }
        return characteristic
    }

    @Synchronized
    fun writeWheelCharacteristic(cmd: ByteArray?): Boolean {
        if (wheelConnection == null || cmd == null || wheelConnection!!.state != ConnectionState.CONNECTED) {
            return false
        }
        val stringBuilder = StringBuilder(cmd.size)
        for (aData in cmd) {
            stringBuilder.append(String.format(Locale.US, "%02X", aData))
        }
        Timber.i("Transmitted: %s", stringBuilder.toString())
        try {
            when (WheelData.getInstance().wheelType) {
                WHEEL_TYPE.KINGSONG -> {
                    val characteristic = getServiceCharacteristic(
                        Constants.KINGSONG_SERVICE_UUID,
                        Constants.KINGSONG_READ_CHARACTER_UUID
                    ) ?: return false
                    return wheelConnection!!.writeCharacteristic(characteristic, cmd, WriteType.WITHOUT_RESPONSE)
                }
                WHEEL_TYPE.GOTWAY, WHEEL_TYPE.GOTWAY_VIRTUAL, WHEEL_TYPE.VETERAN -> {
                    val characteristic = getServiceCharacteristic(
                        Constants.GOTWAY_SERVICE_UUID,
                        Constants.GOTWAY_READ_CHARACTER_UUID
                    ) ?: return false
                    return wheelConnection!!.writeCharacteristic(characteristic, cmd, WriteType.WITHOUT_RESPONSE)
                }
                WHEEL_TYPE.NINEBOT -> {
                    if (WheelData.getInstance().protoVer.compareTo("") == 0) {
                        val characteristic = getServiceCharacteristic(
                            Constants.NINEBOT_SERVICE_UUID,
                            Constants.NINEBOT_WRITE_CHARACTER_UUID
                        ) ?: return false
                        return wheelConnection!!.writeCharacteristic(characteristic, cmd, WriteType.WITHOUT_RESPONSE)
                    }
                    // if S2 or Mini, then pass to Ninebot_Z case
                    Timber.i("Passing to NZ")
                    val characteristic = getServiceCharacteristic(
                        Constants.NINEBOT_Z_SERVICE_UUID,
                        Constants.NINEBOT_Z_WRITE_CHARACTER_UUID
                    ) ?: return false
                    return wheelConnection!!.writeCharacteristic(characteristic, cmd, WriteType.WITHOUT_RESPONSE)
                }
                WHEEL_TYPE.NINEBOT_Z -> {
                    val characteristic = getServiceCharacteristic(
                        Constants.NINEBOT_Z_SERVICE_UUID,
                        Constants.NINEBOT_Z_WRITE_CHARACTER_UUID
                    ) ?: return false
                    return wheelConnection!!.writeCharacteristic(characteristic, cmd, WriteType.WITHOUT_RESPONSE)
                }
                WHEEL_TYPE.INMOTION -> {
                    val characteristic = getServiceCharacteristic(
                        Constants.INMOTION_WRITE_SERVICE_UUID,
                        Constants.INMOTION_WRITE_CHARACTER_UUID
                    ) ?: return false
                    val buf = ByteArray(20)
                    val i2 = cmd.size / 20
                    val i3 = cmd.size - i2 * 20
                    var i4 = 0
                    while (i4 < i2) {
                        System.arraycopy(cmd, i4 * 20, buf, 0, 20)
                        if (!wheelConnection!!.writeCharacteristic(characteristic, buf, WriteType.WITHOUT_RESPONSE)) {
                            return false
                        }
                        try {
                            Thread.sleep(20)
                        } catch (e: InterruptedException) {
                            e.printStackTrace()
                        }
                        i4++
                    }
                    if (i3 > 0) {
                        System.arraycopy(cmd, i2 * 20, buf, 0, i3)
                        if (!wheelConnection!!.writeCharacteristic(characteristic, buf, WriteType.WITHOUT_RESPONSE)) {
                            return false
                        }
                    }
                    return true
                }
                WHEEL_TYPE.INMOTION_V2 -> {
                    val characteristic = getServiceCharacteristic(
                        Constants.INMOTION_V2_SERVICE_UUID,
                        Constants.INMOTION_V2_WRITE_CHARACTER_UUID
                    ) ?: return false
                    return wheelConnection!!.writeCharacteristic(characteristic, cmd, WriteType.WITHOUT_RESPONSE)
                }
                else -> {}
            }
        } catch (e: NullPointerException) {
            // sometimes mBluetoothGatt is null... If the user starts to connect and disconnect quickly
            Timber.i("writeBluetoothGattCharacteristic throws NullPointerException: %s", e.message)
        }
        return false
    }

    fun getWheelServices(): List<BluetoothGattService>? {
        return wheelConnection?.services
    }

    fun getWheelService(service: UUID): BluetoothGattService? {
        return wheelConnection?.getService(service)
    }

    fun writeWheelDescriptor(descriptor: BluetoothGattDescriptor, value: ByteArray) {
        val success = wheelConnection?.writeDescriptor(descriptor, value)
        Timber.i("Write descriptor %b", success)
    }

    fun getDisconnectTime(): Date? {
        return mDisconnectTime
    }

    private fun startBeepTimer() {
        wl = mgr?.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, wakeLogTag)?.apply {
            acquire(5 * 60 * 1000L /*5 minutes*/)
        }
        timerTicks = 0
        val noConnectionSound = WheelLog.AppConfig.noConnectionSound * 1000
        val beepTimerTask: TimerTask = object : TimerTask() {
            override fun run() {
                timerTicks++
                if (timerTicks * noConnectionSound > 300000) {
                    stopBeepTimer()
                }
                playSound(applicationContext, R.raw.sound_no_connection)
            }
        }
        beepTimer = Timer().apply {
            scheduleAtFixedRate(
                beepTimerTask,
                noConnectionSound.toLong(),
                noConnectionSound.toLong()
            )
        }
    }

    private fun stopBeepTimer() {
        wl?.release()
        wl = null
        beepTimer?.cancel()
        beepTimer = null
    }

    inner class LocalBinder : Binder() {
        fun getService(): BluetoothService {
            return this@BluetoothService
        }
    }
}