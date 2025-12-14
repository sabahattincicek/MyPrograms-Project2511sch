package com.saboon.project_2511sch.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.saboon.project_2511sch.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Query("DELETE FROM users")
    suspend fun deleteAllUsers()

    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE is_active = 1 LIMIT 1")
    suspend fun getActiveUser(): UserEntity

    @Query("UPDATE users SET is_active = 0")
    suspend fun setAllUserInactive()

    @Query("UPDATE users SET is_active = 0 WHERE id != :id")
    suspend fun setAllUserInactiveExceptUser(id: String)

}