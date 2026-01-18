package com.saboon.project_2511sch.presentation.home

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat.getString
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.saboon.project_2511sch.databinding.RecyclerListRowHomeBinding
import com.saboon.project_2511sch.databinding.RecyclerListRowHomeHeaderBinding
import com.saboon.project_2511sch.util.toFormattedString
import com.google.android.material.color.MaterialColors
import com.saboon.project_2511sch.R
import com.saboon.project_2511sch.domain.model.Task
import com.saboon.project_2511sch.util.ModelColors

class RecyclerAdapterHome :
    ListAdapter<HomeDisplayItem, RecyclerView.ViewHolder>(HomeDiffCallback()) {

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_CONTENT = 1
    }

    class HeaderViewHolder(private val binding: RecyclerListRowHomeHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: HomeDisplayItem.HeaderItem) {
            binding.tvDay.text = item.date.toFormattedString("EEEE")
            binding.tvDate.text = item.date.toFormattedString("dd.MM.yyyy")
        }
    }

    class ContentViewHolder(private val binding: RecyclerListRowHomeBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: HomeDisplayItem.ContentItem) {
            val programTable = item.programTable
            val course = item.course
            val task = item.task

            when (task) {
                is Task.Lesson -> {
                    binding.tvDate1.text = task.timeStart.toFormattedString("HH:mm")
                    binding.tvDate2.text = task.timeEnd.toFormattedString("HH:mm")
                    binding.tvContent1.text = "${course.title}, ${task.title}"
                    binding.tvContent1Sub.text = task.description
                    binding.tvContent2.text = task.date.toFormattedString("EEEE")
                    binding.tvContent2Sub.text = task.place
                }

                is Task.Exam -> {
                    binding.tvDate1.text = task.timeStart.toFormattedString("HH:mm")
                    binding.tvDate2.text = task.timeEnd.toFormattedString("HH:mm")
                    binding.tvContent1.text = "${course.title}, ${task.title}"
                    binding.tvContent1Sub.text = "${getString(binding.root.context,R.string.target_score)}: ${task.targetScore}"
                    binding.tvContent2.text = task.date.toFormattedString("dd.MM.yyyy")
                    binding.tvContent2Sub.text = task.place
                }

                is Task.Homework -> {
                    binding.tvDate1.text = task.dueDate.toFormattedString("dd/MM")
                    binding.tvDate2.text = task.dueDate.toFormattedString("yyyy")
                    binding.tvContent1.text = "${course.title}, ${task.title}"
                    binding.tvContent1Sub.text = task.description
                }
            }

            val customContainerColorAttr = ModelColors.getThemeAttrForCustomContainerColor(course.color)
            val themeAwareCustomContainerColor = MaterialColors.getColor(binding.root, customContainerColorAttr, Color.BLACK)
            binding.llContainer.background = GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                intArrayOf(Color.TRANSPARENT, themeAwareCustomContainerColor)
            )
            val onCustomContainerColorAttr = ModelColors.getThemeAttrForOnCustomContainerColor(course.color)
            val themeAwareOnCustomContainerColor = MaterialColors.getColor(binding.root, onCustomContainerColorAttr, Color.BLACK)
            binding.viewDivider.setBackgroundColor(themeAwareOnCustomContainerColor)
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_HEADER -> {
                val binding = RecyclerListRowHomeHeaderBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                HeaderViewHolder(binding)
            }

            VIEW_TYPE_CONTENT -> {
                val binding = RecyclerListRowHomeBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                ContentViewHolder(binding)
            }

            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {
        val item = getItem(position)
        when (holder) {
            is HeaderViewHolder -> holder.bind(item as HomeDisplayItem.HeaderItem)
            is ContentViewHolder -> holder.bind(item as HomeDisplayItem.ContentItem)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is HomeDisplayItem.HeaderItem -> VIEW_TYPE_HEADER
            is HomeDisplayItem.ContentItem -> VIEW_TYPE_CONTENT
        }
    }

    class HomeDiffCallback : DiffUtil.ItemCallback<HomeDisplayItem>() {
        override fun areItemsTheSame(
            oldItem: HomeDisplayItem,
            newItem: HomeDisplayItem
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: HomeDisplayItem,
            newItem: HomeDisplayItem
        ): Boolean {
            return oldItem == newItem
        }
    }
}