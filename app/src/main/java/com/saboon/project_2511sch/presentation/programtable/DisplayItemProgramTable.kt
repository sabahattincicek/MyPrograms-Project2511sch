package com.saboon.project_2511sch.presentation.programtable

import com.saboon.project_2511sch.domain.model.ProgramTable
import com.saboon.project_2511sch.util.BaseDisplayListItem

sealed class DisplayItemProgramTable: BaseDisplayListItem {
    data class ContentProgramTable(val programTable: ProgramTable): DisplayItemProgramTable(){
        override val id: String = programTable.id
        override val viewType: Int = BaseDisplayListItem.VIEW_TYPE_CONTENT
    }
    data class FooterProgramTable(val count: Int): DisplayItemProgramTable(){
        override val id: String = "footer_program_table"
        override val viewType: Int = BaseDisplayListItem.VIEW_TYPE_FOOTER
        override val isClickable: Boolean = false
    }
}