package com.saboon.project_2511sch.domain.alarm

import com.saboon.project_2511sch.domain.model.Course
import com.saboon.project_2511sch.domain.model.Tag
import com.saboon.project_2511sch.domain.model.Task

interface IAlarmScheduler {
    fun scheduleReminder(tag: Tag, course: Course, task: Task)
    fun scheduleAbsenceReminder(tag: Tag, course: Course, task: Task)
    fun rescheduleReminder(tag: Tag, course: Course, currentTask: Task)
    fun rescheduleAbsenceReminder(tag: Tag, course: Course, currentTask: Task)
    fun cancel(task: Task)
}