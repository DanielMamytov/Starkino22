package co.nisari.katisnar.presentation.ui.starlocation

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Build
import android.os.Bundle
import android.text.InputType
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import co.nisari.katisnar.R
import co.nisari.katisnar.databinding.FragmentStarLocationBinding
import co.nisari.katisnar.databinding.FragmentStarLocationEditBinding
import co.nisari.katisnar.presentation.data.model.Weather
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class StarLocationEditFragment : Fragment() {

    private lateinit var binding: FragmentStarLocationEditBinding
    private val vm: StarLocationEditViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.O)
    private val dateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    @RequiresApi(Build.VERSION_CODES.O)
    private val timeFmt = DateTimeFormatter.ofPattern("HH:mm")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentStarLocationEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1) Режим: создание или редактирование
        val args = arguments
        val id = if (args != null && args.containsKey("id")) args.getLong("id") else null
        if (id != null) {
            vm.load(id)
            binding.btnDelete.visibility = View.VISIBLE
        } else {
            binding.btnDelete.visibility = View.GONE
            // Префилл текущей датой/временем
            if (vm.state.value.date == null) vm.onDatePicked(LocalDate.now())
            if (vm.state.value.time == null) vm.onTimePicked(LocalTime.now().withSecond(0).withNano(0))
        }

        // 2) Подписка на состояние
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            vm.state.collect { s ->
                // name
                if (binding.etName.text.toString() != s.name)
                    binding.etName.setText(s.name)

                // location
                binding.txtLocation.setTextIfDifferent(s.location)

                // date
                binding.txtDate.text = s.date?.format(dateFmt) ?: ""

                val dateText = s.date?.format(dateFmt) ?: ""
                binding.txtDate.setTextIfDifferent(dateText)

                // lat/lng
                if (binding.txtLatitude.text?.toString() != s.lat) {
                    binding.txtLatitude.setText(s.lat)
                }
                if (binding.txtLongitude.text?.toString() != s.lng) {
                    binding.txtLongitude.setText(s.lng)
                }

                binding.txtLatitude.setTextIfDifferent(s.lat)
                binding.txtLongitude.setTextIfDifferent(s.lng)

                val weatherText = s.weather?.name
                    ?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "Weather"
                binding.txtWeather.setTextIfDifferent(weatherText)

                // notes
                if (binding.etNotes.text.toString() != s.notes)
                    binding.etNotes.setText(s.notes)
            }
        }

        // 3) Слушатели ввода
        binding.etName.doOnTextChanged { t, _, _, _ -> vm.onNameChanged(t?.toString().orEmpty()) }
        binding.etNotes.doOnTextChanged { t, _, _, _ -> vm.onNotesChanged(t?.toString().orEmpty()) }

        // 4) Выбор даты
        binding.txtDate.setOnClickListener { showDatePicker() }
        binding.icArrowDate.setOnClickListener { showDatePicker() }

        // 5) Выбор времени
        binding.txtTime.setOnClickListener { showTimePicker() }
        binding.icArrowTime.setOnClickListener { showTimePicker() }

        // 6) Ввод координат (диалоги на TextView, т.к. у тебя они не EditText)
        binding.txtLocation.setOnClickListener { showLocationDialog() }
        binding.txtLatitude.setOnClickListener { showCoordDialog(isLat = true) }
        binding.txtLongitude.setOnClickListener { showCoordDialog(isLat = false) }

        // 7) Выбор погоды
        binding.txtWeather.setOnClickListener { showWeatherDialog() }
        binding.icDropdown.setOnClickListener { showWeatherDialog() }
        binding.icWeather.setOnClickListener { showWeatherDialog() }

        // 8) Кнопки
        binding.btnSave.setOnClickListener { vm.onSave() }
        binding.btnCancel.setOnClickListener { vm.onBack() }
        binding.btnBack.setOnClickListener { vm.onBack() }
        binding.btnDelete.setOnClickListener { vm.requestDelete() }

        // 9) UI-события (тосты / диалоги / навигация)
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            vm.events.collect { e ->
                when (e) {
                    is UiEvent.ShowToast -> toast(e.message)
                    is UiEvent.NavigateBack -> findNavController().popBackStack()
                    is UiEvent.ShowDeleteDialog -> showDeleteDialog()
                    else -> Unit
                }
            }
        }
    }

    // ---------- helpers ----------

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showDatePicker() {
        val now = vm.state.value.date ?: LocalDate.now()
        DatePickerDialog(
            requireContext(), { _, y, m, d ->
                vm.onDatePicked(LocalDate.of(y, m + 1, d))
            }, now.year, now.monthValue - 1, now.dayOfMonth
        ).show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showTimePicker() {
        val now = vm.state.value.time ?: LocalTime.now().withSecond(0).withNano(0)
        TimePickerDialog(
            requireContext(),
            { _, hh, mm -> vm.onTimePicked(LocalTime.of(hh, mm)) },
            now.hour, now.minute, true
        ).show()
    }

    private fun showCoordDialog(isLat: Boolean) {
        val ctx = requireContext()
        val input = EditText(ctx).apply {
            inputType = InputType.TYPE_NUMBER_FLAG_SIGNED or InputType.TYPE_NUMBER_FLAG_DECIMAL or
                    InputType.TYPE_CLASS_NUMBER
            setText(if (isLat) vm.state.value.lat else vm.state.value.lng)
        }
        AlertDialog.Builder(ctx)
            .setTitle(if (isLat) "Latitude (−90..90)" else "Longitude (−180..180)")
            .setView(input)
            .setPositiveButton("OK") { _, _ ->
                val v = input.text?.toString().orEmpty()
                if (isLat) vm.onLatChanged(v) else vm.onLngChanged(v)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showWeatherDialog() {
        val items = Weather.values().map {
            it.name.lowercase().replaceFirstChar { c -> c.uppercase() }
        }.toTypedArray()

        AlertDialog.Builder(requireContext())
            .setTitle("Weather")
            .setItems(items) { _, which ->
                vm.onWeatherSelected(Weather.values()[which])
            }
            .show()
    }

    private fun showLocationDialog() {
        val ctx = requireContext()
        val input = EditText(ctx).apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
            setText(vm.state.value.location)
            setSelection(text?.length ?: 0)
        }

        AlertDialog.Builder(ctx)
            .setTitle("Location")
            .setView(input)
            .setPositiveButton("OK") { _, _ ->
                vm.onLocationChanged(input.text?.toString().orEmpty())
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeleteDialog() {
        AlertDialog.Builder(requireContext())
            .setMessage("Please confirm deletion")
            .setPositiveButton("Confirm") { _, _ -> vm.onDeleteConfirm() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun toast(msg: String) =
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()

    private fun TextView.setTextIfDifferent(value: CharSequence?) {
        val newText = value?.toString().orEmpty()
        if (text.toString() != newText) {
            setText(newText)
        }
    }
}
