package co.nisari.katisnar.presentation.ui.stararticle

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.nisari.katisnar.R
import co.nisari.katisnar.presentation.data.repository.ArticleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class StarArticleDetailViewModel @Inject constructor(
    private val repository: ArticleRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    data class UiState(
        val title: String = "",
        val content: String = "",
        val coverResId: Int = R.drawable.img_night_city,
        val isLoading: Boolean = true,
    )

    sealed interface UiEvent {
        data object ArticleNotFound : UiEvent
        data object CloseScreen : UiEvent
    }

    private val articleId = savedStateHandle.get<Long>("articleId")?.takeIf { it > 0 }

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<UiEvent>()
    val events = _events.asSharedFlow()

    private var observeJob: Job? = null

    init {
        if (articleId == null) {
            viewModelScope.launch {
                _events.emit(UiEvent.ArticleNotFound)
                _events.emit(UiEvent.CloseScreen)
            }
        } else {
            observeArticle(articleId)
        }
    }

    private fun observeArticle(id: Long) {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            repository.getById(id).collectLatest { entity ->
                if (entity == null) {
                    _state.update { it.copy(isLoading = false) }
                    _events.emit(UiEvent.ArticleNotFound)
                    _events.emit(UiEvent.CloseScreen)
                    return@collectLatest
                }

                _state.update {
                    it.copy(
                        title = entity.title,
                        content = entity.content,
                        coverResId = entity.coverUri ?: R.drawable.img_night_city,
                        isLoading = false
                    )
                }
            }
        }
    }
}
