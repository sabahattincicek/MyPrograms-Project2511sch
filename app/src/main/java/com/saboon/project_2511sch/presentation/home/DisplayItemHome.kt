package com.saboon.project_2511sch.presentation.home

import com.saboon.project_2511sch.domain.model.Course
import com.saboon.project_2511sch.domain.model.ProgramTable
import com.saboon.project_2511sch.domain.model.Task
import com.saboon.project_2511sch.util.BaseDisplayListItem

sealed class DisplayItemHome: BaseDisplayListItem {


    data class HeaderItemHome(val date: Long) : DisplayItemHome(){
        override val id: String = date.toString()
        override val viewType: Int = BaseDisplayListItem.VIEW_TYPE_HEADER
        override val isClickable: Boolean = false
    }
    data class ContentItemHome(
        val programTable: ProgramTable,
        val course: Course,
        val task: Task,
        val occurrenceId:String
    ): DisplayItemHome(){
        override val id: String = occurrenceId
        override val viewType: Int = BaseDisplayListItem.VIEW_TYPE_CONTENT
    }
    data class FooterItemHome(
        val startDate: Long,
        val endDate: Long,
        val itemCount: Int
    ): DisplayItemHome(){
        override val id: String = "footer_home"
        override val viewType: Int = BaseDisplayListItem.VIEW_TYPE_FOOTER
        override val isClickable: Boolean = false
    }
}