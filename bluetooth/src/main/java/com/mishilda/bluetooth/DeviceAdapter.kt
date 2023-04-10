package com.mishilda.bluetooth

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mishilda.bluetooth.databinding.ListItemDeviceBinding

class DeviceAdapter: ListAdapter<Device, DeviceAdapter.DeviceHolder>(Comparator()) {
    class DeviceHolder(view: View): RecyclerView.ViewHolder(view) {
        private val binding = ListItemDeviceBinding.bind(view)
        fun bind(device: Device) = with(binding) {
            tvName.text = device.name
            tvMac.text = device.mac
        }
    }

    class Comparator : DiffUtil.ItemCallback<Device>() {
        override fun areItemsTheSame(oldItem: Device, newItem: Device): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Device, newItem: Device): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_device, parent, false)
        return DeviceHolder(view)
    }

    override fun onBindViewHolder(holder: DeviceHolder, position: Int) {
        holder.bind(getItem(position))
    }
}