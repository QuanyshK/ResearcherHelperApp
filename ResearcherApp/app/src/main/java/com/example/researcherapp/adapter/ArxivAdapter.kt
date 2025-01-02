package com.example.researcherapp.adapter

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.researcherapp.data.model.ArxivEntry
import com.example.researcherapp.databinding.ItemArticleBinding
import com.example.researcherapp.databinding.ItemLoadingFooterBinding

private const val VIEW_TYPE_ITEM = 0
private const val VIEW_TYPE_LOADING = 1

class ArxivAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items = mutableListOf<ArxivEntry?>()

    fun submitList(newItems: List<ArxivEntry>, isLoading: Boolean) {
        items.clear()
        items.addAll(newItems)
        if (isLoading) {
            items.add(null)  // Добавляем null для футера
        }
        notifyDataSetChanged()
    }

    fun getCurrentList(): List<ArxivEntry> {
        return items.filterNotNull()
    }

    override fun getItemViewType(position: Int): Int {
        return if (items[position] == null) VIEW_TYPE_LOADING else VIEW_TYPE_ITEM
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_ITEM) {
            val binding = ItemArticleBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            ArxivViewHolder(binding)
        } else {
            val binding = ItemLoadingFooterBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            LoadingViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ArxivViewHolder) {
            items[position]?.let { holder.bind(it) }
        }
    }

    override fun getItemCount(): Int = items.size

    class ArxivViewHolder(private val binding: ItemArticleBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(entry: ArxivEntry) {
            binding.textViewTitle.text = entry.title
            binding.textViewSummary.text = entry.summary
            val pdfLink = entry.link.find { it.title == "pdf" }?.href
            binding.buttonDownload.setOnClickListener {
                pdfLink?.let {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(it))
                    binding.root.context.startActivity(intent)
                }
            }
        }
    }

    class LoadingViewHolder(binding: ItemLoadingFooterBinding) :
        RecyclerView.ViewHolder(binding.root)
}