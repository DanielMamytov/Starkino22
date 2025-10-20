package co.nisari.katisnar.presentation.ui.checklist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import co.nisari.katisnar.databinding.ItemChecklistEditBinding

class ChecklistDetailItemsAdapter(
    private val onCheckedChange: (Long, Boolean) -> Unit
) : ListAdapter<CheckListDetailViewModel.ChecklistItemUi, ChecklistDetailItemsAdapter.ViewHolder>(DiffCallback) {

    object DiffCallback : DiffUtil.ItemCallback<CheckListDetailViewModel.ChecklistItemUi>() {
        override fun areItemsTheSame(
            oldItem: CheckListDetailViewModel.ChecklistItemUi,
            newItem: CheckListDetailViewModel.ChecklistItemUi
        ): Boolean = oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: CheckListDetailViewModel.ChecklistItemUi,
            newItem: CheckListDetailViewModel.ChecklistItemUi
        ): Boolean = oldItem == newItem
    }

    inner class ViewHolder(private val binding: ItemChecklistEditBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CheckListDetailViewModel.ChecklistItemUi) {
            with(binding) {
                cbDone.setOnCheckedChangeListener(null)
                cbDone.isChecked = item.isChecked
                cbDone.setOnCheckedChangeListener { _, checked ->
                    onCheckedChange(item.id, checked)
                }

                if (etItem.text.toString() != item.text) {
                    etItem.setText(item.text)
                }
                etItem.isFocusable = false
                etItem.isFocusableInTouchMode = false
                etItem.isCursorVisible = false
                etItem.keyListener = null
                etItem.isLongClickable = false

            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemChecklistEditBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
