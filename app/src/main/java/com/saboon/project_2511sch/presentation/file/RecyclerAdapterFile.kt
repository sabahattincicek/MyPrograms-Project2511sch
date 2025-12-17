package com.saboon.project_2511sch.presentation.file

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.saboon.project_2511sch.databinding.RecyclerListRowFileBinding
import com.saboon.project_2511sch.domain.model.File
import com.saboon.project_2511sch.util.toFormattedString

class RecyclerAdapterFile : ListAdapter<File, RecyclerAdapterFile.FileViewHolder>(FileDiffCallback()) {


    var onItemClickListener: ((File) -> Unit)? = null

    class FileViewHolder(private val binding: RecyclerListRowFileBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(item: File){
            binding.tvTitle.text = item.title
            binding.tvDate.text = item.createdAt.toFormattedString("dd.MM.yyyy")
        }
    }
    class FileDiffCallback : DiffUtil.ItemCallback<File>(){
        override fun areItemsTheSame(
            oldItem: File,
            newItem: File
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: File,
            newItem: File
        ): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): FileViewHolder {
        val binding = RecyclerListRowFileBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FileViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: FileViewHolder,
        position: Int
    ) {
        val item = getItem(position)
        holder.bind(item)
        holder.itemView.setOnClickListener {
            onItemClickListener?.invoke(item)
        }
    }
}