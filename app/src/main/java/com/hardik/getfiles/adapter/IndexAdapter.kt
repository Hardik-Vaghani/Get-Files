package com.hardik.getfiles.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.hardik.getfiles.databinding.ItemIndexPreviewBinding
import java.io.File

class IndexAdapter: RecyclerView.Adapter<IndexAdapter.IndexViewHolder>(), Filterable {
    private val TAG = "BASE_TAG" + IndexAdapter::class.java
    inner class IndexViewHolder(val binding: ItemIndexPreviewBinding):RecyclerView.ViewHolder(binding.root)

    private val differCallback = object: DiffUtil.ItemCallback<File>(){
        override fun areItemsTheSame(
            oldItem: File,
            newItem: File
        ): Boolean {
            return oldItem.absolutePath == newItem.absolutePath && oldItem.path == newItem.path
        }

        override fun areContentsTheSame(
            oldItem: File,
            newItem: File
        ): Boolean {
            return oldItem == newItem
        }

    }
    val differ = AsyncListDiffer(this@IndexAdapter,differCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IndexViewHolder {
        return IndexViewHolder(ItemIndexPreviewBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: IndexViewHolder, position: Int) {
        val indexItem = differ.currentList[position]

        holder.binding.itemIndexPreviewTvTitle.text = indexItem.absolutePath
        holder.binding.itemIndexPreviewTvTitle.setHorizontallyScrolling(true)
        holder.binding.itemIndexPreviewTvTitle.isSelected = true

        // Change item width based on position or other logic
        val layoutParams = holder.itemView.layoutParams
        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT // Full width for odd positions
        holder.itemView.layoutParams = layoutParams

        holder.itemView.setOnClickListener {
            onItemClickListener?.invoke(indexItem)
        }

    }

    private var onItemClickListener : ((File) -> Unit)? = null

    fun setOnItemClickListener(listener: (File) -> Unit){
        onItemClickListener = listener
    }

    // Function to clear selection
    fun clearSelection() {
        val currentList = differ.currentList
        currentList.forEach { it }
        setOriginalList(currentList)
    }

    private var originalList: List<File> = emptyList()

    // Function to set the original list
    fun setOriginalList(list: List<File>) {
        originalList = list
//        differ.submitList(list)
        differ.submitList(originalList.map { it })// Restore isSelected state from originalList
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filteredList = if (constraint.isNullOrEmpty()) {
                    originalList
                } else {
                    originalList.filter {
                        it.absolutePath.contains(constraint, true) ||
                                it.path.contains(constraint, true)
                    }
                }

                val filterResults = FilterResults()
                filterResults.values = filteredList
                return filterResults
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                differ.submitList(results?.values as List<File>?)
            }
        }
    }

}