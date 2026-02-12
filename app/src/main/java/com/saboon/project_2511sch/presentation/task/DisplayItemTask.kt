package com.saboon.project_2511sch.presentation.task

import com.saboon.project_2511sch.domain.model.Course
import com.saboon.project_2511sch.domain.model.ProgramTable
import com.saboon.project_2511sch.domain.model.Task
import com.saboon.project_2511sch.util.BaseDisplayListItem

sealed class DisplayItemTask: BaseDisplayListItem {
    data class HeaderTask(val taskType: String): DisplayItemTask(){
        override val id: String = taskType
        override val viewType: Int = BaseDisplayListItem.VIEW_TYPE_HEADER
        override val isClickable: Boolean = false
    }
    data class ContentTask(
        val programTable: ProgramTable,
        val course: Course,
        val task: Task
    ): DisplayItemTask() {
        override val id: String = task.id
        override val viewType: Int = BaseDisplayListItem.VIEW_TYPE_CONTENT
    }
    data class FooterTask(val count: Int): DisplayItemTask(){
        override val id: String = "footer_task"
        override val viewType: Int = BaseDisplayListItem.VIEW_TYPE_FOOTER
        override val isClickable: Boolean = false
    }
}