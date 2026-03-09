package com.saboon.project_2511sch.presentation.tag

import com.saboon.project_2511sch.domain.model.Tag
import com.saboon.project_2511sch.util.BaseDisplayListItem

sealed class DisplayItemTag: BaseDisplayListItem {
    data class ContentTag(
        val tag: Tag
    ): DisplayItemTag(){
        override val id: String = tag.id
        override val viewType: Int = BaseDisplayListItem.VIEW_TYPE_CONTENT
    }
    data class FooterTag(val count: Int): DisplayItemTag(){
        override val id: String = "footer_tag"
        override val viewType: Int = BaseDisplayListItem.VIEW_TYPE_FOOTER
        override val isClickable: Boolean = false
    }
}