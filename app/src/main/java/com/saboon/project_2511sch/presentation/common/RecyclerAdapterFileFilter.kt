package com.saboon.project_2511sch.presentation.common

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.saboon.project_2511sch.databinding.RowGeneralItemBinding
import com.saboon.project_2511sch.databinding.RowSingleTextViewBinding
import com.saboon.project_2511sch.databinding.RowSingleTextViewLeftBinding
import com.saboon.project_2511sch.domain.model.BaseModel
import com.saboon.project_2511sch.presentation.course.DisplayItemCourse
import com.saboon.project_2511sch.presentation.programtable.DisplayItemProgramTable
import com.saboon.project_2511sch.presentation.task.DisplayItemTask
import com.saboon.project_2511sch.util.BaseDiffCallback
import com.saboon.project_2511sch.util.BaseDisplayListItem
import com.saboon.project_2511sch.util.BaseViewHolder
import com.saboon.project_2511sch.util.toFormattedString

class RecyclerAdapterFileFilter: ListAdapter<BaseDisplayListItem, BaseViewHolder>(BaseDiffCallback()) {
    var onClickItemListener: ((BaseModel) -> Unit)? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when(viewType){
            BaseDisplayListItem.VIEW_TYPE_HEADER -> {
                val binding = RowSingleTextViewLeftBinding.inflate(inflater, parent, false)
                HeaderFilterViewHolder(binding)
            }
            BaseDisplayListItem.VIEW_TYPE_CONTENT -> {
                val binding = RowGeneralItemBinding.inflate(inflater, parent, false)
                ContentFilterViewHolder(binding)
            }

            BaseDisplayListItem.VIEW_TYPE_FOOTER -> {
                val binding = RowSingleTextViewBinding.inflate(inflater, parent, false)
                FooterFilterViewHolder(binding)
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
            when(baseItem){
                is DisplayItemProgramTable.ContentProgramTable -> {
                    onClickItemListener?.invoke(baseItem.programTable)
                }
                is DisplayItemCourse.ContentCourse -> {
                    onClickItemListener?.invoke(baseItem.course)
                }
                is DisplayItemTask.ContentTask -> {
                    onClickItemListener?.invoke(baseItem.task)
                }
            }
        }
        holder.bind(item)
    }
    override fun getItemViewType(position: Int): Int {
        return getItem(position).viewType
    }

    inner class HeaderFilterViewHolder(private val binding: RowSingleTextViewLeftBinding): BaseViewHolder(binding.root){
        override fun bind(item: BaseDisplayListItem) {
            super.bind(item)
            when(item){
                is DisplayItemCourse.HeaderCourse -> {
                    binding.tvContent.text = item.title
                }
                is DisplayItemTask.HeaderTask -> {
                    binding.tvContent.text = item.taskType
                }
            }
        }
    }
    inner class ContentFilterViewHolder(private val binding: RowGeneralItemBinding): BaseViewHolder(binding.root){
        override fun bind(item: BaseDisplayListItem) {
            super.bind(item)
            when(item){
                is DisplayItemProgramTable.ContentProgramTable -> {
                    binding.tvMainContent.text = item.programTable.title
                    binding.tvSubContent.text = item.programTable.createdAt.toFormattedString("MMM yyyy")
                }
                is DisplayItemCourse.ContentCourse -> {
                    binding.tvMainContent.text = item.course.title
                    binding.tvSubContent.text = item.course.createdAt.toFormattedString("MMM yyyy")
                }
                is DisplayItemTask.ContentTask -> {
                    binding.tvMainContent.text = item.task.title
                    binding.tvSubContent.text = item.task.createdAt.toFormattedString("MMM yyyy")
                }
            }
        }
    }
    inner class FooterFilterViewHolder(private val binding: RowSingleTextViewBinding): BaseViewHolder(binding.root){
        override fun bind(item: BaseDisplayListItem) {
            super.bind(item)
            when(item){
                is DisplayItemProgramTable.FooterProgramTable -> {
                    binding.tvContent.text = "Count: ${item.count}"
                }
                is DisplayItemCourse.FooterCourse -> {
                    binding.tvContent.text = "Count: ${item.count}"
                }
                is DisplayItemTask.FooterTask -> {
                    binding.tvContent.text = "Count: ${item.count}"
                }
            }
        }
    }
}