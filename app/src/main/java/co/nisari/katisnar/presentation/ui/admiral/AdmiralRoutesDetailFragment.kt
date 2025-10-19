package co.nisari.katisnar.presentation.ui.admiral

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import co.nisari.katisnar.databinding.FragmentAdmiralRoutesDetailBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop


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
                .drop(1)
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
    }
}