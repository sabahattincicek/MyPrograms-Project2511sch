package com.saboon.project_2511sch.domain.usecase.home

import android.icu.util.Calendar
import android.util.Log
import com.saboon.project_2511sch.domain.model.Course
import com.saboon.project_2511sch.domain.model.Tag
import com.saboon.project_2511sch.domain.model.Task
import com.saboon.project_2511sch.domain.repository.ICourseRepository
import com.saboon.project_2511sch.domain.repository.ITagRepository
import com.saboon.project_2511sch.domain.repository.ITaskRepository
import com.saboon.project_2511sch.presentation.common.FilterGeneric
import com.saboon.project_2511sch.presentation.common.FilterTask
import com.saboon.project_2511sch.presentation.home.DisplayItemHome
import com.saboon.project_2511sch.util.RecurrenceRule
import com.saboon.project_2511sch.util.Resource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject
/**
 * UseCase to fetch and prepare the list of items to be displayed on the Home screen.
 */
class GetHomeDisplayItemsUseCase @Inject constructor(
    private val tagRepository: ITagRepository,
    private val courseRepository: ICourseRepository,
    private val taskRepository: ITaskRepository
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(filterTask: FilterTask, startDate: Long, endDate: Long): Flow<Resource<List<DisplayItemHome>>> {
        return tagRepository.getAllActive().flatMapLatest { tagResource ->
            when(tagResource) {
                is Resource.Error -> flowOf(Resource.Error(tagResource.message ?: "Tags could not be loaded"))
                is Resource.Idle -> flowOf(Resource.Idle())
                is Resource.Loading -> flowOf(Resource.Loading())
                is Resource.Success -> {
                    val allActiveTags = tagResource.data ?: emptyList()

                    // Early return if no tags exist to prevent unnecessary database queries
                    if (allActiveTags.isEmpty()) return@flatMapLatest flowOf(Resource.Success(emptyList()))

                    // Get list of Tag IDs to fetch related courses
                    val tagIds = allActiveTags.map { it.id }

                    // 2. Fetch all active Courses belonging to these Tags
                    courseRepository.getAllActivesByTagIds(tagIds).flatMapLatest { courseResource ->
                        when(courseResource) {
                            is Resource.Error -> flowOf(Resource.Error(courseResource.message ?: "Courses can not loaded"))
                            is Resource.Idle -> flowOf(Resource.Idle())
                            is Resource.Loading -> flowOf(Resource.Loading())
                            is Resource.Success -> {
                                val allActiveCourses = courseResource.data ?: emptyList()

                                // If no courses found, return empty list
                                if (allActiveCourses.isEmpty()) return@flatMapLatest flowOf(Resource.Success(emptyList()))

                                val courseIds = allActiveCourses.map { it.id }

                                taskRepository.getAllTasksByCourseIds(courseIds).map { taskResource ->
                                    when(taskResource) {
                                        is Resource.Error -> Resource.Error(taskResource.message ?: "Tasks can not loaded")
                                        is Resource.Idle -> Resource.Idle()
                                        is Resource.Loading -> Resource.Loading()
                                        is Resource.Success -> {
                                            val tasks = taskResource.data ?: emptyList()
                                            // Görev tipine göre (Lesson, Exam, Homework) filtreleme
                                            val filteredTasks = tasks.filter { task ->
                                                when(task) {
                                                    is Task.Lesson -> filterTask.lesson
                                                    is Task.Exam -> filterTask.exam
                                                    is Task.Homework -> filterTask.homework
                                                }
                                            }

                                            if (filteredTasks.isEmpty()) return@map Resource.Success(emptyList())

                                            val displayItems = generateAndGroupDisplayList(
                                                allActiveTags,
                                                allActiveCourses,
                                                filteredTasks,
                                                startDate,
                                                endDate
                                            )
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
//
//    private fun generateAndGroupDisplayList(
//        tags: List<Tag>,
//        courses: List<Course>,
//        tasks: List<Task>,
//        startDate: Long,
//        endDate: Long
//    ): List<DisplayItemHome> {
//        Log.d("GetHomeDisplayItemsUC", "generate: Processing ${tasks.size} tasks for ${tags.size} tables")
//        val finalEvents = mutableListOf<DisplayItemHome.ContentItemHome>()
//
//        // Maps for O(1) lookup speed
//        val tagMap = tags.associateBy { it.id }
//        val courseMap = courses.associateBy { it.id }
//
//        tasks.forEach { task ->
//            // Task no longer has programTableId/tagId. We reach the Tag via the Course.
//            val course = courseMap[task.courseId]
//            val tag = course?.tagId?.let { tagMap[it] }
//
//            if (tag != null && course != null) {
//                when(task) {
//                    is Task.Lesson -> {
//                        val rRule = task.recurrenceRule
//                        Log.d("GetHomeDisplayItemsUC", "Lesson Rule: ${task.recurrenceRule}, Task Date: ${task.date}, Week: $startDate - $endDate")
//
//                        val fromDate = rRule.dtStart
//                        val untilDate = rRule.until
//                        var occurrenceDate = task.date
//
//                        if (rRule.freq == RecurrenceRule.Frequency.ONCE){
//                            if (task.date in startDate..endDate && task.date in fromDate..untilDate){
//                                finalEvents.add(
//                                    DisplayItemHome.ContentItemHome(
//                                        tag = tag,
//                                        course = course,
//                                        task = task,
//                                        occurrenceId = task.id
//                                    )
//                                )
//                            }
//                        }else{
//                            //if lesson started in the past, get date to begin of current week
//                            while (occurrenceDate < startDate && occurrenceDate < untilDate) {
//                                val next = rRule.getNextOccurrence(occurrenceDate) ?: break
//                                occurrenceDate = next
//                            }
//                            while (occurrenceDate in startDate..endDate && occurrenceDate in fromDate..untilDate){
//                                finalEvents.add(
//                                    DisplayItemHome.ContentItemHome(
//                                        tag = tag,
//                                        course = course,
//                                        task = task.copy(date = occurrenceDate),
//                                        occurrenceId = "${task.id}_${occurrenceDate}"
//                                    )
//                                )
//                                occurrenceDate = rRule.getNextOccurrence(occurrenceDate) ?: (endDate + 1)
//                            }
//                        }
//                    }
//                    is Task.Exam -> {
//                        Log.d("GetHomeDisplayItemsUC", "Exam Date: ${task.date}, In Week: ${task.date in startDate..endDate}")
//
//                        if (task.date in startDate..endDate) {
//                            finalEvents.add(
//                                DisplayItemHome.ContentItemHome(
//                                    occurrenceId = "single_${task.id}",
//                                    tag = tag,
//                                    course = course,
//                                    task = task
//                                )
//                            )
//                        }
//                    }
//                    is Task.Homework -> {
//                        Log.d("GetHomeDisplayItemsUC", "Homework Due: ${task.dueDate}, In Week: ${task.dueDate in startDate..endDate}")
//
//                        if (task.dueDate in startDate..endDate) {
//                            finalEvents.add(
//                                DisplayItemHome.ContentItemHome(
//                                    occurrenceId = "single_${task.id}",
//                                    tag = tag,
//                                    course = course,
//                                    task = task
//                                )
//                            )
//                        }
//                    }
//                }
//            }
//        }
//
//        if (finalEvents.isEmpty()) {
//            Log.d("GetHomeDisplayItemsUC", "generate: No tasks found in range, returning empty list.")
//            return emptyList()
//        }
//
//        finalEvents
//            .sortWith(compareBy<DisplayItemHome.ContentItemHome> {
//                when(val t = it.task) {
//                    is Task.Lesson -> getDayStartMillis(t.date)
//                    is Task.Exam -> getDayStartMillis(t.date)
//                    is Task.Homework -> getDayStartMillis(t.dueDate)
//                }
//            }.thenBy {
//                when(val t = it.task) {
//                    is Task.Lesson -> t.timeStart
//                    is Task.Exam -> t.timeStart
//                    is Task.Homework -> 0L
//                }
//            })
//
//        val displayItemsWithHeaders = mutableListOf<DisplayItemHome>()
//        var lastHeaderDate: Long? = null
//
//        finalEvents.forEach { event ->
//            val taskDate = when(val t = event.task){
//                is Task.Lesson -> t.date
//                is Task.Exam -> t.date
//                is Task.Homework -> t.dueDate
//            }
//            val eventDay = getDayStartMillis(taskDate)
//            if (eventDay != lastHeaderDate){
//                displayItemsWithHeaders.add(DisplayItemHome.HeaderItemHome(date = eventDay))
//                lastHeaderDate = eventDay
//            }
//            displayItemsWithHeaders.add(event)
//        }
//        val footerItem = DisplayItemHome.FooterItemHome(startDate, endDate, finalEvents.size)
//        displayItemsWithHeaders.add(footerItem)
//        Log.d("GetHomeDisplayItemsUC", "generate: Final list size with headers: ${displayItemsWithHeaders.size}")
//        return displayItemsWithHeaders
//    }

    private fun generateAndGroupDisplayList(
        tags: List<Tag>,        courses: List<Course>,
        tasks: List<Task>,
        startDate: Long,
        endDate: Long
    ): List<DisplayItemHome> {
        val finalEvents = mutableListOf<DisplayItemHome.ContentItemHome>()

        val tagMap = tags.associateBy { it.id }
        val courseMap = courses.associateBy { it.id }

        // 1. First, collect all task occurrences exactly as before
        tasks.forEach { task ->
            val course = courseMap[task.courseId]
            val tag = course?.tagId?.let { tagMap[it] }

            if (tag != null && course != null) {
                when(task) {
                    is Task.Lesson -> {
                        val rRule = task.recurrenceRule
                        val fromDate = rRule.dtStart
                        val untilDate = rRule.until
                        var occurrenceDate = task.date

                        if (rRule.freq == RecurrenceRule.Frequency.ONCE){
                            if (task.date in startDate..endDate && task.date in fromDate..untilDate){
                                finalEvents.add(DisplayItemHome.ContentItemHome(tag, course, task, task.id))
                            }
                        } else {
                            while (occurrenceDate < startDate && occurrenceDate < untilDate) {
                                val next = rRule.getNextOccurrence(occurrenceDate) ?: break
                                occurrenceDate = next
                            }
                            while (occurrenceDate in startDate..endDate && occurrenceDate in fromDate..untilDate){
                                finalEvents.add(DisplayItemHome.ContentItemHome(tag, course, task.copy(date = occurrenceDate), "${task.id}_${occurrenceDate}"))
                                occurrenceDate = rRule.getNextOccurrence(occurrenceDate) ?: (endDate + 1)
                            }
                        }
                    }
                    is Task.Exam -> {
                        if (task.date in startDate..endDate) {
//                            finalEvents.add(DisplayItemHome.ContentItemHome("single_${task.id}", tag, course, task))
                            finalEvents.add(
                                DisplayItemHome.ContentItemHome(
                                    occurrenceId = "single_${task.id}",
                                    tag = tag,
                                    course = course,
                                    task = task
                                )
                            )
                        }
                    }
                    is Task.Homework -> {
                        if (task.dueDate in startDate..endDate) {
//                            finalEvents.add(DisplayItemHome.ContentItemHome("single_${task.id}", tag, course, task))
                            finalEvents.add(
                                DisplayItemHome.ContentItemHome(
                                    occurrenceId = "single_${task.id}",
                                    tag = tag,
                                    course = course,
                                    task = task
                                )
                            )
                        }
                    }
                }
            }
        }

        // 2. Map occurrences by their day start millis for easy access
        val eventsGroupedByDay = finalEvents.groupBy {
            val taskDate = when(val t = it.task) {
                is Task.Lesson -> t.date
                is Task.Exam -> t.date
                is Task.Homework -> t.dueDate
            }
            getDayStartMillis(taskDate)
        }

        val displayItemsWithHeaders = mutableListOf<DisplayItemHome>()

        // 3. Loop through EVERY day between startDate and endDate
        val calendar = Calendar.getInstance()
        var currentDayMillis = getDayStartMillis(startDate)
        val finalDayMillis = getDayStartMillis(endDate)

        while (currentDayMillis <= finalDayMillis) {
            // ALWAYS add a Header for the current day, even if it's empty
            displayItemsWithHeaders.add(DisplayItemHome.HeaderItemHome(date = currentDayMillis))

            // Add tasks belonging to this day (if any), sorted by time
            val tasksForThisDay = eventsGroupedByDay[currentDayMillis]
            if (tasksForThisDay != null) {
                val sortedTasks = tasksForThisDay.sortedBy {
                    when(val t = it.task) {
                        is Task.Lesson -> t.timeStart
                        is Task.Exam -> t.timeStart
                        is Task.Homework -> 0L
                    }
                }
                displayItemsWithHeaders.addAll(sortedTasks)
            }

            // Move to the next day
            calendar.timeInMillis = currentDayMillis
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            currentDayMillis = getDayStartMillis(calendar.timeInMillis)
        }

        // 4. Add summary Footer at the end
        val footerItem = DisplayItemHome.FooterItemHome(startDate, endDate, finalEvents.size)
        displayItemsWithHeaders.add(footerItem)

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
