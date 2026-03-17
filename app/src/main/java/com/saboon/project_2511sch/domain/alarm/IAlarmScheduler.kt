package com.saboon.project_2511sch.domain.alarm

import com.saboon.project_2511sch.domain.model.Course
import com.saboon.project_2511sch.domain.model.Tag
import com.saboon.project_2511sch.domain.model.Task

/**
 * Interface defining the contract for scheduling alarms in the system.
 */
interface IAlarmScheduler {

    /**
     * Schedules a reminder notification for a specific task.
     * This is called when a task is created or updated.
     */
    fun schedule(course: Course, task: Task)

    /**
     * Cancels any existing alarms for the given task.
     * Useful when a task is deleted or the user turns off reminders.
     */
    fun cancel(course: Course, task: Task)

    /**
     * Called by the Receiver to set the next occurrence
     * if the task is a recurring lesson.
     */
    fun reschedule(course: Course, task: Task)

    /** Synchronizes all alarms for a specific course.
     * This is typically called when a course's active status changes,
     * ensuring all its tasks' reminders and absence checks are updated accordingly.
     */
    fun checkAndSyncCourseAlarms(course: Course, tasks: List<Task>)

    /**
     * Synchronizes all alarms for every course associated with a specific tag.
     * Use this when a tag is activated or deactivated to perform a bulk update
     * on all related course notifications in one operation.
     */
    fun checkAndSyncTagAlarms(tag: Tag, courses: List<Course>, tasks: List<Task>)
}