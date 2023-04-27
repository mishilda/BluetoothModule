package com.mishilda.bluetooth

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.mishilda.bluetooth.databinding.FragmentListBinding


class DeviceListFragment : Fragment() {
    private lateinit var deviceAdapter: DeviceAdapter
    private var bAdapter: BluetoothAdapter? = null
    private lateinit var binding: FragmentListBinding
    private lateinit var btLauncher: ActivityResultLauncher<Intent>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.imBluetoothOn.setOnClickListener {
            btLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
        }
        initRcViews()

        registerBtLauncher()
        initBtAdapter()
        bluetoothState()
    }

    private fun initRcViews() = with(binding) {
        rcViewPaired.layoutManager = LinearLayoutManager(requireContext())
        deviceAdapter = DeviceAdapter()
        rcViewPaired.adapter = deviceAdapter
    }

    private fun getPairedDevices() {
        try {
            val listDevices = ArrayList<Device>()
            val deviceList = bAdapter?.bondedDevices as Set<BluetoothDevice>
            deviceList.forEach {
                listDevices.add(
                    Device(it.name, it.address)
                )
            }
            binding.tvPairedEmpty.visibility =
                if (listDevices.isEmpty()) View.VISIBLE else View.GONE
            deviceAdapter.submitList(listDevices)
        } catch (e: SecurityException) {}
    }

    private fun initBtAdapter(){
        val bManager = activity?.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bAdapter = bManager.adapter
    }

    private fun bluetoothState(){
        if (bAdapter?.isEnabled == true)
            changeButtonColor(binding.imBluetoothOn, Color.GREEN)
            getPairedDevices()
    }

    private fun registerBtLauncher() {
        btLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ){
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
}