package com.saboon.project_2511sch.domain.usecase.task

import com.google.common.truth.Truth.assertThat
import com.saboon.project_2511sch.domain.repository.ICourseRepository
import com.saboon.project_2511sch.domain.repository.ITaskRepository
import com.saboon.project_2511sch.util.Resource
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

@ExperimentalCoroutinesApi
class TestGetCourseTasksUseCase : TestBaseTask() {

    private val courseRepository = mockk<ICourseRepository>()
    private val taskRepository = mockk<ITaskRepository>()
    private val useCase = GetCourseTasksUseCase(courseRepository, taskRepository)

    private val course = baseCourse.copy(id = "c1", title = "Eski ad")

    @Test
    fun `returns the tasks with the current state of the course`() = runTest {
        // GIVEN: the course was renamed after the caller got its copy
        val renamed = course.copy(title = "Yeni ad")
        val lesson = baseLesson.copy(id = "l1", courseId = "c1")
        every { courseRepository.getAll() } returns flowOf(Resource.Success(listOf(renamed)))
        every { taskRepository.getAllByCourseId("c1") } returns flowOf(Resource.Success(listOf(lesson, baseExam)))

        // WHEN
        val result = useCase(course).first()

        // THEN
        assertThat(result.data?.course).isEqualTo(renamed)
        assertThat(result.data?.tasks).containsExactly(lesson, baseExam).inOrder()
    }

    @Test
    fun `falls back to the given course when it is not in the repository`() = runTest {
        // GIVEN
        every { courseRepository.getAll() } returns flowOf(Resource.Success(emptyList()))
        every { taskRepository.getAllByCourseId("c1") } returns flowOf(Resource.Success(emptyList()))

        // WHEN
        val result = useCase(course).first()

        // THEN
        assertThat(result.data?.course).isEqualTo(course)
        assertThat(result.data?.tasks).isEmpty()
    }

    @Test
    fun `course repository error returns Error`() = runTest {
        // GIVEN
        every { courseRepository.getAll() } returns flowOf(Resource.Error("Database Error"))
        every { taskRepository.getAllByCourseId("c1") } returns flowOf(Resource.Success(emptyList()))

        // WHEN
        val result = useCase(course).first()

        // THEN
        assertThat(result).isInstanceOf(Resource.Error::class.java)
    }

    @Test
    fun `loading course repository returns Loading`() = runTest {
        // GIVEN
        every { courseRepository.getAll() } returns flowOf(Resource.Loading())
        every { taskRepository.getAllByCourseId("c1") } returns flowOf(Resource.Success(emptyList()))

        // WHEN
        val result = useCase(course).first()

        // THEN
        assertThat(result).isInstanceOf(Resource.Loading::class.java)
    }
}
