package co.nisari.katisnar.presentation.ui.admiral

import android.content.Intent
import android.net.Uri
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
import co.nisari.katisnar.R
import co.nisari.katisnar.databinding.FragmentAdmiralRoutesDetailBinding
import co.nisari.katisnar.presentation.ui.starlocation.UiEvent
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import java.util.Locale


@AndroidEntryPoint
class AdmiralRoutesDetailFragment : Fragment() {

    private lateinit var binding: FragmentAdmiralRoutesDetailBinding
    private val vm: StarRouteDetailViewModel by viewModels()
    private val pointsAdapter = AdmiralRoutePointsAdapter()

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

        // 1) аргумент id (через SafeArgs или обычный Bundle)
        val routeId = requireArguments().getLong("id", -1L)
        if (routeId == -1L) {
            Toast.makeText(requireContext(), "Item not found", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
            return
        }
        vm.load(routeId)

        // 2) кнопки
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

        // 3) подписка на данные
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            vm.state.collectLatest { data ->
                if (data == null) {
                    pointsAdapter.submit(emptyList())
                    binding.rvPoints.isVisible = false
                    binding.cardPointSummary.isVisible = false
                    binding.txtPointsTitle.text = resources.getQuantityString(
                        R.plurals.points_count,
                        0,
                        0
                    )
                    binding.txtLatitude.text = "-"
                    binding.txtLongitude.text = "-"
                    binding.txtLocation2.text = getString(R.string.route_detail_location_empty)
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
                val countText = resources.getQuantityString(
                    R.plurals.points_count,
                    points.size,
                    points.size
                )
                binding.txtPointsTitle.text = countText

                if (points.isEmpty()) {
                    binding.txtLatitude.text = "-"
                    binding.txtLongitude.text = "-"
                    binding.txtLocation2.text = getString(R.string.route_detail_location_empty)
                    binding.cardPointSummary.isVisible = false
                    binding.rvPoints.isVisible = false
                    pointsAdapter.submit(emptyList())
                } else {
                    val firstPoint = points.first()
                    val lastPoint = points.last()
                    binding.txtLatitude.text = formatCoordinate(firstPoint.lat)
                    binding.txtLongitude.text = formatCoordinate(firstPoint.lng)
                    binding.txtLocation2.text = if (points.size == 1) {
                        getString(
                            R.string.route_detail_location_single,
                            firstPoint.lat,
                            firstPoint.lng
                        )
                    } else {
                        getString(
                            R.string.route_detail_location_multi,
                            firstPoint.lat,
                            firstPoint.lng,
                            lastPoint.lat,
                            lastPoint.lng,
                            points.size
                        )
                    }
                    binding.cardPointSummary.isVisible = true
                    binding.rvPoints.isVisible = true
                    pointsAdapter.submit(points, startIndex = 1)
                }
            }
        }

        // 4) UI-события
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            vm.ui.collect { e ->
                when (e) {
                    is UiEvent.OpenMaps1 -> {
                        try {
                            // Сначала попробуем открыть приложение Google Maps
                            startActivity(Intent(Intent.ACTION_VIEW, e.uri).apply {
                                setPackage("com.google.android.apps.maps")
                            })
                        } catch (appMissing: Exception) {
                            try {
                                // Если приложение отсутствует — откроем любым браузером
                                startActivity(Intent(Intent.ACTION_VIEW, e.uri))
                            } catch (ex: Exception) {
                                Toast.makeText(requireContext(), "Can’t start Google Maps app", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    is UiEvent.NavigateBack -> findNavController().popBackStack()
                    is UiEvent.OpenMaps -> {
                        val geoUri = Uri.parse(
                            "geo:${e.lat},${e.lng}?q=${e.lat},${e.lng}(${Uri.encode(e.name)})"
                        )
                        try {
                            startActivity(Intent(Intent.ACTION_VIEW, geoUri))
                        } catch (ex: Exception) {
                            Toast.makeText(requireContext(), "Can’t start Google Maps app", Toast.LENGTH_SHORT).show()
                        }
                    }
                    is UiEvent.NavigateToEdit -> {
                        val args = e.id?.let { bundleOf("id" to it) } ?: bundleOf()
                        findNavController().navigate(
                            R.id.action_admiralRouteDetailFragment_to_admiralRouteEditFragment,
                            args
                        )
                    }
                    is UiEvent.ShowDeleteDialog -> {
                        MaterialAlertDialogBuilder(requireContext())
                            .setMessage(R.string.dialog_delete_title)
                            .setPositiveButton(R.string.dialog_delete_confirm) { _, _ ->
                                vm.confirmDelete(e.id)
                            }
                            .setNegativeButton(R.string.dialog_delete_cancel, null)
                            .show()
                    }
                    is UiEvent.ShowToast -> Toast.makeText(requireContext(), e.message, Toast.LENGTH_SHORT).show()
                    else -> Unit
                }
            }
        }

    }

    private fun formatCoordinate(value: Double): String = String.format(Locale.US, "%1$.4f", value)
}

