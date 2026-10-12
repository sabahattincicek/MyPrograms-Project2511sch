package com.saboon.project_2511sch.domain.model

/**
 * The courses that share one tag, as shown together in the course list.
 *
 * @property tag The shared tag, or null for the group of courses without a tag.
 * @property courses The courses of the group, in repository order.
 */
data class CourseGroup(
    val tag: Tag?,
    val courses: List<Course>
)
