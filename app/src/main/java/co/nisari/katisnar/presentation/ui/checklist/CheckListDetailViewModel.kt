package co.nisari.katisnar.presentation.ui.checklist

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
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CheckListDetailViewModel @Inject constructor(
    private val repository: ChecklistRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    data class ChecklistItemUi(
        val id: Long,
        val checklistId: Long,
        val text: String,
        val isChecked: Boolean
    )

    data class UiState(
        val id: Long,
        val title: String = "",
        val items: List<ChecklistItemUi> = emptyList()
    )

    sealed interface UiEvent {
        data class CloseScreen(val showToast: Boolean) : UiEvent
    }

    private val checklistId: Long = savedStateHandle.get<Long>("checklistId")?.takeIf { it > 0 }
        ?: throw IllegalStateException("Checklist id is required")

    private val _state = MutableStateFlow(UiState(id = checklistId))
    val state: StateFlow<UiState> = _state.asStateFlow()

    private val _events = Channel<UiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private var shouldShowDeletionToast = false

    init {
        viewModelScope.launch {
            repository.getById(checklistId).collect { checklist ->
                if (checklist == null) {
                    _events.send(UiEvent.CloseScreen(shouldShowDeletionToast))
                    shouldShowDeletionToast = false
                } else {
                    _state.update { current ->
                        current.copy(
                            title = checklist.checklist.title,
                            items = checklist.items.map { item ->
                                ChecklistItemUi(
                                    id = item.id,
                                    checklistId = checklist.checklist.id,
                                    text = item.text,
                                    isChecked = item.isChecked
                                )
                            }
                        )
                    }
                }
            }
        }
    }

    fun onItemChecked(itemId: Long, isChecked: Boolean) {
        val currentItem = _state.value.items.firstOrNull { it.id == itemId } ?: return
        _state.update { state ->
            state.copy(
                items = state.items.map { item ->
                    if (item.id == itemId) item.copy(isChecked = isChecked) else item
                }
            )
        }
        viewModelScope.launch {
            repository.updateItem(
                ChecklistItemEntity(
                    id = itemId,
                    checklistId = currentItem.checklistId,
                    text = currentItem.text,
                    isChecked = isChecked
                )
            )
        }
    }

    fun onDeleteConfirmed() {
        viewModelScope.launch {
            shouldShowDeletionToast = true
            repository.deleteChecklist(checklistId)
        }
    }
}
