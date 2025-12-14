package com.saboon.project_2511sch.domain.usecase.auth

import com.saboon.project_2511sch.domain.model.User
import com.saboon.project_2511sch.domain.repository.IAuthRepository
import com.saboon.project_2511sch.util.Resource
import javax.inject.Inject

class CreateLocalUserUseCase @Inject constructor(
    private val authRepository: IAuthRepository
) {
    suspend operator fun invoke(user: User): Resource<Unit>{
        return authRepository.createLocalUser(user)
    }
}