package com.mishilda.bluetooth

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mishilda.bluetooth.databinding.ListItemDeviceBinding

class DeviceAdapter(private val listener: Listener, val adapterType: Boolean):
    ListAdapter<Device, DeviceAdapter.DeviceHolder>(Comparator()) {
    private var oldCheckBox: CheckBox? = null

    class DeviceHolder(
        view: View, private val adapter:DeviceAdapter,
        private val listener: Listener,
        val adapterType: Boolean
        ): RecyclerView.ViewHolder(view) {
        private val binding = ListItemDeviceBinding.bind(view)
        private var item1: Device? = null
        init {
            binding.checkBox.setOnClickListener {
                item1?.let { it1 -> listener.onClick(it1) }
                adapter.selectCheckBox(it as CheckBox)
            }
            itemView.setOnClickListener {
                if (adapterType) {
                    try {
                        item1?.device?.createBond()
                    } catch (e:SecurityException) {}
                } else {
                    item1?.let { it1 -> listener.onClick(it1) }
                    adapter.selectCheckBox(binding.checkBox)
                }
            }
        }
        fun bind(item: Device) = with(binding) {
            checkBox.visibility = if (adapterType) View.GONE else View.VISIBLE
            item1 = item
            try {
                tvName.text = item.device.name
                tvMac.text = item.device.address
            }  catch (e:SecurityException) {}
            if (item.isChecked)
                adapter.selectCheckBox(checkBox)
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
        return DeviceHolder(view, this, listener, adapterType)
    }

    override fun onBindViewHolder(holder: DeviceHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun selectCheckBox(checkBox: CheckBox) {
        oldCheckBox?.isChecked = false
        oldCheckBox = checkBox
        oldCheckBox?.isChecked = true
    }

    interface Listener {
        fun onClick(device: Device)
    }
}