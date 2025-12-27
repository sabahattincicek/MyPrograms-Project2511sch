package com.saboon.project_2511sch.domain.alarm

import com.saboon.project_2511sch.domain.model.Course
import com.saboon.project_2511sch.domain.model.ProgramTable
import com.saboon.project_2511sch.domain.model.Task

interface IAlarmScheduler {
    fun scheduleReminder(programTable: ProgramTable, course: Course, task: Task)
    fun scheduleAbsenceReminder(programTable: ProgramTable, course: Course, task: Task)
    fun rescheduleReminder(programTable: ProgramTable, course: Course, currentTask: Task)
    fun rescheduleAbsenceReminder(programTable: ProgramTable, course: Course, currentTask: Task)
    fun cancel(task: Task)
}