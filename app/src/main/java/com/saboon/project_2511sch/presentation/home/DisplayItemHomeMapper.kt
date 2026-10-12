package com.saboon.project_2511sch.presentation.home

import com.saboon.project_2511sch.domain.model.HomeTimeline

/**
 * Builds the rows of the home timeline: for every day a date header followed by its tasks,
 * then a footer summarizing the shown range.
 * Returns an empty list when the timeline has nothing to show, so the screen shows its empty state.
 */
fun HomeTimeline.toHomeDisplayItems(): List<DisplayItemHome> {
    if (!hasTasks) return emptyList()

    val items = mutableListOf<DisplayItemHome>()
    days.forEach { day ->
        items.add(DisplayItemHome.HeaderItemHome(date = day.date))
        day.items.forEach { item ->
            items.add(DisplayItemHome.ContentItemHome(item.tag, item.course, item.task, item.occurrenceId))
        }
    }
    items.add(DisplayItemHome.FooterItemHome(startDate, endDate, itemCount))
    return items
}
