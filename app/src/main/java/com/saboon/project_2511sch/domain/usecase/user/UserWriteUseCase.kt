package com.saboon.project_2511sch.domain.usecase.user

import com.saboon.project_2511sch.domain.model.User
import com.saboon.project_2511sch.domain.repository.IUserRepository
import com.saboon.project_2511sch.util.Resource
import javax.inject.Inject

class UserWriteUseCase @Inject constructor(
    private val userRepository: IUserRepository
) {
    suspend fun insert(user: User): Resource<User>{
        return userRepository.insert(user)
    }
    suspend fun update(user: User): Resource<User>{
        return userRepository.update(user)
    }
    suspend fun delete(user: User): Resource<User>{
        return userRepository.delete(user)
    }
}