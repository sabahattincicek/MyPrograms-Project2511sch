package com.saboon.project_2511sch.domain.usecase.home

import com.google.common.truth.Truth.assertThat
import com.saboon.project_2511sch.domain.model.FilterTask
import com.saboon.project_2511sch.domain.model.Task
import com.saboon.project_2511sch.domain.repository.ICourseRepository
import com.saboon.project_2511sch.domain.repository.ITagRepository
import com.saboon.project_2511sch.domain.repository.ITaskRepository
import com.saboon.project_2511sch.domain.usecase.task.TestBaseTask
import com.saboon.project_2511sch.util.RecurrenceRule
import com.saboon.project_2511sch.util.Resource
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.TimeZone

@ExperimentalCoroutinesApi
class TestGetHomeTimelineUseCase : TestBaseTask() {

    private val tagRepository = mockk<ITagRepository>()
    private val courseRepository = mockk<ICourseRepository>()
    private val taskRepository = mockk<ITaskRepository>()
    private val useCase = GetHomeTimelineUseCase(tagRepository, courseRepository, taskRepository)
    private lateinit var originalZone: TimeZone

    private val hour = 60 * 60 * 1000L
    private val day = 24 * hour
    private val week = 7 * day
    // Monday 2026-09-07 00:00 UTC
    private val monday = 1_788_739_200_000L
    private val rangeStart = monday
    private val rangeEnd = monday + week - 1

    private val course = baseCourse.copy(id = "c1", tagId = null)

    @Before
    fun setup() {
        // UTC keeps day boundaries and weekly steps free of daylight saving shifts
        originalZone = TimeZone.getDefault()
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
        every { tagRepository.getAllActive() } returns flowOf(Resource.Success(emptyList()))
        every { courseRepository.getAllActive() } returns flowOf(Resource.Success(listOf(course)))
    }

    @After
    fun tearDown() {
        TimeZone.setDefault(originalZone)
    }

    private fun givenTasks(vararg tasks: Task) {
        every { taskRepository.getAllTasksByCourseIds(listOf("c1")) } returns flowOf(Resource.Success(tasks.toList()))
    }

    private fun weeklyLesson(firstDate: Long) = baseLesson.copy(
        id = "l1",
        courseId = "c1",
        date = firstDate,
        timeStart = firstDate,
        recurrenceRule = RecurrenceRule(RecurrenceRule.Frequency.WEEKLY, firstDate, Long.MAX_VALUE)
    )

    @Test
    fun `weekly lesson that started earlier appears once on its weekday in the range`() = runTest {
        // GIVEN: first session on Wednesday three weeks before the shown week
        val firstSession = monday - 3 * week + 2 * day + 9 * hour
        givenTasks(weeklyLesson(firstSession))

        // WHEN
        val timeline = useCase(FilterTask(), rangeStart, rangeEnd, hideEmptyDays = true).first().data!!

        // THEN
        assertThat(timeline.hasTasks).isTrue()
        assertThat(timeline.days).hasSize(1)
        assertThat(timeline.days[0].date).isEqualTo(monday + 2 * day)
        val item = timeline.days[0].items.single()
        assertThat((item.task as Task.Lesson).date).isEqualTo(monday + 2 * day + 9 * hour)
        assertThat(item.occurrenceId).isEqualTo("l1_${monday + 2 * day + 9 * hour}")
    }

    @Test
    fun `every day of the range is returned when empty days are not hidden`() = runTest {
        // GIVEN
        givenTasks(weeklyLesson(monday + 9 * hour))

        // WHEN
        val timeline = useCase(FilterTask(), rangeStart, rangeEnd, hideEmptyDays = false).first().data!!

        // THEN
        assertThat(timeline.days).hasSize(7)
        assertThat(timeline.days.map { it.items.size }).containsExactly(1, 0, 0, 0, 0, 0, 0).inOrder()
        assertThat(timeline.itemCount).isEqualTo(1)
    }

