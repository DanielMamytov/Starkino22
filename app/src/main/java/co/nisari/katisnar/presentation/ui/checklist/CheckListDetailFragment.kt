package co.nisari.katisnar.presentation.ui.checklist

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
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import co.nisari.katisnar.R
import co.nisari.katisnar.databinding.FragmentCheckListDetailBinding
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CheckListDetailFragment : Fragment() {

    private var _binding: FragmentCheckListDetailBinding? = null
    private val binding get() = _binding!!

    private val args: CheckListDetailFragmentArgs by navArgs()
    private val viewModel: CheckListDetailViewModel by viewModels()

    private val normalStrokeColor by lazy { Color.parseColor("#B8FFFFFF") }
    private val errorStrokeColor by lazy { Color.parseColor("#FF0000") }

    private val adapter by lazy {
        ChecklistDetailItemsAdapter { id, checked -> viewModel.onItemChecked(id, checked) }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCheckListDetailBinding.inflate(inflater, container, false)
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
            btnEdit.setOnClickListener {
                val argsBundle = bundleOf("checklistId" to args.checklistId)
                findNavController().navigate(
                    R.id.action_checkListDetailFragment_to_starChecklistEditFragment,
                    argsBundle
                )
            }
            btnDelete.setOnClickListener { showDeleteDialog() }

            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            recyclerView.adapter = adapter
            recyclerView.itemAnimator = null
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    binding.txtName.text = state.title
                    adapter.submitList(state.items)
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
                        is CheckListDetailViewModel.UiEvent.CloseScreen -> {
                            if (event.showToast) {
                                Toast.makeText(
                                    requireContext(),
                                    R.string.checklist_deleted_toast,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            findNavController().popBackStack()
                        }
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

    private fun resetValidation() {
        setCardStroke(binding.cardName, false)
        setCardStroke(binding.cardGoals, false)
    }

    private fun applyValidation(state: CheckListDetailViewModel.UiState) {
        setCardStroke(binding.cardName, state.title.trim().isEmpty())
        val hasItems = state.items.any { it.text.trim().isNotEmpty() }
        setCardStroke(binding.cardGoals, !hasItems)
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
