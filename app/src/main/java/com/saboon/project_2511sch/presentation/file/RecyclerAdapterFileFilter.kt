package com.saboon.project_2511sch.presentation.file

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.saboon.project_2511sch.databinding.RecyclerListRowFileFilterBinding
import com.saboon.project_2511sch.domain.model.BaseModel

class RecyclerAdapterFileFilter: ListAdapter<BaseModel, RecyclerAdapterFileFilter.FileFilterViewHolder>(FileFilterDiffCallback()) {
    var onClickItemListener: ((BaseModel) -> Unit)? = null
    class FileFilterViewHolder(private val binding: RecyclerListRowFileFilterBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(item: BaseModel){
            binding.tvTitle.text = item.title
            binding.tvDescription.text = item.description
        }
    }
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): FileFilterViewHolder {
        val binding = RecyclerListRowFileFilterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FileFilterViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: FileFilterViewHolder,
        position: Int
    ) {
        val item = getItem(position)
        holder.bind(item)
        holder.itemView.setOnClickListener {
            onClickItemListener?.invoke(item)
        }
    }
    open class FileFilterDiffCallback: DiffUtil.ItemCallback<BaseModel>() {
        override fun areItemsTheSame(
            oldItem: BaseModel,
            newItem: BaseModel
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: BaseModel,
            newItem: BaseModel
        ): Boolean {
            return oldItem == newItem
        }

    }
}