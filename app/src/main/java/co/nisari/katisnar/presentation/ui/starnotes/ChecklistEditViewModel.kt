package co.nisari.katisnar.presentation.ui.starnotes

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.nisari.katisnar.presentation.data.local.ChecklistItemEntity
import co.nisari.katisnar.presentation.data.repository.ChecklistRepository
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
class ChecklistEditViewModel @Inject constructor(
    private val repository: ChecklistRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    data class EditableChecklistItem(
        val localId: Long,
        val text: String,
        val isChecked: Boolean
    )

    data class UiState(
        val id: Long? = null,
        val title: String = "",
        val items: List<EditableChecklistItem> = emptyList(),
        val createdAt: Long = System.currentTimeMillis()
    ) {
        val isExisting: Boolean get() = id != null
    }

    sealed interface UiEvent {
        data class ShowToast(val message: String) : UiEvent
        data object CloseScreen : UiEvent
    }

    private val checklistId: Long? = savedStateHandle.get<Long>("checklistId")?.takeIf { it > 0 }

    private val _state = MutableStateFlow(UiState(id = checklistId))
    val state: StateFlow<UiState> = _state.asStateFlow()

    private val _events = Channel<UiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        if (checklistId != null) {
            viewModelScope.launch {
                repository.getById(checklistId).filterNotNull().collect { checklist ->
                    _state.update {
                        it.copy(
                            title = checklist.checklist.title,
                            items = checklist.items.map { item ->
                                EditableChecklistItem(
                                    localId = item.id,
                                    text = item.text,
                                    isChecked = item.isChecked
                                )
                            },
                            createdAt = checklist.checklist.createdAt
                        )
                    }
                }
            }
        }
    }

    fun onTitleChanged(title: String) {
        _state.update { it.copy(title = title) }
    }

    fun onAddItem() {
        val newItem = EditableChecklistItem(System.currentTimeMillis(), "", false)
        _state.update { state -> state.copy(items = state.items + newItem) }
    }

    fun onItemRemoved(id: Long) {
        _state.update { it.copy(items = it.items.filterNot { item -> item.localId == id }) }
    }

    fun onItemChecked(id: Long, checked: Boolean) {
        _state.update { state ->
            state.copy(items = state.items.map { item ->
                if (item.localId == id) item.copy(isChecked = checked) else item
            })
        }
    }

    fun onItemTextChanged(id: Long, text: String) {
        _state.update { state ->
            state.copy(items = state.items.map { item ->
                if (item.localId == id) item.copy(text = text) else item
            })
        }
    }

    fun onSaveClicked(emptyTitleMessage: String, emptyItemsMessage: String) {
        val current = _state.value
        val title = current.title.trim()
        if (title.isBlank()) {
            viewModelScope.launch { _events.send(UiEvent.ShowToast(emptyTitleMessage)) }
            return
        }

        val preparedItems = current.items
            .map { it.copy(text = it.text.trim()) }
            .filter { it.text.isNotBlank() }
            .map { ChecklistItemEntity(id = it.localId, checklistId = current.id ?: 0, text = it.text, isChecked = it.isChecked) }

        if (preparedItems.isEmpty()) {
            viewModelScope.launch { _events.send(UiEvent.ShowToast(emptyItemsMessage)) }
            return
        }

        viewModelScope.launch {
            if (current.id == null) {
                repository.insertChecklist(title, preparedItems, createdAt = current.createdAt)
            } else {
                repository.updateChecklist(current.id, title, current.createdAt, preparedItems)
            }
            _events.send(UiEvent.CloseScreen)
        }
    }

    fun onDeleteConfirmed() {
        val id = _state.value.id ?: return
        viewModelScope.launch {
            repository.deleteChecklist(id)
            _events.send(UiEvent.CloseScreen)
        }
    }
}
