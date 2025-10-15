package co.nisari.katisnar.presentation.ui.starlocation

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.text.Spanned
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import co.nisari.katisnar.R
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

    /** Включать подсветку ошибок только после первого нажатия Save */
    private var validationActivated = false

    @RequiresApi(Build.VERSION_CODES.O)
    private val dateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    @RequiresApi(Build.VERSION_CODES.O)
    private val timeFmt = DateTimeFormatter.ofPattern("HH:mm")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentStarLocationEditBinding.inflate(inflater, container, false)
        validationActivated = false
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
            if (vm.state.value.time == null) vm.onTimePicked(
                LocalTime.now().withSecond(0).withNano(0)
            )
        }

        // 2) Подписка на состояние
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            vm.state.collect { s ->
                // name
                if (binding.etName.text.toString() != s.name)
                    binding.etName.setText(s.name)

                // location
                binding.txtLocation.setTextIfDifferent(s.location)

                // time
                val timeText = s.time?.format(timeFmt) ?: ""
                binding.txtTime.setTextIfDifferent(timeText)

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

                syncErrorMasks()
            }
        }

        // 3) Слушатели ввода
        binding.etName.doOnTextChanged { t, _, _, _ ->
            vm.onNameChanged(t?.toString().orEmpty())
            markNameIfFilled()
        }
        binding.etNotes.doOnTextChanged { t, _, _, _ ->
            vm.onNotesChanged(t?.toString().orEmpty())
            markNotesIfFilled()
        }
        binding.txtTime.doOnTextChanged { t, _, _, _ ->
            vm.onTimeTextChanged(
                t?.toString().orEmpty()
            )
            markTimeIfFilled()
        }

        // 4) Выбор даты
        val openDate = {
            showDatePicker()
        }
        binding.txtDate.setOnClickListener { openDate() }
        binding.icArrowDate.setOnClickListener { openDate() }

        // 5) Выбор времени
        val openTime = {
            showTimePicker()
        }
        binding.txtTime.setOnClickListener { openTime() }
        binding.icArrowTime.setOnClickListener { openTime() }

        // 6) Ввод координат (диалоги на TextView, т.к. у тебя они не EditText)
        binding.txtLocation.doOnTextChanged { t, _, _, _ ->
            vm.onLocationChanged(t?.toString().orEmpty())
            markLocationIfFilled()
        }
        binding.txtLatitude.doOnTextChanged { t, _, _, _ ->
            vm.onLatChanged(t?.toString().orEmpty())
            markLatitudeIfValid()
        }
        binding.txtLongitude.doOnTextChanged { t, _, _, _ ->
            vm.onLngChanged(t?.toString().orEmpty())
            markLongitudeIfValid()
        }

        binding.boxName.setOnClickListener { focusAndShowKeyboard(binding.etName) }
        binding.boxLocation.setOnClickListener { focusAndShowKeyboard(binding.txtLocation) }
        binding.boxLatitude.setOnClickListener { focusAndShowKeyboard(binding.txtLatitude) }
        binding.boxLongitude.setOnClickListener { focusAndShowKeyboard(binding.txtLongitude) }
        binding.boxNotes.setOnClickListener { focusAndShowKeyboard(binding.etNotes) }

        binding.txtLatitude.filters = arrayOf(RangeInputFilter(-90.0, 90.0))
        binding.txtLongitude.filters = arrayOf(RangeInputFilter(-180.0, 180.0))
        // 7) Выбор погоды
        val openWeather = { showWeatherDialog() }
        binding.txtWeather.setOnClickListener { openWeather() }
        binding.icDropdown.setOnClickListener { openWeather() }
        binding.icWeather.setOnClickListener { openWeather() }

        // 8) Кнопки
        binding.btnSave.setOnClickListener { onSaveClicked() }
        binding.btnCancel.setOnClickListener { vm.onBack() }
        binding.btnBack.setOnClickListener { vm.onBack() }
        binding.btnDelete.setOnClickListener { vm.requestDelete() }

        // 9) UI-события (тосты / диалоги / навигация)
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            vm.events.collect { e ->
                when (e) {
                    is UiEvent.ShowToast -> toast(e.message)
                    is UiEvent.NavigateBack -> findNavController().popBackStack()
                    is UiEvent.NavigateToList -> {
                        val controller = findNavController()
                        val popped = controller.popBackStack(R.id.starLocationFragment, false)
                        if (!popped) {
                            controller.navigate(R.id.action_starLocationEditFragment_to_starLocationFragment)
                        }
                    }
                    is UiEvent.ShowDeleteDialog -> showDeleteDialog()
                    else -> Unit
                }
            }
        }

        setNameError(false)
        setLocationError(false)
        setDateError(false)
        setTimeError(false)
        setLatitudeError(false)
        setLongitudeError(false)
        setWeatherError(false)
        setNotesError(false)
    }

    private fun onSaveClicked() {
        if (!validationActivated) validationActivated = true
        validateAndMark()
        vm.onSave()
    }

    private fun validateAndMark() {
        val state = vm.state.value
        val nameEmpty = state.name.trim().isEmpty()
        val locationEmpty = state.location.trim().isEmpty()
        val dateEmpty = state.date == null
        val timeEmpty = state.time == null
        val latText = state.lat.trim()
        val lngText = state.lng.trim()
        val latValue = latText.toDoubleOrNull()
        val lngValue = lngText.toDoubleOrNull()
        val latInvalid = latValue == null || latValue !in -90.0..90.0
        val lngInvalid = lngValue == null || lngValue !in -180.0..180.0
        val weatherEmpty = state.weather == null
        val notesEmpty = state.notes.trim().isEmpty()

        if (validationActivated) {
            setNameError(nameEmpty)
            setLocationError(locationEmpty)
            setDateError(dateEmpty)
            setTimeError(timeEmpty)
            setLatitudeError(latText.isEmpty() || latInvalid)
            setLongitudeError(lngText.isEmpty() || lngInvalid)
            setWeatherError(weatherEmpty)
            setNotesError(notesEmpty)
        }
    }

    private fun syncErrorMasks() {
        if (!validationActivated) return
        val state = vm.state.value
        setNameError(state.name.trim().isEmpty())
        setLocationError(state.location.trim().isEmpty())
        setDateError(state.date == null)
        setTimeError(state.time == null)
        val latValue = state.lat.trim().toDoubleOrNull()
        val lngValue = state.lng.trim().toDoubleOrNull()
        setLatitudeError(state.lat.trim().isEmpty() || latValue == null || latValue !in -90.0..90.0)
        setLongitudeError(state.lng.trim().isEmpty() || lngValue == null || lngValue !in -180.0..180.0)
        setWeatherError(state.weather == null)
        setNotesError(state.notes.trim().isEmpty())
    }

    private fun markNameIfFilled() {
        if (!validationActivated) return
        if (!binding.etName.text?.toString()?.trim().isNullOrEmpty()) setNameError(false)
    }

    private fun markLocationIfFilled() {
        if (!validationActivated) return
        if (!binding.txtLocation.text?.toString()?.trim().isNullOrEmpty()) setLocationError(false)
    }

    private fun markDateIfFilled() {
        if (!validationActivated) return
        if (!binding.txtDate.text?.toString()?.trim().isNullOrEmpty()) setDateError(false)
    }

    private fun markTimeIfFilled() {
        if (!validationActivated) return
        if (!binding.txtTime.text?.toString()?.trim().isNullOrEmpty()) setTimeError(false)
    }

    private fun markLatitudeIfValid() {
        if (!validationActivated) return
        val text = binding.txtLatitude.text?.toString()?.trim()
        val value = text?.toDoubleOrNull()
        if (!text.isNullOrEmpty() && value != null && value in -90.0..90.0) {
            setLatitudeError(false)
        }
    }

    private fun markLongitudeIfValid() {
        if (!validationActivated) return
        val text = binding.txtLongitude.text?.toString()?.trim()
        val value = text?.toDoubleOrNull()
        if (!text.isNullOrEmpty() && value != null && value in -180.0..180.0) {
            setLongitudeError(false)
        }
    }

    private fun markWeatherIfFilled() {
        if (!validationActivated) return
        if (vm.state.value.weather != null) setWeatherError(false)
    }

    private fun markNotesIfFilled() {
        if (!validationActivated) return
        if (!binding.etNotes.text?.toString()?.trim().isNullOrEmpty()) setNotesError(false)
    }

    private fun setNameError(error: Boolean) {
        binding.boxName.setBackgroundResource(if (error) R.drawable.edittext_border_error_bg else R.drawable.edittext_border_bg)
    }

    private fun setLocationError(error: Boolean) {
        binding.boxLocation.setBackgroundResource(if (error) R.drawable.edittext_border_error_bg else R.drawable.edittext_border_bg)
    }

    private fun setDateError(error: Boolean) {
        binding.boxDate.setBackgroundResource(if (error) R.drawable.text_border_error else R.drawable.text_border)
    }

    private fun setTimeError(error: Boolean) {
        binding.boxTime.setBackgroundResource(if (error) R.drawable.text_border_error else R.drawable.text_border)
    }

    private fun setLatitudeError(error: Boolean) {
        binding.boxLatitude.setBackgroundResource(if (error) R.drawable.edittext_border_error_bg else R.drawable.edittext_border_bg)
    }

    private fun setLongitudeError(error: Boolean) {
        binding.boxLongitude.setBackgroundResource(if (error) R.drawable.edittext_border_error_bg else R.drawable.edittext_border_bg)
    }

    private fun setWeatherError(error: Boolean) {
        binding.boxWeather.setBackgroundResource(if (error) R.drawable.edittext_border_error_bg else R.drawable.edittext_border_bg)
    }

    private fun setNotesError(error: Boolean) {
        binding.boxNotes.setBackgroundResource(if (error) R.drawable.edittext_border_error_bg else R.drawable.edittext_border_bg)
    }

    // ---------- helpers ----------

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showDatePicker() {
        val now = vm.state.value.date ?: LocalDate.now()
        DatePickerDialog(
            requireContext(), { _, y, m, d ->
                vm.onDatePicked(LocalDate.of(y, m + 1, d))
                markDateIfFilled()
            }, now.year, now.monthValue - 1, now.dayOfMonth
        ).show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showTimePicker() {
        val now = vm.state.value.time ?: LocalTime.now().withSecond(0).withNano(0)
        TimePickerDialog(
            requireContext(),
            { _, hh, mm ->
                vm.onTimePicked(LocalTime.of(hh, mm))
                markTimeIfFilled()
            },
            now.hour, now.minute, true
        ).show()
    }

    private fun showWeatherDialog() {
        val items = Weather.values().map {
            it.name.lowercase().replaceFirstChar { c -> c.uppercase() }
        }.toTypedArray()

        AlertDialog.Builder(requireContext())
            .setTitle("Weather")
            .setItems(items) { _, which ->
                vm.onWeatherSelected(Weather.values()[which])
                markWeatherIfFilled()
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
                markLocationIfFilled()
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

    private fun focusAndShowKeyboard(editText: EditText) {
        editText.requestFocus()
        editText.post {
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
            editText.setSelection(editText.text?.length ?: 0)
        }
    }

    private fun TextView.setTextIfDifferent(value: CharSequence?) {
        val newText = value?.toString().orEmpty()
        if (text.toString() != newText) {
            setText(newText)
        }
    }


    private class RangeInputFilter(
        private val min: Double,
        private val max: Double
    ) : InputFilter {

        override fun filter(
            source: CharSequence?,
            start: Int,
            end: Int,
            dest: Spanned?,
            dstart: Int,
            dend: Int
        ): CharSequence? {
            val destLen = dest?.length ?: 0
            val safeStart = dstart.coerceIn(0, destLen)
            val safeEnd = dend.coerceIn(0, destLen)

            val prefix = dest?.subSequence(0, safeStart)?.toString() ?: ""
            val middle = source?.subSequence(start, end)?.toString() ?: ""
            val suffix = dest?.subSequence(safeEnd, destLen)?.toString() ?: ""

            val newValue = prefix + middle + suffix

            // Разрешаем промежуточные состояния ввода
            if (newValue.isBlank() || newValue == "-" || newValue == "." || newValue == "-.") {
                return null
            }

            val number = newValue.toDoubleOrNull() ?: return ""
            return if (number in min..max) null else ""
        }
    }

    }
