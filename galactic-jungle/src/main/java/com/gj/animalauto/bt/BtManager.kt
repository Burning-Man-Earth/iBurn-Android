package com.gj.animalauto.bt

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import timber.log.Timber


/**
 * Created by dbro on 7/14/17.
 */
public class BtManager(val context: Context) {

    val btAdapter = BluetoothAdapter.getDefaultAdapter()

    private var registeredReceiver = false
    private val discoveryReceiver: BtDiscoveryReceiver by lazy {
        BtDiscoveryReceiver()
    }

    var discoveryCallback: ((BluetoothDevice) -> Unit)? = null

    fun startDiscovery(callback: (BluetoothDevice) -> Unit) {

        if (btAdapter == null) {
            Timber.e("BT Adapter not available. Cannot start discovery!")
            return
        }

        discoveryCallback = callback

        btAdapter.startDiscovery()

        if (!registeredReceiver) {
            val intentFilter = IntentFilter()
            intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
            intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
            intentFilter.addAction(BluetoothDevice.ACTION_FOUND)
            context.registerReceiver(discoveryReceiver, intentFilter)
            registeredReceiver = true
        }
    }

    fun stopDiscovery() {
        if (registeredReceiver) {
            context.unregisterReceiver(discoveryReceiver)
            registeredReceiver = false
        }
    }

    fun getDeviceWithMac(macAddress: String): BluetoothDevice {
        return btAdapter.getRemoteDevice(macAddress)
    }

    private inner class BtDiscoveryReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {

            intent?.let { intent ->

                val action = intent.action

                when (action) {
                    BluetoothAdapter.ACTION_DISCOVERY_STARTED -> Timber.d("BT Discovery started")
                    BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> Timber.d("BT Discovery finished")
                    BluetoothDevice.ACTION_FOUND -> {
                        val device: BluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                        Timber.d("Found device %s", device.name)
                        discoveryCallback?.invoke(device)
                    }
                    else -> Timber.w("Received unknown action: $action")
                }
            }
        }
    }
}