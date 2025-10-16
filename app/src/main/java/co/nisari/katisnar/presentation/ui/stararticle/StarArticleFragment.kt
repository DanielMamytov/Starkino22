package co.nisari.katisnar.presentation.ui.stararticle

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import co.nisari.katisnar.databinding.FragmentStarArticleBinding
import co.nisari.katisnar.presentation.ui.stararticle.model.ArticleListItem
import co.nisari.katisnar.presentation.ui.stararticle.model.StarArticleDataSource
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StarArticleFragment : Fragment() {

    private var _binding: FragmentStarArticleBinding? = null
    private val binding get() = _binding!!
    private val articleAdapter by lazy {
        StarArticleAdapter(::onArticleClicked)
    }


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
        setupRecyclerView()
        setupToolbar()
        articleAdapter.submitList(StarArticleDataSource.articles)
    }


    private fun setupRecyclerView() {
        binding.rvArticles.adapter = articleAdapter
    }

    private fun setupToolbar() {
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun onArticleClicked(article: ArticleListItem) {
        val direction = StarArticleFragmentDirections
            .actionStarArticleFragmentToStarArticleDetailFragment(article.id)
        findNavController().navigate(direction)
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
