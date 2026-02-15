package com.saboon.project_2511sch.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saboon.project_2511sch.domain.model.User
import com.saboon.project_2511sch.domain.usecase.user.UserReadUseCase
import com.saboon.project_2511sch.domain.usecase.user.UserWriteUseCase
import com.saboon.project_2511sch.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ViewModelUser @Inject constructor(
    private val userWriteUseCase: UserWriteUseCase,
    private val userReadUseCase: UserReadUseCase
): ViewModel() {
    private val _operationEvent = Channel<Resource<User>>()
    val operationEvent = _operationEvent.receiveAsFlow()

    //STATE
    val currentUser: StateFlow<Resource<User?>> = userReadUseCase.getActive()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Companion.WhileSubscribed(5000),
            initialValue = Resource.Idle()
        )


    // ACTIONS
    fun insert(user: User) = executeWriteAction {
        userWriteUseCase.insert(user)
    }
    fun update(user: User) = executeWriteAction {
        userWriteUseCase.update(user)
    }
    fun delete(user: User) = executeWriteAction {
        userWriteUseCase.delete(user)
    }

    private fun executeWriteAction(action: suspend () -> Resource<User>) {
        viewModelScope.launch {
            try {
                _operationEvent.send(Resource.Loading())
                _operationEvent.send(action())
            } catch (e: Exception) {
                _operationEvent.send(Resource.Error(e.localizedMessage ?: "Unexpected error"))
            }
        }
    }
}