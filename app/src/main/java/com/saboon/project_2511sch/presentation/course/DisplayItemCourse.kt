package com.saboon.project_2511sch.presentation.course

import com.saboon.project_2511sch.domain.model.Course
import com.saboon.project_2511sch.domain.model.Tag
import com.saboon.project_2511sch.util.BaseDisplayListItem

sealed class DisplayItemCourse: BaseDisplayListItem {
    data class HeaderCourse(
        val tag: Tag?
    ): DisplayItemCourse(){
        override val id: String = tag?.title ?: "Untagged"
        override val viewType: Int = BaseDisplayListItem.VIEW_TYPE_HEADER
        override val isClickable: Boolean = false
    }
    data class ContentCourse(
        val course: Course,
        val tag: Tag?,
    ): DisplayItemCourse(){
        override val id: String = course.id
        override val viewType: Int = BaseDisplayListItem.VIEW_TYPE_CONTENT
    }
    data class FooterCourse(
        val count: Int
    ): DisplayItemCourse(){
        override val id: String = "footer_course"
        override val viewType: Int = BaseDisplayListItem.VIEW_TYPE_FOOTER
        override val isClickable: Boolean = false

    }
}
