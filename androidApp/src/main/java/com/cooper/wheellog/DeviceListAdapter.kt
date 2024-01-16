package com.cooper.wheellog

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.cooper.wheellog.app.WheelLog

// Adapter for holding devices found through scanning.
class DeviceListAdapter(appCompatActivity: AppCompatActivity) : BaseAdapter() {
    private val mLeDevices: ArrayList<BluetoothDevice>
    private val mLeAdvDatas: ArrayList<String>
    private val mInflator: LayoutInflater

    internal class ViewHolder {
        var deviceName: TextView? = null
        var deviceAddress: TextView? = null
    }

    init {
        mLeDevices = ArrayList()
        mLeAdvDatas = ArrayList()
        mInflator = appCompatActivity.layoutInflater
    }

    fun addDevice(device: BluetoothDevice, advData: String) {
        if (!WheelLog.AppConfig.showUnknownDevices) {
            @SuppressLint("MissingPermission") val deviceName = device.getName()
            if (deviceName == null || deviceName.length == 0) return
        }
        if (!mLeDevices.contains(device)) {
            mLeDevices.add(device)
            mLeAdvDatas.add(advData)
        }
    }

    fun getDevice(position: Int): BluetoothDevice {
        return mLeDevices[position]
    }

    fun getAdvData(position: Int): String {
        return mLeAdvDatas[position]
    }

    //    public void clear() {
    //        mLeDevices.clear();
    //    }
    override fun getCount(): Int {
        return mLeDevices.size
    }

    override fun getItem(i: Int): Any {
        return mLeDevices[i]
    }

    override fun getItemId(i: Int): Long {
        return i.toLong()
    }

    override fun getView(i: Int, view: View, viewGroup: ViewGroup): View {
        var view = view
        val viewHolder: ViewHolder
        // General ListView optimization code.
        if (view == null) {
            view = mInflator.inflate(R.layout.scan_list_item, null)
            viewHolder = ViewHolder()
            viewHolder.deviceAddress = view.findViewById(R.id.device_address)
            viewHolder.deviceName = view.findViewById(R.id.device_name)
            view.tag = viewHolder
        } else {
            viewHolder = view.tag as ViewHolder
        }
        val device = mLeDevices[i]
        @SuppressLint("MissingPermission") val deviceName = device.getName()
        if (deviceName != null && deviceName.length > 0) viewHolder.deviceName!!.text =
            deviceName else viewHolder.deviceName!!.setText(R.string.unknown_device)
        viewHolder.deviceAddress!!.text = device.getAddress()
        return view
    }
}