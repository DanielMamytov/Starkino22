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
class LocationListViewModel @Inject constructor(
    private val repository: StarLocationRepository
) : ViewModel() {

    val locations: StateFlow<List<StarLocation>> = repository.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    fun onAddClick() {
        viewModelScope.launch {
            _uiEvent.send(UiEvent.NavigateToEdit(null))
        }
    }

    fun onLocationClick(id: Long) {
        viewModelScope.launch {
            _uiEvent.send(UiEvent.NavigateToDetail(id))
        }
    }
}
