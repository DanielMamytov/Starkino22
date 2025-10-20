package co.nisari.katisnar.presentation.ui.admiral

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import android.graphics.Typeface
import android.view.Gravity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import co.nisari.katisnar.R
import co.nisari.katisnar.databinding.FragmentStarRoutineBinding
import co.nisari.katisnar.presentation.ui.starlocation.UiEvent
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StarRouteFragment : Fragment() {

    private lateinit var binding: FragmentStarRoutineBinding
    private val vm: StarRouteListViewModel by viewModels()

    private val adapter by lazy {
        StarRouteAdapter(
            onCardClick = { vm.onItemClick(it.id) },
            onMoreClick = { vm.onMoreDetailsClick(it.id) },
            onLongClick = { vm.onItemLongClick(it.id) }
        )
    }

    private var emptyView: TextView? = null

    private fun showEmpty(show: Boolean) {
        val parent = binding.root as ViewGroup

        if (show) {
            if (emptyView == null) {
                emptyView = TextView(requireContext()).apply {
                    id = View.generateViewId()
                    text = "No routes yet. Add your first route"
                    setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
                    textSize = 16f
                    setTypeface(typeface, Typeface.BOLD)
                    gravity = Gravity.CENTER
                    setPadding(24, 24, 24, 24)
                }

                ensureHasId(parent)

                val lp: ViewGroup.LayoutParams = when (parent) {
                    is ConstraintLayout -> {
                        ConstraintLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        ).apply {
                            topToTop = parent.id
                            bottomToBottom = parent.id
                            startToStart = parent.id
                            endToEnd = parent.id
                        }
                    }
                    else -> {
                        ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    }
                }

                parent.addView(emptyView, lp)
            }
            emptyView?.visibility = View.VISIBLE
        } else {
            emptyView?.visibility = View.GONE
        }
    }

    private fun ensureHasId(vg: ViewGroup) {
        if (vg.id == View.NO_ID) {
            vg.id = View.generateViewId()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        emptyView?.let { view ->
            (view.parent as? ViewGroup)?.removeView(view)
        }
        emptyView = null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentStarRoutineBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvLocations.adapter = adapter

        binding.btnAddRoute.setOnClickListener { vm.onAddRouteClick() }
        binding.btnBack.setOnClickListener { vm.onBack() }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            vm.routes.collect { list ->
                adapter.submitList(list)
                showEmpty(list.isEmpty())
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            vm.ui.collect { e ->
                when (e) {
                    is UiEvent.NavigateBack -> findNavController().popBackStack()
                    is UiEvent.NavigateToDetail -> {
                        findNavController().navigate(
                            R.id.action_starRouteFragment_to_admiralRouteDetailFragment,
                            bundleOf("id" to e.id)
                        )
                    }
                    is UiEvent.NavigateToEdit -> {
                        if (e.id == null) {
                            findNavController().navigate(R.id.action_starRouteFragment_to_admiralRouteEditFragment)
                        } else {
                            findNavController().navigate(
                                R.id.action_starRouteFragment_to_admiralRouteEditFragment,
                                bundleOf("id" to e.id)
                            )
                        }
                    }
                    is UiEvent.ShowToast -> Toast.makeText(requireContext(), e.message, Toast.LENGTH_SHORT).show()
                    is UiEvent.ShowDeleteDialog -> showDeleteDialog(e.id)
                    else -> Unit
                }
            }
        }
    }

    private fun showDeleteDialog(routeId: Long) {
        MaterialAlertDialogBuilder(requireContext())
            .setMessage(getString(R.string.dialog_delete_title))
            .setPositiveButton(R.string.dialog_delete_confirm) { _, _ ->
                vm.onDeleteConfirmed(routeId)
            }
            .setNegativeButton(R.string.dialog_delete_cancel, null)
            .show()
    }
}
