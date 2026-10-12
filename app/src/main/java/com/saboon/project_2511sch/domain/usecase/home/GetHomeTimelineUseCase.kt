package com.saboon.project_2511sch.domain.usecase.home

import com.saboon.project_2511sch.domain.model.Course
import com.saboon.project_2511sch.domain.model.FilterTask
import com.saboon.project_2511sch.domain.model.HomeDay
import com.saboon.project_2511sch.domain.model.HomeTimeline
import com.saboon.project_2511sch.domain.model.HomeTimelineItem
import com.saboon.project_2511sch.domain.model.Tag
import com.saboon.project_2511sch.domain.model.Task
import com.saboon.project_2511sch.domain.repository.ICourseRepository
import com.saboon.project_2511sch.domain.repository.ITagRepository
import com.saboon.project_2511sch.domain.repository.ITaskRepository
import com.saboon.project_2511sch.util.RecurrenceRule
import com.saboon.project_2511sch.util.Resource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.util.Calendar
import javax.inject.Inject

/**
 * UseCase that builds the day by day timeline of tasks for the home screen and the home widget.
 *
 * It takes the tasks of every visible course (active, and either untagged or with an active
 * tag), keeps the requested task types, expands repeating lessons into one occurrence per
 * session inside the date range and groups the occurrences by day.
 * How the timeline is drawn (day headers, footer) is decided by the presentation layer.
 */
class GetHomeTimelineUseCase @Inject constructor(
    private val tagRepository: ITagRepository,
    private val courseRepository: ICourseRepository,
    private val taskRepository: ITaskRepository
) {
    /**
     * @param filterTask Which task types to include.
     * @param startDate Start of the range (milliseconds).
     * @param endDate End of the range (milliseconds).
     * @param hideEmptyDays When true, days without any task are left out of the result.
     * @return A Flow of the timeline. [HomeTimeline.hasTasks] is false when there is no visible
     * course or no task of the requested types.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(
        filterTask: FilterTask,
        startDate: Long,
        endDate: Long,
        hideEmptyDays: Boolean
    ): Flow<Resource<HomeTimeline>> {
        val emptyTimeline = HomeTimeline(startDate, endDate, emptyList(), hasTasks = false)

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
                    return@flatMapLatest flowOf(Resource.Success(emptyTimeline))
                }

                // 3. Fetch all tasks belonging to the validated courses
                taskRepository.getAllTasksByCourseIds(validCourses.map { it.id }).map { taskResource ->
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

                            // Nothing to show at all: let the UI show its empty state
                            if (filteredTasks.isEmpty()) return@map Resource.Success(emptyTimeline)

                            val days = buildDays(activeTags, validCourses, filteredTasks, startDate, endDate, hideEmptyDays)
                            Timber.d("Home timeline built: tasks=%d, days=%d", filteredTasks.size, days.size)
                            Resource.Success(HomeTimeline(startDate, endDate, days, hasTasks = true))
                        }
                        is Resource.Error -> Resource.Error(taskResource.message ?: "Tasks could not be loaded")
                        is Resource.Loading -> Resource.Loading()
                        is Resource.Idle -> Resource.Idle()
                    }
                }
            } else if (tagRes is Resource.Error || courseRes is Resource.Error) {
                Timber.w("Home timeline could not be loaded: tag=%s, course=%s", tagRes.message, courseRes.message)
                flowOf(Resource.Error("Error loading active tags or courses"))
            } else {
                flowOf(Resource.Loading())
            }
        }
    }

    /**
     * Expands the tasks into their occurrences inside the range and groups them by day.
     * Every day of the range is returned, unless [hideEmptyDays] removes the ones without tasks.
     */
    private fun buildDays(
        tags: List<Tag>,
        courses: List<Course>,
        tasks: List<Task>,
        startDate: Long,
        endDate: Long,
        hideEmptyDays: Boolean
    ): List<HomeDay> {
        val occurrences = mutableListOf<HomeTimelineItem>()

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
                                occurrences.add(HomeTimelineItem(tag, course, task, task.id))
                            }
                        } else {
                            while (occurrenceDate < startDate && occurrenceDate < untilDate) {
                                val next = rRule.getNextOccurrence(occurrenceDate) ?: break
                                occurrenceDate = next
                            }
                            while (occurrenceDate in startDate..endDate && occurrenceDate in fromDate..untilDate){
                                occurrences.add(
                                    HomeTimelineItem(
                                        tag, course, task.copy(date = occurrenceDate), "${task.id}_${occurrenceDate}"
                                    )
                                )
                                occurrenceDate = rRule.getNextOccurrence(occurrenceDate) ?: (endDate + 1)
                            }
                        }
                    }
                    is Task.Exam -> {
                        if (task.date in startDate..endDate) {
                            occurrences.add(HomeTimelineItem(tag, course, task, "single_${task.id}"))
                        }
                    }
                    is Task.Homework -> {
                        if (task.dueDate in startDate..endDate) {
                            occurrences.add(HomeTimelineItem(tag, course, task, "single_${task.id}"))
                        }
                    }
                }
            }
        }

        // Step 2: Group task occurrences by day
        val occurrencesByDay = occurrences.groupBy {
            val taskDate = when(val t = it.task) {
                is Task.Lesson -> t.date
                is Task.Exam -> t.date
                is Task.Homework -> t.dueDate
            }
            getDayStartMillis(taskDate)
        }

        // Step 3: Walk through every day of the range
        val days = mutableListOf<HomeDay>()
        val calendar = Calendar.getInstance()
        var currentDayMillis = getDayStartMillis(startDate)
        val finalDayMillis = getDayStartMillis(endDate)

        while (currentDayMillis <= finalDayMillis) {
            val itemsOfDay = occurrencesByDay[currentDayMillis]
            if (itemsOfDay != null) {
                val sortedItems = itemsOfDay.sortedBy {
                    when(val t = it.task) {
                        is Task.Lesson -> t.timeStart
                        is Task.Exam -> t.timeStart
                        is Task.Homework -> 0L
                    }
                }
                days.add(HomeDay(currentDayMillis, sortedItems))
            } else if (!hideEmptyDays){
                days.add(HomeDay(currentDayMillis, emptyList()))
            }

            calendar.timeInMillis = currentDayMillis
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            currentDayMillis = getDayStartMillis(calendar.timeInMillis)
        }

        return days
    }

    /**
     * Returns midnight (local time) of the day [timestamp] falls on.
     */
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
