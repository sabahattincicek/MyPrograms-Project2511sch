package com.saboon.project_2511sch.presentation.task

import com.saboon.project_2511sch.domain.model.CourseTasks

/**
 * Builds the rows of the task list of a course: the tasks grouped by type (lesson, exam,
 * homework), each group under a header carrying the type name, then a footer with the task count.
 * Returns an empty list when the course has no task, so the screen shows its empty state.
 */
fun CourseTasks.toTaskDisplayItems(): List<DisplayItemTask> {
    if (tasks.isEmpty()) return emptyList()

    val items = mutableListOf<DisplayItemTask>()
    tasks.groupBy { it::class.simpleName ?: "Other" }.forEach { (type, tasksOfType) ->
        items.add(DisplayItemTask.HeaderTask(type))
        tasksOfType.forEach { task ->
            items.add(DisplayItemTask.ContentTask(course, task))
        }
    }
    items.add(DisplayItemTask.FooterTask(tasks.size))
    return items
}
