package com.saboon.project_2511sch.domain.repository

import com.saboon.project_2511sch.domain.model.User
import com.saboon.project_2511sch.util.Resource
import kotlinx.coroutines.flow.Flow

interface IUserRepository {
    suspend fun insert(user: User): Resource<User>
    suspend fun update(user: User): Resource<User>
    suspend fun delete(user: User): Resource<User>
    fun getById(id: String): Flow<Resource<User>>
    fun getActive(): Flow<Resource<User?>>
}