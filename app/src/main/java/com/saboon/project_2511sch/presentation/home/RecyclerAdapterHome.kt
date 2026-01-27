package com.saboon.project_2511sch.presentation.home

import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.drawable.GradientDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.getString
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.saboon.project_2511sch.databinding.RecyclerListRowHomeHeaderBinding
import com.saboon.project_2511sch.util.toFormattedString
import com.google.android.material.color.MaterialColors
import com.saboon.project_2511sch.R
import com.saboon.project_2511sch.databinding.RecyclerListRowHomeContentBinding
import com.saboon.project_2511sch.databinding.RecyclerListRowHomeFooterBinding
import com.saboon.project_2511sch.domain.model.Course
import com.saboon.project_2511sch.domain.model.Task
import com.saboon.project_2511sch.util.ModelColors
import java.util.Calendar
import java.util.concurrent.TimeUnit

class RecyclerAdapterHome :
    ListAdapter<HomeDisplayItem, RecyclerView.ViewHolder>(HomeDiffCallback()) {

    var onItemClickListener:((Course) -> Unit)? = null

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_CONTENT = 1
        private const val VIEW_TYPE_FOOTER = 2
    }

    class HeaderViewHolder(private val binding: RecyclerListRowHomeHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(today: Long, item: HomeDisplayItem.HeaderItem) {
            binding.tvContent1.text = item.date.toFormattedString("EEEE - dd MMMM yyyy")

            val diffMillis = item.date - today
            val diffDays = TimeUnit.MILLISECONDS.toDays(diffMillis)

            binding.tvContent2.text = when {
                diffDays == 0L -> getString(binding.root.context, R.string.today)
                diffDays == 1L -> getString(binding.root.context, R.string.tomorrow)
                diffDays == -1L -> getString(binding.root.context, R.string.yesterday)
                diffDays > 0 -> "In $diffDays days"
                else -> "${kotlin.math.abs(diffDays)} days ago"
            }

            if (item.date < today) {binding.llContainer.alpha = 0.3f}
            else {binding.llContainer.alpha = 1.0f}
        }
    }

    class ContentViewHolder(private val binding: RecyclerListRowHomeContentBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(today: Long, item: HomeDisplayItem.ContentItem) {
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

                    val customColorAttrContainer = ModelColors.getThemeAttrForCustomContainerColor(course.color)
                    val customColorAttrTask = ModelColors.getThemeAttrForCustomColor(ModelColors.MODEL_COLOR_LESSON)
                    val themeAwareCustomColorContainer = MaterialColors.getColor(binding.root, customColorAttrContainer, Color.BLACK)
                    val themeAwareCustomColorTask = MaterialColors.getColor(binding.root, customColorAttrTask, Color.BLACK)
                    binding.llContainer.background = GradientDrawable(
                        GradientDrawable.Orientation.LEFT_RIGHT,
                        intArrayOf(Color.TRANSPARENT, themeAwareCustomColorContainer)
                    )
                    binding.viewDivider.setBackgroundColor(themeAwareCustomColorTask)
                }

                is Task.Exam -> {
                    binding.tvDate1.text = task.timeStart.toFormattedString("HH:mm")
                    binding.tvDate2.text = task.timeEnd.toFormattedString("HH:mm")
                    binding.tvContent1.text = "${course.title}, ${task.title}"
                    binding.tvContent1Sub.text = "${getString(binding.root.context,R.string.target_score)}: ${task.targetScore}"
                    binding.tvContent2.text = task.date.toFormattedString("dd.MM.yyyy")
                    binding.tvContent2Sub.text = task.place

                    val customColorAttrContainer = ModelColors.getThemeAttrForCustomContainerColor(course.color)
                    val customColorAttrTask = ModelColors.getThemeAttrForCustomColor(ModelColors.MODEL_COLOR_EXAM)
                    val themeAwareCustomColorContainer = MaterialColors.getColor(binding.root, customColorAttrContainer, Color.BLACK)
                    val themeAwareCustomColorTask = MaterialColors.getColor(binding.root, customColorAttrTask, Color.BLACK)
                    binding.llContainer.background = GradientDrawable(
                        GradientDrawable.Orientation.LEFT_RIGHT,
                        intArrayOf(Color.TRANSPARENT, themeAwareCustomColorContainer)
                    )
                    binding.viewDivider.setBackgroundColor(themeAwareCustomColorTask)
                }

                is Task.Homework -> {
                    binding.tvDate1.text = task.dueTime.toFormattedString("HH:mm")
                    binding.tvDate2.text = ""
                    binding.tvContent1.text = "${course.title}, ${task.title}"
                    binding.tvContent1Sub.text = task.description
                    binding.tvContent2.text = ""
                    binding.tvContent2Sub.text = ""

                    val customColorAttrContainer = ModelColors.getThemeAttrForCustomContainerColor(course.color)
                    val customColorAttrTask = ModelColors.getThemeAttrForCustomColor(ModelColors.MODEL_COLOR_HOMEWORK)
                    val themeAwareCustomColorContainer = MaterialColors.getColor(binding.root, customColorAttrContainer, Color.BLACK)
                    val themeAwareCustomColorTask = MaterialColors.getColor(binding.root, customColorAttrTask, Color.BLACK)
                    binding.llContainer.background = GradientDrawable(
                        GradientDrawable.Orientation.LEFT_RIGHT,
                        intArrayOf(Color.TRANSPARENT, themeAwareCustomColorContainer)
                    )
                    binding.viewDivider.setBackgroundColor(themeAwareCustomColorTask)
                }
            }

            val taskDate = when(task){
                is Task.Lesson -> task.date
                is Task.Exam -> task.date
                is Task.Homework -> task.dueDate
            }

            if (taskDate < today) {binding.llContainer.alpha = 0.3f}
            else {binding.llContainer.alpha = 1.0f}
        }
    }
    class FooterViewHolder(private val binding: RecyclerListRowHomeFooterBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(today: Long, item: HomeDisplayItem.FooterItem){
            val startDay = item.startDate
            val endDay = item.endDate
            val itemCount = item.itemCount

            binding.tvDateRange.text = "Showing date range: ${startDay.toFormattedString("dd.MM.yyyy")} - ${endDay.toFormattedString("dd.MM.yyyy")}"
            binding.tvItemCount.text = "Showing items: ${itemCount}"
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
                val binding = RecyclerListRowHomeContentBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                ContentViewHolder(binding)
            }

            VIEW_TYPE_FOOTER -> {
                val binding = RecyclerListRowHomeFooterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                FooterViewHolder(binding)
            }

            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val item = getItem(position)
        when (holder) {
            is HeaderViewHolder -> holder.bind(today, item as HomeDisplayItem.HeaderItem)
            is ContentViewHolder -> holder.bind(today, item as HomeDisplayItem.ContentItem)
            is FooterViewHolder -> holder.bind(today, item as HomeDisplayItem.FooterItem)
        }
        holder.itemView.setOnClickListener {
            when(item) {
                is HomeDisplayItem.HeaderItem -> {}
                is HomeDisplayItem.ContentItem -> {
                    onItemClickListener?.invoke(item.course)
                }
                is HomeDisplayItem.FooterItem -> {}
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is HomeDisplayItem.HeaderItem -> VIEW_TYPE_HEADER
            is HomeDisplayItem.ContentItem -> VIEW_TYPE_CONTENT
            is HomeDisplayItem.FooterItem -> VIEW_TYPE_FOOTER
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