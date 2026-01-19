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
    operator fun invoke(programTable: ProgramTable): Flow<Resource<List<HomeDisplayItem>>> {

        return combine(
            courseRepository.getCoursesByProgramTableId(programTable.id),
            taskRepository.getAllTaskByProgramTableId(programTable.id)
        ) { coursesResult, tasksResult ->
            if (coursesResult is Resource.Error) {
                return@combine Resource.Error(coursesResult.message ?: "Failed to load courses.")
            }
            if (tasksResult is Resource.Error) {
                return@combine Resource.Error(tasksResult.message ?: "Failed to load tasks.")
            }

            val courses = (coursesResult as? Resource.Success)?.data ?: emptyList()
            val tasks = (tasksResult as? Resource.Success)?.data ?: emptyList()

            val displayItems = generateAndGroupDisplayList(programTable, courses, tasks)
            Resource.Success(displayItems)
        }
    }

    private fun generateAndGroupDisplayList(
        programTable: ProgramTable,
        courses: List<Course>,
        tasks: List<Task>
    ): List<HomeDisplayItem> {
        val finalEvents = mutableListOf<HomeDisplayItem.ContentItem>()
        val courseMap = courses.associateBy { it.id }

        val calendar = Calendar.getInstance()
        val startDate = getDayStartMillis(calendar.timeInMillis)
        calendar.add(Calendar.DAY_OF_YEAR, 30)
        val endDate = calendar.timeInMillis

        tasks.forEach { task ->
            val course = courseMap[task.courseId]
            if (course != null) {
                when(task) {
                    is Task.Lesson -> {
                        val rRule = RecurrenceRule.fromRuleString(task.recurrenceRule)
                        val untilDate = rRule.until
                        
                        // Corrected: task.date is the anchor (determines the day of week)
                        // rRule.dtStart is the visibility start filter
                        var occurrenceDate = task.date
                        val visibilityStart = rRule.dtStart

                        if (rRule.freq == RecurrenceRule.Frequency.ONCE){
                            if (task.date in startDate..endDate && task.date >= visibilityStart){
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
                            while (occurrenceDate <= endDate && occurrenceDate <= untilDate){
                                // Only add if within the 30-day window AND after the visibility start date
                                if (occurrenceDate >= startDate && occurrenceDate >= visibilityStart){
                                    val occurrenceId = "${task.id}_${occurrenceDate}"
                                    val occurrenceSchedule = task.copy(
                                        date = occurrenceDate
                                    )
                                    finalEvents.add(
                                        HomeDisplayItem.ContentItem(
                                            programTable = programTable,
                                            course = course,
                                            task = occurrenceSchedule,
                                            occurrenceId = occurrenceId
                                        )
                                    )
                                }
                                occurrenceDate = rRule.getNextOccurrence(occurrenceDate) ?: (endDate + 1)
                            }
                        }
                    }
                    is Task.Exam -> {
                        if (task.date in startDate..endDate) {
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
                        if (task.dueDate in startDate..endDate) {
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
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }

}
