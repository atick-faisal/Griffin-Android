package ai.andromeda.griffin.device

import ai.andromeda.griffin.R
import ai.andromeda.griffin.database.SensorModel
import android.app.Application
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.sensor_list_item.view.*

class SensorAdapter(
    private val application: Application,
    private val clickListener: SensorClickListener
) :
    ListAdapter<SensorModel, SensorAdapter.SensorViewHolder>(SensorDiffUtil()){

    class SensorViewHolder private constructor(private val rootView: View) :
        RecyclerView.ViewHolder(rootView) {

        companion object {
            fun from(parent: ViewGroup): SensorViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val rootView = layoutInflater.inflate(
                    R.layout.sensor_list_item,
                    parent,
                    false
                )
                return SensorViewHolder(rootView)
            }
        }

        fun bind(application: Application, item: SensorModel) {
            rootView.sensorNameText.text = item.sensorName
            if (item.sensorStatus == 0) {
                rootView.sensorStatusImage.setImageResource(R.drawable.red_circle)
            }
            else {
                rootView.sensorStatusImage.setImageResource(R.drawable.red_circle)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            SensorViewHolder {
        return SensorViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: SensorViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(application, item)
        holder.itemView.setOnClickListener {
            clickListener.onClick(item)
        }
    }
}

class SensorDiffUtil : DiffUtil.ItemCallback<SensorModel>() {
    override fun areItemsTheSame(
        oldItem: SensorModel,
        newItem: SensorModel
    ): Boolean {
        return oldItem.sensorName == newItem.sensorName
    }

    override fun areContentsTheSame(
        oldItem: SensorModel,
        newItem: SensorModel
    ): Boolean {
        return oldItem == newItem
    }
}

class SensorClickListener(val clickListener: (sensor: SensorModel) -> Unit) {
    fun onClick(sensor: SensorModel) = clickListener(sensor)
}