package co.nisari.katisnar.presentation.ui.admiral

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import co.nisari.katisnar.R

class AdmiralRoutePointsAdapter : RecyclerView.Adapter<AdmiralRoutePointsAdapter.PointViewHolder>() {

    private var items: List<RoutePoint> = emptyList()
    private var startIndex: Int = 1

    fun submit(points: List<RoutePoint>, startIndex: Int = 1) {
        this.items = points
        this.startIndex = startIndex
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PointViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_point, parent, false)
        return PointViewHolder(view)
    }

    override fun onBindViewHolder(holder: PointViewHolder, position: Int) {
        val point = items[position]
        val displayIndex = startIndex + position
        holder.bind(point, displayIndex)
    }

    override fun getItemCount(): Int = items.size

    inner class PointViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.tv_point_title)
        private val latitude: EditText = itemView.findViewById(R.id.et_latitude1)
        private val longitude: EditText = itemView.findViewById(R.id.et_longitude1)
        private val location: EditText = itemView.findViewById(R.id.et_location1)

        init {
            disableEditing(latitude)
            disableEditing(longitude)
            disableEditing(location)
        }

        fun bind(point: RoutePoint, index: Int) {
            title.text = itemView.context.getString(R.string.point_title_placeholder, index)
            setTextIfDifferent(latitude, point.lat.toString())
            setTextIfDifferent(longitude, point.lng.toString())
            setTextIfDifferent(location, point.location)
        }

        private fun setTextIfDifferent(editText: EditText, value: String) {
            if (editText.text.toString() != value) {
                editText.setText(value)
            }
        }

        private fun disableEditing(editText: EditText) {
            editText.isFocusable = false
            editText.isFocusableInTouchMode = false
            editText.isClickable = false
            editText.isLongClickable = false
            editText.isCursorVisible = false
            editText.keyListener = null
        }
    }
}
