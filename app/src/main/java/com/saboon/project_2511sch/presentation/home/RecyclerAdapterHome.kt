package com.saboon.project_2511sch.presentation.home

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat.getString
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.ListAdapter
import com.saboon.project_2511sch.util.toFormattedString
import com.saboon.project_2511sch.R
import com.saboon.project_2511sch.databinding.RowHomeFooterBinding
import com.saboon.project_2511sch.databinding.RowHomeHeaderBinding
import com.saboon.project_2511sch.databinding.RowMainContentBinding
import com.saboon.project_2511sch.domain.model.Course
import com.saboon.project_2511sch.domain.model.ProgramTable
import com.saboon.project_2511sch.domain.model.Task
import com.saboon.project_2511sch.presentation.settings.SettingsConstants
import com.saboon.project_2511sch.util.BaseDiffCallback
import com.saboon.project_2511sch.util.BaseDisplayListItem
import com.saboon.project_2511sch.util.BaseViewHolder
import com.saboon.project_2511sch.util.ModelColorConstats
import com.saboon.project_2511sch.util.SwipeRevealLayout
import java.util.Calendar
import java.util.concurrent.TimeUnit

class RecyclerAdapterHome :
    ListAdapter<DisplayItemHome, BaseViewHolder>(BaseDiffCallback()) {

    var onItemClickListener:((ProgramTable, Course) -> Unit)? = null

    private var openedLayout: SwipeRevealLayout? = null

    var isColorEnabled: Boolean = true
        @SuppressLint("NotifyDataSetChanged")
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    var colorSource: String = SettingsConstants.HomeListItemColorSource.DEFAULT
        @SuppressLint("NotifyDataSetChanged")
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when(viewType){
            BaseDisplayListItem.VIEW_TYPE_HEADER -> {
                val binding = RowHomeHeaderBinding.inflate(inflater, parent, false)
                HeaderHomeViewHolder(binding)
            }
            BaseDisplayListItem.VIEW_TYPE_CONTENT -> {
                val binding = RowMainContentBinding.inflate(inflater, parent, false)
                ContentViewHolder(binding)
            }
            BaseDisplayListItem.VIEW_TYPE_FOOTER -> {
                val binding = RowHomeFooterBinding.inflate(inflater, parent, false)
                FooterHomeViewHolder(binding)
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
            if (baseItem is DisplayItemHome.ContentItemHome){
                onItemClickListener?.invoke(baseItem.programTable, baseItem.course)
            }
        }
        holder.bind(item)
    }
    override fun getItemViewType(position: Int): Int {
        return getItem(position).viewType
    }
    inner class HeaderHomeViewHolder(private val binding: RowHomeHeaderBinding) :
        BaseViewHolder(binding.root) {
        override fun bind(item: BaseDisplayListItem) {
            super.bind(item)
            if (item is DisplayItemHome.HeaderItemHome){
                val today = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis

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
    }

    inner class ContentViewHolder(private val binding: RowMainContentBinding) :
        BaseViewHolder(binding.root) {
        override fun bind(item: BaseDisplayListItem) {
            super.bind(item)
            if (item is DisplayItemHome.ContentItemHome) {
                val programTable = item.programTable
                val course = item.course
                val task = item.task
                val today = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis

                val color = if (colorSource == SettingsConstants.HomeListItemColorSource.FROM_PROGRAM_TABLE) {
                    programTable.color
                } else {
                    course.color
                }

                when (task) {
                    is Task.Lesson -> {
                        binding.tvDate1.text = task.timeStart.toFormattedString("HH:mm")
                        binding.tvDate2.text = task.timeEnd.toFormattedString("HH:mm")
                        binding.tvContent1.text = "${course.title}, ${task.title}"
                        if (task.description != ""){
                            binding.tvContent1Sub.text = task.description
                        }else{
                            binding.tvContent1Sub.text = course.people
                        }
                        binding.tvContent2.text = task.place
                        binding.tvContent2Sub.text = "Absence: ${course.absence}"

                        val dividerColor = ModelColorConstats.LESSON.toColorInt()
                        binding.viewDivider.setBackgroundColor(dividerColor)
                        if (isColorEnabled){
                            val containerColor = color.getContainerColor(binding.root.context)
                            binding.llContainer.background = GradientDrawable(
                                GradientDrawable.Orientation.LEFT_RIGHT,
                                intArrayOf(Color.TRANSPARENT, containerColor)
                            )
                        }
                    }

                    is Task.Exam -> {
                        binding.tvDate1.text = task.timeStart.toFormattedString("HH:mm")
                        binding.tvDate2.text = task.timeEnd.toFormattedString("HH:mm")
                        binding.tvContent1.text = "${course.title}, ${task.title}"
                        if (task.achievedScore != null){
                            binding.tvContent1Sub.text = "${getString(binding.root.context, R.string.achieved_score)}: ${task.achievedScore}"
                        }else{
                            if (task.description != ""){
                                binding.tvContent1Sub.text = task.description
                            }else{
                                binding.tvContent1Sub.text = "${getString(binding.root.context, R.string.target_score)}: ${task.targetScore}"
                            }
                        }
                        binding.tvContent2.text = task.date.toFormattedString("dd.MM.yyyy")
                        binding.tvContent2Sub.text = task.place

                        val dividerColor = ModelColorConstats.EXAM.toColorInt()
                        binding.viewDivider.setBackgroundColor(dividerColor)
                        if (isColorEnabled){
                            val containerColor = color.getContainerColor(binding.root.context)
                            binding.llContainer.background = GradientDrawable(
                                GradientDrawable.Orientation.LEFT_RIGHT,
                                intArrayOf(Color.TRANSPARENT, containerColor)
                            )
                        }
                    }

                    is Task.Homework -> {
                        binding.tvDate1.text = task.dueTime.toFormattedString("HH:mm")
                        binding.tvDate2.text = ""
                        binding.tvContent1.text = "${course.title}, ${task.title}"
                        binding.tvContent1Sub.text = task.description
                        binding.tvContent2.text = ""
                        binding.tvContent2Sub.text = ""

                        val dividerColor = ModelColorConstats.HOMEWORK.toColorInt()
                        binding.viewDivider.setBackgroundColor(dividerColor)
                        if (isColorEnabled){
                            val containerColor = color.getContainerColor(binding.root.context)
                            binding.llContainer.background = GradientDrawable(
                                GradientDrawable.Orientation.LEFT_RIGHT,
                                intArrayOf(Color.TRANSPARENT, containerColor)
                            )
                        }
                    }
                }

                val taskDate = when (task) {
                    is Task.Lesson -> task.date
                    is Task.Exam -> task.date
                    is Task.Homework -> task.dueDate
                }

                if (taskDate < today) {
                    binding.llContainer.alpha = 0.3f
                } else {
                    binding.llContainer.alpha = 1.0f
                }
            }

            binding.slSwipe.close()
            binding.slSwipe.onOpened = {

                if (openedLayout != null && openedLayout != binding.slSwipe) {
                    openedLayout?.close()
                }

                openedLayout = binding.slSwipe
            }
        }
    }
    inner class FooterHomeViewHolder(private val binding: RowHomeFooterBinding): BaseViewHolder(binding.root){
        override fun bind(item: BaseDisplayListItem) {
            super.bind(item)
            if (item is DisplayItemHome.FooterItemHome){
                val itemCount = item.itemCount
                val startDay = item.startDate
                val endDay = item.endDate
                val today = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis

                binding.tvToday.text = "Today: ${today.toFormattedString("dd.MM.yyyy")}"
                binding.tvDateRange.text = "Showing date range: ${startDay.toFormattedString("dd.MM.yyyy")} - ${endDay.toFormattedString("dd.MM.yyyy")}"
                binding.tvItemCount.text = "Showing items: ${itemCount}"
            }
        }
    }

    fun getTodayPosition(): Int{
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        return currentList.indexOfFirst {
            it is DisplayItemHome.HeaderItemHome && it.date == today
        }
    }
}