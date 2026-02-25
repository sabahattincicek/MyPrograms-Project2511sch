package com.saboon.project_2511sch.presentation.programtable

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.saboon.project_2511sch.databinding.RowGeneralItemBinding
import com.saboon.project_2511sch.databinding.RowSingleTextViewBinding
import com.saboon.project_2511sch.domain.model.ProgramTable
import com.saboon.project_2511sch.util.BaseDiffCallback
import com.saboon.project_2511sch.util.BaseDisplayListItem
import com.saboon.project_2511sch.util.BaseViewHolder
import com.saboon.project_2511sch.util.toFormattedString

class RecyclerAdapterProgramTables :
    ListAdapter<DisplayItemProgramTable, BaseViewHolder>(BaseDiffCallback<DisplayItemProgramTable>()) {

    var onItemClickListener: ((ProgramTable) -> Unit)? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when(viewType){
            BaseDisplayListItem.VIEW_TYPE_CONTENT -> {
                val binding = RowGeneralItemBinding.inflate(inflater, parent, false)
                ContentProgramTableViewHolder(binding)
            }

            BaseDisplayListItem.VIEW_TYPE_FOOTER -> {
                val binding = RowSingleTextViewBinding.inflate(inflater, parent, false)
                FooterProgramTableViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Unknown viewType")
        }
    }

    override fun onBindViewHolder(
        holder: BaseViewHolder,
        position: Int
    ) {
        val item = getItem(position)
        holder.onItemClickListener = { baseItem ->
            if (baseItem is DisplayItemProgramTable.ContentProgramTable){
                onItemClickListener?.invoke(baseItem.programTable)
            }
        }
        holder.bind(item)
    }

    override fun getItemViewType(position: Int): Int {
        return getItem(position).viewType
    }

    inner class ContentProgramTableViewHolder(private val binding: RowGeneralItemBinding): BaseViewHolder(binding.root){
        override fun bind(item: BaseDisplayListItem) {
            super.bind(item) //for click logic
            if (item is DisplayItemProgramTable.ContentProgramTable){
                binding.tvContent1.text = item.programTable.title
                binding.tvContent1Sub.text = item.programTable.description
                binding.tvContent2.visibility = View.GONE
                binding.tvContent2Sub.text = item.programTable.createdAt.toFormattedString("MMM yyyy")
                if (!item.programTable.isActive) binding.llContainer.alpha = 0.3f
                else binding.llContainer.alpha = 1.0f
            }
        }
    }
    inner class FooterProgramTableViewHolder(private val binding: RowSingleTextViewBinding): BaseViewHolder(binding.root){
        override fun bind(item: BaseDisplayListItem) {
            super.bind(item)
            if (item is DisplayItemProgramTable.FooterProgramTable){
//                binding.tvContent.text = "Count: ${item.count}"
                binding.tvContent.text = ""
            }
        }
    }

}
