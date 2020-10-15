package ai.andromeda.griffin.home

import ai.andromeda.griffin.R
import ai.andromeda.griffin.database.DeviceEntity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.device_list_item.view.*

class DeviceAdapter(private val clickListener: (device: DeviceEntity) -> Unit) :
    RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder>() {

    //--------------- DEVICE LIST -----------------//
    var deviceList = listOf<DeviceEntity>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    //------------------- VIEW HOLDER ---------------------//
    class DeviceViewHolder private constructor(view: View) :
        RecyclerView.ViewHolder(view) {

        private val deviceNameText: TextView = view.deviceNameText
        private val numSensorsText: TextView = view.numSensorsText
        private val numLockedText: TextView = view.numLockedText
        private val numUnlockedText: TextView = view.numUnlockedText
        private val res = view.context.resources

        //---------------- INSTANTIATE ----------------//
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

        //----------------- BIND ------------------//
        fun bind(item: DeviceEntity) {
            deviceNameText.text = item.deviceName
            numSensorsText.text = item.numSensors.toString()
            numUnlockedText.text = res.getString(
                R.string.open, item.numSensors.minus(item.lockedSensors)
            )
            numLockedText.text = res.getString(R.string.locked, item.lockedSensors)
        }
    }

    //-------------- ON CREATE VIEW HOLDER ---------------//
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        return DeviceViewHolder.from(parent)
    }

    //------------------ ON BIND VIEW HOLDER ----------------//
    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        val item = deviceList[position]
        holder.bind(item)
        holder.itemView.setOnClickListener {
            clickListener(item)
        }
    }

    //------------- ITEM COUNT -----------//
    override fun getItemCount() = deviceList.size
}