package co.nisari.katisnar.presentation.ui.stararticle

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import co.nisari.katisnar.R
import co.nisari.katisnar.databinding.ItemArticleBinding

class StarArticleAdapter(
    private val onArticleClick: (ArticleListItem) -> Unit
) : ListAdapter<ArticleListItem, StarArticleAdapter.ViewHolder>(DiffCallback) {

    object DiffCallback : DiffUtil.ItemCallback<ArticleListItem>() {
        override fun areItemsTheSame(oldItem: ArticleListItem, newItem: ArticleListItem): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: ArticleListItem, newItem: ArticleListItem): Boolean =
            oldItem == newItem
    }

    inner class ViewHolder(private val binding: ItemArticleBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ArticleListItem) {
            with(binding) {
                txtTitle.text = item.title
                txtPreview.text = item.preview
                imgCover.setImageResource(R.drawable.img_night_city)
                root.setOnClickListener { onArticleClick(item) }
                btnMoreDetails.setOnClickListener { onArticleClick(item) }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemArticleBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
