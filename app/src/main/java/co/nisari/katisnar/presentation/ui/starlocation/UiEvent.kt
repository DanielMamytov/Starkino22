package co.nisari.katisnar.presentation.ui.starlocation


sealed class UiEvent {
    object NavigateBack : UiEvent()

    // Список/карточка → детали
    data class NavigateToDetail(val id: Long) : UiEvent()

    // Детали/список → редактирование (id = null ⇒ создание)
    data class NavigateToEdit(val id: Long?) : UiEvent()

    // Детали → показать диалог удаления
    data class ShowDeleteDialog(val id: Long) : UiEvent()

    // Детали → открыть карты
    data class OpenMaps(val lat: Double, val lng: Double, val name: String) : UiEvent()

    // Тосты
    data class ShowToast(val message: String) : UiEvent()
}
