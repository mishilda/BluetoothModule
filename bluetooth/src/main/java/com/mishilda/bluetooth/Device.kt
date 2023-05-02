package com.mishilda.bluetooth

import android.bluetooth.BluetoothDevice

data class Device(
    val device: BluetoothDevice,
    val isChecked: Boolean,
)
