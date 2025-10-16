package co.nisari.katisnar.presentation.ui.stararticle

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import co.nisari.katisnar.R
import co.nisari.katisnar.databinding.FragmentStarArticleBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class StarArticleFragment : Fragment() {

    private var _binding: FragmentStarArticleBinding? = null
    private val binding get() = _binding!!

    private val viewModel: StarArticleListViewModel by viewModels()

    private val articlesAdapter by lazy { StarArticleAdapter(viewModel::onArticleClicked) }

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

        setupUi()
        observeState()
        observeEvents()
    }

    private fun setupUi() {
        binding.rvArticles.adapter = articlesAdapter
        binding.btnBack.setOnClickListener { findNavController().popBackStack() }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collectLatest { state ->
                    articlesAdapter.submitList(state.articles)
                }
            }
        }
    }

    private fun observeEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.collectLatest { event ->
                    when (event) {
                        is StarArticleListViewModel.UiEvent.NavigateToArticle ->
                            navigateToArticleDetail(event.articleId)
                    }
                }
            }
        }
    }

    private fun navigateToArticleDetail(articleId: Long) {
        val actionId = R.id.action_starArticleFragment_to_starArticleDetailFragment
        val args = Bundle().apply { putLong("articleId", articleId) }
        findNavController().navigate(actionId, args)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
