package com.saboon.project_2511sch.domain.usecase.task

import com.google.common.truth.Truth.assertThat
import com.saboon.project_2511sch.domain.model.Task
import com.saboon.project_2511sch.domain.repository.ITaskRepository
import com.saboon.project_2511sch.util.Resource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class TestLessonAbsenceWriteUseCase : TestBaseTask() {

    private val taskRepository = mockk<ITaskRepository>()
    private val taskWriteUseCase = mockk<TaskWriteUseCase>()
    private lateinit var useCase: LessonAbsenceWriteUseCase
    private val saved = slot<Task>()

    @Before
    fun setup() {
        useCase = LessonAbsenceWriteUseCase(taskRepository, taskWriteUseCase)
        coEvery { taskWriteUseCase.update(capture(saved)) } answers { Resource.Success(saved.captured) }
    }

    @Test
    fun `add appends the date to the stored lesson`() = runTest {
        // GIVEN
        val stored = baseLesson.copy(id = "l1", date = 1_000L, absence = listOf(100L))
        every { taskRepository.getById("l1") } returns flowOf(Resource.Success(stored))

        // WHEN
        val result = useCase.add("l1", 200L)

        // THEN
        assertThat(result).isInstanceOf(Resource.Success::class.java)
        assertThat((saved.captured as Task.Lesson).absence).containsExactly(100L, 200L).inOrder()
    }

    @Test
    fun `add keeps the lesson date untouched`() = runTest {
        // GIVEN: the home screen works with copies whose date is a later session; the stored date must win
        val stored = baseLesson.copy(id = "l1", date = 1_000L)
        every { taskRepository.getById("l1") } returns flowOf(Resource.Success(stored))

        // WHEN
        useCase.add("l1", 9_000L)

        // THEN
        assertThat((saved.captured as Task.Lesson).date).isEqualTo(1_000L)
    }

    @Test
    fun `add allows the same date twice`() = runTest {
        // GIVEN
        val stored = baseLesson.copy(id = "l1", absence = listOf(100L))
        every { taskRepository.getById("l1") } returns flowOf(Resource.Success(stored))

        // WHEN
        useCase.add("l1", 100L)

        // THEN
        assertThat((saved.captured as Task.Lesson).absence).containsExactly(100L, 100L)
    }

    @Test
    fun `add skips Loading and uses the first loaded state`() = runTest {
        // GIVEN
        val stored = baseLesson.copy(id = "l1")
        every { taskRepository.getById("l1") } returns flowOf(Resource.Loading(), Resource.Success(stored))

        // WHEN
        val result = useCase.add("l1", 100L)

        // THEN
        assertThat(result).isInstanceOf(Resource.Success::class.java)
    }

    @Test
    fun `add returns Error when the repository fails`() = runTest {
        // GIVEN
        every { taskRepository.getById("l1") } returns flowOf(Resource.Error("Database Error"))

        // WHEN
        val result = useCase.add("l1", 100L)

        // THEN
        assertThat(result).isInstanceOf(Resource.Error::class.java)
        assertThat(result.message).isEqualTo("Database Error")
        coVerify(exactly = 0) { taskWriteUseCase.update(any()) }
    }

    @Test
    fun `add returns Error when the task is not a lesson`() = runTest {
        // GIVEN
        every { taskRepository.getById("e1") } returns flowOf(Resource.Success(baseExam))

        // WHEN
        val result = useCase.add("e1", 100L)

        // THEN
        assertThat(result).isInstanceOf(Resource.Error::class.java)
        assertThat(result.message).isEqualTo(LessonAbsenceWriteUseCase.LESSON_NOT_FOUND)
        coVerify(exactly = 0) { taskWriteUseCase.update(any()) }
    }

    @Test
    fun `add returns the Error of a failed save`() = runTest {
        // GIVEN
        every { taskRepository.getById("l1") } returns flowOf(Resource.Success(baseLesson.copy(id = "l1")))
        coEvery { taskWriteUseCase.update(any()) } returns Resource.Error("Write Error")

        // WHEN
        val result = useCase.add("l1", 100L)

        // THEN
        assertThat(result).isInstanceOf(Resource.Error::class.java)
        assertThat(result.message).isEqualTo("Write Error")
    }

    @Test
    fun `remove deletes the chosen date only`() = runTest {
        // GIVEN
        val stored = baseLesson.copy(id = "l1", absence = listOf(100L, 200L, 300L))
        every { taskRepository.getById("l1") } returns flowOf(Resource.Success(stored))

        // WHEN
        val result = useCase.remove("l1", 200L)

        // THEN
        assertThat(result).isInstanceOf(Resource.Success::class.java)
        assertThat((saved.captured as Task.Lesson).absence).containsExactly(100L, 300L).inOrder()
    }

    @Test
    fun `remove deletes a single entry of a duplicated date`() = runTest {
        // GIVEN
        val stored = baseLesson.copy(id = "l1", absence = listOf(100L, 100L))
        every { taskRepository.getById("l1") } returns flowOf(Resource.Success(stored))

        // WHEN
        useCase.remove("l1", 100L)

        // THEN
        assertThat((saved.captured as Task.Lesson).absence).containsExactly(100L)
    }

    @Test
    fun `remove returns Error and saves nothing when the date is not recorded`() = runTest {
        // GIVEN
        val stored = baseLesson.copy(id = "l1", absence = listOf(100L))
        every { taskRepository.getById("l1") } returns flowOf(Resource.Success(stored))

        // WHEN
        val result = useCase.remove("l1", 999L)

        // THEN
        assertThat(result).isInstanceOf(Resource.Error::class.java)
        assertThat(result.message).isEqualTo(LessonAbsenceWriteUseCase.ABSENCE_NOT_FOUND)
        coVerify(exactly = 0) { taskWriteUseCase.update(any()) }
    }
}
