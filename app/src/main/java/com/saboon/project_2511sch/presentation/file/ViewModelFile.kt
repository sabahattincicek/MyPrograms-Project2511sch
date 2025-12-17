package com.saboon.project_2511sch.presentation.file

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saboon.project_2511sch.domain.model.File
import com.saboon.project_2511sch.domain.usecase.file.GetAllFilesByCourseIdUseCase
import com.saboon.project_2511sch.domain.usecase.file.InsertNewFileUseCase
import com.saboon.project_2511sch.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ViewModelFile @Inject constructor(
    private val insertNewFileUseCase: InsertNewFileUseCase,
    private val getAllFilesByCourseIdUseCase: GetAllFilesByCourseIdUseCase
) : ViewModel() {
    private val _insertNewFileEvent = Channel<Resource<File>>()
    val insertNewFileEvent = _insertNewFileEvent.receiveAsFlow()

    private val _filesState = MutableStateFlow<Resource<List<File>>>(Resource.Idle())
    val filesState = _filesState.asStateFlow()

    fun insertNewFile(file: File){
        viewModelScope.launch {
            try {
                _insertNewFileEvent.send(Resource.Loading())
                val result = insertNewFileUseCase.invoke(file)
                _insertNewFileEvent.send(result)
            }catch (e: Exception){
                _insertNewFileEvent.send(Resource.Error(e.localizedMessage?:"An unexpected error occurred in ViewModel."))
            }
        }
    }

    fun getAllFilesByCourseId(id: String){
        viewModelScope.launch {
            try {
                _filesState.value = Resource.Loading()
                getAllFilesByCourseIdUseCase.invoke(id).collect { resource ->
                    _filesState.value = resource
                }
            }catch (e: Exception){
                _filesState.value = Resource.Error(e.localizedMessage?:"An unexpected error occurred in ViewModel.")
            }
        }
    }
}