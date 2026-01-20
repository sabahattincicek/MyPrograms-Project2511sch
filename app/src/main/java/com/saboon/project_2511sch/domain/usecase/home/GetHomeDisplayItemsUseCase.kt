package com.saboon.project_2511sch.domain.usecase.home

import android.icu.util.Calendar
import com.saboon.project_2511sch.domain.model.Course
import com.saboon.project_2511sch.domain.model.ProgramTable
import com.saboon.project_2511sch.domain.model.Task
import com.saboon.project_2511sch.domain.repository.ICourseRepository
import com.saboon.project_2511sch.domain.repository.IProgramTableRepository
import com.saboon.project_2511sch.domain.repository.ITaskRepository
import com.saboon.project_2511sch.presentation.home.HomeDisplayItem
import com.saboon.project_2511sch.util.RecurrenceRule
import com.saboon.project_2511sch.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class GetHomeDisplayItemsUseCase @Inject constructor(
    private val programTableRepository: IProgramTableRepository,
    private val courseRepository: ICourseRepository,
    private val taskRepository: ITaskRepository
) {
    operator fun invoke(programTableList: List<ProgramTable>): Flow<Resource<List<HomeDisplayItem>>> {

        return combine(
            programTableRepository.getActiveProgramTableList(),
            courseRepository.getCoursesByProgramTableId(programTableList.first().id),
            taskRepository.getAllTaskByProgramTableId(programTableList.first().id)
        ) { programTablesResult, coursesResult, tasksResult ->
            if (programTablesResult is Resource.Error) {
                return@combine Resource.Error(programTablesResult.message ?: "Failed to load courses.")
            }
            if (coursesResult is Resource.Error) {
                return@combine Resource.Error(coursesResult.message ?: "Failed to load courses.")
            }
            if (tasksResult is Resource.Error) {
                return@combine Resource.Error(tasksResult.message ?: "Failed to load tasks.")
            }

            val programTables = (programTablesResult as? Resource.Success)?.data ?: emptyList()
            val courses = (coursesResult as? Resource.Success)?.data ?: emptyList()
            val tasks = (tasksResult as? Resource.Success)?.data ?: emptyList()

            val displayItems = generateAndGroupDisplayList(programTables, courses, tasks)
            Resource.Success(displayItems)
        }
    }

    private fun generateAndGroupDisplayList(
        programTables: List<ProgramTable>,
        courses: List<Course>,
        tasks: List<Task>
    ): List<HomeDisplayItem> {
        val finalEvents = mutableListOf<HomeDisplayItem.ContentItem>()

        val programTableMap = programTables.associateBy { it.id }
        val courseMap = courses.associateBy { it.id }

        val calendar = Calendar.getInstance()

        //find monday of the current week
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) // sunday = 1, monday = 2 ...
        //calculate how many days to go back to reach monday
        val daysFromMonday = if (dayOfWeek == Calendar.SUNDAY) 6 else dayOfWeek - Calendar.MONDAY
        calendar.add(Calendar.DAY_OF_YEAR, -daysFromMonday)

        val weekStart = getDayStartMillis(calendar.timeInMillis) //monday 00:00:00

        //find sunday of the current week
        calendar.add(Calendar.DAY_OF_YEAR, 6) //sunday 23:59:59
        val weekEnd = calendar.timeInMillis

        tasks.forEach { task ->
            val programTable = programTableMap[task.programTableId]
            val course = courseMap[task.courseId]
            if (programTable != null && course != null) {
                when(task) {
                    is Task.Lesson -> {
                        val rRule = RecurrenceRule.fromRuleString(task.recurrenceRule)
                        val fromDate = rRule.dtStart
                        val untilDate = rRule.until
                        var occurrenceDate = task.date

                        if (rRule.freq == RecurrenceRule.Frequency.ONCE){
                            if (task.date in weekStart..weekEnd && task.date in fromDate..untilDate){
                                finalEvents.add(
                                    HomeDisplayItem.ContentItem(
                                        programTable = programTable,
                                        course = course,
                                        task = task,
                                        occurrenceId = task.id
                                    )
                                )
                            }
                        }else{
                            while (occurrenceDate in weekStart..weekEnd && occurrenceDate in fromDate..untilDate){
                                finalEvents.add(
                                    HomeDisplayItem.ContentItem(
                                        programTable = programTable,
                                        course = course,
                                        task = task.copy(date = occurrenceDate),
                                        occurrenceId = "${task.id}_${occurrenceDate}"
                                    )
                                )
                                occurrenceDate = rRule.getNextOccurrence(occurrenceDate) ?: (weekEnd + 1)
                            }
                        }
                    }
                    is Task.Exam -> {
                        if (task.date in weekStart..weekEnd) {
                            finalEvents.add(
                                HomeDisplayItem.ContentItem(
                                    occurrenceId = "single_${task.id}",
                                    programTable = programTable,
                                    course = course,
                                    task = task
                                )
                            )
                        }
                    }
                    is Task.Homework -> {
                        if (task.dueDate in weekStart..weekEnd) {
                            finalEvents.add(
                                HomeDisplayItem.ContentItem(
                                    occurrenceId = "single_${task.id}",
                                    programTable = programTable,
                                    course = course,
                                    task = task
                                )
                            )
                        }
                    }
                }
            }
        }


        finalEvents
            .sortWith(compareBy<HomeDisplayItem.ContentItem> {
                when(val t = it.task) {
                    is Task.Lesson -> getDayStartMillis(t.date)
                    is Task.Exam -> getDayStartMillis(t.date)
                    is Task.Homework -> getDayStartMillis(t.dueDate)
                }
            }.thenBy {
                when(val t = it.task) {
                    is Task.Lesson -> t.timeStart
                    is Task.Exam -> t.timeStart
                    is Task.Homework -> 0L
                }
            })

        val displayItemsWithHeaders = mutableListOf<HomeDisplayItem>()
        var lastHeaderDate: Long? = null

        finalEvents.forEach { event ->
            val taskDate = when(val t = event.task){
                is Task.Lesson -> t.date
                is Task.Exam -> t.date
                is Task.Homework -> t.dueDate
            }
            val eventDay = getDayStartMillis(taskDate)
            if (eventDay != lastHeaderDate){
                displayItemsWithHeaders.add(HomeDisplayItem.HeaderItem(date = eventDay))
                lastHeaderDate = eventDay
            }
            displayItemsWithHeaders.add(event)
        }
        return displayItemsWithHeaders
    }

    private fun getDayStartMillis(timestamp: Long):Long{
        val calendar = Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE,0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }

}
