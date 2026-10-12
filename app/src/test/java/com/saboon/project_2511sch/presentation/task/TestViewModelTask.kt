package com.saboon.project_2511sch.presentation.task

import com.google.common.truth.Truth.assertThat
import com.saboon.project_2511sch.domain.alarm.IAlarmScheduler
import com.saboon.project_2511sch.domain.model.AbsenceRecord
import com.saboon.project_2511sch.domain.model.Course
import com.saboon.project_2511sch.domain.usecase.task.GetCourseAbsenceRecordsUseCase
import com.saboon.project_2511sch.domain.usecase.task.GetTaskDisplayItemUseCase
import com.saboon.project_2511sch.domain.usecase.task.TaskWriteUseCase
import com.saboon.project_2511sch.domain.usecase.task.TestBaseTask
import com.saboon.project_2511sch.util.OperationType
import com.saboon.project_2511sch.util.Resource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class TestViewModelTask : TestBaseTask() {

    private val taskWriteUseCase = mockk<TaskWriteUseCase>()
    private val getTaskDisplayItemUseCase = mockk<GetTaskDisplayItemUseCase>()
    private val alarmScheduler = mockk<IAlarmScheduler>(relaxed = true)
    private val course = mockk<Course>(relaxed = true)
    private lateinit var viewModel: ViewModelTask

    @Before
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = ViewModelTask(
            taskWriteUseCase,
            getTaskDisplayItemUseCase,
            GetCourseAbsenceRecordsUseCase(),
            alarmScheduler
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `absenceRecordsState is empty when no course is selected`() = runTest {
        // GIVEN: a subscriber, so the WhileSubscribed state flow starts collecting
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.absenceRecordsState.collect {} }

        // THEN
        assertThat(viewModel.absenceRecordsState.value).isEmpty()
        job.cancel()
    }

    @Test
    fun `absenceRecordsState lists absences of the selected course lessons`() = runTest {
        // GIVEN: the course has a lesson with two absences, an exam and the list header/footer
        val lesson = baseLesson.copy(id = "l1", title = "Teori", absence = listOf(100L, 200L))
        val displayItems = listOf(
            DisplayItemTask.HeaderTask("Lesson"),
            DisplayItemTask.ContentTask(course, lesson),
            DisplayItemTask.ContentTask(course, baseExam),
            DisplayItemTask.FooterTask(2)
        )
        every { getTaskDisplayItemUseCase.invoke(course) } returns flowOf(Resource.Success(displayItems))
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.absenceRecordsState.collect {} }

        // WHEN
        viewModel.loadTasksBy(course)

        // THEN
        assertThat(viewModel.absenceRecordsState.value).containsExactly(
            AbsenceRecord("l1", "Teori", 200L),
            AbsenceRecord("l1", "Teori", 100L)
        ).inOrder()
        job.cancel()
    }

    @Test
    fun `absenceRecordsState is empty when tasks fail to load`() = runTest {
        // GIVEN
        every { getTaskDisplayItemUseCase.invoke(course) } returns flowOf(Resource.Error("Database Error"))
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.absenceRecordsState.collect {} }

        // WHEN
        viewModel.loadTasksBy(course)

        // THEN
        assertThat(viewModel.absenceRecordsState.value).isEmpty()
        job.cancel()
    }

    @Test
    fun `update success reschedules alarm and emits Success`() = runTest {
        // GIVEN
        coEvery { taskWriteUseCase.update(baseLesson) } returns Resource.Success(baseLesson)

        // WHEN
        viewModel.update(course, baseLesson)
        val loading = viewModel.operationEvent.first()
        val result = viewModel.operationEvent.first()

        // THEN
        assertThat(loading).isInstanceOf(Resource.Loading::class.java)
        assertThat(result).isInstanceOf(Resource.Success::class.java)
        assertThat(result.data?.operationType).isEqualTo(OperationType.UPDATE)
        coVerify { alarmScheduler.cancel(course, baseLesson) }
        coVerify { alarmScheduler.schedule(course, baseLesson) }
    }

    @Test
    fun `update failure emits Error and leaves alarm untouched`() = runTest {
        // GIVEN
        coEvery { taskWriteUseCase.update(baseLesson) } returns Resource.Error("Database Error")

        // WHEN
        viewModel.update(course, baseLesson)
        viewModel.operationEvent.first()
        val result = viewModel.operationEvent.first()

        // THEN
        assertThat(result).isInstanceOf(Resource.Error::class.java)
        assertThat(result.message).isEqualTo("Database Error")
        coVerify(exactly = 0) { alarmScheduler.schedule(any(), any()) }
    }
}
