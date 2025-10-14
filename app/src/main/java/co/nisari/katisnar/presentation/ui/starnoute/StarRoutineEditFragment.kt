package co.nisari.katisnar.presentation.ui.starnoute

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
import co.nisari.katisnar.R
import co.nisari.katisnar.databinding.FragmentStarNotesEditBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class StarRoutineEditFragment : Fragment() {

    private var _binding: FragmentStarNotesEditBinding? = null
    private val binding get() = _binding!!

    private val viewModel: StarRoutineEditViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStarNotesEditBinding.inflate(inflater, container, false)
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
            btnCancel.setOnClickListener { findNavController().popBackStack() }
            btnSave.setOnClickListener { onSaveClicked() }
            btnDelete.setOnClickListener { showDeleteDialog() }

            txtName.doAfterTextChanged { editable ->
                viewModel.onNameChanged(editable?.toString().orEmpty())
                if (!editable.isNullOrBlank()) {
                    txtName.error = null
                }
            }
            txtNotes.doAfterTextChanged { editable ->
                viewModel.onNotesChanged(editable?.toString().orEmpty())
                if (!editable.isNullOrBlank()) {
                    txtNotes.error = null
                }
            }
        }
    }

    private fun onSaveClicked() {
        val name = binding.txtName.text?.toString().orEmpty().trim()
        val notes = binding.txtNotes.text?.toString().orEmpty().trim()
        val fillAllFieldsMessage = getString(R.string.toast_fill_all_fields)

        var hasError = false

        if (name.isBlank()) {
            binding.txtName.error = getString(R.string.error_required_field)
            hasError = true
        }

        if (notes.isBlank()) {
            binding.txtNotes.error = getString(R.string.error_required_field)
            hasError = true
        }

        if (hasError) {
            Toast.makeText(requireContext(), fillAllFieldsMessage, Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.onSaveClicked(fillAllFieldsMessage)
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    binding.btnDelete.isVisible = state.isExisting

                    if (binding.txtName.text.toString() != state.name) {
                        binding.txtName.setText(state.name)
                        binding.txtName.setSelection(binding.txtName.text?.length ?: 0)
                    }

                    if (binding.txtNotes.text.toString() != state.notes) {
                        binding.txtNotes.setText(state.notes)
                        binding.txtNotes.setSelection(binding.txtNotes.text?.length ?: 0)
                    }
                }
            }
        }
    }

    private fun observeEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.collect { event ->
                    when (event) {
                        is StarRoutineEditViewModel.UiEvent.ShowToast ->
                            Toast.makeText(requireContext(), event.message, Toast.LENGTH_SHORT).show()
                        StarRoutineEditViewModel.UiEvent.CloseScreen ->
                            findNavController().popBackStack()
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
