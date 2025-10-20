package co.nisari.katisnar.presentation.ui.starlocation

import android.net.Uri


sealed class UiEvent {
    object NavigateBack : UiEvent()
    object NavigateToList : UiEvent()

    data class NavigateToDetail(val id: Long) : UiEvent()

    data class NavigateToEdit(val id: Long?) : UiEvent()

    data class ShowDeleteDialog(val id: Long) : UiEvent()

    data class OpenMaps(val lat: Double, val lng: Double, val name: String) : UiEvent()

    data class OpenMaps1(val uri: Uri) : UiEvent()

    data class ShowToast(val message: String) : UiEvent()
}
