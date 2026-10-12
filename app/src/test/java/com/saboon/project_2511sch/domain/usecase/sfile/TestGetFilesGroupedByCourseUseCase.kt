package com.saboon.project_2511sch.domain.usecase.sfile

import com.google.common.truth.Truth.assertThat
import com.saboon.project_2511sch.domain.model.CourseFiles
import com.saboon.project_2511sch.domain.repository.ICourseRepository
import com.saboon.project_2511sch.domain.repository.ISFileRepository
import com.saboon.project_2511sch.domain.repository.ITagRepository
import com.saboon.project_2511sch.domain.usecase.task.TestBaseTask
import com.saboon.project_2511sch.util.Resource
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

@ExperimentalCoroutinesApi
class TestGetFilesGroupedByCourseUseCase : TestBaseTask() {

    private val sFileRepository = mockk<ISFileRepository>()
    private val tagRepository = mockk<ITagRepository>()
    private val courseRepository = mockk<ICourseRepository>()
    private val useCase = GetFilesGroupedByCourseUseCase(sFileRepository, tagRepository, courseRepository)

    private val activeTag = baseTag.copy(id = "t1")
    private val math = baseCourse.copy(id = "c1", tagId = "t1")
    private val sport = baseCourse.copy(id = "c2", tagId = null)
    private val hidden = baseCourse.copy(id = "c3", tagId = "inactive-tag")

    @Test
    fun `files are grouped by course and courses with an inactive tag are left out`() = runTest {
        // GIVEN
        val notes = baseSFile.copy(id = "f1", courseId = "c1")
        val slides = baseSFile.copy(id = "f2", courseId = "c1")
        val plan = baseSFile.copy(id = "f3", courseId = "c2")
        every { tagRepository.getAllActive() } returns flowOf(Resource.Success(listOf(activeTag)))
        every { courseRepository.getAllActive() } returns flowOf(Resource.Success(listOf(math, sport, hidden)))
        every { sFileRepository.getAllByCourseIds(listOf("c1", "c2")) } returns
            flowOf(Resource.Success(listOf(notes, plan, slides)))

        // WHEN
        val result = useCase().first()

        // THEN
        assertThat(result.data).containsExactly(
            CourseFiles(math, listOf(notes, slides)),
            CourseFiles(sport, listOf(plan))
        ).inOrder()
        verify { sFileRepository.getAllByCourseIds(listOf("c1", "c2")) }
    }

    @Test
    fun `no visible course returns an empty list without reading files`() = runTest {
        // GIVEN
        every { tagRepository.getAllActive() } returns flowOf(Resource.Success(emptyList()))
        every { courseRepository.getAllActive() } returns flowOf(Resource.Success(listOf(hidden)))

        // WHEN
        val result = useCase().first()

        // THEN
        assertThat(result.data).isEmpty()
        verify(exactly = 0) { sFileRepository.getAllByCourseIds(any()) }
    }

    @Test
    fun `file repository error returns Error`() = runTest {
        // GIVEN
        every { tagRepository.getAllActive() } returns flowOf(Resource.Success(emptyList()))
        every { courseRepository.getAllActive() } returns flowOf(Resource.Success(listOf(sport)))
        every { sFileRepository.getAllByCourseIds(any()) } returns flowOf(Resource.Error("Database Error"))

        // WHEN
        val result = useCase().first()

        // THEN
        assertThat(result).isInstanceOf(Resource.Error::class.java)
        assertThat(result.message).isEqualTo("Database Error")
    }

    @Test
    fun `course repository error returns Error`() = runTest {
        // GIVEN
        every { tagRepository.getAllActive() } returns flowOf(Resource.Success(emptyList()))
        every { courseRepository.getAllActive() } returns flowOf(Resource.Error("Database Error"))

        // WHEN
        val result = useCase().first()

        // THEN
        assertThat(result).isInstanceOf(Resource.Error::class.java)
    }

    @Test
    fun `getByCourse returns the files of that course`() = runTest {
        // GIVEN
        val notes = baseSFile.copy(id = "f1", courseId = "c1")
        every { sFileRepository.getAllByCourseId("c1") } returns flowOf(Resource.Success(listOf(notes)))

        // WHEN
        val result = useCase.getByCourse(math).first()

        // THEN
        assertThat(result.data).containsExactly(notes)
    }
}
