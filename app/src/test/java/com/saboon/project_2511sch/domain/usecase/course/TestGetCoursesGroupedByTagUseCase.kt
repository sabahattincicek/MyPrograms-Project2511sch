package com.saboon.project_2511sch.domain.usecase.course

import com.google.common.truth.Truth.assertThat
import com.saboon.project_2511sch.domain.model.CourseGroup
import com.saboon.project_2511sch.domain.repository.ICourseRepository
import com.saboon.project_2511sch.domain.repository.ITagRepository
import com.saboon.project_2511sch.domain.usecase.task.TestBaseTask
import com.saboon.project_2511sch.util.Resource
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

@ExperimentalCoroutinesApi
class TestGetCoursesGroupedByTagUseCase : TestBaseTask() {

    private val tagRepository = mockk<ITagRepository>()
    private val courseRepository = mockk<ICourseRepository>()
    private val useCase = GetCoursesGroupedByTagUseCase(tagRepository, courseRepository)

    private val tag1 = baseTag.copy(id = "t1", title = "Guz")
    private val math = baseCourse.copy(id = "c1", tagId = "t1", title = "Matematik")
    private val physics = baseCourse.copy(id = "c2", tagId = "t1", title = "Fizik")
    private val untagged = baseCourse.copy(id = "c3", tagId = null, title = "Spor")

    @Test
    fun `courses are grouped under their tag and untagged courses under null`() = runTest {
        // GIVEN
        every { tagRepository.getAll() } returns flowOf(Resource.Success(listOf(tag1)))
        every { courseRepository.getAll() } returns flowOf(Resource.Success(listOf(math, untagged, physics)))

        // WHEN
        val result = useCase().first()

        // THEN
        assertThat(result.data).containsExactly(
            CourseGroup(tag1, listOf(math, physics)),
            CourseGroup(null, listOf(untagged))
        ).inOrder()
    }

    @Test
    fun `course whose tag no longer exists is grouped with a null tag`() = runTest {
        // GIVEN
        val orphan = baseCourse.copy(id = "c9", tagId = "deleted-tag")
        every { tagRepository.getAll() } returns flowOf(Resource.Success(emptyList()))
        every { courseRepository.getAll() } returns flowOf(Resource.Success(listOf(orphan)))

        // WHEN
        val result = useCase().first()

        // THEN
        assertThat(result.data).containsExactly(CourseGroup(null, listOf(orphan)))
    }

    @Test
    fun `no course returns an empty list`() = runTest {
        // GIVEN
        every { tagRepository.getAll() } returns flowOf(Resource.Success(listOf(tag1)))
        every { courseRepository.getAll() } returns flowOf(Resource.Success(emptyList()))

        // WHEN
        val result = useCase().first()

        // THEN
        assertThat(result).isInstanceOf(Resource.Success::class.java)
        assertThat(result.data).isEmpty()
    }

    @Test
    fun `repository error returns Error`() = runTest {
        // GIVEN
        every { tagRepository.getAll() } returns flowOf(Resource.Success(emptyList()))
        every { courseRepository.getAll() } returns flowOf(Resource.Error("Database Error"))

        // WHEN
        val result = useCase().first()

        // THEN
        assertThat(result).isInstanceOf(Resource.Error::class.java)
    }

    @Test
    fun `loading repository returns Loading`() = runTest {
        // GIVEN
        every { tagRepository.getAll() } returns flowOf(Resource.Loading())
        every { courseRepository.getAll() } returns flowOf(Resource.Success(emptyList()))

        // WHEN
        val result = useCase().first()

        // THEN
        assertThat(result).isInstanceOf(Resource.Loading::class.java)
    }
}
