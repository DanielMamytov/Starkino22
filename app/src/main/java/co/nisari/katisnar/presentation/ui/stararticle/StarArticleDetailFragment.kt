package co.nisari.katisnar.presentation.ui.stararticle

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import co.nisari.katisnar.R
import co.nisari.katisnar.databinding.FragmentStarArticleDetailBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class StarArticleDetailFragment : Fragment() {

    private var _binding: FragmentStarArticleDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: StarArticleDetailViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentStarArticleDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener { findNavController().popBackStack() }

        val articleId = arguments?.getLong("articleId")
        if (articleId == null) {
            Toast.makeText(requireContext(), R.string.article_not_found, Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
            return
        }

        viewModel.loadArticle(articleId)

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.article.collectLatest { state ->
                state?.let {
                    binding.articleTitle.text = it.title
                    binding.txtContent.text = it.content
                    binding.imgCover.setImageResource(it.coverResId)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.events.collectLatest { event ->
                when (event) {
                    StarArticleDetailViewModel.ArticleDetailEvent.ArticleNotFound -> {
                        Toast.makeText(
                            requireContext(),
                            R.string.article_not_found,
                            Toast.LENGTH_SHORT
                        ).show()
                        findNavController().popBackStack()
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
