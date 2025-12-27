package com.saboon.project_2511sch.presentation.home

import com.saboon.project_2511sch.domain.model.Course
import com.saboon.project_2511sch.domain.model.ProgramTable
import com.saboon.project_2511sch.domain.model.Task

sealed class HomeDisplayItem {

    abstract val id : String

    data class HeaderItem(val date: Long) : HomeDisplayItem(){
        override val id: String
            get() = date.toString()
    }
    data class ContentItem(
        val programTable: ProgramTable,
        val course: Course,
        val task: Task,
        val occurrenceId:String
    ): HomeDisplayItem(){
        override val id: String
            get() = occurrenceId
    }
}