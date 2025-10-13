package co.nisari.katisnar.presentation.ui.starnotes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import co.nisari.katisnar.R
import co.nisari.katisnar.databinding.FragmentChecklistEditBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ChecklistEditFragment : Fragment() {

    private var _binding: FragmentChecklistEditBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ChecklistEditViewModel by viewModels()

    private val adapter by lazy {
        ChecklistEditItemsAdapter(
            onCheckedChanged = { id, checked -> viewModel.onItemChecked(id, checked) },
            onTextChanged = { id, text -> viewModel.onItemTextChanged(id, text) },
            onRemove = { id -> viewModel.onItemRemoved(id) }
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChecklistEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUi()
        observeState()
        observeEvents()
    }

    private fun setupUi() {
        with(binding) {
            btnBack.setOnClickListener { findNavController().popBackStack() }
            btnSave.setOnClickListener {
                viewModel.onSaveClicked(
                    getString(R.string.toast_enter_checklist_name),
                    getString(R.string.toast_add_checklist_item)
                )
            }
            btnDelete.setOnClickListener { showDeleteDialog() }
            btnAddItem.setOnClickListener { viewModel.onAddItem() }
            etTitle.doAfterTextChanged { text ->
                viewModel.onTitleChanged(text?.toString().orEmpty())
            }

            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            recyclerView.adapter = adapter
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    binding.btnDelete.isVisible = state.isExisting
                    if (binding.etTitle.text.toString() != state.title) {
                        binding.etTitle.setText(state.title)
                        binding.etTitle.setSelection(binding.etTitle.text?.length ?: 0)
                    }
                    adapter.submitList(state.items)
                    binding.emptyChecklistHint.isVisible = state.items.isEmpty()
                }
            }
        }
    }

    private fun observeEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.collect { event ->
                    when (event) {
                        is ChecklistEditViewModel.UiEvent.ShowToast ->
                            Toast.makeText(requireContext(), event.message, Toast.LENGTH_SHORT).show()
                        ChecklistEditViewModel.UiEvent.CloseScreen -> findNavController().popBackStack()
                    }
                }
            }
        }
    }

    private fun showDeleteDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.dialog_delete_title)
            .setPositiveButton(R.string.dialog_delete_confirm) { _, _ ->
                viewModel.onDeleteConfirmed()
            }
            .setNegativeButton(R.string.dialog_delete_cancel, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
