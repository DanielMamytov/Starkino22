package co.nisari.katisnar.presentation.ui.starlocation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.nisari.katisnar.presentation.data.local.StarLocation
import co.nisari.katisnar.presentation.data.repository.StarLocationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StarLocationDetailViewModel @Inject constructor(
    private val repository: StarLocationRepository
) : ViewModel() {

    private val _location = MutableStateFlow<StarLocation?>(null)
    val location: StateFlow<StarLocation?> = _location

    private val _uiEvent = Channel<UiEvent>(Channel.BUFFERED)
    val uiEvent = _uiEvent.receiveAsFlow()

    fun loadLocation(id: Long) {
        viewModelScope.launch {
            repository.getById(id).collect { _location.value = it }
        }
    }

    fun onBackClick() = viewModelScope.launch {
        _uiEvent.send(UiEvent.NavigateBack)
    }

    fun onEditClick(id: Long) = viewModelScope.launch {
        _uiEvent.send(UiEvent.NavigateToEdit(id))
    }

    fun onDeleteClick(id: Long) = viewModelScope.launch {
        _uiEvent.send(UiEvent.ShowDeleteDialog(id))
    }

    fun confirmDelete(id: Long) = viewModelScope.launch {
        repository.deleteById(id)
        _uiEvent.send(UiEvent.NavigateBack)
    }

    fun onShowOnMap(lat: Double, lng: Double, name: String) = viewModelScope.launch {
        _uiEvent.send(UiEvent.OpenMaps(lat, lng, name))
    }
}

