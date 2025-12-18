package com.saboon.project_2511sch.presentation.file

import android.util.Log
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

    private val TAG = "ViewModelFile"

    private val _insertNewFileEvent = Channel<Resource<File>>()
    val insertNewFileEvent = _insertNewFileEvent.receiveAsFlow()

    private val _filesState = MutableStateFlow<Resource<List<File>>>(Resource.Idle())
    val filesState = _filesState.asStateFlow()

    fun insertNewFile(file: File) {
        Log.d(TAG, "insertNewFile: called with file title: ${file.title}")
        viewModelScope.launch {
            try {
                Log.d(TAG, "insertNewFile: Sending Loading state.")
                _insertNewFileEvent.send(Resource.Loading())
                val result = insertNewFileUseCase.invoke(file)
                Log.i(TAG, "insertNewFile: Received result from UseCase: $result")
                _insertNewFileEvent.send(result)
            } catch (e: Exception) {
                Log.e(TAG, "insertNewFile: Exception caught while inserting file.", e)
                _insertNewFileEvent.send(Resource.Error(e.localizedMessage ?: "An unexpected error occurred in ViewModel."))
            }
        }
    }

    fun getAllFilesByCourseId(id: String) {
        Log.d(TAG, "getAllFilesByCourseId: called with course ID: $id")
        viewModelScope.launch {
            try {
                Log.d(TAG, "getAllFilesByCourseId: Setting Loading state.")
                _filesState.value = Resource.Loading()
                getAllFilesByCourseIdUseCase.invoke(id).collect { resource ->
                    Log.d(TAG, "getAllFilesByCourseId: Collected new resource for filesState: $resource")
                    _filesState.value = resource
                }
            } catch (e: Exception) {
                Log.e(TAG, "getAllFilesByCourseId: Exception caught while getting files.", e)
                _filesState.value = Resource.Error(e.localizedMessage ?: "An unexpected error occurred in ViewModel.")
            }
        }
    }
}
