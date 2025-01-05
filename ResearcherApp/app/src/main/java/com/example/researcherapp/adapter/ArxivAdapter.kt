package com.example.researcherapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.researcherapp.R
import com.example.researcherapp.data.model.ArxivEntry
import com.example.researcherapp.databinding.ItemArticleBinding

class ArxivAdapter(
    private val onDetailsClick: (ArxivEntry) -> Unit
) : ListAdapter<ArxivEntry, RecyclerView.ViewHolder>(ArxivItemCallback()) {

    companion object {
        private const val VIEW_TYPE_ITEM = 0
        private const val VIEW_TYPE_LOADING = 1
    }

    private var isLoading = false

    override fun getItemViewType(position: Int): Int {
        return if (position == itemCount - 1 && isLoading) VIEW_TYPE_LOADING else VIEW_TYPE_ITEM
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_ITEM) {
            ViewHolder(
                ItemArticleBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent, false
                )
            )
        } else {
            LoadingViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.item_loading_footer, parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ViewHolder) {
            holder.bind(getItem(position))
        }
    }

    override fun getItemCount(): Int {
        return super.getItemCount() + if (isLoading) 1 else 0
    }

    fun setLoading(isLoading: Boolean) {
        val previousState = this.isLoading
        this.isLoading = isLoading

        if (isLoading && !previousState) {
            notifyItemInserted(itemCount - 1)
        } else if (!isLoading && previousState) {
            notifyItemRemoved(itemCount)
        }
    }

    inner class ViewHolder(
        private val binding: ItemArticleBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(entry: ArxivEntry) {
            with(binding) {
                textViewTitle.text = entry.title
                textViewSummary.text = entry.summary
                buttonDownload.text = "Details"
                buttonDownload.setOnClickListener {
                    onDetailsClick(entry)
                }
            }
        }
    }

    class LoadingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}

private class ArxivItemCallback : DiffUtil.ItemCallback<ArxivEntry>() {
    override fun areItemsTheSame(oldItem: ArxivEntry, newItem: ArxivEntry): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: ArxivEntry, newItem: ArxivEntry): Boolean {
        return oldItem == newItem
    }
}
