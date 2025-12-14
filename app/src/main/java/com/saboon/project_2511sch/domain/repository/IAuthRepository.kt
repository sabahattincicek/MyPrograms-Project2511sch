package com.saboon.project_2511sch.domain.repository

import com.saboon.project_2511sch.domain.model.User
import com.saboon.project_2511sch.util.Resource
import kotlinx.coroutines.flow.Flow

interface IAuthRepository {

    suspend fun createLocalUser(user: User): Resource<Unit>

    suspend fun getActiveUser(): Resource<User>
}