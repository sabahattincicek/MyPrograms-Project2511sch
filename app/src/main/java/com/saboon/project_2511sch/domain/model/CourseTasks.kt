package com.saboon.project_2511sch.domain.model

/**
 * A course together with all of its tasks (lessons, exams and homeworks).
 *
 * @property course The current state of the course.
 * @property tasks Every task of the course, in repository order.
 */
data class CourseTasks(
    val course: Course,
    val tasks: List<Task>
)
