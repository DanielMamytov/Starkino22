package co.nisari.katisnar.presentation.ui.stararticle

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import co.nisari.katisnar.R
import co.nisari.katisnar.databinding.FragmentStarArticleBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class StarArticleFragment : Fragment() {

    private var _binding: FragmentStarArticleBinding? = null
    private val binding get() = _binding!!
    private val viewModel: StarArticleListViewModel by viewModels()
    private lateinit var adapter: StarArticleAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentStarArticleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = StarArticleAdapter { article ->
            viewModel.onArticleClick(article.id)
        }

        binding.rvArticles.adapter = adapter
        binding.btnBack.setOnClickListener { findNavController().popBackStack() }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.articles.collectLatest { articles ->
                adapter.submitList(articles)
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.events.collectLatest { event ->
                when (event) {
                    is StarArticleUiEvent.NavigateToDetail -> {
                        findNavController().navigate(
                            R.id.action_starArticleFragment_to_starArticleDetailFragment,
                            bundleOf("articleId" to event.id)
                        )
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
