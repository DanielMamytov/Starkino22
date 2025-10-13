package co.nisari.katisnar.presentation.ui.starroutine

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import co.nisari.katisnar.R
import co.nisari.katisnar.databinding.FragmentStarNoteBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StarNotesFragment : Fragment() {

    private lateinit var binding: FragmentStarNoteBinding

    private val notesAdapter by lazy { NotesAdapter() }
    private val checklistAdapter by lazy { ChecklistAdapter() }

    private var showingNotes = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentStarNoteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // начальное состояние
        setActiveTab(isNotes = true)

        binding.btnNotes.setOnClickListener {
            if (!showingNotes) setActiveTab(true)
        }

        binding.btnChecklist.setOnClickListener {
            if (showingNotes) setActiveTab(false)
        }

        binding.btnAddGoal.setOnClickListener {
            if (showingNotes) {
                Toast.makeText(requireContext(), "Add Note clicked", Toast.LENGTH_SHORT).show()
                // Навигация к NoteEditScreen
            } else {
                Toast.makeText(requireContext(), "Add Checklist clicked", Toast.LENGTH_SHORT).show()
                // Навигация к ChecklistEditScreen
            }
        }
    }

    private fun setActiveTab(isNotes: Boolean) {
        showingNotes = isNotes

        // Обновляем кнопки
        binding.btnNotes.isChecked = isNotes
        binding.btnChecklist.isChecked = !isNotes

        // Меняем адаптер списка
        binding.rvLocations.adapter = if (isNotes) notesAdapter else checklistAdapter

        // Меняем текст кнопки
        binding.btnAddGoal.text = if (isNotes) "Add note" else "Add checklist"
    }
}
