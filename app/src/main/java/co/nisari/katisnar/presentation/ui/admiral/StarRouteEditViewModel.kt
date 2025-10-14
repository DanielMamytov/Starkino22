package co.nisari.katisnar.presentation.ui.admiral

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.nisari.katisnar.presentation.data.repository.StarRouteRepository
import co.nisari.katisnar.presentation.ui.starlocation.UiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class StarRouteEditViewModel @Inject constructor(
    private val repo: StarRouteRepository
) : ViewModel() {

    data class EditState(
        val id: Long? = null,
        val name: String = "",
        val date: LocalDate? = null,
        val time: LocalTime? = null,
        val description: String = "",
        val points: List<PointItem> = emptyList(), // (latStr, lngStr) в UI
        val isLoading: Boolean = false
    )

    val state = MutableStateFlow(EditState())

    private val _ui = Channel<UiEvent>(Channel.BUFFERED)
    val ui = _ui.receiveAsFlow()

    fun load(id: Long) {
        viewModelScope.launch {
            repo.getRouteWithPoints(id).collect { data ->
                data?.let {
                    state.value = state.value.copy(
                        id = it.route.id,
                        name = it.route.name,
                        date = it.route.date,
                        time = it.route.time,
                        description = it.route.description,
                        points = it.points.map { p ->
                            PointItem(
                                lat = p.lat.toString(),
                                lng = p.lng.toString(),
                                // если в entity нет location — просто не заполняем:
                                location = ""
                            )
                        }

                    )
                }
            }
        }
    }

    fun onBack() = viewModelScope.launch { _ui.send(UiEvent.NavigateBack) }

    fun onNameChange(v: String) = state.update { it.copy(name = v) }
    fun onDatePick(v: LocalDate) = state.update { it.copy(date = v) }
    fun onTimePick(v: LocalTime) = state.update { it.copy(time = v) }
    fun onDescChange(v: String) = state.update { it.copy(description = v) }

    fun addPoint(latStr: String, lngStr: String, locationStr: String) {
        val lat = latStr.toDoubleOrNull()
        val lng = lngStr.toDoubleOrNull()
        if (lat == null || lng == null || lat !in -90.0..90.0 || lng !in -180.0..180.0) {
            viewModelScope.launch {
                _ui.send(UiEvent.ShowToast("Check latitude and longitude values before adding point"))
            }
            return
        }
        state.update { s ->
            s.copy(points = s.points + PointItem(latStr, lngStr, locationStr))
        }

    }

    fun removePoint(index: Int) {
        state.update { s ->
            val m = s.points.toMutableList()
            if (index in m.indices) m.removeAt(index)
            s.copy(points = m)
        }
    }

    fun requestDelete() {
        val id = state.value.id ?: return
        viewModelScope.launch { _ui.send(UiEvent.ShowDeleteDialog(id)) }
    }

    fun confirmDelete() {
        val id = state.value.id ?: return
        viewModelScope.launch {
            repo.deleteById(id)
            _ui.send(UiEvent.NavigateBack)
        }
    }

    fun onSave() {
        val s = state.value
        if (s.name.isBlank() || s.date == null || s.time == null || s.description.isBlank()) {
            viewModelScope.launch { _ui.send(UiEvent.ShowToast("Enter data to all fields before save")) }
            return
        }
        if (s.points.isEmpty()) {
            viewModelScope.launch { _ui.send(UiEvent.ShowToast("Add point of route to create new route")) }
            return
        }

        val points: List<Pair<Double, Double>> = s.points.map { pi ->
            (pi.lat.toDoubleOrNull() ?: Double.NaN) to (pi.lng.toDoubleOrNull() ?: Double.NaN)
        }
        if (points.any { it.first.isNaN() || it.second.isNaN() }) {
            viewModelScope.launch {
                _ui.send(UiEvent.ShowToast("Check latitude and longitude values before adding point"))
            }
            return
        }
        viewModelScope.launch {
            val route = StarRoute(
                id = s.id ?: 0L,
                name = s.name,
                date = s.date!!,
                time = s.time!!,
                description = s.description
            )
            if (s.id == null) repo.insert(route, points) else repo.update(route, points)
            _ui.send(UiEvent.NavigateBack)
        }
    }

    // Инициализация значений для режима Create
    fun prefillNowIfNeeded(nowDate: LocalDate, nowTime: LocalTime) {
        state.update { st ->
            st.copy(
                date = st.date ?: nowDate,
                time = st.time ?: nowTime
            )
        }
    }
}
