package com.saboon.project_2511sch.presentation.sfile

import com.saboon.project_2511sch.domain.model.Course
import com.saboon.project_2511sch.domain.model.ProgramTable
import com.saboon.project_2511sch.domain.model.SFile
import com.saboon.project_2511sch.domain.model.Task
import com.saboon.project_2511sch.util.BaseDisplayListItem

sealed class DisplayItemSFile: BaseDisplayListItem {
    data class HeaderSFile(val header: String): DisplayItemSFile(){
        override val id: String = header
        override val viewType: Int = BaseDisplayListItem.VIEW_TYPE_HEADER
        override val isClickable: Boolean = false
    }
    data class ContentSFile(
        val programTable: ProgramTable?,
        val course: Course?,
        val task: Task?,
        val sFile: SFile
    ): DisplayItemSFile(){
        override val id: String = sFile.id
        override val viewType: Int = BaseDisplayListItem.VIEW_TYPE_CONTENT
    }
    data class FooterSFile(val count: Int): DisplayItemSFile(){
        override val id: String = "footer_file"
        override val viewType: Int = BaseDisplayListItem.VIEW_TYPE_FOOTER
    }
}