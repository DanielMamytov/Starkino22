package co.nisari.katisnar.presentation.ui.admiral

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
import android.widget.LinearLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import co.nisari.katisnar.R
import co.nisari.katisnar.databinding.FragmentAdmiralRouteEditBinding
import co.nisari.katisnar.presentation.ui.starlocation.UiEvent
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class AdmiralRouteEditFragment : Fragment() {

    private lateinit var binding: FragmentAdmiralRouteEditBinding
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
            onRemove = { index ->
                vm.removePoint(index)
            }
        )
    }



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentAdmiralRouteEditBinding.inflate(inflater, container, false)
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
        }

        // список точек
        binding.recyclerView.adapter = pointsAdapter

        // поля
        binding.etName.doOnTextChanged { t, _, _, _ -> vm.onNameChange(t?.toString().orEmpty()) }

        // дата
        binding.icArrowDate.setOnClickListener { showDatePicker(vm.state.value.date) { vm.onDatePick(it) } }
        binding.txtDate.setOnClickListener { showDatePicker(vm.state.value.date) { vm.onDatePick(it) } }

        // время
        binding.icArrowTime.setOnClickListener { showTimePicker(vm.state.value.time) { vm.onTimePick(it) } }
        binding.txtTime.setOnClickListener { showTimePicker(vm.state.value.time) { vm.onTimePick(it) } }

        // описание — у тебя TextView txt_description; сделаем клик-диалог редактирования:
        binding.cvDescription.setOnClickListener { showDescriptionDialog() }

        // кнопки
        binding.btnBack.setOnClickListener { vm.onBack() }
        binding.btnDelete.setOnClickListener { vm.requestDelete() }
        binding.btnAddPoint.setOnClickListener {
            showAddPointDialog { la, lo -> vm.addPoint(la, lo) }
        }
        binding.btnSave.setOnClickListener { vm.onSave() }
        binding.btnCancel.setOnClickListener { vm.onBack() }

        // подписка на состояние
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            vm.state.collect { s ->
                // name
                if (binding.etName.text?.toString() != s.name) binding.etName.setText(s.name)

                // date/time
                binding.txtDate.text = s.date?.format(dateFmt) ?: ""
                binding.txtTime.setText(s.time?.format(timeFmt) ?: "")

                // description
                binding.txtDescription.text = s.description

                // points
                pointsAdapter.submit(s.points)
            }
        }

        // UI-события
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            vm.ui.collect { e ->
                when (e) {
                    is UiEvent.NavigateBack -> findNavController().popBackStack()
                    is UiEvent.ShowToast ->
                        Toast.makeText(requireContext(), e.message, Toast.LENGTH_SHORT).show()

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

    private fun showAddPointDialog(onConfirm: (String, String) -> Unit) {
        val container = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 8, 32, 8)
        }
        val etLat = EditText(requireContext()).apply {
            hint = "Latitude (−90..90)"
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL or InputType.TYPE_NUMBER_FLAG_SIGNED
        }
        val etLng = EditText(requireContext()).apply {
            hint = "Longitude (−180..180)"
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL or InputType.TYPE_NUMBER_FLAG_SIGNED
        }
        container.addView(etLat)
        container.addView(etLng)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Add Point")
            .setView(container)
            .setPositiveButton("Add") { _, _ ->
                onConfirm(etLat.text.toString().trim(), etLng.text.toString().trim())
            }
            .setNegativeButton("Cancel", null)
            .show()
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
            .setPositiveButton("OK") { _, _ -> vm.onDescChange(et.text.toString()) }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
