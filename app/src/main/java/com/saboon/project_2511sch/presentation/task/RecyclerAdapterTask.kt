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
import com.saboon.project_2511sch.util.ModelColorConstats
import com.saboon.project_2511sch.util.SwipeRevealLayout
import com.saboon.project_2511sch.util.toFormattedString

/**
 * RecyclerView adapter for the task list of a single course (course details screen).
 *
 * Renders three row types from [DisplayItemTask]: a section header per task type, the task
 * rows themselves (lesson, exam or homework) and a footer. Lesson rows can be swiped to reveal
 * the absence counter with its "+" and "-" buttons.
 *
 * The adapter only reports user actions through its listeners; it never changes a task itself.
 */
class RecyclerAdapterTask:
    ListAdapter<DisplayItemTask, BaseViewHolder>(BaseDiffCallback<DisplayItemTask>()) {

    var onContentItemClickListener:((Task) -> Unit)? = null

    var onAbsenceIncreaseClickListener: ((Task.Lesson) -> Unit)? = null

    var onAbsenceDecreaseClickListener: ((Task.Lesson) -> Unit)? = null

    private var openedLayout: SwipeRevealLayout? = null

    /**
     * Inflates the row layout that matches the given view type.
     */
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

    /**
     * Binds the item at [position] and forwards row clicks on task rows to [onContentItemClickListener].
     */
    override fun onBindViewHolder(
        holder: BaseViewHolder,
        position: Int
    ) {
        val item = getItem(position)
        holder.onItemClickListener = { baseItem ->
            if (baseItem is DisplayItemTask.ContentTask){
                onContentItemClickListener?.invoke(baseItem.task)
            }
        }
        holder.bind(item)
    }

    /**
     * Returns the row type (header, content or footer) declared by the item itself.
     */
    override fun getItemViewType(position: Int): Int {
        return getItem(position).viewType
    }

    /**
     * Section header row showing the localized name of a task type.
     */
    inner class HeaderTaskViewHolder(private val binding: RowSingleTextViewLeftBinding): BaseViewHolder(binding.root){
        /**
         * Shows the title that belongs to the header's task type.
         */
        override fun bind(item: BaseDisplayListItem) {
            super.bind(item) //for click logic
            if (item is DisplayItemTask.HeaderTask){
                when(item.taskType){
                    Task.Lesson::class.simpleName -> binding.tvContent.text = binding.root.context.getString(R.string.courseSession)
                    Task.Exam::class.simpleName -> binding.tvContent.text = binding.root.context.getString(R.string.exam)
                    Task.Homework::class.simpleName -> binding.tvContent.text = binding.root.context.getString(R.string.homework)
                }

            }
        }
    }
    /**
     * Task row. Lessons additionally expose the swipeable absence counter.
     */
    inner class ContentTaskViewHolder(private val binding: RowMainContentBinding) : BaseViewHolder(binding.root){
        /**
         * Fills the row according to the task type and wires its click and absence buttons.
         */
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

                        binding.slSwipe.isSwipeable = true
                        binding.tvAbsenceCount.text = task.absence.size.toString()
                        binding.btnAbsenceDecrease.setOnClickListener {
                            onAbsenceDecreaseClickListener?.invoke(task)
                        }
                        binding.btnAbsenceIncrease.setOnClickListener {
                            onAbsenceIncreaseClickListener?.invoke(task)
                        }
                    }
                    is Task.Exam -> {
                        binding.tvDate1.text = task.timeStart.toFormattedString("HH:mm")
                        binding.tvDate2.text = task.timeEnd.toFormattedString("HH:mm")
                        binding.tvContent1.text = task.title
                        binding.tvContent1Sub.text = "${binding.root.context.getString(R.string.target_score)}: ${task.targetScore}"
                        binding.tvContent2.text = task.date.toFormattedString("dd MMMM yyyy")
                        binding.tvContent2Sub.text = task.place

                        binding.viewDivider.setBackgroundColor(ModelColorConstats.EXAM.toColorInt())

                        binding.slSwipe.isSwipeable = false
                    }
                    is Task.Homework -> {
                        binding.tvDate1.text = task.dueTime.toFormattedString("HH:mm")
                        binding.tvDate2.text = ""
                        binding.tvContent1.text = task.title
                        binding.tvContent1Sub.text = task.description
                        binding.tvContent2.text = task.dueDate.toFormattedString("dd MMMM yyyy")
                        binding.tvContent2Sub.text = ""

                        binding.viewDivider.setBackgroundColor(ModelColorConstats.HOMEWORK.toColorInt())

                        binding.slSwipe.isSwipeable = false
                    }
                }
                binding.mcvForeground.setOnClickListener {
                    onContentItemClickListener?.invoke(task)
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
    }

    /**
     * Footer row. Currently left empty; it only keeps space below the last task.
     */
    inner class FooterTaskViewHolder(private val binding: RowSingleTextViewBinding): BaseViewHolder(binding.root){
        /**
         * Clears the footer text.
         */
        override fun bind(item: BaseDisplayListItem) {
            super.bind(item)
//            if (item is DisplayItemTask.FooterTask){
//                binding.tvContent.text = "${item.count} ${binding.root.context.getString(R.string.tasks)}"
//            }
            binding.tvContent.text = ""
        }
    }
}