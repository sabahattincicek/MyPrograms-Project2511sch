package com.saboon.project_2511sch.domain.repository

import com.saboon.project_2511sch.domain.model.User
import com.saboon.project_2511sch.util.Resource
import kotlinx.coroutines.flow.Flow

interface IUserRepository {

    suspend fun insertUser(user: User): Resource<Unit>

    fun getAllUsers(): Flow<Resource<List<User>>>

    suspend fun getActiveUser(): Resource<User>

}