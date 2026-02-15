package com.saboon.project_2511sch.domain.usecase.user

import com.saboon.project_2511sch.domain.model.User
import com.saboon.project_2511sch.domain.repository.IUserRepository
import com.saboon.project_2511sch.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UserReadUseCase @Inject constructor(
    private val userRepository: IUserRepository
){
    fun getActive(): Flow<Resource<User?>>{
        return userRepository.getActive()
    }
}