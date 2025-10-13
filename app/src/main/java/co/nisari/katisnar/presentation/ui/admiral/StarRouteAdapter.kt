package co.nisari.katisnar.presentation.ui.admiral

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import co.nisari.katisnar.R
import java.time.format.DateTimeFormatter

class StarRouteAdapter(
    private val onCardClick: (StarRoute) -> Unit,
    private val onMoreClick: (StarRoute) -> Unit
) : ListAdapter<StarRoute, StarRouteAdapter.VH>(Diff) {

    object Diff : DiffUtil.ItemCallback<StarRoute>() {
        override fun areItemsTheSame(o: StarRoute, n: StarRoute) = o.id == n.id
        override fun areContentsTheSame(o: StarRoute, n: StarRoute) = o == n
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private val dateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    @RequiresApi(Build.VERSION_CODES.O)
    private val timeFmt = DateTimeFormatter.ofPattern("HH:mm")

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val name: TextView = itemView.findViewById(R.id.txt_city_location)
        private val date: TextView = itemView.findViewById(R.id.txt_date)
        private val time: TextView = itemView.findViewById(R.id.txt_time)
        private val more: ImageView = itemView.findViewById(R.id.btn_more_details)

        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(item: StarRoute) {
            name.text = item.name
            date.text = item.date.format(dateFmt) + ","
            time.text = item.time.format(timeFmt)

            itemView.setOnClickListener { onCardClick(item) }
            more.setOnClickListener { onMoreClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_star_route, parent, false)
        return VH(v)
    }
    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))
}
