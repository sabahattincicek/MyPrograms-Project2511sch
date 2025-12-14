package com.saboon.project_2511sch.domain.usecase.auth

import com.saboon.project_2511sch.domain.repository.IAuthRepository
import javax.inject.Inject

class GetCurrentUserUseCase @Inject constructor(
    private val authRepository: IAuthRepository
) {

}