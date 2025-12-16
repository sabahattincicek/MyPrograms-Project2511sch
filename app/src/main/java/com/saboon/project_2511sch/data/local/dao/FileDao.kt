package com.saboon.project_2511sch.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.saboon.project_2511sch.data.local.entity.FileEntity

@Dao
interface FileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(file: FileEntity)
}