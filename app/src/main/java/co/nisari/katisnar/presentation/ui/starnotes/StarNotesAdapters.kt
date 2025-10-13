package co.nisari.katisnar.presentation.ui.starnotes

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import co.nisari.katisnar.databinding.ItemStarNotesBinding
import co.nisari.katisnar.databinding.ItemStarNotesChecklistBinding

class NotesAdapter(
    private val onClick: (NoteListItem) -> Unit
) : ListAdapter<NoteListItem, NotesAdapter.ViewHolder>(DiffCallback) {

    object DiffCallback : DiffUtil.ItemCallback<NoteListItem>() {
        override fun areItemsTheSame(oldItem: NoteListItem, newItem: NoteListItem): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: NoteListItem, newItem: NoteListItem): Boolean =
            oldItem == newItem
    }

    inner class ViewHolder(private val binding: ItemStarNotesBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: NoteListItem) {
            with(binding) {
                txtCityLocation.text = item.title
                txtDate.text = item.date
                txtTime.text = item.time
                root.setOnClickListener { onClick(item) }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemStarNotesBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class StarChecklistsAdapter(
    private val onClick: (ChecklistListItem) -> Unit
) : ListAdapter<ChecklistListItem, StarChecklistsAdapter.ViewHolder>(DiffCallback) {

    object DiffCallback : DiffUtil.ItemCallback<ChecklistListItem>() {
        override fun areItemsTheSame(oldItem: ChecklistListItem, newItem: ChecklistListItem): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: ChecklistListItem, newItem: ChecklistListItem): Boolean =
            oldItem == newItem
    }

    inner class ViewHolder(private val binding: ItemStarNotesChecklistBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ChecklistListItem) {
            with(binding) {
                txtCityLocation.text = item.title
                txtDate.text = item.summary
                txtTime.text = item.secondary
                root.setOnClickListener { onClick(item) }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemStarNotesChecklistBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