    @Test
    fun `tasks of one day are sorted by start time`() = runTest {
        // GIVEN
        val late = weeklyLesson(monday + 14 * hour).copy(id = "late")
        val early = baseExam.copy(id = "early", courseId = "c1", date = monday + 8 * hour, timeStart = monday + 8 * hour)
        givenTasks(late, early)

        // WHEN
        val timeline = useCase(FilterTask(), rangeStart, rangeEnd, hideEmptyDays = true).first().data!!

        // THEN
        assertThat(timeline.days.single().items.map { it.task.id }).containsExactly("early", "late").inOrder()
    }

    @Test
    fun `filter leaves out the unwanted task types`() = runTest {
        // GIVEN
        val exam = baseExam.copy(id = "e1", courseId = "c1", date = monday + day)
        val homework = baseHomework.copy(id = "h1", courseId = "c1", dueDate = monday + 2 * day)
        givenTasks(weeklyLesson(monday + 9 * hour), exam, homework)

        // WHEN
        val timeline = useCase(
            FilterTask(lesson = false, exam = true, homework = false), rangeStart, rangeEnd, hideEmptyDays = true
        ).first().data!!

        // THEN
        assertThat(timeline.days.flatMap { it.items }.map { it.task.id }).containsExactly("e1")
    }

    @Test
    fun `tasks outside the range are not shown`() = runTest {
        // GIVEN: an exam next week, a one-off lesson last week
        val exam = baseExam.copy(id = "e1", courseId = "c1", date = monday + week + day)
        val oldLesson = baseLesson.copy(id = "l0", courseId = "c1", date = monday - day)
        givenTasks(exam, oldLesson)

        // WHEN
        val timeline = useCase(FilterTask(), rangeStart, rangeEnd, hideEmptyDays = true).first().data!!

        // THEN: there are tasks, just none this week
        assertThat(timeline.hasTasks).isTrue()
        assertThat(timeline.days).isEmpty()
    }

    @Test
    fun `lesson is not shown after its recurrence ended`() = runTest {
        // GIVEN: the lesson repeated only until the week before the shown week
        val firstSession = monday - 3 * week + 9 * hour
        val lesson = weeklyLesson(firstSession).copy(
            recurrenceRule = RecurrenceRule(RecurrenceRule.Frequency.WEEKLY, firstSession, monday - day)
        )
        givenTasks(lesson)

        // WHEN
        val timeline = useCase(FilterTask(), rangeStart, rangeEnd, hideEmptyDays = true).first().data!!

        // THEN
        assertThat(timeline.days).isEmpty()
    }

    @Test
    fun `course with an inactive tag is left out`() = runTest {
        // GIVEN
        val hidden = baseCourse.copy(id = "c1", tagId = "inactive-tag")
        every { courseRepository.getAllActive() } returns flowOf(Resource.Success(listOf(hidden)))

        // WHEN
        val timeline = useCase(FilterTask(), rangeStart, rangeEnd, hideEmptyDays = false).first().data!!

        // THEN
        assertThat(timeline.hasTasks).isFalse()
        assertThat(timeline.days).isEmpty()
    }

    @Test
    fun `no task of the wanted types means nothing to show`() = runTest {
        // GIVEN
        givenTasks(weeklyLesson(monday + 9 * hour))

        // WHEN
        val timeline = useCase(
            FilterTask(lesson = false, exam = true, homework = true), rangeStart, rangeEnd, hideEmptyDays = false
        ).first().data!!

        // THEN
        assertThat(timeline.hasTasks).isFalse()
    }

    @Test
    fun `task repository error returns Error`() = runTest {
        // GIVEN
        every { taskRepository.getAllTasksByCourseIds(any()) } returns flowOf(Resource.Error("Database Error"))

        // WHEN
        val result = useCase(FilterTask(), rangeStart, rangeEnd, hideEmptyDays = false).first()

        // THEN
        assertThat(result).isInstanceOf(Resource.Error::class.java)
        assertThat(result.message).isEqualTo("Database Error")
    }
}
