package com.cooper.wheellog

import android.util.Log
import com.samsung.android.sdk.accessory.SASocket

class GearSAPServiceProviderConnection :
    SASocket(GearSAPServiceProviderConnection::class.java.getName()) {
    private val connectionID: Int
    private var mParent: GearService? = null
    fun setParent(gearService: GearService?) {
        mParent = gearService
        Log.d(TAG, "Set Parent")
    }

    init {
        connectionID = ++nextID
        Log.d(TAG, "GearSAPServiceProviderConnection")
    }

    override fun onServiceConnectionLost(reason: Int) {
        if (mParent != null) {
            mParent!!.removeConnection(this)
        }
        Log.d(TAG, "Set OnServiceConnectionLost")
    }

    override fun onReceive(channelID: Int, data: ByteArray) {
        Log.d(TAG, "OnReceive")
    }

    override fun onError(channelID: Int, errorString: String, errorCode: Int) {
        Log.e(TAG, "ERROR:$errorString | $errorCode")
    }

    companion object {
        var nextID = 1
        const val TAG = "SAPServiceProvider"
    }
}
