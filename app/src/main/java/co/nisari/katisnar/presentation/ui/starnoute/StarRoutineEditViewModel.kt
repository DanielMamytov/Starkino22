package co.nisari.katisnar.presentation.ui.starnoute

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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StarRoutineEditViewModel @Inject constructor(
    private val repository: NoteRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    data class UiState(
        val id: Long? = null,
        val name: String = "",
        val notes: String = "",
        val createdAt: Long = System.currentTimeMillis()
    ) {
        val isExisting: Boolean get() = id != null
    }

    sealed interface UiEvent {
        data class ShowToast(val message: String) : UiEvent
        data object CloseScreen : UiEvent
    }

    private val noteId = savedStateHandle.get<Long>("noteId")?.takeIf { it > 0 }

    private val _state = MutableStateFlow(UiState(id = noteId))
    val state: StateFlow<UiState> = _state.asStateFlow()

    private val _events = Channel<UiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        if (noteId != null) {
            viewModelScope.launch {
                repository.getById(noteId).collectLatest { entity ->
                    if (entity == null) {
                        _events.send(UiEvent.CloseScreen)
                    } else {
                        val (name, notes) = NoteTextMapper.split(entity.text)
                        _state.update {
                            it.copy(
                                name = name,
                                notes = notes,
                                createdAt = entity.createdAt
                            )
                        }
                    }
                }
            }
        }
    }

    fun onNameChanged(name: String) {
        _state.update { it.copy(name = name) }
    }

    fun onNotesChanged(notes: String) {
        _state.update { it.copy(notes = notes) }
    }

    fun onSaveClicked(emptyNameMessage: String, emptyNotesMessage: String) {
        val current = _state.value
        val name = current.name.trim()
        val notes = current.notes.trim()
        if (name.isBlank()) {
            viewModelScope.launch { _events.send(UiEvent.ShowToast(emptyNameMessage)) }
            return
        }

        if (notes.isBlank()) {
            viewModelScope.launch { _events.send(UiEvent.ShowToast(emptyNotesMessage)) }
            return
        }

        val text = NoteTextMapper.combine(name, notes)

        viewModelScope.launch {
            if (current.id == null) {
                repository.insert(NoteEntity(text = text))
            } else {
                repository.update(
                    NoteEntity(
                        id = current.id,
                        text = text,
                        createdAt = current.createdAt
                    )
                )
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
