package co.nisari.katisnar.presentation.ui.admiral

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
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

    private var validationActivated = false

    private val normalStrokeColor by lazy { Color.parseColor("#B8FFFFFF") }
    private val errorStrokeColor by lazy { Color.parseColor("#FF0000") }

    private data class ValidationResult(
        val nameEmpty: Boolean,
        val dateEmpty: Boolean,
        val timeEmpty: Boolean,
        val descriptionEmpty: Boolean,
        val emptyLocationIndices: Set<Int>,
        val emptyCoordinateIndices: Set<Int>
    ) {
        val hasError: Boolean =
            nameEmpty ||
                dateEmpty ||
                timeEmpty ||
                descriptionEmpty ||
                emptyLocationIndices.isNotEmpty() ||
                emptyCoordinateIndices.isNotEmpty()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdmiralRouteEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val id = arguments?.getLong("id", -1L)?.takeIf { it != -1L }
        if (id != null) {
            vm.load(id)
            binding.btnDelete.visibility = View.VISIBLE
        } else {
            binding.btnDelete.visibility = View.GONE
            vm.prefillNowIfNeeded(LocalDate.now(), LocalTime.now().withSecond(0).withNano(0))
            vm.ensureAtLeastOnePoint()
        }


        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = pointsAdapter
            isNestedScrollingEnabled = false
        }
        binding.etName.doOnTextChanged { t, _, _, _ ->
            vm.onNameChange(t?.toString().orEmpty())
            markNameIfFilled()
        }

        val openDatePicker = {
            showDatePicker(vm.state.value.date) {
                vm.onDatePick(it)
                markDateIfFilled()
            }
        }
        binding.icArrowDate.setOnClickListener { openDatePicker() }
        binding.txtDate.setOnClickListener { openDatePicker() }

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



        binding.btnBack.setOnClickListener { vm.onBack() }
        binding.btnDelete.setOnClickListener { vm.requestDelete() }
        binding.btnAddPoint.setOnClickListener { vm.addEmptyPoint() }
        binding.btnCancel.setOnClickListener { vm.onBack() }
        binding.btnSave.setOnClickListener { onSaveClicked() }

        binding.txtDescription.doOnTextChanged { t, _, _, _ ->
            vm.onDescChange(t?.toString().orEmpty())
            markDescriptionIfFilled()
        }

        binding.cvDescription.setOnClickListener {
            binding.txtDescription.requestFocus()
            binding.txtDescription.setSelection(binding.txtDescription.text?.length ?: 0)
            binding.txtDescription.performClick()
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(binding.txtDescription, InputMethodManager.SHOW_IMPLICIT)
            binding.root.post {
                (binding.root as? View)?.let {
                    it.parent?.requestChildFocus(it, binding.txtDescription)
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            vm.state.collectLatest { s ->
                if (binding.etName.text?.toString() != s.name) binding.etName.setText(s.name)

                binding.txtDate.text = s.date?.format(dateFmt) ?: ""
                val timeStr = s.time?.format(timeFmt) ?: ""
                if (binding.txtTime.text?.toString() != timeStr) binding.txtTime.setText(timeStr)

                if (binding.txtDescription.text?.toString() != s.description) {
                    binding.txtDescription.setText(s.description)
                }

                pointsAdapter.submit(s.points)

                syncErrorMasks()
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            vm.ui.collectLatest { e ->
                when (e) {
                    is UiEvent.NavigateBack -> findNavController().popBackStack()
                    is UiEvent.ShowToast -> Toast.makeText(
                        requireContext(),
                        e.message,
                        Toast.LENGTH_SHORT
                    ).show()

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

        validationActivated = false
        setNameError(false)
        setDateError(false)
        setTimeError(false)
        setDescriptionError(false)
        pointsAdapter.validationActivated = false
    }

    private fun onSaveClicked() {
        val result = computeValidation()

        if (!result.hasError) {
            vm.onSave()
            return
        }

        if (!validationActivated) {
            validationActivated = true
        }

        pointsAdapter.validationActivated = true

        applyValidationToUI(result)

        Toast.makeText(requireContext(), "Enter Latitude, Enter longitude", Toast.LENGTH_SHORT).show()
    }

    private fun computeValidation(): ValidationResult {
        val nameEmpty = binding.etName.text?.toString()?.trim().isNullOrEmpty()
        val dateEmpty = binding.txtDate.text?.toString()?.trim().isNullOrEmpty()
        val timeEmpty = binding.txtTime.text?.toString()?.trim().isNullOrEmpty()
        val descriptionEmpty =
            binding.txtDescription.text?.toString()?.trim().isNullOrEmpty()

        val snapshot = pointsAdapter.snapshotItems()
        val emptyLocationIndices = snapshot.mapIndexedNotNull { index, item ->
            if (item.location.trim().isEmpty()) index else null
        }.toSet()
        val emptyCoordinateIndices = snapshot.mapIndexedNotNull { index, item ->
            if (item.lat.trim().isEmpty() || item.lng.trim().isEmpty()) index else null
        }.toSet()

        return ValidationResult(
            nameEmpty = nameEmpty,
            dateEmpty = dateEmpty,
            timeEmpty = timeEmpty,
            descriptionEmpty = descriptionEmpty,
            emptyLocationIndices = emptyLocationIndices,
            emptyCoordinateIndices = emptyCoordinateIndices
        )
    }

    private fun syncErrorMasks() {
        if (!validationActivated) return
        val result = computeValidation()
        applyValidationToUI(result)
    }

    private fun applyValidationToUI(result: ValidationResult) {
        if (!validationActivated) {
            setNameError(false)
            setDateError(false)
            setTimeError(false)
            setDescriptionError(false)
            binding.recyclerView.adapter?.notifyDataSetChanged()
            return
        }

        setNameError(result.nameEmpty)
        setDateError(result.dateEmpty)
        setTimeError(result.timeEmpty)
        setDescriptionError(result.descriptionEmpty)

        binding.recyclerView.adapter?.notifyDataSetChanged()
    }

    private fun markDescriptionIfFilled() {
        if (!validationActivated) return
        if (!binding.txtDescription.text?.toString()?.trim().isNullOrEmpty()) {
            setDescriptionError(false)
        }
    }


    private fun markNameIfFilled() {
        if (!validationActivated) return
        if (!binding.etName.text?.toString()?.trim().isNullOrEmpty()) setNameError(false)
    }


    private fun markDateIfFilled() {
        if (!validationActivated) return
        if (!binding.txtDate.text?.toString()?.trim().isNullOrEmpty()) setDateError(false)
    }

    private fun markTimeIfFilled() {
        if (!validationActivated) return
        if (!binding.txtTime.text?.toString()?.trim().isNullOrEmpty()) setTimeError(false)
    }

    private fun setNameError(error: Boolean) {
        val card: MaterialCardView = binding.name
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
