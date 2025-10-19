package co.nisari.katisnar.presentation.ui.admiral

import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.nisari.katisnar.presentation.data.repository.StarRouteRepository
import co.nisari.katisnar.presentation.ui.starlocation.UiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class StarRouteDetailViewModel @Inject constructor(
    private val repo: StarRouteRepository
) : ViewModel() {

    private val _state = MutableStateFlow<StarRouteWithPoints?>(null)
    val state: StateFlow<StarRouteWithPoints?> = _state

    private var currentRouteId: Long? = null
    private var routeDeleted = false
    private var missingRouteNotified = false

    private val _ui = Channel<UiEvent>(Channel.BUFFERED)
    val ui = _ui.receiveAsFlow()

    @RequiresApi(Build.VERSION_CODES.O)
    val dateFmt: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    @RequiresApi(Build.VERSION_CODES.O)
    val timeFmt: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    fun load(id: Long) {
        currentRouteId = id
        missingRouteNotified = false
        viewModelScope.launch {
            repo.getRouteWithPoints(id).collect { loaded ->
                if (loaded == null) {
                    if (routeDeleted) {
                        return@collect
                    }
                    if (!missingRouteNotified) {
                        missingRouteNotified = true
                        _ui.send(UiEvent.ShowToast("Item not found"))
                        _ui.send(UiEvent.NavigateBack)
                    }
                    return@collect
                }
                missingRouteNotified = false
                currentRouteId = loaded.route.id
                _state.value = loaded
            }
        }
    }

    fun onBack() = viewModelScope.launch { _ui.send(UiEvent.NavigateBack) }

    fun onEdit() {
        val id = currentRouteId ?: _state.value?.route?.id ?: return
        viewModelScope.launch { _ui.send(UiEvent.NavigateToEdit(id)) }
    }

    fun onDelete() {
        val id = currentRouteId ?: _state.value?.route?.id ?: return
        viewModelScope.launch { _ui.send(UiEvent.ShowDeleteDialog(id)) }
    }

    fun confirmDelete(id: Long) = viewModelScope.launch {
        routeDeleted = true
        repo.deleteById(id)
        _ui.send(UiEvent.NavigateBack)
    }

    fun isRouteDeleted(): Boolean = routeDeleted

    fun onRouteDeletionHandled() {
        routeDeleted = false
    }

    fun onShowOnMaps() {
        val data = _state.value ?: return
        val pts = data.points

        if (pts.isEmpty()) {
            viewModelScope.launch {
                _ui.send(UiEvent.ShowToast("Add point of route to create new route"))
            }
            return
        }

        // Google Maps App/Web поддерживают до ~25 пунктов (origin + destination + <=23 waypoints).
        // Ограничим на всякий случай, чтобы не было слишком длинной ссылки.
        val limited = pts.take(25)

        val uri = if (limited.size == 1) {
            val destination = "${limited.first().lat},${limited.first().lng}"
            Uri.parse(buildString {
                append("https://www.google.com/maps/dir/?api=1")
                append("&destination=").append(destination)
                append("&travelmode=driving")
            })
        } else {
            val origin = "${limited.first().lat},${limited.first().lng}"
            val destination = "${limited.last().lat},${limited.last().lng}"

            val waypoints = if (limited.size > 2) {
                limited.subList(1, limited.lastIndex)
                    .joinToString("|") { "${it.lat},${it.lng}" }
            } else null

            Uri.parse(buildString {
                append("https://www.google.com/maps/dir/?api=1")
                append("&origin=").append(origin)
                append("&destination=").append(destination)
                waypoints?.let { append("&waypoints=").append(it) }
                append("&travelmode=driving")
            })
        }

        viewModelScope.launch { _ui.send(UiEvent.OpenMaps1(uri)) }
    }

}
