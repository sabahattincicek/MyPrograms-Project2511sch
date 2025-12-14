package com.saboon.project_2511sch.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saboon.project_2511sch.domain.model.User
import com.saboon.project_2511sch.domain.usecase.auth.CreateLocalUserUseCase
import com.saboon.project_2511sch.domain.usecase.auth.GetCurrentUserUseCase
import com.saboon.project_2511sch.domain.usecase.auth.GetLocalAuthStateUseCase
import com.saboon.project_2511sch.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.jetbrains.annotations.Async
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val createLocalUserUseCase: CreateLocalUserUseCase
): ViewModel() {

    private val _createLocalUserEvent = Channel<Resource<Unit>>()
    val createUserEvent = _createLocalUserEvent.receiveAsFlow()
    fun createLocalUser(user: User){
        viewModelScope.launch {
            val result = createLocalUserUseCase.invoke(user)
            _createLocalUserEvent.send(result)
        }
    }




}