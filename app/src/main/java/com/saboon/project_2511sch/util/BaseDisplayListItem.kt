package com.saboon.project_2511sch.util

interface BaseDisplayListItem {
    val id: String
    val viewType: Int
    val isClickable: Boolean get() = true

    companion object {
        const val VIEW_TYPE_HEADER = 1
        const val VIEW_TYPE_CONTENT = 2
        const val VIEW_TYPE_FOOTER = 3
    }
}