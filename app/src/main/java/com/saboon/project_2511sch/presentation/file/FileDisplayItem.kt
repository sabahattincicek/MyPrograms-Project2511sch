package com.saboon.project_2511sch.presentation.file

import com.saboon.project_2511sch.domain.model.Course
import com.saboon.project_2511sch.domain.model.File
import com.saboon.project_2511sch.domain.model.ProgramTable
import com.saboon.project_2511sch.domain.model.Task
import com.saboon.project_2511sch.presentation.home.HomeDisplayItem

sealed class FileDisplayItem {
    abstract val id: String

    data class HeaderItem(
        val programTable: ProgramTable?,
        val course: Course?,
        val task: Task?
    ): FileDisplayItem(){
        override val id: String = "header_${programTable?.id}_${course?.id}_${task?.id}"
        val displayPath: String get() {
            val parts = mutableListOf<String>()
            programTable?.let { parts.add(it.title) }
            course?.let { parts.add(it.title) }
            task?.let { parts.add(it.title) }
            return parts.joinToString(" > ")
        }
    }
    data class ContentItem(val file: File): FileDisplayItem(){
        override val id: String = file.id
    }
    data class Footer(
        val count: Int,
        val programTable: ProgramTable?,
        val course: Course?,
        val task: Task?
    ): FileDisplayItem(){
        override val id: String = "footer"
    }

}