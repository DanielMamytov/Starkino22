package co.nisari.katisnar.presentation.ui.starlocation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.nisari.katisnar.presentation.data.local.StarLocation
import co.nisari.katisnar.presentation.data.model.Weather
import co.nisari.katisnar.presentation.data.repository.StarLocationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class StarLocationEditViewModel @Inject constructor(
    private val repo: StarLocationRepository
) : ViewModel() {

    data class EditState(
        val id: Long? = null,
        val name: String = "",
        val location: String = "",
        val date: LocalDate? = null,
        val time: LocalTime? = null,
        val timeDisplay: String = "",
        val lat: String = "",
        val lng: String = "",
        val weather: Weather? = null,
        val notes: String = "",
        val isLoading: Boolean = false
    )

    val state = MutableStateFlow(EditState())

    @RequiresApi(Build.VERSION_CODES.O)
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    private val _events = Channel<UiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    @RequiresApi(Build.VERSION_CODES.O)
    fun load(id: Long) {
        viewModelScope.launch {
            repo.getById(id).collect { loc ->
                loc?.let {
                    val time = it.time
                    val timeDisplay = time?.format(timeFormatter).orEmpty()
                    state.value = state.value.copy(
                        id = it.id,
                        name = it.name,
                        location = it.location,
                        date = it.date,
                        time = time,
                        timeDisplay = timeDisplay,
                        lat = it.lat.toString(),
                        lng = it.lng.toString(),
                        weather = it.weather,
                        notes = it.notes
                    )
                }
            }
        }
    }

    fun onNameChanged(v: String) { state.update { it.copy(name = v) } }
    fun onLocationChanged(v: String) { state.update { it.copy(location = v) } }
    fun onDatePicked(v: LocalDate) { state.update { it.copy(date = v) } }
    @RequiresApi(Build.VERSION_CODES.O)
    fun onTimePicked(v: LocalTime) {
        state.update {
            it.copy(
                time = v,
                timeDisplay = v.format(timeFormatter)
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun onTimeTextChanged(value: String) {
        state.update { current ->
            val parsedTime = value.takeIf { it.isNotBlank() }
                ?.let { runCatching { LocalTime.parse(it, timeFormatter) }.getOrNull() }

            current.copy(
                time = parsedTime,
                timeDisplay = value
            )
        }
    }
    fun onLatChanged(v: String) { state.update { it.copy(lat = v) } }
    fun onLngChanged(v: String) { state.update { it.copy(lng = v) } }
    fun onWeatherSelected(v: Weather) { state.update { it.copy(weather = v) } }
    fun onNotesChanged(v: String) { state.update { it.copy(notes = v) } }

    fun onSave() {
        val s = state.value
        val lat = s.lat.toDoubleOrNull()
        val lng = s.lng.toDoubleOrNull()

        viewModelScope.launch {
            when {
                s.name.isBlank() || s.location.isBlank() || s.date == null || s.time == null ||
                s.notes.isBlank() || lat == null || lng == null || s.weather == null -> {
                    _events.send(UiEvent.ShowToast("Enter data to all fields before save"))
                }
                lat !in -90.0..90.0 || lng !in -180.0..180.0 -> {
                    _events.send(UiEvent.ShowToast("Check latitude and longitude values before save"))
                }
                else -> {
                    val item = StarLocation(
                        id = s.id ?: 0,
                        name = s.name,
                        location = s.location,
                        date = s.date,
                        time = s.time,
                        lat = lat,
                        lng = lng,
                        weather = s.weather,
                        notes = s.notes
                    )
                    if (s.id == null) repo.insert(item) else repo.update(item)
                    _events.send(UiEvent.NavigateBack)
                }
            }
        }
    }

    fun onDeleteConfirm() {
        val id = state.value.id ?: return
        viewModelScope.launch {
            repo.deleteById(id)
            _events.send(UiEvent.NavigateBack)
        }
    }

    fun requestDelete() {
        val id = state.value.id ?: return
        viewModelScope.launch { _events.send(UiEvent.ShowDeleteDialog(id)) }
    }

    fun onBack() { viewModelScope.launch { _events.send(UiEvent.NavigateBack) } }
}
