package com.example.researcherapp.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.researcherapp.data.model.Article
import com.example.researcherapp.databinding.ItemScienceBinding

class ScienceAdapter(private val articles: List<Article>) :
    RecyclerView.Adapter<ScienceAdapter.ArticleViewHolder>() {

    inner class ArticleViewHolder(private val binding: ItemScienceBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(article: Article) {
            binding.articleTitle.text = "Title: ${article.title}"
            binding.articleLink.text = "DOI: ${article.doi}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
        val binding = ItemScienceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ArticleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        holder.bind(articles[position])
    }

    override fun getItemCount(): Int = articles.size
}
