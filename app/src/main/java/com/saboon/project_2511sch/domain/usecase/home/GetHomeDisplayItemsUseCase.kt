package com.saboon.project_2511sch.domain.usecase.home

import android.icu.util.Calendar
import com.saboon.project_2511sch.domain.model.Course
import com.saboon.project_2511sch.domain.model.Tag
import com.saboon.project_2511sch.domain.model.Task
import com.saboon.project_2511sch.domain.repository.ICourseRepository
import com.saboon.project_2511sch.domain.repository.ITagRepository
import com.saboon.project_2511sch.domain.repository.ITaskRepository
import com.saboon.project_2511sch.presentation.common.FilterTask
import com.saboon.project_2511sch.presentation.home.DisplayItemHome
import com.saboon.project_2511sch.util.RecurrenceRule
import com.saboon.project_2511sch.util.Resource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
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
        // 1. Fetch active Tags and active Courses simultaneously using combine
        return combine(
            tagRepository.getAllActive(),
            courseRepository.getAllActive()
        ) { tagResource, courseResource ->
            Pair(tagResource, courseResource)
        }.flatMapLatest { (tagRes, courseRes) ->

            if (tagRes is Resource.Success && courseRes is Resource.Success) {
                val activeTags = tagRes.data ?: emptyList()
                val activeCourses = courseRes.data ?: emptyList()

                // Map active tags by ID for O(1) optimized lookup
                val activeTagsMap = activeTags.associateBy { it.id }

                // 2. Filter courses: Include if tagId is null OR if tagId exists in active tags
                val validCourses = activeCourses.filter { course ->
                    course.tagId == null || activeTagsMap.containsKey(course.tagId)
                }

                // Early return if no valid courses are found
                if (validCourses.isEmpty()) {
                    return@flatMapLatest flowOf(Resource.Success(emptyList()))
                }

                val validCourseIds = validCourses.map { it.id }

                // 3. Fetch all tasks belonging to the validated courses
                taskRepository.getAllTasksByCourseIds(validCourseIds).map { taskResource ->
                    when(taskResource) {
                        is Resource.Success -> {
                            val tasks = taskResource.data ?: emptyList()

                            // Filter tasks based on UI toggles (Lesson, Exam, Homework)
                            val filteredTasks = tasks.filter { task ->
                                when(task) {
                                    is Task.Lesson -> filterTask.lesson
                                    is Task.Exam -> filterTask.exam
                                    is Task.Homework -> filterTask.homework
                                }
                            }

                            // Trigger Empty State UI if no tasks match filters after validation
                            if (filteredTasks.isEmpty()) return@map Resource.Success(emptyList())

                            // Process the validated data into UI-ready display items
                            val displayItems = generateAndGroupDisplayList(
                                activeTags,
                                validCourses,
                                filteredTasks,
                                startDate,
                                endDate
                            )
                            Resource.Success(displayItems)
                        }
                        is Resource.Error -> Resource.Error(taskResource.message ?: "Tasks could not be loaded")
                        is Resource.Loading -> Resource.Loading()
                        is Resource.Idle -> Resource.Idle()
                    }
                }
            } else if (tagRes is Resource.Error || courseRes is Resource.Error) {
                flowOf(Resource.Error("Error loading active tags or courses"))
            } else {
                flowOf(Resource.Loading())
            }
        }
    }
    private fun generateAndGroupDisplayList(
        tags: List<Tag>,
        courses: List<Course>,
        tasks: List<Task>,
        startDate: Long,
        endDate: Long
    ): List<DisplayItemHome> {
        val finalEvents = mutableListOf<DisplayItemHome.ContentItemHome>()

        val tagMap = tags.associateBy { it.id }
        val courseMap = courses.associateBy { it.id }

        // Step 1: Collect occurrences within the date range for each task
        tasks.forEach { task ->
            val course = courseMap[task.courseId]
            val tag = course?.tagId?.let { tagMap[it] } // Tag can be null for "Untagged" courses

            if (course != null) { // Only tag needs to be checked against active list, course is already active
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
                                finalEvents.add(
                                    DisplayItemHome.ContentItemHome(
                                        tag, course, task.copy(date = occurrenceDate), "${task.id}_${occurrenceDate}"
                                    )
                                )
                                occurrenceDate = rRule.getNextOccurrence(occurrenceDate) ?: (endDate + 1)
                            }
                        }
                    }
                    is Task.Exam -> {
                        if (task.date in startDate..endDate) {
                            finalEvents.add(DisplayItemHome.ContentItemHome(tag, course, task, "single_${task.id}"))
                        }
                    }
                    is Task.Homework -> {
                        if (task.dueDate in startDate..endDate) {
                            finalEvents.add(DisplayItemHome.ContentItemHome(tag, course, task, "single_${task.id}"))
                        }
                    }
                }
            }
        }

        // Step 2: Group task occurrences by day for UI Header placement
        val eventsGroupedByDay = finalEvents.groupBy {
            val taskDate = when(val t = it.task) {
                is Task.Lesson -> t.date
                is Task.Exam -> t.date
                is Task.Homework -> t.dueDate
            }
            getDayStartMillis(taskDate)
        }

        val displayItemsWithHeaders = mutableListOf<DisplayItemHome>()
        val calendar = Calendar.getInstance()
        var currentDayMillis = getDayStartMillis(startDate)
        val finalDayMillis = getDayStartMillis(endDate)

        // Step 3: Loop through every day in range to ensure mandatory Headers
        while (currentDayMillis <= finalDayMillis) {
            displayItemsWithHeaders.add(DisplayItemHome.HeaderItemHome(date = currentDayMillis))

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

            calendar.timeInMillis = currentDayMillis
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            currentDayMillis = getDayStartMillis(calendar.timeInMillis)
        }

        // Step 4: Add summary footer
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
