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


@AndroidEntryPoint
class AdmiralRoutesDetailFragment : Fragment() {

    private lateinit var binding: FragmentAdmiralRoutesDetailBinding
    private val vm: StarRouteDetailViewModel by viewModels()
    private val pointsAdapter = AdmiralRoutePointsAdapter()
    private var navigatingAfterDelete = false
    private var initialRouteLoaded = false

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
            vm.state
                .collectLatest { data ->
                    if (data == null) {
                        if (!initialRouteLoaded) {
                            return@collectLatest
                        }
                        if (navigatingAfterDelete || vm.isRouteDeleted()) {
                            navigatingAfterDelete = true
                            return@collectLatest
                        }
                        Toast.makeText(requireContext(), "Item not found", Toast.LENGTH_SHORT).show()
                        findNavController().popBackStack()
                        return@collectLatest
                    }
                    initialRouteLoaded = true
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
//                    val countText = resources.getQuantityString(
//                        R.plurals.points_count,
//                        points.size,
//                        points.size
//                    )
//                    binding.txtPointsTitle.text = countText
//
//                    val latText = if (points.isEmpty()) "-" else points.first().lat.toString()
//                    val lngText = if (points.isEmpty()) "-" else points.first().lng.toString()
//                    binding.txtLatitude.text = latText
//                    binding.txtLongitude.text = lngText
//                    binding.txtLocation2.text = when {
//                        points.isEmpty() -> getString(R.string.route_detail_location_empty)
//                        points.size == 1 -> getString(
//                            R.string.route_detail_location_single,
//                            points.first().lat,
//                            points.first().lng
//                        )
//                        else -> getString(
//                            R.string.route_detail_location_multi,
//                            points.first().lat,
//                            points.first().lng,
//                            points.last().lat,
//                            points.last().lng,
//                            points.size
//                        )
//                    }
//                val r = data.route
//                binding.txtName.text = r.name
//                binding.txtDate.text = r.date.format(dateFmt)
//                binding.txtTime.setText(r.time.format(timeFmt))
//                binding.txtDescription.text = r.description

                // блок с точками:
                // в твоём макете есть фиксированный “1 Point” + txt_latitude/txt_longitude.
                // Покажем количество точек и первую точку (как минимум).
//                val count = data.points.size
                // Найти TextView с текстом “1 Point” у тебя без id — лучше дай ему id:
                // android:id="@+id/txt_points_title"
                // Тогда:
                // binding.txtPointsTitle.text = if (count == 1) "1 Point" else "$count Points"

//                if (count > 0) {
//                    val p0 = data.points.first()
//                    binding.txtLatitude.text = p0.lat.toString()
//                    binding.txtLongitude.text = p0.lng.toString()
//                } else {
//                    binding.txtLatitude.text = ""
//                    binding.txtLongitude.text = ""
//                }
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
                    is UiEvent.NavigateBack -> {
                        if (vm.isRouteDeleted()) {
                            navigatingAfterDelete = true
                            findNavController().popBackStack(R.id.starRouteFragment, false)
                            vm.onRouteDeletionHandled()
                        } else {
                            findNavController().popBackStack()
                        }
                    }
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

//    private fun formatCoordinate(value: Double): String = String.format(Locale.US, "%1$.4f", value)
}

