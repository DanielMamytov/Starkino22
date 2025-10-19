package co.nisari.katisnar.presentation.ui.starlocation

import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import co.nisari.katisnar.R
import co.nisari.katisnar.databinding.FragmentStarLocationDetailBinding
import co.nisari.katisnar.presentation.ui.starlocation.StarLocationDetailViewModel
import co.nisari.katisnar.presentation.ui.starlocation.UiEvent
import dagger.hilt.android.AndroidEntryPoint
import com.google.android.material.card.MaterialCardView
import co.nisari.katisnar.presentation.data.local.StarLocation
import eightbitlab.com.blurview.BlurTarget
import eightbitlab.com.blurview.BlurView
import eightbitlab.com.blurview.RenderScriptBlur
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class StarLocationDetailFragment : Fragment() {

    private var _binding: FragmentStarLocationDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: StarLocationDetailViewModel by viewModels()
    private var currentLocation: StarLocation? = null

    private val normalStrokeColor by lazy { Color.parseColor("#B8FFFFFF") }
    private val errorStrokeColor by lazy { Color.parseColor("#FF0000") }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStarLocationDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        val blurView = requireView().findViewById<BlurView>(R.id.blurView)
//        val decorView = requireActivity().window.decorView
//        val windowBackground: Drawable = decorView.background
//
//        blurView.setupWith(decorView.findViewById(android.R.id.content)) // размывает всё под ним
//            .setFrameClearDrawable(windowBackground)
//            .setBlurRadius(25f) // сила размытия
//
//        blurView.setOutlineProvider(ViewOutlineProvider.BACKGROUND)
//        blurView.setClipToOutline(true)




        val id = arguments?.getLong("id") ?: run {
            Toast.makeText(
                requireContext(),
                getString(R.string.toast_star_location_not_found),
                Toast.LENGTH_SHORT
            ).show()
            findNavController().popBackStack()
            return
        }
        viewModel.loadLocation(id)


        // Подписка на данные
        resetValidation()

        var hasLoadedLocation = false

        binding.root.findViewById<View>(R.id.btn_show_map).setOnClickListener {
            currentLocation?.let { loc ->
                viewModel.onShowOnMap(loc.lat, loc.lng, loc.location)
            }
        }

        binding.btnEdit.setOnClickListener {
            currentLocation?.let { viewModel.onEditClick(it.id) }
        }

        binding.btnDelete.setOnClickListener {
            currentLocation?.let { viewModel.onDeleteClick(it.id) }
        }

        binding.btnBack.setOnClickListener {
            viewModel.onBackClick()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.location
                    .collectLatest { loc ->
                        if (loc == null) {
                            if (!hasLoadedLocation) {
                                return@collectLatest
                            }

                            currentLocation = null

                            Toast.makeText(
                                requireContext(),
                                getString(R.string.toast_star_location_deleted),
                                Toast.LENGTH_SHORT
                            ).show()

                            val controller = findNavController()
                            if (controller.currentDestination?.id == R.id.starLocationDetailFragment) {
                                val popped = controller.popBackStack(R.id.starLocationFragment, false)
                                if (!popped) {
                                    controller.popBackStack()
                                }
                            }
                            return@collectLatest
                        }

                        hasLoadedLocation = true
                        currentLocation = loc

                        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

                        binding.txtName.text = loc.name
                        binding.txtLocation.text = loc.location
                        binding.txtDate.text = loc.date.format(dateFormatter)
                        binding.txtTime.text = loc.time.format(timeFormatter)
                        binding.txtLatitude.text = String.format("%.4f", loc.lat)
                        binding.txtLongitude.text = String.format("%.4f", loc.lng)
                        binding.txtWeather.text =
                            loc.weather.name.lowercase().replaceFirstChar { it.uppercase() }
                        binding.txtNotes.text = loc.notes

                        applyValidation(loc)
                    }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiEvent.collect { event ->
                    when (event) {
                    is UiEvent.NavigateBack -> findNavController().popBackStack()

                    is UiEvent.NavigateToEdit -> {
                        if (event.id != null) {
                            findNavController().navigate(
                                R.id.action_starLocationDetailFragment_to_starLocationEditFragment,
                                Bundle().apply {
                                    putLong(
                                        "id",
                                        event.id
                                    )
                                } // здесь уже Long, не Long?
                            )
                        } else {
                            // вариант на будущее: открыть создание без аргументов
                            findNavController().navigate(
                                R.id.action_starLocationDetailFragment_to_starLocationEditFragment
                            )
                        }
                    }

                    is UiEvent.ShowDeleteDialog -> showDeleteDialog(event.id)

                    is UiEvent.OpenMaps -> openMaps(event.lat, event.lng, event.name)

                    is UiEvent.ShowToast ->
                        Toast.makeText(requireContext(), event.message, Toast.LENGTH_SHORT).show()

                    is UiEvent.NavigateToDetail -> TODO()
                    is UiEvent.OpenMaps1 -> TODO()
                    is UiEvent.NavigateToList -> Unit
                    }
                }
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        currentLocation = null
        _binding = null
    }


    private fun showDeleteDialog(id: Long) {
        AlertDialog.Builder(requireContext())
            .setMessage(R.string.dialog_delete_title)
            .setPositiveButton(R.string.dialog_delete_confirm) { _, _ ->
                viewModel.confirmDelete(id)
            }
            .setNegativeButton(R.string.dialog_delete_cancel, null)
            .show()
    }

    private fun resetValidation() {
        listOf(
            binding.cardName,
            binding.cardLocation,
            binding.cardDatetime,
            binding.cardLatitude,
            binding.cardLongitude,
            binding.cardWeather,
            binding.cardNotes
        ).forEach { setCardError(it, false) }
    }

    private fun applyValidation(loc: StarLocation) {
        setCardError(binding.cardName, loc.name.trim().isEmpty())
        setCardError(binding.cardLocation, loc.location.trim().isEmpty())
        setCardError(binding.cardDatetime, loc.date == null || loc.time == null)
        setCardError(binding.cardLatitude, loc.lat !in -90.0..90.0)
        setCardError(binding.cardLongitude, loc.lng !in -180.0..180.0)
        setCardError(binding.cardWeather, loc.weather == null)
        setCardError(binding.cardNotes, loc.notes.trim().isEmpty())
    }

    private fun setCardError(card: MaterialCardView, error: Boolean) {
        card.strokeWidth = resources.getDimensionPixelSize(R.dimen.stroke_2dp)
        card.strokeColor = if (error) errorStrokeColor else normalStrokeColor
    }

    private fun openMaps(lat: Double, lng: Double, name: String) {
        try {
            val label = URLEncoder.encode(name, "UTF-8")
            val uri = Uri.parse("geo:$lat,$lng?q=$lat,$lng($label)")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(requireContext(), "Can’t start Google Maps app", Toast.LENGTH_SHORT).show()
        }
    }
}
