package com.saboon.project_2511sch.data.repository

import com.saboon.project_2511sch.domain.model.User
import com.saboon.project_2511sch.domain.repository.IAuthRepository
import com.saboon.project_2511sch.domain.repository.IUserRepository
import com.saboon.project_2511sch.util.Resource
import javax.inject.Inject

class AuthRepositoryImp @Inject constructor(
    private val userRepository: IUserRepository
) : IAuthRepository{
    override suspend fun createLocalUser(user: User): Resource<Unit> {
        return userRepository.insertUser(user)
    }

    override suspend fun getActiveUser(): Resource<User> {
        return userRepository.getActiveUser()
    }


}