package co.nisari.katisnar.presentation.ui.starnotes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import co.nisari.katisnar.R
import co.nisari.katisnar.databinding.FragmentStarNoteBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class StarNotesFragment : Fragment() {

    private var _binding: FragmentStarNoteBinding? = null
    private val binding get() = _binding!!

    private val viewModel: StarNotesViewModel by viewModels()

    private val notesAdapter by lazy { NotesAdapter { viewModel.onNoteClicked(it.id) } }
    private val checklistsAdapter by lazy { StarChecklistsAdapter { viewModel.onChecklistClicked(it.id) } }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStarNoteBinding.inflate(inflater, container, false)
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
            btnNotes.setOnClickListener {
                if (!btnNotes.isChecked) {
                    btnNotes.isChecked = true
                    return@setOnClickListener
                }
                viewModel.onTabSelected(StarNotesViewModel.Tab.NOTES)
            }
            btnChecklist.setOnClickListener {
                if (!btnChecklist.isChecked) {
                    btnChecklist.isChecked = true
                    return@setOnClickListener
                }
                viewModel.onTabSelected(StarNotesViewModel.Tab.CHECKLIST)
            }
            btnAddGoal.setOnClickListener { viewModel.onAddClicked() }
            btnBack.setOnClickListener { findNavController().popBackStack() }

            rvLocations.itemAnimator = null
            rvLocations.adapter = notesAdapter
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    val isNotesTab = state.activeTab == StarNotesViewModel.Tab.NOTES
                    binding.btnNotes.isChecked = isNotesTab
                    binding.btnChecklist.isChecked = !isNotesTab

                    val targetAdapter: RecyclerView.Adapter<*> = if (isNotesTab) notesAdapter else checklistsAdapter
                    if (binding.rvLocations.adapter !== targetAdapter) {
                        binding.rvLocations.adapter = targetAdapter
                    }

                    notesAdapter.submitList(state.notes)
                    checklistsAdapter.submitList(state.checklists)

                    binding.btnAddGoal.text = getString(
                        if (isNotesTab) R.string.add_note else R.string.add_checklist
                    )

                    binding.txtEmptyState.isVisible = if (isNotesTab) state.isNotesEmpty else state.isChecklistEmpty
                    binding.txtEmptyState.text = getString(
                        if (isNotesTab) R.string.notes_empty_state else R.string.checklists_empty_state
                    )
                }
            }
        }
    }

    private fun observeEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.collect { event ->
                    when (event) {
                        is StarNotesViewModel.UiEvent.NavigateToNoteCreator -> navigateToNoteCreator()
                        is StarNotesViewModel.UiEvent.NavigateToNoteDetail -> navigateToNoteDetail(event.noteId)
                        is StarNotesViewModel.UiEvent.NavigateToChecklistDetail ->
                            navigateToChecklistDetail(event.checklistId)
                        is StarNotesViewModel.UiEvent.NavigateToChecklistEditor ->
                            navigateToChecklistEditor(event.checklistId)
                    }
                }
            }
        }
    }

    private fun navigateToNoteCreator() {
        findNavController().navigate(R.id.action_starNoteFragment_to_starRoutineEditFragment)
    }

    private fun navigateToNoteDetail(noteId: Long) {
        val actionId = R.id.action_starNoteFragment_to_starRoutineDetailFragment
        val args = Bundle().apply { putLong("noteId", noteId) }
        findNavController().navigate(actionId, args)
    }

    private fun navigateToChecklistDetail(checklistId: Long) {
        val actionId = R.id.action_starNoteFragment_to_checkListDetailFragment
        val args = Bundle().apply { putLong("checklistId", checklistId) }
        findNavController().navigate(actionId, args)
    }

    private fun navigateToChecklistEditor(checklistId: Long?) {
        val actionId = R.id.action_starNoteFragment_to_starChecklistEditFragment
        val args = if (checklistId != null) Bundle().apply { putLong("checklistId", checklistId) } else null
        findNavController().navigate(actionId, args)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
