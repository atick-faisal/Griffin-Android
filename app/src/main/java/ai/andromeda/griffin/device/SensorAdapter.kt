package ai.andromeda.griffin.device

import ai.andromeda.griffin.R
import ai.andromeda.griffin.database.SensorModel
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.sensor_list_item.view.*

class SensorAdapter(private val clickListener: (view: Int, position: Int) -> Unit) :
    RecyclerView.Adapter<SensorAdapter.SensorViewHolder>() {

    var sensorList = listOf<SensorModel>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    class SensorViewHolder private constructor(view: View) :
        RecyclerView.ViewHolder(view) {

        private val sensorNameText: TextView = view.sensorNameText
        private val sensorStatusText: TextView = view.sensorStatusText
        private val sensorStatusBackground: ImageView = view.sensorStatusBackground
        private val sensorStatusImage: ImageView = view.sensorStatusImage
        private val res = view.context.resources
        private val context = view.context

        companion object {
            fun from(parent: ViewGroup): SensorViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater.inflate(
                    R.layout.sensor_list_item,
                    parent,
                    false
                )
                return SensorViewHolder(view)
            }
        }

        fun bind(item: SensorModel) {
            sensorNameText.text = item.sensorName
            when(item.sensorStatus) {
                0 -> {
                    sensorStatusText.text = res.getString(R.string.locked_status)
                    sensorStatusText.background = ContextCompat.getDrawable(
                        context, R.drawable.green_pill
                    )
                    sensorStatusBackground.setImageResource(R.drawable.green_circle)
                    sensorStatusImage.setImageResource(R.drawable.ic_lock)
                }
                1 -> {
                    sensorStatusText.text = res.getString(R.string.unlocked_status)
                    sensorStatusText.background = ContextCompat.getDrawable(
                        context, R.drawable.red_pill
                    )
                    sensorStatusBackground.setImageResource(R.drawable.red_circle)
                    sensorStatusImage.setImageResource(R.drawable.ic_unlock)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            SensorViewHolder {
        return SensorViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: SensorViewHolder, position: Int) {
        val item = sensorList[position]
        holder.bind(item)
        holder.itemView.sensorStatusImage.setOnClickListener {
            clickListener(0, position)
        }
        holder.itemView.nameEditButton.setOnClickListener {
            clickListener(1, position)
        }
    }

    override fun getItemCount() = sensorList.size
}