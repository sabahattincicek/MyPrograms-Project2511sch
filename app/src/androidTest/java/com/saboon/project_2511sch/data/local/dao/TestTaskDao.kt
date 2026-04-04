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
class TestTaskDao : TestBaseDao() {

    private lateinit var taskDao: TaskDao

    @Before
    fun setup(){
        taskDao = database.taskDao()

        //for sqlite foreign key
        runTest {
            database.courseDao().insert(baseCourseEntity.copy(id = "c1"))
            database.courseDao().insert(baseCourseEntity.copy(id = "c2"))
            database.courseDao().insert(baseCourseEntity.copy(id = "c3"))
        }
    }

    @Test
    fun getAllLessonsByCourseIds_returnMultipleMatches() = runTest {
        val l1 = baseTaskLessonEntity.copy(id = "l1", courseId = "c1", isActive = true)
        val l2 = baseTaskLessonEntity.copy(id = "l2", courseId = "c1", isActive = true)
        val l3 = baseTaskLessonEntity.copy(id = "l3", courseId = "c2", isActive = false)
        val l4 = baseTaskLessonEntity.copy(id = "l4", courseId = "c3", isActive = false)

        taskDao.insertLesson(l1)
        taskDao.insertLesson(l2)
        taskDao.insertLesson(l3)
        taskDao.insertLesson(l4)

        val result = taskDao.getAllLessonsByCourseIds(listOf("c1", "c2")).first()

        assertThat(result).hasSize(2)
        val ids = result.map { it.id }
        assertThat(ids).containsExactly("l1", "l2")
    }
    @Test
    fun getAllExamsByCourseIds_returnMultipleMatches() = runTest {
        val e1 = baseTaskExamEntity.copy(id = "e1", courseId = "c1", isActive = true)
        val e2 = baseTaskExamEntity.copy(id = "e2", courseId = "c1", isActive = true)
        val e3 = baseTaskExamEntity.copy(id = "e3", courseId = "c2", isActive = false)
        val e4 = baseTaskExamEntity.copy(id = "e4", courseId = "c3", isActive = true)

        taskDao.insertExam(e1)
        taskDao.insertExam(e2)
        taskDao.insertExam(e3)
        taskDao.insertExam(e4)

        val result = taskDao.getAllExamsByCourseIds(listOf("c1", "c2")).first()

        assertThat(result).hasSize(2)
        val ids = result.map { it.id }
        assertThat(ids).containsExactly("e1", "e2")
    }
    @Test
    fun getAllHomeworksByCourseIds_returnMultipleMatches() = runTest {
        val e1 = baseTaskHomeworkEntity.copy(id = "h1", courseId = "c1", isActive = true)
        val e2 = baseTaskHomeworkEntity.copy(id = "h2", courseId = "c1", isActive = true)
        val e3 = baseTaskHomeworkEntity.copy(id = "h3", courseId = "c2", isActive = false)
        val e4 = baseTaskHomeworkEntity.copy(id = "h4", courseId = "c3", isActive = true)

        taskDao.insertHomework(e1)
        taskDao.insertHomework(e2)
        taskDao.insertHomework(e3)
        taskDao.insertHomework(e4)

        val result = taskDao.getAllHomeworksByCourseIds(listOf("c1", "c2")).first()

        assertThat(result).hasSize(2)
        val ids = result.map { it.id }
        assertThat(ids).containsExactly("h1", "h2")
    }
}