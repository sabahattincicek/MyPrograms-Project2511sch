package com.saboon.project_2511sch.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saboon.project_2511sch.domain.model.User
import com.saboon.project_2511sch.domain.usecase.user.GetAllUsersUseCase
import com.saboon.project_2511sch.domain.usecase.user.InsertNewUserUseCase
import com.saboon.project_2511sch.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ViewModelMain @Inject constructor(
    private val insertNewUserUseCase: InsertNewUserUseCase,
    private val getAllUsersUseCase: GetAllUsersUseCase
): ViewModel() {

    private val _insertNewUserEvent = MutableSharedFlow<Resource<User>>(replay = 0)
    val insertNewUserEvent = _insertNewUserEvent.asSharedFlow()

    private val _usersState = MutableStateFlow<Resource<List<User>>>(Resource.Idle())
    val usersState = _usersState.asStateFlow()


    fun insertNewUser(user: User){
        viewModelScope.launch {
            try {
                _insertNewUserEvent.emit(Resource.Loading())
                val result = insertNewUserUseCase.invoke(user)
                _insertNewUserEvent.emit(result)
            }catch (e: Exception){
                _insertNewUserEvent.emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred in ViewModel."))
            }
        }
    }

    fun getAllUsers(){
        viewModelScope.launch {
            try {
                _usersState.emit(Resource.Loading())
                val result = getAllUsersUseCase.invoke().collect { resource ->
                    _usersState.emit(resource)
                }
            }catch (e: Exception){
                _usersState.emit(Resource.Error(e.localizedMessage?:"An unexcepted error occurred in ViewModel"))
            }
        }
    }
}