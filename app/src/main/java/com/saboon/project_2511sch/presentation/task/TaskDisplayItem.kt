package com.saboon.project_2511sch.presentation.task

import com.saboon.project_2511sch.domain.model.Course
import com.saboon.project_2511sch.domain.model.ProgramTable
import com.saboon.project_2511sch.domain.model.Task
import com.saboon.project_2511sch.domain.model.TaskType

sealed class TaskDisplayItem {
    abstract val id: String

    data class HeaderItem(val type: TaskType): TaskDisplayItem(){
        override val id: String
            get() =  type::class.simpleName.toString()
    }

    data class ContentItem(
        val task: Task,
        val occurrenceId: String
    ): TaskDisplayItem() {
        override val id: String
            get() = occurrenceId
    }
}