package com.saboon.project_2511sch.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.saboon.project_2511sch.data.local.entity.ProgramTableEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgramTableDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(programTableEntity: ProgramTableEntity)

    @Update
    suspend fun update(programTableEntity: ProgramTableEntity)

    @Delete
    suspend fun delete(programTableEntity: ProgramTableEntity)

    @Query("SELECT * FROM program_tables")
    fun getAll(): Flow<List<ProgramTableEntity>>

    @Query("SELECT * FROM program_tables WHERE is_active = 1")
    fun getAllActive(): Flow<List<ProgramTableEntity>>

    @Query("SELECT COUNT(*) FROM program_tables")
    suspend fun getAllCount(): Int

    @Query("SELECT COUNT(*) FROM program_tables WHERE is_active = 1")
    suspend fun getAllActiveCount(): Int
}