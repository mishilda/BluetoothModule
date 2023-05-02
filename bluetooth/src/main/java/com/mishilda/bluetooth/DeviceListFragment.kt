package com.mishilda.bluetooth

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.AndroidException
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.mishilda.bluetooth.databinding.FragmentListBinding


class DeviceListFragment : Fragment(), DeviceAdapter.Listener {
    private var preferences: SharedPreferences? = null
    private lateinit var deviceAdapter: DeviceAdapter
    private lateinit var discoveryAdapter: DeviceAdapter
    private var bAdapter: BluetoothAdapter? = null
    private lateinit var binding: FragmentListBinding
    private lateinit var btLauncher: ActivityResultLauncher<Intent>
    private lateinit var pLauncher: ActivityResultLauncher<Array<String>>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        preferences =
            activity?.getSharedPreferences(BluetoothConstants.PREFERENCES, Context.MODE_PRIVATE)
        binding.imBluetoothOn.setOnClickListener {
            btLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
        }

        intentFilters()
        checkPermissions()
        initRcViews()
        registerBtLauncher()
        initBtAdapter()
        bluetoothState()

        binding.imBluetoothSearch.setOnClickListener {
            try {
                Log.d("MyLog", "start")
                if (bAdapter?.isDiscovering == true) {
                    bAdapter?.cancelDiscovery()
                    Log.d("MyLog", "sr")
                }

                if (!bAdapter?.startDiscovery()!!)
                    Log.d("MyLog", "no work")
            } catch (_: SecurityException) {
            }
        }
    }

    private fun initRcViews() = with(binding) {
        rcViewPaired.layoutManager = LinearLayoutManager(requireContext())
        rcViewSearch.layoutManager = LinearLayoutManager(requireContext())
        deviceAdapter = DeviceAdapter(this@DeviceListFragment, false)
        discoveryAdapter = DeviceAdapter(this@DeviceListFragment, true)
        rcViewPaired.adapter = deviceAdapter
        rcViewSearch.adapter = discoveryAdapter
    }

    private fun getPairedDevices() {
        try {
            val listDevices = ArrayList<Device>()
            val deviceList = bAdapter?.bondedDevices as Set<BluetoothDevice>
            deviceList.forEach {
                listDevices.add(
                    Device(
                        it,
                        preferences?.getString(BluetoothConstants.MAC, "") == it.address
                    )
                )
            }
            binding.tvPairedEmpty.visibility =
                if (listDevices.isEmpty()) View.VISIBLE else View.GONE
            deviceAdapter.submitList(listDevices)
        } catch (e: SecurityException) {
        }
    }

    private fun initBtAdapter() {
        val bManager = activity?.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bAdapter = bManager.adapter
    }

    private fun bluetoothState() {
        if (bAdapter?.isEnabled == true) {
            changeButtonColor(binding.imBluetoothOn, Color.GREEN)
            getPairedDevices()
        }
    }

    private fun registerBtLauncher() {
        btLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == Activity.RESULT_OK) {
                changeButtonColor(binding.imBluetoothOn, Color.GREEN)
                getPairedDevices()
                Snackbar.make(binding.root, "Bluetooth is on", Snackbar.LENGTH_LONG).show()
            } else {
                Snackbar.make(binding.root, "Bluetooth is off", Snackbar.LENGTH_LONG).show()
                changeButtonColor(binding.imBluetoothOn, Color.RED)
            }
        }
    }

    private fun checkPermissions() {
        if (!checkBtPermission()) {
            registerPermissionListener()
            launchBtPermissions()
        }
    }

    private fun launchBtPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            pLauncher.launch(
                arrayOf(
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.BLUETOOTH_SCAN
                )
            )
        } else {
            pLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            )
        }
    }

    private fun registerPermissionListener() {
        pLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) {
        }
    }

    private fun saveMac(mac: String) {
        val editor = preferences?.edit()
        editor?.putString(BluetoothConstants.MAC, mac)
        editor?.apply()
    }

    override fun onClick(item: Device) {
        saveMac(item.device.address)
    }

    private val bReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device =
                        intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                    val list = mutableSetOf<Device>()
                    list.addAll(discoveryAdapter.currentList)
                    if (device != null) list.add(Device(device, false))
                    discoveryAdapter.submitList(list.toList())
                    binding.tvSearchEmpty.visibility =
                        if (list.isEmpty()) View.VISIBLE else View.GONE
                    try {
                        Log.d("MyLog", "Device: ${device?.name}")
                    } catch (e: SecurityException) {
                    }
                }
                BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                    getPairedDevices()
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                }
            }
        }

    }

    private fun intentFilters() {
        val f1 = IntentFilter(BluetoothDevice.ACTION_FOUND)
        val f2 = IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        val f3 = IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)

        activity?.registerReceiver(bReceiver, f1)
        activity?.registerReceiver(bReceiver, f2)
        activity?.registerReceiver(bReceiver, f3)
    }
}