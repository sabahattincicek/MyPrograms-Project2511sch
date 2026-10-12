package com.saboon.project_2511sch.domain.model

/**
 * The tasks of all active courses laid out day by day for a date range (home screen and widget).
 *
 * @property startDate Start of the requested range (milliseconds).
 * @property endDate End of the requested range (milliseconds).
 * @property days The days of the range in order. Days without tasks are included unless the
 * caller asked to hide them.
 * @property hasTasks False when there is nothing to show at all (no active course, or no task
 * left after filtering), in which case [days] is empty and the UI shows its empty state.
 */
data class HomeTimeline(
    val startDate: Long,
    val endDate: Long,
    val days: List<HomeDay>,
    val hasTasks: Boolean
) {
    /** Number of task occurrences in the whole range. */
    val itemCount: Int get() = days.sumOf { it.items.size }
}

/**
 * One calendar day of the timeline.
 *
 * @property date Midnight (local time) of the day, in milliseconds.
 * @property items The task occurrences of the day, sorted by start time.
 */
data class HomeDay(
    val date: Long,
    val items: List<HomeTimelineItem>
)

/**
 * One occurrence of a task on the timeline.
 *
 * For a repeating lesson there is one item per session and [task] is a copy of the lesson
 * whose `date` is the date of that session, not the lesson's own start date.
 *
 * @property tag Tag of the course, or null when the course has none.
 * @property course The course the task belongs to.
 * @property task The task, or the session copy of a repeating lesson.
 * @property occurrenceId Id that is unique per occurrence, usable as a stable list key.
 */
data class HomeTimelineItem(
    val tag: Tag?,
    val course: Course,
    val task: Task,
    val occurrenceId: String
)
