package co.nisari.katisnar.presentation.ui.admiral

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import co.nisari.katisnar.R
import co.nisari.katisnar.databinding.FragmentAdmiralRouteEditBinding
import co.nisari.katisnar.databinding.FragmentAdmiralRoutesDetailBinding
import co.nisari.katisnar.presentation.ui.starlocation.UiEvent
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class AdmiralRoutesDetailFragment : Fragment() {

    private lateinit var binding: FragmentAdmiralRoutesDetailBinding
    private val vm: StarRouteDetailViewModel by viewModels()

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
        val id = requireArguments().getLong("id", -1L)
        if (id == -1L) {
            Toast.makeText(requireContext(), "Item not found", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
            return
        }
        vm.load(id)

        // 2) кнопки
        binding.btnBack.setOnClickListener { vm.onBack() }
        binding.btnEdit.setOnClickListener { vm.onEdit() }
        binding.btnDelete.setOnClickListener { vm.onDelete() }
        binding.btnShowMap.setOnClickListener { vm.onShowOnMaps() }

        // 3) подписка на данные
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            vm.state.collect { data ->
                if (data == null) {
                    Toast.makeText(requireContext(), "Item not found", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                    return@collect
                }
                val r = data.route
                binding.txtName.text = r.name
                binding.txtDate.text = r.date.format(dateFmt)
                binding.txtTime.setText(r.time.format(timeFmt))
                binding.txtDescription.text = r.description

                // блок с точками:
                // в твоём макете есть фиксированный “1 Point” + txt_latitude/txt_longitude.
                // Покажем количество точек и первую точку (как минимум).
                val count = data.points.size
                // Найти TextView с текстом “1 Point” у тебя без id — лучше дай ему id:
                // android:id="@+id/txt_points_title"
                // Тогда:
                // binding.txtPointsTitle.text = if (count == 1) "1 Point" else "$count Points"

                if (count > 0) {
                    val p0 = data.points.first()
                    binding.txtLatitude.text = p0.lat.toString()
                    binding.txtLongitude.text = p0.lng.toString()
                } else {
                    binding.txtLatitude.text = ""
                    binding.txtLongitude.text = ""
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
                    // ... остальные ветки (NavigateBack, ShowDeleteDialog, ShowToast и т.д.)
                    else -> Unit
                }
            }
        }

    }
}

