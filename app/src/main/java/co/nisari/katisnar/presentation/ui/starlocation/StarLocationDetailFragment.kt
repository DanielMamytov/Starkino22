package co.nisari.katisnar.presentation.ui.starlocation

import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import co.nisari.katisnar.R
import co.nisari.katisnar.databinding.FragmentStarLocationDetailBinding
import co.nisari.katisnar.presentation.ui.starlocation.StarLocationDetailViewModel
import co.nisari.katisnar.presentation.ui.starlocation.UiEvent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import java.net.URLEncoder
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class StarLocationDetailFragment : Fragment() {

    private lateinit var binding: FragmentStarLocationDetailBinding
    private val viewModel: StarLocationDetailViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStarLocationDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val id = arguments?.getLong("id") ?: run {
            Toast.makeText(requireContext(), "Item not found", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
            return
        }
        viewModel.loadLocation(id)


        // Подписка на данные
        lifecycleScope.launchWhenStarted {
            viewModel.location.collectLatest { loc ->
                if (loc == null) {
                    Toast.makeText(requireContext(), "Item not found", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                    return@collectLatest
                }

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

                // Кнопка показать на карте
                binding.root.findViewById<View>(R.id.btn_show_map).setOnClickListener {
                    viewModel.onShowOnMap(loc.lat, loc.lng, loc.location)
                }

                // Edit
                binding.btnEdit.setOnClickListener {
                    viewModel.onEditClick(loc.id)
                }

                // Delete
                binding.btnDelete.setOnClickListener {
                    viewModel.onDeleteClick(loc.id)
                }

                // Back
                binding.btnBack.setOnClickListener {
                    viewModel.onBackClick()
                }
            }
        }

        lifecycleScope.launchWhenStarted {
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
                }
            }
        }
    }


    private fun showDeleteDialog(id: Long) {
        AlertDialog.Builder(requireContext())
            .setMessage("Please confirm deletion")
            .setPositiveButton("Confirm") { _, _ -> viewModel.confirmDelete(id) }
            .setNegativeButton("Cancel", null)
            .show()
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
