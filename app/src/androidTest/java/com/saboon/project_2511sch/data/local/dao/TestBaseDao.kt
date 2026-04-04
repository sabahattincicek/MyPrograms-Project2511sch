package com.saboon.project_2511sch.data.local.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.saboon.project_2511sch.data.local.database.Database
import com.saboon.project_2511sch.data.local.entity.CourseEntity
import com.saboon.project_2511sch.data.local.entity.UserEntity
import com.saboon.project_2511sch.util.IdGenerator
import org.junit.After
import org.junit.Before

abstract class TestBaseDao{
    protected lateinit var database: Database

    @Before
    fun initDb(){
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            Database::class.java
        ).allowMainThreadQueries().build()
    }

    @After
    fun closeDb(){
        database.close()
    }

    val baseUserEntity = UserEntity(
        id = "test-user",
        createdAt = 0L,
        createdBy = "",
        appVersionAtCreation = "1.0.0",
        updatedAt = 0L,
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
    val baseCourseEntity = CourseEntity(
        id = "test-course",
        createdAt = 0L,
        createdBy = "",
        appVersionAtCreation = "1.0.0",
        updatedAt = 0L,
        version = 1,
        isActive = true,
        isDeleted = false,
        deletedAt = 0L,
        syncStatus = 0,
        contentHash = "",
        serverVersion = 1,
        tagId = null,
        title = "",
        description = "",
        people = "",
        color = ""
    )
}
