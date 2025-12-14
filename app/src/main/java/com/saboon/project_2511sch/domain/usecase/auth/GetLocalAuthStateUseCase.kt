package com.saboon.project_2511sch.domain.usecase.auth

import com.saboon.project_2511sch.domain.repository.IAuthRepository
import javax.inject.Inject

class GetLocalAuthStateUseCase @Inject constructor(
    private val authRepository: IAuthRepository
) {

}