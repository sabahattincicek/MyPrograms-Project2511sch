package com.saboon.project_2511sch.domain.model

/**
 * Which task types a task list should contain.
 *
 * Used by the home timeline and the home widget to narrow the list down to lessons, exams
 * and / or homeworks. All types are included by default.
 *
 * @property lesson Include lesson sessions.
 * @property exam Include exams.
 * @property homework Include homeworks.
 */
data class FilterTask(
    val lesson: Boolean = true,
    val exam: Boolean = true,
    val homework: Boolean = true
)
