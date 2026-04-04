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
class TestTagDao : TestBaseDao() {
    private lateinit var tagDao: TagDao

    @Before
    fun setup(){
        tagDao = database.tagDao()
    }

    @Test
    fun insertAndGetById_returnsCorrectTag() = runTest {
        val t1 = baseTagEntity.copy(id = "t1")

        tagDao.insert(t1)

        val result = tagDao.getById("t1").first()

        assertThat(result.id).isEqualTo("t1")
    }

    @Test
    fun getAll_returnsMultipleTags() = runTest {
        val t1 = baseTagEntity.copy(id = "t1")
        val t2 = baseTagEntity.copy(id = "t2")
        val t3 = baseTagEntity.copy(id = "t3")

        tagDao.insert(t1)
        tagDao.insert(t2)
        tagDao.insert(t3)

        val result = tagDao.getAll().first()

        assertThat(result).hasSize(3)
        val ids = result.map { it.id }
        assertThat(ids).containsExactly("t1", "t2", "t3")
    }

    @Test
    fun getAllActive_returnsOnlyActiveTags() = runTest {
        val t1 = baseTagEntity.copy(id = "t1")
        val t2 = baseTagEntity.copy(id = "t2")
        val t3 = baseTagEntity.copy(id = "t3", isActive = false)

        tagDao.insert(t1)
        tagDao.insert(t2)
        tagDao.insert(t3)

        val result = tagDao.getAllActive().first()

        assertThat(result).hasSize(2)
        val ids = result.map { it.id }
        assertThat(ids).containsExactly("t1", "t2")
    }
}