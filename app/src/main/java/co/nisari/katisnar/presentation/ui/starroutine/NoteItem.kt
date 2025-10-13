package co.nisari.katisnar.presentation.ui.starroutine

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import co.nisari.katisnar.R

data class NoteItem(
    val id: Long,
    val title: String,
    val date: String,
    val time: String
)

class NotesAdapter(
    private val onClick: (NoteItem) -> Unit = {}
) : RecyclerView.Adapter<NotesAdapter.VH>() {

    private val items = mutableListOf<NoteItem>()

    fun submit(list: List<NoteItem>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        private val txtTitle: TextView = view.findViewById(R.id.txt_city_location)
        private val txtDate: TextView = view.findViewById(R.id.txt_date)
        private val txtTime: TextView = view.findViewById(R.id.txt_time)
//        private val img: ImageView = view.findViewById(R.id.imageView) // если добавишь id в макет

        fun bind(item: NoteItem) {
            txtTitle.text = item.title
            txtDate.text = item.date
            txtTime.text = item.time
            itemView.setOnClickListener { onClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_star_notes, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(items[position])

    override fun getItemCount(): Int = items.size
}
