package com.saboon.project_2511sch.presentation.schedule

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.saboon.project_2511sch.R
import com.saboon.project_2511sch.domain.model.Task
import com.saboon.project_2511sch.util.toFormattedString

class RecyclerAdapterSchedule: ListAdapter<Task, RecyclerAdapterSchedule.ScheduleViewHolder>(ScheduleDiffCallback()) {

    var onItemClickListener:((Task) -> Unit)? = null

    class ScheduleViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val scheduleTitle: TextView = view.findViewById(R.id.tv_title)
        val scheduleDescription: TextView = view.findViewById(R.id.tv_description)
        val scheduleStartTime: TextView = view.findViewById(R.id.tv_start_time)
        val scheduleEndTime: TextView = view.findViewById(R.id.tv_end_time)
        val scheduleDate: TextView = view.findViewById(R.id.tv_date)
        val schedulePlace: TextView = view.findViewById(R.id.tv_place)
    }

    class ScheduleDiffCallback: DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(
            oldItem: Task,
            newItem: Task
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: Task,
            newItem: Task
        ): Boolean {
            return oldItem == newItem
        }

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ScheduleViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_list_row_schedules, parent, false)
        return ScheduleViewHolder(view)
    }

    override fun onBindViewHolder(holder: ScheduleViewHolder, position: Int) {
        val schedule = getItem(position)
        holder.scheduleTitle.text = schedule.title
        holder.scheduleDescription.text = schedule.description
        holder.scheduleStartTime.text = schedule.startTime.toFormattedString("HH:mm")
        holder.scheduleEndTime.text = schedule.endTime.toFormattedString("HH:mm")
        holder.scheduleDate.text = schedule.date.toFormattedString("dd MMMM yyyy")
        holder.schedulePlace.text = schedule.place

        holder.itemView.setOnClickListener {
            onItemClickListener?.invoke(schedule)
        }
    }
}