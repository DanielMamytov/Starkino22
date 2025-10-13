package co.nisari.katisnar.presentation.ui.checklist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView
import co.nisari.katisnar.R
import co.nisari.katisnar.presentation.data.model.ChecklistItem

class ChecklistAdapter(
    private val items: MutableList<ChecklistItem>,
    private val onCheckedChange: (ChecklistItem) -> Unit
) : RecyclerView.Adapter<ChecklistAdapter.VH>() {

    inner class VH(val view: View) : RecyclerView.ViewHolder(view) {
        val cb: CheckBox = view.findViewById(R.id.cbItem)
        fun bind(item: ChecklistItem) {
            cb.text = item.text
            cb.isChecked = item.checked

            // снимаем старый listener, чтобы не дёргался при переиспользовании
            cb.setOnCheckedChangeListener(null)
            cb.isChecked = item.checked
            cb.setOnCheckedChangeListener { _, isChecked ->
                item.checked = isChecked
                onCheckedChange(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_checklist, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) =
        holder.bind(items[position])

    override fun getItemCount(): Int = items.size

    fun add(text: String) {
        val pos = items.size
        items.add(ChecklistItem(text = text))
        notifyItemInserted(pos)
    }

    fun set(itemsNew: List<ChecklistItem>) {
        items.clear()
        items.addAll(itemsNew)
        notifyDataSetChanged()
    }
}
