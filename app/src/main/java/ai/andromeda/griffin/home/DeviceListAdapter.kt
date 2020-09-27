package ai.andromeda.griffin.home

import ai.andromeda.griffin.R
import ai.andromeda.griffin.database.DeviceEntity
import android.app.Application
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.device_list_item.view.*

class DeviceListAdapter(
    private val application: Application,
    private val clickListener: DeviceClickListener
) :
    ListAdapter<DeviceEntity, DeviceListAdapter.DeviceViewHolder>(MyDiffUtil()){

    class DeviceViewHolder private constructor(private val rootView: View) :
        RecyclerView.ViewHolder(rootView) {

        companion object {
            fun from(parent: ViewGroup): DeviceViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val rootView = layoutInflater.inflate(
                    R.layout.device_list_item,
                    parent,
                    false
                )
                return DeviceViewHolder(rootView)
            }
        }

        fun bind(application: Application, item: DeviceEntity) {
            rootView.deviceNameText.text = item.deviceName
            rootView.numSensorsText.text = item.numSensors.toString()
            rootView.numUnlockedText.text = application.resources.getString(
                R.string.open, item.numSensors.minus(item.lockedSensors)
            )
            rootView.numLockedText.text = application.resources.getString(
                R.string.locked, item.lockedSensors
            )
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            DeviceViewHolder {
        return DeviceViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(application, item)
        holder.itemView.setOnClickListener {
            clickListener.onClick(item)
        }
    }
}

class MyDiffUtil : DiffUtil.ItemCallback<DeviceEntity>() {
    override fun areItemsTheSame(
        oldItem: DeviceEntity,
        newItem: DeviceEntity
    ): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
        oldItem: DeviceEntity,
        newItem: DeviceEntity
    ): Boolean {
        return oldItem == newItem
    }
}

class DeviceClickListener(val clickListener: (deviceId: String) -> Unit) {
    fun onClick(device: DeviceEntity) = clickListener(device.deviceId?: "M")
}