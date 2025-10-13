package co.nisari.katisnar.presentation.ui.starnotes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.nisari.katisnar.presentation.data.local.ChecklistItemEntity
import co.nisari.katisnar.presentation.data.local.NoteEntity
import co.nisari.katisnar.presentation.data.local.ChecklistWithItems
import co.nisari.katisnar.presentation.data.repository.ChecklistRepository
import co.nisari.katisnar.presentation.data.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class StarNotesViewModel @Inject constructor(
    noteRepository: NoteRepository,
    checklistRepository: ChecklistRepository
) : ViewModel() {

    enum class Tab { NOTES, CHECKLIST }

    data class UiState(
        val activeTab: Tab = Tab.NOTES,
        val notes: List<NoteListItem> = emptyList(),
        val checklists: List<ChecklistListItem> = emptyList()
    ) {
        val isNotesEmpty: Boolean get() = notes.isEmpty()
        val isChecklistEmpty: Boolean get() = checklists.isEmpty()
    }

    sealed interface UiEvent {
        data class NavigateToNote(val noteId: Long?) : UiEvent
        data class NavigateToChecklist(val checklistId: Long?) : UiEvent
    }

    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())

    private val _activeTab = MutableStateFlow(Tab.NOTES)
    private val _events = Channel<UiEvent>(Channel.BUFFERED)

    private val notesFlow = noteRepository.getAll().map { entities ->
        entities.map { it.toNoteListItem() }
    }

    private val checklistsFlow = checklistRepository.getAll().map { entities ->
        entities.map { it.toChecklistListItem() }
    }

    val uiState: StateFlow<UiState> = combine(_activeTab, notesFlow, checklistsFlow) { tab, notes, checklists ->
        UiState(tab, notes, checklists)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState())

    val events = _events.receiveAsFlow()

    fun onTabSelected(tab: Tab) {
        if (_activeTab.value != tab) {
            _activeTab.value = tab
        }
    }

    fun onAddClicked() {
        viewModelScope.launch {
            when (_activeTab.value) {
                Tab.NOTES -> _events.send(UiEvent.NavigateToNote(null))
                Tab.CHECKLIST -> _events.send(UiEvent.NavigateToChecklist(null))
            }
        }
    }

    fun onNoteClicked(id: Long) {
        viewModelScope.launch {
            _events.send(UiEvent.NavigateToNote(id))
        }
    }

    fun onChecklistClicked(id: Long) {
        viewModelScope.launch {
            _events.send(UiEvent.NavigateToChecklist(id))
        }
    }

    private fun NoteEntity.toNoteListItem(): NoteListItem {
        val created = Date(createdAt)
        return NoteListItem(
            id = id,
            title = text.lineSequence().firstOrNull()?.takeIf { it.isNotBlank() } ?: text.take(40),
            date = dateFormatter.format(created),
            time = timeFormatter.format(created),
            fullText = text
        )
    }

    private fun ChecklistWithItems.toChecklistListItem(): ChecklistListItem {
        val total = items.size
        val checked = items.count(ChecklistItemEntity::isChecked)
        return ChecklistListItem(
            id = checklist.id,
            title = checklist.title,
            summary = "$checked completed",
            secondary = "$total items"
        )
    }
}

data class NoteListItem(
    val id: Long,
    val title: String,
    val date: String,
    val time: String,
    val fullText: String
)

data class ChecklistListItem(
    val id: Long,
    val title: String,
    val summary: String,
    val secondary: String
)
