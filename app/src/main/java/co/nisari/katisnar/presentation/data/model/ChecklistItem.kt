package co.nisari.katisnar.presentation.data.model

data class ChecklistItem(
    val id: Long = System.currentTimeMillis(),
    var text: String,
    var checked: Boolean = false
)
