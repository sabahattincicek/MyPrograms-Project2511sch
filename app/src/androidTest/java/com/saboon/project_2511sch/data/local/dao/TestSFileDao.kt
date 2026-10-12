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
class TestSFileDao : TestBaseDao(){
    private lateinit var sFileDao: SFileDao

    @Before
    fun setup(){
        sFileDao = database.sFileDao()

        //for sqlite foreign key
        runTest {
            database.courseDao().insert(baseCourseEntity.copy(id = "c1"))
            database.courseDao().insert(baseCourseEntity.copy(id = "c2"))
            database.courseDao().insert(baseCourseEntity.copy(id = "c3"))
        }
    }

    @Test
    fun getAll_returnsAllSFiles() = runTest {
        val sf1 = baseSFileEntity.copy(id = "sf1", courseId = "c1")
        val sf2 = baseSFileEntity.copy(id = "sf2", courseId = "c2")
        val sf3 = baseSFileEntity.copy(id = "sf3", courseId = "c3")

        sFileDao.insert(sf1)
        sFileDao.insert(sf2)
        sFileDao.insert(sf3)

        val result = sFileDao.getAll().first()

        assertThat(result).hasSize(3)
        val ids = result.map { it.id }
        assertThat(ids).containsExactly("sf1", "sf2", "sf3")
    }

    @Test
    fun getAllByCourseId_returnsOnlySFilesWithMatchesCourse() = runTest {
        val sf1 = baseSFileEntity.copy(id = "sf1", courseId = "c1")
        val sf2 = baseSFileEntity.copy(id = "sf2", courseId = "c1")
        val sf3 = baseSFileEntity.copy(id = "sf3", courseId = "c2")

        sFileDao.insert(sf1)
        sFileDao.insert(sf2)
        sFileDao.insert(sf3)

        val result = sFileDao.getAllByCourseId("c1").first()

        assertThat(result).hasSize(2)
        val ids = result.map { it.id }
        assertThat(ids).containsExactly("sf1", "sf2")
    }

    @Test
    fun getAllByCourseIds_returnsMultipleMatches() = runTest {
        val sf1 = baseSFileEntity.copy(id = "sf1", courseId = "c1")
        val sf2 = baseSFileEntity.copy(id = "sf2", courseId = "c2")
        val sf3 = baseSFileEntity.copy(id = "sf3", courseId = "c3")

        sFileDao.insert(sf1)
        sFileDao.insert(sf2)
        sFileDao.insert(sf3)

        val result = sFileDao.getAllByCourseIds(listOf("c1", "c2")).first()

        assertThat(result).hasSize(2)
        val ids = result.map { it.id }
        assertThat(ids).containsExactly("sf1", "sf2")
    }
}