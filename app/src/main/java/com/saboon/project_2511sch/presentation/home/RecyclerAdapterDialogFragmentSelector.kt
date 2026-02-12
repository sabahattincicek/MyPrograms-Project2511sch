package com.saboon.project_2511sch.presentation.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.saboon.project_2511sch.databinding.RecyclerListRowProgramTableSelectorBinding
import com.saboon.project_2511sch.domain.model.BaseModel

class RecyclerAdapterDialogFragmentSelector : ListAdapter<BaseModel, RecyclerAdapterDialogFragmentSelector.DialogFragmentHomeViewHolder>(DialogFragmentHomeDiffCallback()) {
    var onItemCheckedChangeListener: ((Boolean, BaseModel) -> Unit)? = null

    inner class DialogFragmentHomeViewHolder(private val binding: RecyclerListRowProgramTableSelectorBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(item: BaseModel){
            binding.tvTitle.text = item.title
            binding.tvDescription.text = item.description
            binding.ckbFilter.setOnCheckedChangeListener(null)
            binding.ckbFilter.isChecked = item.isActive
            binding.ckbFilter.setOnCheckedChangeListener { buttonView, isChecked ->
                onItemCheckedChangeListener?.invoke(isChecked, item)
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DialogFragmentHomeViewHolder {
        val binding = RecyclerListRowProgramTableSelectorBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DialogFragmentHomeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DialogFragmentHomeViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    open class DialogFragmentHomeDiffCallback: DiffUtil.ItemCallback<BaseModel>() {
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