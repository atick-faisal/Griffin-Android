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
                    sensorStatusImage.setImageResource(R.drawable.green_circle)
                }
                1 -> {
                    sensorStatusText.text = res.getString(R.string.unlocked_status)
                    sensorStatusText.background = ContextCompat.getDrawable(
                        context, R.drawable.red_pill
                    )
                    sensorStatusImage.setImageResource(R.drawable.red_circle)
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

//class SensorAdapter(
//    private val application: Application,
//    private val clickListener: SensorClickListener
//) :
//    ListAdapter<SensorModel, SensorAdapter.SensorViewHolder>(SensorDiffUtil()){
//
//    class SensorViewHolder private constructor(private val rootView: View) :
//        RecyclerView.ViewHolder(rootView) {
//
//        companion object {
//            fun from(parent: ViewGroup): SensorViewHolder {
//                val layoutInflater = LayoutInflater.from(parent.context)
//                val rootView = layoutInflater.inflate(
//                    R.layout.sensor_list_item,
//                    parent,
//                    false
//                )
//                return SensorViewHolder(rootView)
//            }
//        }
//
//        fun bind(application: Application, item: SensorModel) {
//            rootView.sensorNameText.text = item.sensorName
//            if (item.sensorStatus == 0) {
//                rootView.sensorStatusImage.setImageResource(R.drawable.red_circle)
//            }
//            else {
//                rootView.sensorStatusImage.setImageResource(R.drawable.green_circle)
//            }
//        }
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
//            SensorViewHolder {
//        return SensorViewHolder.from(parent)
//    }
//
//    override fun onBindViewHolder(holder: SensorViewHolder, position: Int) {
//        val item = getItem(position)
//        holder.bind(application, item)
//        holder.itemView.setOnClickListener {
//            clickListener.onClick(position)
//        }
//    }
//}
//
//class SensorDiffUtil : DiffUtil.ItemCallback<SensorModel>() {
//    override fun areItemsTheSame(
//        oldItem: SensorModel,
//        newItem: SensorModel
//    ): Boolean {
//        return oldItem.sensorStatus == newItem.sensorStatus
//    }
//
//    override fun areContentsTheSame(
//        oldItem: SensorModel,
//        newItem: SensorModel
//    ): Boolean {
//        return oldItem == newItem
//    }
//}
//
//class SensorClickListener(val clickListener: (position: Int) -> Unit) {
//    fun onClick(position: Int) = clickListener(position)
//}