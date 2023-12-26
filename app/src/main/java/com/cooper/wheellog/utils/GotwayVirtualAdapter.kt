package com.cooper.wheellog.utils

import com.cooper.wheellog.WheelData
import com.cooper.wheellog.utils.gotway.GotwayAdapter
import com.cooper.wheellog.utils.veteran.VeteranAdapter
import timber.log.Timber

class GotwayVirtualAdapter : BaseAdapter() {
    override fun decode(data: ByteArray?): Boolean {
        Timber.i("Begode_Gotway_detect")
        val wd = WheelData.getInstance()
        val result: Boolean
        result =
            if (data!![0] == 0xDC.toByte() && data[1] == 0x5A.toByte() && data[2] == 0x5C.toByte() && data[3].toInt() and 0xF0 == 0x20.toByte()
                    .toInt()
            ) {
                wd.setWheelType(Constants.WHEEL_TYPE.VETERAN)
                wd.setModel("Veteran")
                VeteranAdapter.instance!!.decode(data)
            } else if (data[0] == 0x55.toByte() && data[1] == 0xAA.toByte()) {
                wd.setWheelType(Constants.WHEEL_TYPE.GOTWAY)
                wd.setModel("Begode")
                GotwayAdapter.instance.decode(data)
            } else return false
        return result
    }

    companion object {
        private var INSTANCE: GotwayVirtualAdapter? = null
        @JvmStatic
        val instance: GotwayVirtualAdapter?
            get() {
                if (INSTANCE == null) {
                    INSTANCE = GotwayVirtualAdapter()
                }
                return INSTANCE
            }
    }
}