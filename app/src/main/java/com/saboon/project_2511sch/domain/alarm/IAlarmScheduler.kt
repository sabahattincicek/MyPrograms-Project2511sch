package com.saboon.project_2511sch.domain.alarm

import com.saboon.project_2511sch.domain.model.Course
import com.saboon.project_2511sch.domain.model.ProgramTable
import com.saboon.project_2511sch.domain.model.Schedule

interface IAlarmScheduler {
    fun scheduleReminder(programTable: ProgramTable, course: Course, schedule: Schedule)
    fun scheduleAbsenceReminder(programTable: ProgramTable, course: Course, schedule: Schedule)
    fun rescheduleReminder(programTable: ProgramTable, course: Course, currentSchedule: Schedule)
    fun rescheduleAbsenceReminder(programTable: ProgramTable, course: Course, currentSchedule: Schedule)
    fun cancel(schedule: Schedule)
}