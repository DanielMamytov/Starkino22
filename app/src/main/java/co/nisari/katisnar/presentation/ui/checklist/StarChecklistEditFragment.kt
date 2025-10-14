package co.nisari.katisnar.presentation.ui.checklist

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
import androidx.recyclerview.widget.LinearLayoutManager
import co.nisari.katisnar.R
import co.nisari.katisnar.databinding.FragmentChecklistEditBinding
import co.nisari.katisnar.presentation.ui.starnotes.ChecklistEditItemsAdapter
import co.nisari.katisnar.presentation.ui.starnotes.ChecklistEditViewModel
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class StarChecklistEditFragment : Fragment() {

    private var _binding: FragmentChecklistEditBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ChecklistEditViewModel by viewModels()

    private var validationActivated = false
    private val normalStrokeColor by lazy { Color.parseColor("#B8FFFFFF") }
    private val errorStrokeColor by lazy { Color.parseColor("#FF0000") }

    private val adapter by lazy {
        ChecklistEditItemsAdapter(
            onCheckedChanged = { id, checked -> viewModel.onItemChecked(id, checked) },
            onTextChanged = { id, text -> viewModel.onItemTextChanged(id, text) },
            onRemove = { id -> viewModel.onItemRemoved(id) }
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChecklistEditBinding.inflate(inflater, container, false)
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
            btnAddGoal.setOnClickListener { viewModel.onAddItem() }

            txtName.doAfterTextChanged { text ->
                viewModel.onTitleChanged(text?.toString().orEmpty())
                markNameIfFilled()
            }

            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            recyclerView.adapter = adapter
            recyclerView.itemAnimator = null
        }

        setNameError(false)
        setGoalsError(false)
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    binding.btnDelete.isVisible = state.isExisting
                    if (binding.txtName.text.toString() != state.title) {
                        binding.txtName.setText(state.title)
                        binding.txtName.setSelection(binding.txtName.text?.length ?: 0)
                    }
                    adapter.submitList(state.items)
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

    private fun onSaveClicked() {
        if (!validationActivated) validationActivated = true

        val state = viewModel.state.value
        val titleEmpty = state.title.trim().isEmpty()
        val hasItems = state.items.any { it.text.trim().isNotEmpty() }

        setNameError(titleEmpty)
        setGoalsError(!hasItems)

        when {
            titleEmpty -> Toast.makeText(requireContext(), R.string.toast_enter_checklist_name, Toast.LENGTH_SHORT).show()
            !hasItems -> Toast.makeText(requireContext(), R.string.toast_add_checklist_item, Toast.LENGTH_SHORT).show()
            else -> viewModel.onSaveClicked(
                getString(R.string.toast_enter_checklist_name),
                getString(R.string.toast_add_checklist_item)
            )
        }
    }

    private fun syncValidation(state: ChecklistEditViewModel.UiState) {
        if (!validationActivated) return
        val titleEmpty = state.title.trim().isEmpty()
        val hasItems = state.items.any { it.text.trim().isNotEmpty() }
        setNameError(titleEmpty)
        setGoalsError(!hasItems)
    }

    private fun markNameIfFilled() {
        if (!validationActivated) return
        if (!binding.txtName.text?.toString()?.trim().isNullOrEmpty()) setNameError(false)
    }

    private fun setNameError(error: Boolean) {
        setCardStroke(binding.cardName, error)
    }

    private fun setGoalsError(error: Boolean) {
        setCardStroke(binding.cardGoals, error)
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
