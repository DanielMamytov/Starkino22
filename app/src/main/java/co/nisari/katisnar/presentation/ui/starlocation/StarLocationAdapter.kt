package co.nisari.katisnar.presentation.ui.starlocation

import android.annotation.SuppressLint
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import co.nisari.katisnar.R
import co.nisari.katisnar.presentation.data.local.StarLocation
import java.time.format.DateTimeFormatter

class StarLocationAdapter(
    private val onItemClick: (StarLocation) -> Unit
) : ListAdapter<StarLocation, StarLocationAdapter.LocationViewHolder>(DiffCallback) {

    object DiffCallback : DiffUtil.ItemCallback<StarLocation>() {
        override fun areItemsTheSame(oldItem: StarLocation, newItem: StarLocation): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: StarLocation, newItem: StarLocation): Boolean =
            oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_star_location, parent, false)
        return LocationViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class LocationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtName: TextView = itemView.findViewById(R.id.txt_city_location)
        private val txtDate: TextView = itemView.findViewById(R.id.txt_date)
        private val txtTime: TextView = itemView.findViewById(R.id.txt_time)
        private val txtLat: TextView = itemView.findViewById(R.id.txt_lat)
        private val txtLon: TextView = itemView.findViewById(R.id.txt_lon)

        @RequiresApi(Build.VERSION_CODES.O)
        @SuppressLint("SetTextI18n")
        fun bind(item: StarLocation) {
            txtName.text = item.name

            val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

            txtDate.text = item.date.format(dateFormatter) + ","
            txtTime.text = item.time.format(timeFormatter)

            txtLat.text = String.format("%.4f", item.lat)
            txtLon.text = String.format("%.4f", item.lng)

            // Клик по всей карточке
            itemView.setOnClickListener { onItemClick(item) }
        }
    }
}
