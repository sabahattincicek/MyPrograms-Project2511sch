package com.saboon.project_2511sch.data.local.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.google.common.truth.Truth.assertThat
import com.saboon.project_2511sch.data.local.database.Database
import com.saboon.project_2511sch.data.local.entity.UserEntity
import com.saboon.project_2511sch.util.IdGenerator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@SmallTest
class TestUserDao : TestBaseDao() {
    private lateinit var userDao: UserDao

    @Before
    fun setup(){
        userDao = database.userDao()
    }

    @Test
    fun insertUser_and_getActiveUser_returns_true() = runTest {
        val user = baseUserEntity
        userDao.insert(user)
        val activeUser = userDao.getActive().first()

        assertThat(activeUser).isNotNull()
        assertThat(activeUser).isEqualTo(user)
    }

    @Test
    fun deleteUser_and_getActiveUser_returns_null() = runTest {        // 1. Kullanıcıyı ekle
        val user = baseUserEntity
        userDao.insert(user)

        userDao.delete(user)

        val activeUser = userDao.getActive().first()

        assertThat(activeUser).isNull()
    }
}

