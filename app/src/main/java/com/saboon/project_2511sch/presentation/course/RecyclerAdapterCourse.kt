package com.saboon.project_2511sch.presentation.course

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.saboon.project_2511sch.databinding.RowGeneralItemBinding
import com.saboon.project_2511sch.databinding.RowSingleTextViewBinding
import com.saboon.project_2511sch.databinding.RowSingleTextViewLeftBinding
import com.saboon.project_2511sch.domain.model.Course
import com.saboon.project_2511sch.util.BaseDiffCallback
import com.saboon.project_2511sch.util.BaseDisplayListItem
import com.saboon.project_2511sch.util.BaseViewHolder
import com.saboon.project_2511sch.util.toFormattedString

class RecyclerAdapterCourse: ListAdapter<DisplayItemCourse, BaseViewHolder>(BaseDiffCallback<DisplayItemCourse>()) {

    var onItemClickListener: ((Course) -> Unit)? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when(viewType){
            BaseDisplayListItem.VIEW_TYPE_HEADER -> {
                val binding = RowSingleTextViewLeftBinding.inflate(inflater, parent, false)
                HeaderCourseViewHolder(binding)
            }
            BaseDisplayListItem.VIEW_TYPE_CONTENT -> {
                val binding = RowGeneralItemBinding.inflate(inflater, parent, false)
                ContentCourseViewHolder(binding)
            }

            BaseDisplayListItem.VIEW_TYPE_FOOTER -> {
                val binding = RowSingleTextViewBinding.inflate(inflater, parent, false)
                FooterCourseViewHolder(binding)
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
            if (baseItem is DisplayItemCourse.ContentCourse){
                onItemClickListener?.invoke(baseItem.course)
            }
        }
        holder.bind(item)
    }
    override fun getItemViewType(position: Int): Int {
        return getItem(position).viewType
    }
    inner class HeaderCourseViewHolder(private val binding: RowSingleTextViewLeftBinding): BaseViewHolder(binding.root){
        override fun bind(item: BaseDisplayListItem) {
            super.bind(item)
            if (item is DisplayItemCourse.HeaderCourse){
                binding.tvContent.text = item.title
            }
        }
    }
    inner class ContentCourseViewHolder(private val binding: RowGeneralItemBinding): BaseViewHolder(binding.root){
        override fun bind(item: BaseDisplayListItem) {
            super.bind(item) //for click logic
            if (item is DisplayItemCourse.ContentCourse){
                binding.tvMainContent.text = item.course.title
                binding.tvSubContent.text = item.course.createdAt.toFormattedString("MMM yyyy")
                if (!item.course.isActive) binding.llContainer.alpha = 0.3f
                else binding.llContainer.alpha = 1.0f
            }
        }
    }

    inner class FooterCourseViewHolder(private val binding: RowSingleTextViewBinding): BaseViewHolder(binding.root){
        override fun bind(item: BaseDisplayListItem) {
            super.bind(item)
            if (item is DisplayItemCourse.FooterCourse){
                binding.tvContent.text = "Count: ${item.count}"
            }
        }
    }

}