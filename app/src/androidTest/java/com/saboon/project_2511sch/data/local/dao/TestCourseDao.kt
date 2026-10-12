package com.saboon.project_2511sch.data.local.dao

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@SmallTest
class TestCourseDao : TestBaseDao() {

    private lateinit var courseDao: CourseDao

    @Before
    fun setup(){
        courseDao = database.courseDao()
    }

    @Test
    fun insertAndGetById_returnsCorrectCourse() = runTest {
        val id = "test-course"
        val title = "matematik"
        val course = baseCourseEntity.copy(id = id, title = title)
        courseDao.insert(course)
        val result = courseDao.getById(id).first()
        assertThat(result).isNotNull()
        assertThat(result.title).isEqualTo(title)
    }

    @Test
    fun getAllActive_filtersCorrectly() = runTest {
        val course1 = baseCourseEntity.copy(id = "1", isActive = true)
        val course2 = baseCourseEntity.copy(id = "2", isActive = true)

        courseDao.insert(course1)
        courseDao.insert(course2)

        val activeCourses = courseDao.getAllActive().first()
        assertThat(activeCourses).hasSize(2)
        assertThat(activeCourses[0].id).isEqualTo("1")
    }

    @Test
    fun getAllByTagId_returnsOnlyCoursesWithMatchingTag() = runTest {
        val course1 = baseCourseEntity.copy(id = "1", tagId = "tag1", title = "mat")
        val course2 = baseCourseEntity.copy(id = "2", tagId = "tag2", title = "tur")

        courseDao.insert(course1)
        courseDao.insert(course2)

        val mathCourses = courseDao.getAllByTagId("tag1").first()

        assertThat(mathCourses).hasSize(1)
        assertThat(mathCourses[0].title).isEqualTo("mat")
    }

    @Test
    fun getAllActivesByTagIds_filtersBothActiveAndTagIds() = runTest {
        val course1 = baseCourseEntity.copy(id = "1", isActive = true, tagId = "tag1", title = "A-active")
        val course2 = baseCourseEntity.copy(id = "2", isActive = true, tagId = "tag1", title = "B-inactive")
        val course3 = baseCourseEntity.copy(id = "3", isActive = true, tagId = "tag2", title = "C-active")

        courseDao.insert(course1)
        courseDao.insert(course2)
        courseDao.insert(course3)

        val result = courseDao.getAllActivesByProgramTableIds(listOf("tag1")).first()

        assertThat(result).hasSize(2)
        assertThat(result[0].id).isEqualTo("1")
    }

    @Test
    fun onConflictReplace_updatesExistingCourse() = runTest {
        val course = baseCourseEntity.copy(id = "1", title = "old title")
        courseDao.insert(course)

        val updatedCourse = baseCourseEntity.copy(id = "1", title = "new title")
        courseDao.insert(updatedCourse)

        val result = courseDao.getById("1").first()
        assertThat(result.title).isEqualTo("new title")
    }
}