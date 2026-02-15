package com.saboon.project_2511sch.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.saboon.project_2511sch.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(userEntity: UserEntity)
    @Update
    suspend fun update(userEntity: UserEntity)
    @Delete
    suspend fun delete(userEntity: UserEntity)
    @Query("SELECT * FROM users WHERE is_active = 1 LIMIT 1")
    fun getActive(): Flow<UserEntity?>
}