package com.cooper.wheellog

import android.Manifest
import android.app.Notification
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.cooper.wheellog.utils.Alarms.alarm
import com.cooper.wheellog.utils.Constants
import com.samsung.android.sdk.SsdkUnsupportedException
import com.samsung.android.sdk.accessory.SA
import com.samsung.android.sdk.accessory.SAAgent
import com.samsung.android.sdk.accessory.SAPeerAgent
import com.samsung.android.sdk.accessory.SASocket
import java.io.IOException
import java.util.AbstractCollection
import java.util.Locale
import java.util.Timer
import java.util.TimerTask
import java.util.Vector

class GearService : SAAgent(TAG, GearSAPServiceProviderConnection::class.java) {
    var mBinder = GearBinder()
    var mConnectionBag: AbstractCollection<GearSAPServiceProviderConnection> = Vector()
    var mLocationManager: LocationManager? = null
    var mIsListening = false
    private var keepAliveTimer: Timer? = null
    private val mNotification: Notification? = null

    inner class GearBinder : Binder() {
        val service: GearService
            get() = this@GearService
    }

    var locationListener: LocationListener? = object : LocationListener {
        var mTime: Long = 0
        var mBearing = 0f
        var mSpeed = 0f
        var mLatitude = 0.0
        var mLongitude = 0.0
        var mAltitude = 0.0
        var bHasAltitude = false
        var bHasBearing = false
        var bHasSpeed = false
        var bGpsEnabled = true
        override fun toString(): String {
            //In general this isn't how I would encode something in JSON, but the amount
            //of data is small enough such that I've decided to use String.Format to
            //produce what's needed.
            return String.format(
                Locale.ROOT, "\"gpsEnabled\" :%b," +
                        "\"hasSpeed\":%b, \"gpsSpeed\":%1.2f, \"hasBearing\":%b, \"bearing\":%1.4f," +
                        "\"latitude\":%f, \"longitude\":%f,\"hasAltitude\":%b, \"altitude\":%1.3f",
                bGpsEnabled,
                bHasSpeed, mSpeed,
                bHasBearing, mBearing,
                mLatitude, mLongitude,
                bHasAltitude, mAltitude
            )
        }

        override fun onLocationChanged(location: Location) {
            if (location.hasSpeed().also { bHasSpeed = it }) mSpeed = location.speed
            if (location.hasAltitude().also { bHasAltitude = it }) mAltitude = location.altitude
            if (location.hasSpeed()) mSpeed = location.speed
            if (location.hasBearing().also { bHasBearing = it }) mBearing = location.bearing
            mLatitude = location.latitude
            mLongitude = location.longitude
            mTime = location.time
            //        transmitMessage(); Me lo he llevado a la rutina que se ejecuta de forma temporizada
        }

        override fun onStatusChanged(s: String, i: Int, bundle: Bundle) {}
        override fun onProviderEnabled(s: String) {
            bGpsEnabled = true
        }

        override fun onProviderDisabled(s: String) {
            bGpsEnabled = false
        }
    }

    init {
        //        android.os.Debug.waitForDebugger();  // this line is key for debugging (run and attach debugger)
        Log.d(TAG, "Service instantiated")
    }

    fun transmitMessage(sendingString: String) {
        val sendingMessage = sendingString.toByteArray()
        Log.i(TAG, sendingString)
        for (connection in mConnectionBag) {
            try {
                connection.send(SAP_SERVICE_CHANNEL_ID, sendingMessage)
            } catch (exc: IOException) {
                //
            }
        }
    }

