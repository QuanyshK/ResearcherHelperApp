package com.example.researcherapp.adapter

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.researcherapp.data.model.ArxivEntry
import com.example.researcherapp.databinding.ItemArticleBinding

class ArxivAdapter : ListAdapter<ArxivEntry, ArxivAdapter.ArxivViewHolder>(
    object : DiffUtil.ItemCallback<ArxivEntry>() {
        override fun areItemsTheSame(oldItem: ArxivEntry, newItem: ArxivEntry) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: ArxivEntry, newItem: ArxivEntry) = oldItem == newItem
    }
) {
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArxivViewHolder {
        val binding = ItemArticleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ArxivViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ArxivViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
