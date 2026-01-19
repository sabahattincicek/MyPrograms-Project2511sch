package com.saboon.project_2511sch.presentation.task

import com.saboon.project_2511sch.domain.model.Course
import com.saboon.project_2511sch.domain.model.ProgramTable
import com.saboon.project_2511sch.domain.model.Task
import com.saboon.project_2511sch.domain.model.TaskType

sealed class TaskDisplayItem {
    abstract val id: String

    data class HeaderItem(val title: String): TaskDisplayItem(){
        override val id: String
            get() =  title
    }

    data class ContentItem(
        val task: Task,
        val occurrenceId: String
    ): TaskDisplayItem() {
        override val id: String
            get() = occurrenceId
    }
}