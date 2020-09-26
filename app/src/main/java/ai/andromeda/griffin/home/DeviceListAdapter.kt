package ai.andromeda.griffin.home

import ai.andromeda.griffin.R
import ai.andromeda.griffin.database.DeviceEntity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.device_list_item.view.*

class DeviceListAdapter :
    ListAdapter<DeviceEntity, DeviceListAdapter.DeviceViewHolder>(MyDiffUtil()){

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

        fun bind(item: DeviceEntity) {
            rootView.deviceNameText.text = item.deviceName
            rootView.numSensorsText.text = item.numSensors.toString()
            rootView.numUnlockedText.text =
                (item.numSensors?.minus(item.lockedSensors))
                .toString() + " OPEN"
            rootView.numLockedText.text = item.lockedSensors.toString() + " LOCKED"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            DeviceViewHolder {
        return DeviceViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }
}