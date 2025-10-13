package co.nisari.katisnar.presentation.ui.starnotes

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.nisari.katisnar.presentation.data.local.NoteEntity
import co.nisari.katisnar.presentation.data.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoteEditViewModel @Inject constructor(
    private val repository: NoteRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    data class UiState(
        val id: Long? = null,
        val text: String = "",
        val createdAt: Long = System.currentTimeMillis()
    ) {
        val isExisting: Boolean get() = id != null
    }

    sealed interface UiEvent {
        data class ShowToast(val message: String) : UiEvent
        data object CloseScreen : UiEvent
    }

    private val noteId: Long? = savedStateHandle.get<Long>("noteId")?.takeIf { it > 0 }

    private val _state = MutableStateFlow(UiState(id = noteId))
    val state: StateFlow<UiState> = _state.asStateFlow()

    private val _events = Channel<UiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        if (noteId != null) {
            viewModelScope.launch {
                repository.getById(noteId).filterNotNull().collect { entity ->
                    _state.update { it.copy(text = entity.text, createdAt = entity.createdAt) }
                }
            }
        }
    }

    fun onTextChanged(text: String) {
        _state.update { it.copy(text = text) }
    }

    fun onSaveClicked(emptyMessage: String) {
        val current = _state.value
        val text = current.text.trim()
        if (text.isBlank()) {
            viewModelScope.launch { _events.send(UiEvent.ShowToast(emptyMessage)) }
            return
        }

        viewModelScope.launch {
            if (current.id == null) {
                repository.insert(NoteEntity(text = text))
            } else {
                repository.update(NoteEntity(id = current.id, text = text, createdAt = current.createdAt))
            }
            _events.send(UiEvent.CloseScreen)
        }
    }

    fun onDeleteConfirmed() {
        val id = _state.value.id ?: return
        viewModelScope.launch {
            repository.deleteById(id)
            _events.send(UiEvent.CloseScreen)
        }
    }
}
