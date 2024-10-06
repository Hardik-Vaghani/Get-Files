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

class IndexTypeAdapter: RecyclerView.Adapter<IndexTypeAdapter.IndexViewHolder>(){
    private val TAG = "BASE_TAG" + IndexTypeAdapter::class.java
    inner class IndexViewHolder(val binding: ItemIndexPreviewBinding):RecyclerView.ViewHolder(binding.root)

    private val differCallback = object: DiffUtil.ItemCallback<String>(){
        override fun areItemsTheSame(
            oldItem: String,
            newItem: String
        ): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(
            oldItem: String,
            newItem: String
        ): Boolean {
            return oldItem == newItem
        }

    }
    val differ = AsyncListDiffer(this@IndexTypeAdapter,differCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IndexViewHolder {
        return IndexViewHolder(ItemIndexPreviewBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: IndexViewHolder, position: Int) {
        val indexItem = differ.currentList[position]

        holder.binding.itemIndexPreviewTvTitle.text = indexItem
//        holder.binding.itemIndexPreviewTvTitle.setHorizontallyScrolling(true)
//        holder.binding.itemIndexPreviewTvTitle.isSelected = true

        // Change item width based on position or other logic
        val layoutParams = holder.itemView.layoutParams
        layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT // Wrap content width for odd positions
        holder.itemView.layoutParams = layoutParams

        holder.itemView.setOnClickListener {
            onItemClickListener?.invoke(indexItem)
        }

    }

    private var onItemClickListener : ((String) -> Unit)? = null

    fun setOnItemClickListener(listener: (String) -> Unit){
        onItemClickListener = listener
    }

    // Function to clear selection
    fun clearSelection() {
        val currentList = differ.currentList
        currentList.forEach { it }
        setOriginalList(currentList)
    }

    private var originalList: List<String> = emptyList()

    // Function to set the original list
    fun setOriginalList(list: List<String>) {
        originalList = list
//        differ.submitList(list)
        differ.submitList(originalList.map { it })// Restore isSelected state from originalList
    }

}