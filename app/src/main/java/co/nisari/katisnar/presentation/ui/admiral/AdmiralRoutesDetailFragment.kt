package co.nisari.katisnar.presentation.ui.admiral

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import android.net.Uri
import co.nisari.katisnar.R
import co.nisari.katisnar.databinding.FragmentAdmiralRoutesDetailBinding
import co.nisari.katisnar.presentation.ui.starlocation.UiEvent
import dagger.hilt.android.AndroidEntryPoint
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull

@AndroidEntryPoint
class AdmiralRoutesDetailFragment : Fragment() {

    private lateinit var binding: FragmentAdmiralRoutesDetailBinding
    private val vm: StarRouteDetailViewModel by viewModels()
    private val pointsAdapter = AdmiralRoutePointsAdapter()
    private var navigatingAfterDelete = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentAdmiralRoutesDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private val dateFmt = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")

    @RequiresApi(Build.VERSION_CODES.O)
    private val timeFmt = java.time.format.DateTimeFormatter.ofPattern("HH:mm")

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val routeId = requireArguments().getLong("id", -1L)
        if (routeId == -1L) {
            Toast.makeText(requireContext(), "Item not found", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
            return
        }
        vm.load(routeId)

        binding.btnBack.setOnClickListener { vm.onBack() }
        binding.btnEdit.setOnClickListener { vm.onEdit() }
        binding.btnDelete.setOnClickListener { vm.onDelete() }
        binding.btnShowMap.setOnClickListener { vm.onShowOnMaps() }

        binding.rvPoints.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPoints.adapter = pointsAdapter
        binding.txtTime.apply {
            isFocusable = false
            isFocusableInTouchMode = false
            isClickable = false
            isCursorVisible = false
            isLongClickable = false
            keyListener = null
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            vm.state
                .filterNotNull()
                .collectLatest { data ->
                    if (data == null) {
                        if (navigatingAfterDelete || vm.isRouteDeleted()) {
                            navigatingAfterDelete = true
                            return@collectLatest
                        }
                        Toast.makeText(requireContext(), "Item not found", Toast.LENGTH_SHORT)
                            .show()
                        findNavController().popBackStack()
                        return@collectLatest
                    }
                    val route = data.route
                    binding.txtName.text = route.name
                    binding.txtDate.text = route.date.format(dateFmt)
                    val timeFormatted = route.time.format(timeFmt)
                    if (binding.txtTime.text?.toString() != timeFormatted) {
                        binding.txtTime.setText(timeFormatted)
                    }
                    binding.txtDescription.text = route.description

                    val points = data.points
                    pointsAdapter.submit(points)
                    binding.rvPoints.isVisible = points.isNotEmpty()
                }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            vm.ui.collectLatest { event ->
                when (event) {
                    is UiEvent.NavigateBack -> findNavController().popBackStack()
                    is UiEvent.NavigateToEdit -> {
                        val args = if (event.id != null) {
                            bundleOf("id" to event.id)
                        } else null
                        findNavController().navigate(
                            R.id.action_admiralRouteDetailFragment_to_admiralRouteEditFragment,
                            args
                        )
                    }
                    is UiEvent.ShowToast -> Toast.makeText(
                        requireContext(),
                        event.message,
                        Toast.LENGTH_SHORT
                    ).show()
                    is UiEvent.ShowDeleteDialog -> showDeleteDialog(event.id)
                    is UiEvent.OpenMaps1 -> openMaps(event.uri)
                    else -> Unit
                }
            }
        }
    }

    private fun showDeleteDialog(routeId: Long) {
        MaterialAlertDialogBuilder(requireContext())
            .setMessage(getString(R.string.dialog_delete_title))
            .setPositiveButton(R.string.dialog_delete_confirm) { _, _ ->
                vm.confirmDelete(routeId)
            }
            .setNegativeButton(R.string.dialog_delete_cancel, null)
            .show()
    }

    private fun openMaps(uri: Uri) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(requireContext(), R.string.error_no_maps_app, Toast.LENGTH_SHORT)
                .show()
        }
    }
}