    fun startKeepAliveTimer() { //Se le pueden pasar parámetros
        val timerTask: TimerTask = object : TimerTask() {
            override fun run() {
                var message: String
                message = if (WheelData.instance!! != null) {
                    String.format(
                        Locale.ROOT, "{ \"speed\":%.2f," +
                                "\"voltage\":%.2f,\"current\":%.2f,\"power\":%.2f," +
                                "\"batteryLevel\":%d,\"distance\":%d,\"totalDistance\":%d,\"temperature\":%d," +
                                "\"temperature2\":%d," +
                                "\"angle\":%.2f,\"roll\":%.2f,\"isAlarmExecuting\":%d",  //                        "\"mode\":%s,\"alert\":%s"+
                        WheelData.instance!!.speedDouble,
                        WheelData.instance!!.voltageDouble,
                        WheelData.instance!!.currentDouble,
                        WheelData.instance!!.powerDouble,
                        WheelData.instance!!.batteryLevel,
                        WheelData.instance!!.distance,
                        WheelData.instance!!.totalDistance,
                        WheelData.instance!!.temperature,
                        WheelData.instance!!.temperature2,
                        WheelData.instance!!.angle,
                        WheelData.instance!!.roll,
                        alarm //                        WheelData.instance!!.getModeStr(),
                        //                        WheelData.instance!!.getAlert()
                    )
                } else {
                    "{"
                }
                if (locationListener != null) {
                    if (WheelData.instance!! != null) {
                        message = message + "," + locationMessage
                    } else {
                        message = locationMessage
                    }
                }
                message = "$message}"
                transmitMessage(message)
            }
        }
        keepAliveTimer = Timer()
        keepAliveTimer!!.scheduleAtFixedRate(timerTask, 0, 200) //cada 500ms
    }

    fun removeConnection(connection: GearSAPServiceProviderConnection) {
        mConnectionBag.remove(connection)
        reevaluateNeedToSend()
    }

    fun addConnection(connection: GearSAPServiceProviderConnection) {
        mConnectionBag.add(connection)
        transmitMessage("Mensaje inicial")
        //onServiceConnectionResponse also calls reevaluateNeedTOSend, so there is no need to transmit anything now
        //No entiendo por qué manda un primer mensaje al conectar pero lo dejo por si evita timeouts.
    }

    val locationMessage: String
        get() = if (locationListener == null) {
            ""
        } else locationListener.toString()

    fun startSendingData() {
        if (!mIsListening) {
            val permissionCheck = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                mIsListening = true
                mLocationManager!!.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    1000,
                    1f,
                    locationListener!!
                )
            }
            startKeepAliveTimer() //Location retreived each second, but data sent twice a second.
        }
    }

    fun stopSendingData() {
        if (mIsListening) {
            mLocationManager!!.removeUpdates(locationListener!!)
            mIsListening = false
            keepAliveTimer!!.cancel()
            keepAliveTimer = null
        }
    }

    fun reevaluateNeedToSend() {
        if (mConnectionBag.size == 0) stopSendingData() else startSendingData()
    }

    override fun onFindPeerAgentResponse(agent: SAPeerAgent, i: Int) {}

    //    protected void onServiceConnectionResponse(SASocket currentConnection, int result) {
    override fun onServiceConnectionResponse(
        agent: SAPeerAgent,
        currentConnection: SASocket,
        result: Int
    ) {
        super.onServiceConnectionResponse(agent, currentConnection, result)
        if (result == CONNECTION_SUCCESS) {
            if (currentConnection != null) {
                val connection = currentConnection as GearSAPServiceProviderConnection
                connection.setParent(this)
                addConnection(connection)
                Toast.makeText(baseContext, "GEAR CONNECTION ESTABLISHED", Toast.LENGTH_LONG).show()
                reevaluateNeedToSend() //We start sending when watch connects
            } else {
                Log.e(TAG, "Connection object is null.")
            }
        } else if (result == CONNECTION_ALREADY_EXIST) {
            Log.e(TAG, "CONNECTION_ALREADY_EXISTS")
        } else {
            Log.e(TAG, "connection error result$result")
        }
    }

    override fun onServiceConnectionRequested(agent: SAPeerAgent) {
        acceptServiceConnectionRequest(agent)
        Log.i(TAG, "Accepting connection") //The watch initiates always the connection
    }

    override fun onCreate() {
        //  startForeground();
        super.onCreate()
        mLocationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        val accessory = SA()
        try {
            accessory.initialize(this) //Y expect this to do nothing for non-Samsung devices
        } catch (exc: SsdkUnsupportedException) {
            Log.e(TAG, "Unsupported SDK")
        } catch (exc: Exception) {
            Log.e(TAG, "initialization failed")
            exc.printStackTrace()
            stopSelf()
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        Toast.makeText(baseContext, "Gear Service started", Toast.LENGTH_LONG).show()
        Log.i(TAG, "started")
        startForeground(Constants.MAIN_NOTIFICATION_ID, WheelLog.Notifications.notification)
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        Log.i(TAG, "onBind")
        return mBinder
    }

    override fun onDestroy() {}

    companion object {
        const val TAG = "GearService"
        const val SAP_SERVICE_CHANNEL_ID = 142 //Same as in sapservices.xml on both sides
    }
}
