package com.saboon.project_2511sch.presentation.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saboon.project_2511sch.domain.model.User
import com.saboon.project_2511sch.domain.usecase.user.UserReadUseCase
import com.saboon.project_2511sch.domain.usecase.user.UserWriteUseCase
import com.saboon.project_2511sch.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ViewModelUser @Inject constructor(
    private val userWriteUseCase: UserWriteUseCase,
    private val userReadUseCase: UserReadUseCase
): ViewModel() {
    private val _insertEvent = Channel<Resource<User>>()
    val insertEvent = _insertEvent.receiveAsFlow()
    private val _updateEvent = Channel<Resource<User>>()
    val updateEvent = _updateEvent.receiveAsFlow()
    private val _deleteEvent = Channel<Resource<User>>()
    val deleteEvent = _deleteEvent.receiveAsFlow()

    private val _userState = MutableStateFlow<Resource<User?>>(Resource.Idle())
    private val userState = _userState.asStateFlow()

    //STATE
    fun getById(id: String){
        viewModelScope.launch {
            try {
                _userState.value = Resource.Loading()
                userReadUseCase.getById(id).collect { resource ->
                    _userState.value = resource as Resource<User?>
                }
            }catch (e: Exception){
                _userState.value = Resource.Error(e.localizedMessage ?: "An unexpected error occurred in ViewModel.")
            }
        }
    }
    fun getActive(){
        viewModelScope.launch {
            try {
                _userState.value = Resource.Loading()
                userReadUseCase.getActive().collect { resource ->
                    _userState.value = resource
                }
            }catch (e: Exception){
                _userState.value = Resource.Error(e.localizedMessage ?: "An unexpected error occurred in ViewModel.")
            }
        }
    }

    //EVENT
    fun insert(user: User){
        viewModelScope.launch {
            try{
                _insertEvent.send(Resource.Loading())
                val insertResult = userWriteUseCase.insert(user)
                _insertEvent.send(insertResult)
            }catch (e: Exception){
                _insertEvent.send(Resource.Error(e.localizedMessage ?: "An unexpected error occurred in ViewModel."))
            }
        }
    }
    fun update(user: User){
        viewModelScope.launch {
            try{
                _updateEvent.send(Resource.Loading())
                val updateResult = userWriteUseCase.update(user)
                _updateEvent.send(updateResult)
            }catch (e: Exception){
                _updateEvent.send(Resource.Error(e.localizedMessage ?: "An unexpected error occurred in ViewModel."))
            }
        }
    }
    fun delete(user: User){
        viewModelScope.launch {
            try{
                _deleteEvent.send(Resource.Loading())
                val deleteResult = userWriteUseCase.delete(user)
                _deleteEvent.send(deleteResult)
            }catch (e: Exception){
                _deleteEvent.send(Resource.Error(e.localizedMessage ?: "An unexpected error occurred in ViewModel."))
            }
        }
    }
}