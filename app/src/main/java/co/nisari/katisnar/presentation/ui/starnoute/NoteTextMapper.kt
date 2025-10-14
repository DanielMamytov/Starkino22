package co.nisari.katisnar.presentation.ui.starnoute

internal object NoteTextMapper {

    fun split(text: String): Pair<String, String> {
        val index = text.indexOf('\n')
        return if (index == -1) {
            text to ""
        } else {
            val name = text.substring(0, index)
            val notes = text.substring(index + 1)
            name to notes
        }
    }

    fun combine(name: String, notes: String): String {
        val trimmedName = name.trim()
        val trimmedNotes = notes.trim()
        return when {
            trimmedName.isBlank() -> trimmedNotes
            trimmedNotes.isBlank() -> trimmedName
            else -> "$trimmedName\n$trimmedNotes"
        }
    }
}
