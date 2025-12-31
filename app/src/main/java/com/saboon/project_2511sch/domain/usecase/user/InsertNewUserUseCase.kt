package com.saboon.project_2511sch.domain.usecase.user

import com.saboon.project_2511sch.domain.model.User
import com.saboon.project_2511sch.domain.repository.IUserRepository
import com.saboon.project_2511sch.util.Resource
import javax.inject.Inject

class InsertNewUserUseCase @Inject constructor(
    private val userRepository: IUserRepository
) {
    suspend operator fun invoke(user: User): Resource<User>{
        return userRepository.insert(user)
    }
}