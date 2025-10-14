package co.nisari.katisnar.presentation.ui.starnoute

import android.os.Bundle
import android.graphics.Color
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
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class StarRoutineEditFragment : Fragment() {

    private var _binding: FragmentStarNotesEditBinding? = null
    private val binding get() = _binding!!

    private val viewModel: StarRoutineEditViewModel by viewModels()

    private var validationActivated = false
    private val normalStrokeColor by lazy { Color.parseColor("#B8FFFFFF") }
    private val errorStrokeColor by lazy { Color.parseColor("#FF0000") }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStarNotesEditBinding.inflate(inflater, container, false)
        validationActivated = false
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
                markNameIfFilled()
            }
            txtNotes.doAfterTextChanged { editable ->
                viewModel.onNotesChanged(editable?.toString().orEmpty())
                markNotesIfFilled()
            }
        }

        setNameError(false)
        setNotesError(false)
    }

    private fun onSaveClicked() {
        if (!validationActivated) validationActivated = true
        val name = binding.txtName.text?.toString().orEmpty().trim()
        val notes = binding.txtNotes.text?.toString().orEmpty().trim()
        val fillAllFieldsMessage = getString(R.string.toast_fill_all_fields)

        val nameEmpty = name.isBlank()
        val notesEmpty = notes.isBlank()

        setNameError(nameEmpty)
        setNotesError(notesEmpty)

        if (nameEmpty || notesEmpty) {
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

                    syncValidation(state)
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

    private fun syncValidation(state: StarRoutineEditViewModel.UiState) {
        if (!validationActivated) return
        setNameError(state.name.trim().isEmpty())
        setNotesError(state.notes.trim().isEmpty())
    }

    private fun markNameIfFilled() {
        if (!validationActivated) return
        if (!binding.txtName.text?.toString()?.trim().isNullOrEmpty()) setNameError(false)
    }

    private fun markNotesIfFilled() {
        if (!validationActivated) return
        if (!binding.txtNotes.text?.toString()?.trim().isNullOrEmpty()) setNotesError(false)
    }

    private fun setNameError(error: Boolean) {
        setCardStroke(binding.cardName, error)
    }

    private fun setNotesError(error: Boolean) {
        setCardStroke(binding.cardNotes, error)
    }

    private fun setCardStroke(card: MaterialCardView, error: Boolean) {
        card.strokeWidth = resources.getDimensionPixelSize(R.dimen.stroke_2dp)
        card.strokeColor = if (error) errorStrokeColor else normalStrokeColor
    }
}
