package com.saboon.project_2511sch.presentation.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.saboon.project_2511sch.databinding.RecyclerListRowDialogFragmentHomeBinding
import com.saboon.project_2511sch.domain.model.ProgramTable

class RecyclerAdapterDialogFragmentProgramTableSelector : ListAdapter<ProgramTable, RecyclerAdapterDialogFragmentProgramTableSelector.DialogFragmentHomeViewHolder>(
    DialogFragmentHomeDiffCallback()
) {

    var onItemClickListener: ((ProgramTable) -> Unit)? = null

    class DialogFragmentHomeViewHolder(private val binding: RecyclerListRowDialogFragmentHomeBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ProgramTable){
            binding.tvProgramTableTitle.text = item.title
            binding.tvProgramTableDescription.text = item.description
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DialogFragmentHomeViewHolder {
        val binding = RecyclerListRowDialogFragmentHomeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DialogFragmentHomeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DialogFragmentHomeViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
        holder.itemView.setOnClickListener {
            onItemClickListener?.invoke(item)
        }
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