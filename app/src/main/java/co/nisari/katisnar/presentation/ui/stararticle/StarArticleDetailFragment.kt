package co.nisari.katisnar.presentation.ui.stararticle

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import co.nisari.katisnar.databinding.FragmentStarArticleDetailBinding
import co.nisari.katisnar.presentation.ui.stararticle.model.StarArticleDataSource
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

@AndroidEntryPoint
class StarArticleDetailFragment : Fragment() {

    private var _binding: FragmentStarArticleDetailBinding? = null
    private val binding get() = _binding!!
    private val args: StarArticleDetailFragmentArgs by navArgs()


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
        setupToolbar()
        renderArticle()
    }


    private fun setupToolbar() {
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun renderArticle() {
        val article = StarArticleDataSource.getArticleById(args.articleId)
            ?: run {
                findNavController().popBackStack()
                return
            }

        binding.articleTitle.text = article.title.uppercase(Locale.getDefault())
        binding.txtContent.text = article.content
        binding.imgCover.setImageResource(article.coverResId)
    }




    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
