package com.saboon.project_2511sch.presentation.task

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.saboon.project_2511sch.R
import com.saboon.project_2511sch.databinding.RecyclerListRowTaskBinding
import com.saboon.project_2511sch.databinding.RecyclerListRowTaskHeaderBinding
import com.saboon.project_2511sch.domain.model.Task
import com.saboon.project_2511sch.domain.model.TaskType
import com.saboon.project_2511sch.util.toFormattedString

class RecyclerAdapterTask: ListAdapter<TaskDisplayItem, RecyclerView.ViewHolder>(TaskDiffCallback()) {

    var onItemClickListener:((Task) -> Unit)? = null


    companion object{
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_CONTENT = 1
    }

    class HeaderViewHolder(private val binding: RecyclerListRowTaskHeaderBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(item: TaskDisplayItem.HeaderItem){
            when(item.type){
                TaskType.LESSON -> {binding.tvExamType.text = item.type.toString()}
                TaskType.EXAM -> {binding.tvExamType.text = item.type.toString()}
                TaskType.HOMEWORK -> {binding.tvExamType.text = item.type.toString()}
            }
        }
    }

    class ContentViewHolder(private val binding: RecyclerListRowTaskBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(item: TaskDisplayItem.ContentItem){
            val task = item.task

            when(task){
                is Task.Lesson -> {
                    binding.tvDate1.text = task.timeStart.toFormattedString("HH:mm")
                    binding.tvDate2.text = task.timeEnd.toFormattedString("HH:mm")
                    binding.tvContent1.text = task.title
                    binding.tvContent1Sub.text = task.description
                    binding.tvContent2.text = task.date.toFormattedString("EEEE")
                    binding.tvContent2Sub.text = task.place
                }
                is Task.Exam -> {
                    binding.tvDate1.text = task.timeStart.toFormattedString("HH:mm")
                    binding.tvDate2.text = task.timeEnd.toFormattedString("HH:mm")
                    binding.tvContent1.text = task.title
                    binding.tvContent1Sub.text = task.targetScore.toString()
                    binding.tvContent2.text = task.date.toFormattedString("dd.MM.yyyy")
                    binding.tvContent2Sub.text = task.place
                }
                is Task.Homework -> {
                    binding.tvDate1.text = task.dueDate.toFormattedString("dd/MM")
                    binding.tvDate2.text = task.dueDate.toFormattedString("yyyy")
                    binding.tvContent1.text = task.title
                    binding.tvContent1Sub.text = task.description
                    binding.tvContent2.text = task.submissionType.toString()
                    binding.tvContent2Sub.text = task.link
                }
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        return when(viewType){
            VIEW_TYPE_HEADER -> {
                val binding = RecyclerListRowTaskHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                HeaderViewHolder(binding)
            }
            VIEW_TYPE_CONTENT -> {
                val binding = RecyclerListRowTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                ContentViewHolder(binding)
            }
            else -> {throw IllegalArgumentException("Invalid view type")}
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when(holder){
            is HeaderViewHolder -> {holder.bind(item as TaskDisplayItem.HeaderItem)}
            is ContentViewHolder -> {holder.bind(item as TaskDisplayItem.ContentItem)}
        }
        holder.itemView.setOnClickListener {
            when(item) {
                is TaskDisplayItem.ContentItem -> {
                    onItemClickListener?.invoke(item.task)
                }
                is TaskDisplayItem.HeaderItem -> {}
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when(getItem(position)){
            is TaskDisplayItem.HeaderItem -> VIEW_TYPE_HEADER
            is TaskDisplayItem.ContentItem -> VIEW_TYPE_CONTENT
        }
    }


    class TaskDiffCallback: DiffUtil.ItemCallback<TaskDisplayItem>() {
        override fun areItemsTheSame(
            oldItem: TaskDisplayItem,
            newItem: TaskDisplayItem
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: TaskDisplayItem,
            newItem: TaskDisplayItem
        ): Boolean {
            return oldItem == newItem
        }

    }
}