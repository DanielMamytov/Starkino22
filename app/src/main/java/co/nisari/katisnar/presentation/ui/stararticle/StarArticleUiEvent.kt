package co.nisari.katisnar.presentation.ui.stararticle

sealed interface StarArticleUiEvent {
    data class NavigateToDetail(val id: Long) : StarArticleUiEvent
}
