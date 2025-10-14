package co.nisari.katisnar.presentation.ui.checklist

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import co.nisari.katisnar.R
import co.nisari.katisnar.databinding.FragmentChecklistEditBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder


class StarChecklistEditFragment : Fragment() {

    private lateinit var binding: FragmentChecklistEditBinding
    private lateinit var adapter: ChecklistAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChecklistEditBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ChecklistAdapter(mutableListOf()) { item ->
            // здесь можно сохранять состояние в БД / ViewModel
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        binding.btnAddItem.setOnClickListener {
            showAddDialog { text ->
                if (text.isNotBlank()) adapter.add(text)
            }
        }
    }

    private fun showAddDialog(onOk: (String) -> Unit) {
        val input = EditText(requireContext()).apply {
            hint = "Введите текст пункта"
            setTextColor(Color.WHITE)
            setHintTextColor(Color.parseColor("#9E9E9E"))
        }
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Новый пункт")
            .setView(input)
            .setPositiveButton("Добавить") { _, _ -> onOk(input.text.toString()) }
            .setNegativeButton("Отмена", null)
            .show()
    }
}