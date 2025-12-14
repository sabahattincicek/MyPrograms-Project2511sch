package com.saboon.project_2511sch.domain.usecase.home

import android.icu.util.Calendar
import com.saboon.project_2511sch.domain.model.Course
import com.saboon.project_2511sch.domain.model.ProgramTable
import com.saboon.project_2511sch.domain.model.Schedule
import com.saboon.project_2511sch.domain.repository.ICourseRepository
import com.saboon.project_2511sch.domain.repository.IProgramTableRepository
import com.saboon.project_2511sch.domain.repository.IScheduleRepository
import com.saboon.project_2511sch.presentation.home.HomeDisplayItem
import com.saboon.project_2511sch.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class GetHomeDisplayItemsUseCase @Inject constructor(
    private val programTableRepository: IProgramTableRepository,
    private val courseRepository: ICourseRepository,
    private val scheduleRepository: IScheduleRepository
) {
    operator fun invoke(programTable: ProgramTable): Flow<Resource<List<HomeDisplayItem>>> {
        val courseFlow = courseRepository.getCoursesByProgramTableId(programTable.id)
        val scheduleFlow = scheduleRepository.getSchedulesByProgramTableId(programTable.id)

        return courseFlow.combine(scheduleFlow) { coursesResult, schedulesResult ->
            if (coursesResult is Resource.Error) {
                return@combine Resource.Error(coursesResult.message ?: "Failed to load courses.")
            }
            if (schedulesResult is Resource.Error) {
                return@combine Resource.Error(
                    schedulesResult.message ?: "Failed to load schedules."
                )
            }

            val courses = (coursesResult as? Resource.Success)?.data ?: emptyList()
            val schedules = (schedulesResult as? Resource.Success)?.data ?: emptyList()

            val displayItems = generateAndGroupDisplayList(programTable, courses, schedules)
            Resource.Success(displayItems)
        }
    }

    private fun generateAndGroupDisplayList(
        programTable: ProgramTable,
        courses: List<Course>,
        schedules: List<Schedule>
    ): List<HomeDisplayItem> {
        val finalEvents = mutableListOf<HomeDisplayItem.ContentItem>()
        val courseMap = courses.associateBy { it.id }

        val calendar = Calendar.getInstance()
        val startDate = getDayStartMillis(calendar.timeInMillis)
        calendar.add(Calendar.DAY_OF_YEAR, 30)
        val endDate = calendar.timeInMillis

        schedules.forEach { schedule ->
            val course = courseMap[schedule.courseId]
            if (course != null) {
                if (schedule.recurrenceRule.isBlank()) {
                    finalEvents.add(
                        HomeDisplayItem.ContentItem(
                            occurrenceId = "single_${schedule.id}",
                            programTable = programTable,
                            course = course,
                            schedule = schedule
                        )
                    )
                }
                else {
                    var occurrenceDate = schedule.date
                    while (occurrenceDate <= endDate) {
                        if (occurrenceDate >= startDate) {
                            val occurrenceId = "${schedule.id}_${occurrenceDate}"
                            val occurrenceSchedule = schedule.copy(date = occurrenceDate)

                            finalEvents.add(
                                HomeDisplayItem.ContentItem(
                                    occurrenceId = occurrenceId,
                                    programTable = programTable,
                                    course = course,
                                    schedule = occurrenceSchedule
                                )
                            )
                        }
                        occurrenceDate = getNextOccurrenceDate(occurrenceDate, schedule.recurrenceRule)?:Long.MAX_VALUE
                    }
                }
            }
        }


        finalEvents.sortBy { it.schedule.startTime }
        finalEvents.sortBy { it.schedule.date }

        val displayItemsWithHeaders = mutableListOf<HomeDisplayItem>()
        var lastHeaderDate: Long? = null

        finalEvents.forEach { event ->
            val eventDay = getDayStartMillis(event.schedule.date)
            if (eventDay != lastHeaderDate){
                displayItemsWithHeaders.add(HomeDisplayItem.HeaderItem(date = eventDay))
                lastHeaderDate = eventDay
            }
            displayItemsWithHeaders.add(event)
        }
        return displayItemsWithHeaders
    }


    private fun getNextOccurrenceDate(currentDate: Long, rule: String): Long? {
        val calendar = Calendar.getInstance().apply { timeInMillis = currentDate }
        return when(rule){
            "FREQ=DAILY" -> calendar.apply { add(Calendar.DAY_OF_YEAR, 1) }.timeInMillis
            "FREQ=WEEKLY" -> calendar.apply { add(Calendar.WEEK_OF_YEAR, 1) }.timeInMillis
            "FREQ=MONTHLY" -> calendar.apply { add(Calendar.MONTH, 1) }.timeInMillis
            "FREQ=YEARLY" -> calendar.apply { add(Calendar.YEAR, 1) }.timeInMillis
            else -> null
        }
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




















