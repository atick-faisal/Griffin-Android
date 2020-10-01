package ai.andromeda.griffin.home

import ai.andromeda.griffin.R
import ai.andromeda.griffin.database.DeviceEntity
import ai.andromeda.griffin.database.SensorModel
import android.app.Application
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.device_list_item.view.*
import kotlinx.android.synthetic.main.sensor_list_item.view.*

class DeviceAdapter(private val clickListener: (device: DeviceEntity) -> Unit) :
    RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder>() {

    var deviceList = listOf<DeviceEntity>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    class DeviceViewHolder private constructor(view: View) :
        RecyclerView.ViewHolder(view) {

        private val deviceNameText: TextView = view.deviceNameText
        private val numSensorsText: TextView = view.numSensorsText
        private val numLockedText: TextView = view.numLockedText
        private val numUnlockedText: TextView = view.numUnlockedText
        private val res = view.context.resources

        companion object {
            fun from(parent: ViewGroup): DeviceViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater.inflate(
                    R.layout.device_list_item,
                    parent,
                    false
                )
                return DeviceViewHolder(view)
            }
        }

        fun bind(item: DeviceEntity) {
            deviceNameText.text = item.deviceName
            numSensorsText.text = item.numSensors.toString()
            numUnlockedText.text = res.getString(
                R.string.open, item.numSensors.minus(item.lockedSensors)
            )
            numLockedText.text = res.getString(R.string.locked, item.lockedSensors)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        return DeviceViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        val item = deviceList[position]
        holder.bind(item)
        holder.itemView.setOnClickListener {
            clickListener(item)
        }
    }

    override fun getItemCount() = deviceList.size
}

//class DeviceListAdapter(
//    private val application: Application,
//    private val clickListener: DeviceClickListener
//) :
//    ListAdapter<DeviceEntity, DeviceListAdapter.DeviceViewHolder>(MyDiffUtil()){
//
//    class DeviceViewHolder private constructor(private val rootView: View) :
//        RecyclerView.ViewHolder(rootView) {
//
//        companion object {
//            fun from(parent: ViewGroup): DeviceViewHolder {
//                val layoutInflater = LayoutInflater.from(parent.context)
//                val rootView = layoutInflater.inflate(
//                    R.layout.device_list_item,
//                    parent,
//                    false
//                )
//                return DeviceViewHolder(rootView)
//            }
//        }
//
//        fun bind(application: Application, item: DeviceEntity) {
//            rootView.deviceNameText.text = item.deviceName
//            rootView.numSensorsText.text = item.numSensors.toString()
//            rootView.numUnlockedText.text = application.resources.getString(
//                R.string.open, item.numSensors.minus(item.lockedSensors)
//            )
//            rootView.numLockedText.text = application.resources.getString(
//                R.string.locked, item.lockedSensors
//            )
//        }
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
//            DeviceViewHolder {
//        return DeviceViewHolder.from(parent)
//    }
//
//    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
//        val item = getItem(position)
//        holder.bind(application, item)
//        holder.itemView.setOnClickListener {
//            clickListener.onClick(item)
//        }
//    }
//}
//
//class MyDiffUtil : DiffUtil.ItemCallback<DeviceEntity>() {
//    override fun areItemsTheSame(
//        oldItem: DeviceEntity,
//        newItem: DeviceEntity
//    ): Boolean {
//        return oldItem.id == newItem.id
//    }
//
//    override fun areContentsTheSame(
//        oldItem: DeviceEntity,
//        newItem: DeviceEntity
//    ): Boolean {
//        return oldItem == newItem
//    }
//}
//
//class DeviceClickListener(val clickListener: (device: DeviceEntity) -> Unit) {
//    fun onClick(device: DeviceEntity) = clickListener(device)
//}