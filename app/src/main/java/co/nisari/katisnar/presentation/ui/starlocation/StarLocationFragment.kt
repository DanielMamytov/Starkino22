package co.nisari.katisnar.presentation.ui.starlocation

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import co.nisari.katisnar.R
import co.nisari.katisnar.databinding.FragmentStarLocationBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StarLocationFragment : Fragment() {

    private lateinit var binding: FragmentStarLocationBinding
    private val viewModel: LocationListViewModel by viewModels()
    private lateinit var adapter: StarLocationAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentStarLocationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = StarLocationAdapter { location ->
            viewModel.onLocationClick(location.id)
        }

        val recyclerView = view.findViewById<RecyclerView>(R.id.rv_locations)
        recyclerView.adapter = adapter

        // подписка на список
        lifecycleScope.launchWhenStarted {
            viewModel.locations.collect { adapter.submitList(it) }
        }

        // подписка на события UI
        lifecycleScope.launchWhenStarted {
            viewModel.uiEvent.collect { event ->
                when (event) {
                    is UiEvent.NavigateToDetail -> {
                        // переход на экран деталей
                        findNavController().navigate(
                            R.id.action_starLocationFragment_to_starLocationDetailFragment,
                            bundleOf("id" to event.id)
                        )
                    }

                    is UiEvent.NavigateToEdit -> {
                        // переход на экран создания
                        findNavController().navigate(R.id.action_starLocationFragment_to_starLocationDetailFragment)
                    }

                    else -> Unit
                }
            }
        }

        binding.btnAddLog.setOnClickListener {
        // пример: кнопка добавления
            findNavController().navigate(R.id.action_starLocationFragment_to_starLocationDetailFragment)
        }
    }
}
