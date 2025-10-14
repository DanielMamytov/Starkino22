package co.nisari.katisnar.presentation.ui.starnoute

import android.os.Bundle
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import co.nisari.katisnar.R
import co.nisari.katisnar.databinding.FragmentStarNotesDetailBinding
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class StarRoutineDetailFragment : Fragment() {

    private var _binding: FragmentStarNotesDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: StarRoutineDetailViewModel by viewModels()

    private var currentNoteId: Long? = null

    private val normalStrokeColor by lazy { Color.parseColor("#B8FFFFFF") }
    private val errorStrokeColor by lazy { Color.parseColor("#FF0000") }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStarNotesDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        resetValidation()
        setupUi()
        observeState()
        observeEvents()
    }

    private fun setupUi() {
        with(binding) {
            btnBack.setOnClickListener { findNavController().popBackStack() }
            btnDelete.setOnClickListener { showDeleteDialog() }
            btnEdit.setOnClickListener { navigateToNoteEditor() }
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    currentNoteId = state.id
                    binding.txtName.text = state.name
                    binding.txtNotes.text = state.notes
                    applyValidation(state)
                }
            }
        }
    }

    private fun observeEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.collect { event ->
                    when (event) {
                        StarRoutineDetailViewModel.UiEvent.NoteNotFound ->
                            Toast.makeText(
                                requireContext(),
                                R.string.toast_note_not_found,
                                Toast.LENGTH_SHORT
                            ).show()
                        StarRoutineDetailViewModel.UiEvent.CloseScreen ->
                            findNavController().popBackStack()
                    }
                }
            }
        }
    }

    private fun navigateToNoteEditor() {
        val noteId = currentNoteId ?: return
        val args = bundleOf("noteId" to noteId)
        findNavController().navigate(R.id.action_starRoutineDetailFragment_to_noteEditFragment, args)
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

    private fun resetValidation() {
        setCardStroke(binding.cardName, false)
        setCardStroke(binding.cardNotes, false)
    }

    private fun applyValidation(state: StarRoutineDetailViewModel.UiState) {
        setCardStroke(binding.cardName, state.name.trim().isEmpty())
        setCardStroke(binding.cardNotes, state.notes.trim().isEmpty())
    }

    private fun setCardStroke(card: MaterialCardView, error: Boolean) {
        card.strokeWidth = resources.getDimensionPixelSize(R.dimen.stroke_2dp)
        card.strokeColor = if (error) errorStrokeColor else normalStrokeColor
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
