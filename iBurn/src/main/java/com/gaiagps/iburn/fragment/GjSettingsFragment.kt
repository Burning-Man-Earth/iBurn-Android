package com.gaiagps.iburn.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.gaiagps.iburn.R
import com.gj.animalauto.CarManager
import com.gj.animalauto.OscHostManager
import com.gj.animalauto.PrefsHelper
import timber.log.Timber

/**
 * Created by dbro on 7/23/17.
 */
public class GjSettingsFragment : Fragment() {

    val oscHostItem: View  by lazy {
        val view: View = view!!.findViewById(R.id.paired_osc_host_item)
        view
    }

    val oscHostItemValue: TextView by lazy {
        val view: TextView = view!!.findViewById(R.id.primary_osc_hostname)
        view
    }

    val btItem: View by lazy {
        val view: View = view!!.findViewById(R.id.paired_bt_device_item)
        view
    }

    val btItemValue: TextView by lazy {
        val view: TextView = view!!.findViewById(R.id.primary_bt_mac_addr)
        view
    }

    val carManager by lazy {
        CarManager(context.applicationContext)
    }

    val oscManager by lazy {
        OscHostManager(context.applicationContext)
    }

    val gjPrefs by lazy {
        PrefsHelper(context.applicationContext)
    }


    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        inflater?.let { inflater ->
            val view = inflater.inflate(R.layout.fragment_gj_settings, container, false)

            return view
        }

        return null
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {

        updateItemValueViews()

        oscHostItem.setOnClickListener {
            discoverOscHosts()
        }

        btItem.setOnClickListener {
            discoverBtDevices()
        }
    }

    override fun onStop() {
        super.onStop()

        carManager.stopDiscovery()
        oscManager.stopDiscovery()
    }

    private fun updateItemValueViews() {
        oscHostItemValue.text = gjPrefs.getPrimaryOscHostname()
        btItemValue.text = gjPrefs.getPrimaryCarBtMac()
    }

    private fun discoverBtDevices() {
        carManager.startDiscovery(activity, { selectedBtCar ->

            Timber.d("User selected car %s. Saving as primary and connecting...", selectedBtCar)
            carManager.setPrimaryBtCar(selectedBtCar)
            updateItemValueViews()
        })
    }

    private fun discoverOscHosts() {
        oscManager.startDiscoveryOfNewHost(activity, { selectedHost ->
            oscManager.setPrimaryOscHost(selectedHost.hostname)
            updateItemValueViews()
        })
    }
}