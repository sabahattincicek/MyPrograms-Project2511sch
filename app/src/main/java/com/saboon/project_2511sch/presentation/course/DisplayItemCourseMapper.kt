package com.saboon.project_2511sch.presentation.course

import com.saboon.project_2511sch.domain.model.CourseGroup

/**
 * Builds the rows of the course list: for every group a tag header followed by its courses,
 * then a footer with the total course count.
 * Returns an empty list when there is no course, so the screen shows its empty state.
 */
fun List<CourseGroup>.toCourseDisplayItems(): List<DisplayItemCourse> {
    val courseCount = sumOf { it.courses.size }
    if (courseCount == 0) return emptyList()

    val items = mutableListOf<DisplayItemCourse>()
    forEach { group ->
        items.add(DisplayItemCourse.HeaderCourse(group.tag))
        group.courses.forEach { course ->
            items.add(DisplayItemCourse.ContentCourse(course = course, tag = group.tag))
        }
    }
    items.add(DisplayItemCourse.FooterCourse(courseCount))
    return items
}
