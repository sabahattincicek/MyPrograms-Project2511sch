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
class TestUserDao {
    private lateinit var database: Database
    private lateinit var userDao: UserDao

    @Before
    fun setup(){
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            Database::class.java
        ).allowMainThreadQueries().build()

        userDao = database.userDao()
    }

    @After
    fun tearDown(){
        database.close()
    }

    @Test
    fun insertUser_and_getActiveUser_returns_true() = runTest {
        val user = UserEntity(
            id = IdGenerator.generateId("test"),
            createdAt = System.currentTimeMillis(),
            createdBy = "",
            appVersionAtCreation = "1.0.0",
            updatedAt = System.currentTimeMillis(),
            version = 1,
            isActive = true,
            isDeleted = false,
            deletedAt = 0L,
            syncStatus = 0,
            contentHash = "",
            serverVersion = 1,
            userName = "",
            email = "",
            photoUrl = "",
            lastLoginAt = 0L,
            fullName = "",
            role = "",
            academicLevel = "",
            institution = "",
            aboutMe = ""
        )
        userDao.insert(user)
        val activeUser = userDao.getActive().first()

        assertThat(activeUser).isNotNull()
        assertThat(activeUser).isEqualTo(user)
    }

    @Test
    fun deleteUser_and_getActiveUser_returns_null() = runTest {        // 1. Kullanıcıyı ekle
        val user = UserEntity(
            id = IdGenerator.generateId("test"),
            createdAt = System.currentTimeMillis(),
            createdBy = "",
            appVersionAtCreation = "1.0.0",
            updatedAt = System.currentTimeMillis(),
            version = 1,
            isActive = true,
            isDeleted = false,
            deletedAt = 0L,
            syncStatus = 0,
            contentHash = "",
            serverVersion = 1,
            userName = "",
            email = "",
            photoUrl = "",
            lastLoginAt = 0L,
            fullName = "",
            role = "",
            academicLevel = "",
            institution = "",
            aboutMe = ""
        )
        userDao.insert(user)

        // 2. Kullanıcıyı sil
        userDao.delete(user)

        // 3. Çekmeye çalış
        val activeUser = userDao.getActive().first()

        // 4. Doğrula
        assertThat(activeUser).isNull()
    }
}

