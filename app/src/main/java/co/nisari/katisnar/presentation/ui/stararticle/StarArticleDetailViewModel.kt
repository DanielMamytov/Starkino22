package co.nisari.katisnar.presentation.ui.stararticle

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
import kotlinx.coroutines.launch

@HiltViewModel
class StarArticleDetailViewModel @Inject constructor(
    private val repository: ArticleRepository
) : ViewModel() {

    private val _article = MutableStateFlow<ArticleDetailUiState?>(null)
    val article: StateFlow<ArticleDetailUiState?> = _article.asStateFlow()

    private val _events = MutableSharedFlow<ArticleDetailEvent>()
    val events = _events.asSharedFlow()

    private var loadJob: Job? = null
    private var currentArticleId: Long? = null
    private var notFoundEmitted = false

    fun loadArticle(id: Long) {
        if (currentArticleId == id && loadJob != null) {
            return
        }
        loadJob?.cancel()
        currentArticleId = id
        notFoundEmitted = false
        loadJob = viewModelScope.launch {
            repository.getById(id).collect { entity ->
                if (entity == null) {
                    _article.value = null
                    if (!notFoundEmitted) {
                        notFoundEmitted = true
                        _events.emit(ArticleDetailEvent.ArticleNotFound)
                    }
                } else {
                    notFoundEmitted = false
                    _article.value = ArticleDetailUiState(
                        title = entity.title,
                        content = entity.content,
                        coverResId = entity.coverUri ?: DEFAULT_DETAIL_COVER_RES
                    )
                }
            }
        }
    }

    data class ArticleDetailUiState(
        val title: String,
        val content: String,
        val coverResId: Int
    )

    sealed interface ArticleDetailEvent {
        object ArticleNotFound : ArticleDetailEvent
    }

    companion object {
        private val DEFAULT_DETAIL_COVER_RES = R.drawable.img_article
    }
}
