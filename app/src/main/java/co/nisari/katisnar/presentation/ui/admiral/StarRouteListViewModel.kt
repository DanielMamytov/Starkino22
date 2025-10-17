package co.nisari.katisnar.presentation.ui.admiral

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.nisari.katisnar.presentation.data.repository.StarRouteRepository
import co.nisari.katisnar.presentation.ui.starlocation.UiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StarRouteListViewModel @Inject constructor(
    private val repo: StarRouteRepository
) : ViewModel() {

    val routes: StateFlow<List<StarRoute>> = repo.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _ui = Channel<UiEvent>(Channel.BUFFERED)
    val ui = _ui.receiveAsFlow()

    fun onBack() = viewModelScope.launch { _ui.send(UiEvent.NavigateBack) }
    fun onAddRouteClick() = viewModelScope.launch { _ui.send(UiEvent.NavigateToEdit(null)) }
    fun onItemClick(id: Long) = viewModelScope.launch { _ui.send(UiEvent.NavigateToDetail(id)) }
    fun onMoreDetailsClick(id: Long) = onItemClick(id)
    fun onItemLongClick(id: Long) = viewModelScope.launch { _ui.send(UiEvent.ShowDeleteDialog(id)) }
    fun onDeleteConfirmed(id: Long) = viewModelScope.launch {
        repo.deleteById(id)
        _ui.send(UiEvent.ShowToast("Route deleted"))
    }
}

