package com.saboon.project_2511sch.presentation.course

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.saboon.project_2511sch.R
import com.saboon.project_2511sch.domain.model.Course

class RecyclerAdapterCourse: ListAdapter<Course, RecyclerAdapterCourse.CourseViewHolder>(CourseDiffCallback()) {

    var onItemClickListener: ((Course) -> Unit) ?= null

    class CourseViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val courseTitle: TextView = view.findViewById(R.id.tvCourseTitle)
        val courseDescription: TextView = view.findViewById(R.id.tvCourseDescription)
    }


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CourseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_list_row_courses, parent, false)
        return CourseViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: CourseViewHolder,
        position: Int
    ) {
        val course = getItem(position)
        holder.courseTitle.text = course.title
        holder.courseDescription.text = course.description

        holder.itemView.setOnClickListener {
            onItemClickListener?.invoke(course)
        }
    }

    open class CourseDiffCallback: DiffUtil.ItemCallback<Course>() {
        override fun areItemsTheSame(
            oldItem: Course,
            newItem: Course
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: Course,
            newItem: Course
        ): Boolean {
            return oldItem == newItem
        }

    }
}