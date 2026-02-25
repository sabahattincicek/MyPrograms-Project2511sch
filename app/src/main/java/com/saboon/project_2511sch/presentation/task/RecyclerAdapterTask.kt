package com.saboon.project_2511sch.presentation.task

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat.getString
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.ListAdapter
import com.saboon.project_2511sch.R
import com.saboon.project_2511sch.databinding.RowMainContentBinding
import com.saboon.project_2511sch.databinding.RowSingleTextViewBinding
import com.saboon.project_2511sch.databinding.RowSingleTextViewLeftBinding
import com.saboon.project_2511sch.domain.model.Task
import com.saboon.project_2511sch.util.BaseDiffCallback
import com.saboon.project_2511sch.util.BaseDisplayListItem
import com.saboon.project_2511sch.util.BaseViewHolder
import com.saboon.project_2511sch.util.ModelColor
import com.saboon.project_2511sch.util.ModelColorConstats
import com.saboon.project_2511sch.util.toFormattedString

class RecyclerAdapterTask:
    ListAdapter<DisplayItemTask, BaseViewHolder>(BaseDiffCallback<DisplayItemTask>()) {

    var onItemClickListener:((Task) -> Unit)? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when(viewType){
            BaseDisplayListItem.VIEW_TYPE_HEADER -> {
                val binding = RowSingleTextViewLeftBinding.inflate(inflater, parent, false)
                HeaderTaskViewHolder(binding)
            }
            BaseDisplayListItem.VIEW_TYPE_CONTENT -> {
                val binding = RowMainContentBinding.inflate(inflater, parent, false)
                ContentTaskViewHolder(binding)
            }
            BaseDisplayListItem.VIEW_TYPE_FOOTER -> {
                val binding = RowSingleTextViewBinding.inflate(inflater, parent, false)
                FooterTaskViewHolder(binding)
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
            if (baseItem is DisplayItemTask.ContentTask){
                onItemClickListener?.invoke(baseItem.task)
            }
        }
        holder.bind(item)
    }

    override fun getItemViewType(position: Int): Int {
        return getItem(position).viewType
    }

    inner class HeaderTaskViewHolder(private val binding: RowSingleTextViewLeftBinding): BaseViewHolder(binding.root){
        override fun bind(item: BaseDisplayListItem) {
            super.bind(item) //for click logic
            if (item is DisplayItemTask.HeaderTask){
                binding.tvContent.text = item.taskType
            }
        }
    }
    inner class ContentTaskViewHolder(private val binding: RowMainContentBinding) : BaseViewHolder(binding.root){
        override fun bind(item: BaseDisplayListItem) {
            super.bind(item)
            if (item is DisplayItemTask.ContentTask){
                val task = item.task
                when(task){
                    is Task.Lesson -> {
                        binding.tvDate1.text = task.timeStart.toFormattedString("HH:mm")
                        binding.tvDate2.text = task.timeEnd.toFormattedString("HH:mm")
                        binding.tvContent1.text = task.title
                        binding.tvContent1Sub.text = task.description
                        binding.tvContent2.text = task.date.toFormattedString("EEEE")
                        binding.tvContent2Sub.text = task.place

                        binding.viewDivider.setBackgroundColor(ModelColorConstats.LESSON.toColorInt())
                    }
                    is Task.Exam -> {
                        binding.tvDate1.text = task.timeStart.toFormattedString("HH:mm")
                        binding.tvDate2.text = task.timeEnd.toFormattedString("HH:mm")
                        binding.tvContent1.text = task.title
                        binding.tvContent1Sub.text = "${getString(binding.root.context,R.string.target_score)}: ${task.targetScore}"
                        binding.tvContent2.text = task.date.toFormattedString("dd MMMM yyyy")
                        binding.tvContent2Sub.text = task.place

                        binding.viewDivider.setBackgroundColor(ModelColorConstats.EXAM.toColorInt())
                    }
                    is Task.Homework -> {
                        binding.tvDate1.text = task.dueTime.toFormattedString("HH:mm")
                        binding.tvDate2.text = ""
                        binding.tvContent1.text = task.title
                        binding.tvContent1Sub.text = task.description
                        binding.tvContent2.text = task.dueDate.toFormattedString("dd MMMM yyyy")
                        binding.tvContent2Sub.text = ""

                        binding.viewDivider.setBackgroundColor(ModelColorConstats.HOMEWORK.toColorInt())
                    }
                }
            }
        }
    }

    inner class FooterTaskViewHolder(private val binding: RowSingleTextViewBinding): BaseViewHolder(binding.root){
        override fun bind(item: BaseDisplayListItem) {
            super.bind(item)
            if (item is DisplayItemTask.FooterTask){
                binding.tvContent.text = "Count: ${item.count}"
            }
        }
    }
}