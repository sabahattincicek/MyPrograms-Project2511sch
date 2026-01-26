package com.saboon.project_2511sch.domain.usecase.home

import android.icu.util.Calendar
import android.util.Log
import com.saboon.project_2511sch.domain.model.Course
import com.saboon.project_2511sch.domain.model.ProgramTable
import com.saboon.project_2511sch.domain.model.Task
import com.saboon.project_2511sch.domain.repository.ICourseRepository
import com.saboon.project_2511sch.domain.repository.IProgramTableRepository
import com.saboon.project_2511sch.domain.repository.ITaskRepository
import com.saboon.project_2511sch.presentation.home.FilterTask
import com.saboon.project_2511sch.presentation.home.HomeDisplayItem
import com.saboon.project_2511sch.util.RecurrenceRule
import com.saboon.project_2511sch.util.Resource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetHomeDisplayItemsUseCase @Inject constructor(
    private val programTableRepository: IProgramTableRepository,
    private val courseRepository: ICourseRepository,
    private val taskRepository: ITaskRepository
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(filterTask: FilterTask): Flow<Resource<List<HomeDisplayItem>>> {
        return programTableRepository.getAllActive().flatMapLatest { ptResource ->
            when(ptResource) {
                is Resource.Error -> flowOf(Resource.Error(ptResource.message ?: "ProgramTables can not loaded"))
                is Resource.Idle -> flowOf(Resource.Idle())
                is Resource.Loading -> flowOf(Resource.Loading())
                is Resource.Success -> {
                    val activeTables = ptResource.data ?: emptyList()
                    val tableIds = activeTables.map { it.id }

                    courseRepository.getAllActivesByProgramTableIds(tableIds).flatMapLatest { courseResource ->
                        when(courseResource) {
                            is Resource.Error -> flowOf(Resource.Error(courseResource.message ?: "Courses can not loaded"))
                            is Resource.Idle -> flowOf(Resource.Idle())
                            is Resource.Loading -> flowOf(Resource.Loading())
                            is Resource.Success -> {
                                val activeCourses = courseResource.data ?: emptyList()
                                val courseIds = activeCourses.map { it.id }

                                taskRepository.getAllTasksByCourseIds(courseIds).map { taskResource ->
                                    when(taskResource) {
                                        is Resource.Error -> Resource.Error(taskResource.message ?: "Courses can not loaded")
                                        is Resource.Idle -> Resource.Idle()
                                        is Resource.Loading -> Resource.Loading()
                                        is Resource.Success -> {
                                            val tasks = taskResource.data ?: emptyList()
                                            val filteredTasks = tasks.filter { task ->
                                                when(task) {
                                                    is Task.Lesson -> filterTask.lesson
                                                    is Task.Exam -> filterTask.exam
                                                    is Task.Homework -> filterTask.homework
                                                }
                                            }
                                            val displayItems = generateAndGroupDisplayList(activeTables, activeCourses, filteredTasks)
                                            Resource.Success(displayItems)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun generateAndGroupDisplayList(
        programTables: List<ProgramTable>,
        courses: List<Course>,
        tasks: List<Task>
    ): List<HomeDisplayItem> {
        Log.d("GetHomeDisplayItemsUC", "generate: Processing ${tasks.size} tasks for ${programTables.size} tables")
        val finalEvents = mutableListOf<HomeDisplayItem.ContentItem>()

        val programTableMap = programTables.associateBy { it.id }
        val courseMap = courses.associateBy { it.id }

        val calendar = Calendar.getInstance()

        //find which day of the current week
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) // sunday = 1, monday = 2 ...
        //calculate how many days to go back to reach monday
        val daysFromMonday = if (dayOfWeek == Calendar.SUNDAY) 6 else dayOfWeek - Calendar.MONDAY

        //set calendar to monday
        calendar.add(Calendar.DAY_OF_YEAR, -daysFromMonday)
        val weekStart = getDayStartMillis(calendar.timeInMillis) //monday 00:00:00
        //find sunday of the current week
        calendar.add(Calendar.DAY_OF_YEAR, 6)
        val weekEnd = getDayEndMillis(calendar.timeInMillis) //sunday 23:59:59.999

        tasks.forEach { task ->
            val programTable = programTableMap[task.programTableId]
            val course = courseMap[task.courseId]

            Log.d("GetHomeDisplayItemsUC", "Checking task: ${task.id}, Table: ${task.programTableId}, Course: ${task.courseId}")
            if (programTable == null) Log.w("GetHomeDisplayItemsUC", "Table not found for task ${task.id}")
            if (course == null) Log.w("GetHomeDisplayItemsUC", "Course not found for task ${task.id}")

            if (programTable != null && course != null) {
                when(task) {
                    is Task.Lesson -> {
                        val rRule = RecurrenceRule.fromRuleString(task.recurrenceRule)
                        Log.d("GetHomeDisplayItemsUC", "Lesson Rule: ${task.recurrenceRule}, Task Date: ${task.date}, Week: $weekStart - $weekEnd")

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
                            //if lesson started in the past, get date to begin of current week
                            while (occurrenceDate < weekStart && occurrenceDate < untilDate) {
                                val next = rRule.getNextOccurrence(occurrenceDate) ?: break
                                occurrenceDate = next
                            }
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
                        Log.d("GetHomeDisplayItemsUC", "Exam Date: ${task.date}, In Week: ${task.date in weekStart..weekEnd}")

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
                        Log.d("GetHomeDisplayItemsUC", "Homework Due: ${task.dueDate}, In Week: ${task.dueDate in weekStart..weekEnd}")

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
        Log.d("GetHomeDisplayItemsUC", "generate: Final list size with headers: ${displayItemsWithHeaders.size}")
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

    private fun getDayEndMillis(timestamp: Long): Long {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        return calendar.timeInMillis
    }
}
