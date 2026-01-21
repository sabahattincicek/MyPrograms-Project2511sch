package com.saboon.project_2511sch.presentation.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.saboon.project_2511sch.databinding.RecyclerListRowProgramTableSelectorBinding
import com.saboon.project_2511sch.domain.model.ProgramTable

class RecyclerAdapterDialogFragmentProgramTableSelector : ListAdapter<ProgramTable, RecyclerAdapterDialogFragmentProgramTableSelector.DialogFragmentHomeViewHolder>(
    DialogFragmentHomeDiffCallback()
) {

    var onItemClickListener: ((ProgramTable) -> Unit)? = null
    var onItemCheckedChangeListener: ((Boolean, ProgramTable) -> Unit)? = null

    inner class DialogFragmentHomeViewHolder(private val binding: RecyclerListRowProgramTableSelectorBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ProgramTable){
            binding.tvProgramTableTitle.text = item.title
            binding.tvProgramTableDescription.text = item.description
            binding.ckbProgramTable.setOnCheckedChangeListener(null)
            binding.ckbProgramTable.isChecked = item.isActive
            binding.ckbProgramTable.setOnCheckedChangeListener { buttonView, isChecked ->
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

    open class DialogFragmentHomeDiffCallback: DiffUtil.ItemCallback<ProgramTable>() {
        override fun areItemsTheSame(
            oldItem: ProgramTable,
            newItem: ProgramTable
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: ProgramTable,
            newItem: ProgramTable
        ): Boolean {
            return oldItem == newItem
        }

    }
}