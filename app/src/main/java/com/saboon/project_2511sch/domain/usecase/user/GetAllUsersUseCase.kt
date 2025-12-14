package com.saboon.project_2511sch.domain.usecase.user

import com.saboon.project_2511sch.domain.model.User
import com.saboon.project_2511sch.domain.repository.IUserRepository
import com.saboon.project_2511sch.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllUsersUseCase @Inject constructor(
    private val userRepository: IUserRepository
) {
    operator fun invoke() : Flow<Resource<List<User>>>{
        return userRepository.getAllUsers()
    }
}