package co.nisari.katisnar.presentation.ui.starnotes

import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import co.nisari.katisnar.databinding.ItemChecklistEditBinding

class ChecklistEditItemsAdapter(
    private val onCheckedChanged: (Long, Boolean) -> Unit,
    private val onTextChanged: (Long, String) -> Unit,
    private val onRemove: (Long) -> Unit
) : ListAdapter<ChecklistEditViewModel.EditableChecklistItem, ChecklistEditItemsAdapter.ViewHolder>(DiffCallback) {

    object DiffCallback : DiffUtil.ItemCallback<ChecklistEditViewModel.EditableChecklistItem>() {
        override fun areItemsTheSame(
            oldItem: ChecklistEditViewModel.EditableChecklistItem,
            newItem: ChecklistEditViewModel.EditableChecklistItem
        ): Boolean = oldItem.localId == newItem.localId

        override fun areContentsTheSame(
            oldItem: ChecklistEditViewModel.EditableChecklistItem,
            newItem: ChecklistEditViewModel.EditableChecklistItem
        ): Boolean = oldItem == newItem
    }

    inner class ViewHolder(private val binding: ItemChecklistEditBinding) : RecyclerView.ViewHolder(binding.root) {
        private var textWatcher: TextWatcher? = null

        fun bind(item: ChecklistEditViewModel.EditableChecklistItem) {
            binding.cbDone.setOnCheckedChangeListener(null)
            binding.cbDone.isChecked = item.isChecked
            binding.cbDone.setOnCheckedChangeListener { _, checked ->
                onCheckedChanged(item.localId, checked)
            }

            if (binding.etItem.text.toString() != item.text) {
                binding.etItem.setText(item.text)
                binding.etItem.setSelection(binding.etItem.text?.length ?: 0)
            }

            textWatcher?.let { binding.etItem.removeTextChangedListener(it) }
            textWatcher = binding.etItem.doAfterTextChanged { editable ->
                onTextChanged(item.localId, editable?.toString().orEmpty())
            }

            binding.root.setOnLongClickListener {
                onRemove(item.localId)
                true
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
