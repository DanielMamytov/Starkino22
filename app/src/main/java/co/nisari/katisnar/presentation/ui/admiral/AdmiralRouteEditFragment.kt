package co.nisari.katisnar.presentation.ui.admiral

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import co.nisari.katisnar.R
import co.nisari.katisnar.databinding.FragmentAdmiralRouteEditBinding
import co.nisari.katisnar.presentation.ui.starlocation.UiEvent
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class AdmiralRouteEditFragment : Fragment() {

    private var _binding: FragmentAdmiralRouteEditBinding? = null
    private val binding get() = _binding!!

    private val vm: StarRouteEditViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.O)
    private val dateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    @RequiresApi(Build.VERSION_CODES.O)
    private val timeFmt = DateTimeFormatter.ofPattern("HH:mm")

    private val pointsAdapter by lazy {
        PointAdapter(
            onLatChanged = { index, value ->
                val cur = vm.state.value.points.toMutableList()
                if (index in cur.indices) {
                    cur[index] = cur[index].copy(lat = value)
                    vm.state.update { it.copy(points = cur) }
                }
            },
            onLngChanged = { index, value ->
                val cur = vm.state.value.points.toMutableList()
                if (index in cur.indices) {
                    cur[index] = cur[index].copy(lng = value)
                    vm.state.update { it.copy(points = cur) }
                }
            },
            onLocationChanged = { index, value ->
                val cur = vm.state.value.points.toMutableList()
                if (index in cur.indices) {
                    cur[index] = cur[index].copy(location = value)
                    vm.state.update { it.copy(points = cur) }
                }
            },
            onSave = { index -> vm.onPointSave(index) },
            onRemove = { index -> vm.removePoint(index) }
        )
    }

    /** Включать подсветку ошибок только после первого нажатия Save */
    private var validationActivated = false

    /** Нужна ли обязательность Description */
    private val requireDescription = true

    // Цвета обводки
    private val normalStrokeColor by lazy { Color.parseColor("#B8FFFFFF") } // полупрозрачный белый
    private val errorStrokeColor  by lazy { Color.parseColor("#FF0000") }   // красный

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAdmiralRouteEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // режим: Create vs Edit
        val id = arguments?.getLong("id", -1L)?.takeIf { it != -1L }
        if (id != null) {
            vm.load(id)
            binding.btnDelete.visibility = View.VISIBLE
        } else {
            binding.btnDelete.visibility = View.GONE
            vm.prefillNowIfNeeded(LocalDate.now(), LocalTime.now().withSecond(0).withNano(0))
            vm.ensureAtLeastOnePoint()
        }

        // список точек
        binding.recyclerView.adapter = pointsAdapter

        // ==== ЛИСТЕНЕРЫ ДЛЯ ПОЛЕЙ ====
        // NAME
        binding.etName.doOnTextChanged { t, _, _, _ ->
            vm.onNameChange(t?.toString().orEmpty())
            markNameIfFilled()
        }

        // DATE
        val openDatePicker = {
            showDatePicker(vm.state.value.date) {
                vm.onDatePick(it)
                markDateIfFilled()
            }
        }
        binding.icArrowDate.setOnClickListener { openDatePicker() }
        binding.txtDate.setOnClickListener { openDatePicker() }

        // TIME
        val openTimePicker = {
            showTimePicker(vm.state.value.time) {
                vm.onTimePick(it)
                markTimeIfFilled()
            }
        }
        binding.icArrowTime.setOnClickListener { openTimePicker() }
        binding.txtTime.doOnTextChanged { t, _, _, _ ->
            vm.onTimePickSafely(t?.toString())
            markTimeIfFilled()
        }
        binding.txtTime.setOnClickListener { openTimePicker() }

        // DESCRIPTION (по клику редактируем через диалог)
        binding.cvDescription.setOnClickListener { showDescriptionDialog() }

        // Кнопки
        binding.btnBack.setOnClickListener { vm.onBack() }
        binding.btnDelete.setOnClickListener { vm.requestDelete() }
        binding.btnAddPoint.setOnClickListener { vm.addEmptyPoint() }
        binding.btnCancel.setOnClickListener { vm.onBack() }
        binding.btnSave.setOnClickListener { onSaveClicked() }

        // ==== Подписки ====
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            vm.state.collectLatest { s ->
                // name
                if (binding.etName.text?.toString() != s.name) binding.etName.setText(s.name)

                // date/time
                binding.txtDate.text = s.date?.format(dateFmt) ?: ""
                val timeStr = s.time?.format(timeFmt) ?: ""
                if (binding.txtTime.text?.toString() != timeStr) binding.txtTime.setText(timeStr)

                // description
                binding.txtDescription.text = s.description

                // points
                pointsAdapter.submit(s.points)

                // Поддерживаем визуальное состояние ошибок только после Save
                syncErrorMasks()
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            vm.ui.collectLatest { e ->
                when (e) {
                    is UiEvent.NavigateBack -> findNavController().popBackStack()
                    is UiEvent.ShowToast -> Toast.makeText(requireContext(), e.message, Toast.LENGTH_SHORT).show()
                    is UiEvent.ShowDeleteDialog -> {
                        MaterialAlertDialogBuilder(requireContext())
                            .setMessage("Please confirm deletion")
                            .setPositiveButton("Confirm") { _, _ -> vm.confirmDelete() }
                            .setNegativeButton("Cancel", null)
                            .show()
                    }
                    else -> Unit
                }
            }
        }

        // Стартовое состояние — без красных рамок
        setNameError(false)
        setDateError(false)
        setTimeError(false)
        setDescriptionError(false)
    }

    // ======= SAVE + VALIDATION =======
    private fun onSaveClicked() {
        if (!validationActivated) validationActivated = true
        val hasError = validateAndMark()
        if (!hasError) {
            vm.onSave()
        } else {
            Toast.makeText(requireContext(), "Fill Name, Date and Time", Toast.LENGTH_SHORT).show()
        }
    }

    private fun validateAndMark(): Boolean {
        val nameEmpty = binding.etName.text?.toString()?.trim().isNullOrEmpty()
        val dateEmpty = binding.txtDate.text?.toString()?.trim().isNullOrEmpty()
        val timeEmpty = binding.txtTime.text?.toString()?.trim().isNullOrEmpty()
        val descriptionEmpty = binding.txtDescription.text?.toString()?.trim().isNullOrEmpty()

        if (validationActivated) {
            setNameError(nameEmpty)
            setDateError(dateEmpty)
            setTimeError(timeEmpty)
            setDescriptionError(descriptionEmpty)   // ← ВСЕГДА меняем рамку у description
        } else {
            setNameError(false)
            setDateError(false)
            setTimeError(false)
            setDescriptionError(false)
        }

        // ← Учитываем descriptionEmpty в результате (раз он обязателен)
        return nameEmpty || dateEmpty || timeEmpty || descriptionEmpty
    }


    private fun syncErrorMasks() {
        if (!validationActivated) return
        setNameError(binding.etName.text?.toString()?.trim().isNullOrEmpty())
        setDateError(binding.txtDate.text?.toString()?.trim().isNullOrEmpty())
        setTimeError(binding.txtTime.text?.toString()?.trim().isNullOrEmpty())
        setDescriptionError(binding.txtDescription.text?.toString()?.trim().isNullOrEmpty()) // ← всегда
    }


    private fun markNameIfFilled() {
        if (!validationActivated) return
        if (!binding.etName.text?.toString()?.trim().isNullOrEmpty()) setNameError(false)
    }

    private fun markDescriptionIfFilled() {
        if (!validationActivated) return
        if (!binding.txtDescription.text?.toString()?.trim().isNullOrEmpty()) {
            setDescriptionError(false)   // ← именно description, не name
        }
    }

    private fun markDateIfFilled() {
        if (!validationActivated) return
        if (!binding.txtDate.text?.toString()?.trim().isNullOrEmpty()) setDateError(false)
    }

    private fun markTimeIfFilled() {
        if (!validationActivated) return
        if (!binding.txtTime.text?.toString()?.trim().isNullOrEmpty()) setTimeError(false)
    }

    // ======= ВКЛ/ВЫКЛ КРАСНЫХ РАМОК =======
    private fun setNameError(error: Boolean) {
        val card: MaterialCardView = binding.name   // id у MaterialCardView вокруг Name: @+id/name
        card.strokeWidth = resources.getDimensionPixelSize(R.dimen.stroke_2dp)
        card.strokeColor = if (error) errorStrokeColor else normalStrokeColor
    }

    private fun setDescriptionError(error: Boolean) {
        val card: MaterialCardView = binding.cvDescription
        card.strokeWidth = resources.getDimensionPixelSize(R.dimen.stroke_2dp)
        card.strokeColor = if (error) errorStrokeColor else normalStrokeColor
    }


    private fun setDateError(error: Boolean) {
        val box = binding.boxDate
        box.setBackgroundResource(if (error) R.drawable.text_border_error else R.drawable.text_border)
    }

    private fun setTimeError(error: Boolean) {
        val box = binding.boxTime
        box.setBackgroundResource(if (error) R.drawable.text_border_error else R.drawable.text_border)
    }

    // ======= DIALOGS / PICKERS =======
    @RequiresApi(Build.VERSION_CODES.O)
    private fun showDatePicker(current: LocalDate?, onPicked: (LocalDate) -> Unit) {
        val c = current ?: LocalDate.now()
        DatePickerDialog(requireContext(), { _, y, m, d ->
            onPicked(LocalDate.of(y, m + 1, d))
        }, c.year, c.monthValue - 1, c.dayOfMonth).show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showTimePicker(current: LocalTime?, onPicked: (LocalTime) -> Unit) {
        val t = current ?: LocalTime.now().withSecond(0).withNano(0)
        TimePickerDialog(requireContext(), { _, h, min ->
            onPicked(LocalTime.of(h, min))
        }, t.hour, t.minute, true).show()
    }

    private fun showDescriptionDialog() {
        val et = EditText(requireContext()).apply {
            setText(vm.state.value.description)
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
            setPadding(24, 16, 24, 16)
        }
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Description")
            .setView(et)
            .setPositiveButton("OK") { _, _ ->
                vm.onDescChange(et.text.toString())
                // если уже активирована валидация — снимем красную рамку, если поле непустое
                markDescriptionIfFilled()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

private fun StarRouteEditViewModel.onTimePickSafely(timeStr: String?) {
    if (timeStr.isNullOrBlank()) return
    runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val parts = timeStr.split(":")
            val h = parts.getOrNull(0)?.toInt() ?: return
            val m = parts.getOrNull(1)?.toInt() ?: 0
            onTimePick(LocalTime.of(h, m))
        }
    }
}
