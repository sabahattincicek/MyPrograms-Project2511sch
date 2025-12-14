package com.saboon.project_2511sch.presentation.home

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.saboon.project_2511sch.databinding.RecyclerListRowHomeBinding
import com.saboon.project_2511sch.databinding.RecyclerListRowHomeHeaderBinding
import com.saboon.project_2511sch.util.toFormattedString
import androidx.core.graphics.toColorInt
import com.google.android.material.color.MaterialColors
import com.saboon.project_2511sch.util.ModelColors

class RecyclerAdapterHome : ListAdapter<HomeDisplayItem, RecyclerView.ViewHolder>(HomeDiffCallback()) {

    companion object{
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

    class ContentViewHolder(private val binding: RecyclerListRowHomeBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(item: HomeDisplayItem.ContentItem){
            val programTable = item.programTable
            val course = item.course
            val schedule = item.schedule

            binding.tvCourseTitle.text = course.title
            binding.tvCoursePeople.text = course.people
            binding.tvScheduleStartTime.text = schedule.startTime.toFormattedString("HH:mm")
            binding.tvScheduleEndTime.text = schedule.endTime.toFormattedString("HH:mm")
            binding.tvScheduleTitle.text = schedule.title
            binding.tvSchedulePlace.text = schedule.place

            val customContainerColorAttr = ModelColors.getThemeAttrForCustomContainerColor(course.color)
            val themeAwareCustomContainerColor = MaterialColors.getColor(binding.root, customContainerColorAttr, Color.BLACK)
            binding.llContainer.background = GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                intArrayOf(Color.TRANSPARENT, themeAwareCustomContainerColor)
            )
            val onCustomContainerColorAttr = ModelColors.getThemeAttrForOnCustomContainerColor(course.color)
            val themeAwareOnCustomContainerColor = MaterialColors.getColor(binding.root, onCustomContainerColorAttr, Color.BLACK)
            binding.vSeparator.setBackgroundColor(themeAwareOnCustomContainerColor)
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        return when(viewType){
            VIEW_TYPE_HEADER -> {
                val binding = RecyclerListRowHomeHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                HeaderViewHolder(binding)
            }
            VIEW_TYPE_CONTENT -> {
                val binding = RecyclerListRowHomeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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
        when(holder){
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

    class HomeDiffCallback: DiffUtil.ItemCallback<HomeDisplayItem>() {
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