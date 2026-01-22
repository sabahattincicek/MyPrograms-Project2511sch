package com.saboon.project_2511sch.presentation.file

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saboon.project_2511sch.domain.model.File
import com.saboon.project_2511sch.domain.usecase.file.FileReadUseCase
import com.saboon.project_2511sch.domain.usecase.file.FileWriteUseCase
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
    private val fileWriteUseCase: FileWriteUseCase,
    private val fileReadUseCase: FileReadUseCase
) : ViewModel() {

    private val TAG = "ViewModelFile"

    private val _insertNewFileEvent = Channel<Resource<File>>()
    val insertNewFileEvent = _insertNewFileEvent.receiveAsFlow()

    private val _deleteFileEvent = Channel<Resource<File>>()
    val deleteFileEvent = _deleteFileEvent.receiveAsFlow()

    private val _updateFileEvent = Channel<Resource<File>>()
    val updateFileEvent = _updateFileEvent.receiveAsFlow()

    private val _insertNewNoteEvent = Channel<Resource<File>>()
    val insertNewNoteEvent = _insertNewNoteEvent.receiveAsFlow()

    private val _insertNewLinkEvent = Channel<Resource<File>>()
    val insertNewLinkEvent = _insertNewLinkEvent.receiveAsFlow()

    private val _filesState = MutableStateFlow<Resource<List<File>>>(Resource.Idle())
    val filesState = _filesState.asStateFlow()

    fun insertNewFile(file: File, uri: Uri) {
        Log.d(TAG, "insertNewFile: called with file title: ${file.title}")
        viewModelScope.launch {
            try {
                Log.d(TAG, "insertNewFile: Sending Loading state.")
                _insertNewFileEvent.send(Resource.Loading())
                val result = fileWriteUseCase.insertFile(file, uri)
                Log.i(TAG, "insertNewFile: Received result from UseCase: $result")
                _insertNewFileEvent.send(result)
            } catch (e: Exception) {
                Log.e(TAG, "insertNewFile: Exception caught while inserting file.", e)
                _insertNewFileEvent.send(Resource.Error(e.localizedMessage ?: "An unexpected error occurred in ViewModel."))
            }
        }
    }
    fun insertNewNote(note: File){
        viewModelScope.launch {
            try {
                _insertNewNoteEvent.send(Resource.Loading())
                val result = fileWriteUseCase.insertNote(note)
                _insertNewNoteEvent.send(result)
            }catch (e: Exception){
                _insertNewNoteEvent.send(Resource.Error(e.localizedMessage ?: "An unexpected error occurred in ViewModel."))
            }
        }
    }

    fun insertNewLink(link: File){
        viewModelScope.launch {
            try {
                _insertNewLinkEvent.send(Resource.Loading())
                val result = fileWriteUseCase.insertLink(link)
                _insertNewLinkEvent.send(result)
            }catch (e: Exception){
                _insertNewLinkEvent.send(Resource.Error(e.localizedMessage ?: "An unexpected error occurred in ViewModel."))
            }
        }
    }

    fun deleteFile(file: File) {
        viewModelScope.launch {
            try {
                _deleteFileEvent.send(Resource.Loading())
                val result = fileWriteUseCase.delete(file)
                _deleteFileEvent.send(result)
            }catch (e: Exception){
                _deleteFileEvent.send(Resource.Error(e.localizedMessage ?: "An unexpected error occurred in ViewModel."))
            }
        }
    }

    fun updateFile(file: File){
        viewModelScope.launch {
            try {
                _updateFileEvent.send(Resource.Loading())
                val result = fileWriteUseCase.update(file)
                _updateFileEvent.send(result)
            }catch (e: Exception){
                _updateFileEvent.send(Resource.Error(e.localizedMessage ?: "An unexpected error occurred in ViewModel."))
            }
        }
    }



    fun getAllFilesByCourseId(id: String) {
        Log.d(TAG, "getAllFilesByCourseId: called with course ID: $id")
        viewModelScope.launch {
            try {
                Log.d(TAG, "getAllFilesByCourseId: Setting Loading state.")
                _filesState.value = Resource.Loading()
                fileReadUseCase.getAllByCourseId(id).collect { resource ->
                    Log.d(TAG, "getAllFilesByCourseId: Collected new resource for filesState: $resource")
                    _filesState.value = resource
                }
            } catch (e: Exception) {
                Log.e(TAG, "getAllFilesByCourseId: Exception caught while getting files.", e)
                _filesState.value = Resource.Error(e.localizedMessage ?: "An unexpected error occurred in ViewModel.")
            }
        }
    }

    fun getAllFilesByProgramTableId(id: String){
        viewModelScope.launch {
            try {
                _filesState.value = Resource.Loading()
                val result = fileReadUseCase.getAllByProgramTableId(id).collect { resource ->
                    _filesState.value = resource
                }
            }catch (e: Exception){
                _filesState.value = Resource.Error(e.localizedMessage ?: "An unexpected error occurred in ViewModel.")
            }
        }
    }

    fun getAllFiles(){
        viewModelScope.launch {
            try {
                _filesState.value = Resource.Loading()
                val result = fileReadUseCase.getAll().collect { resource ->
                    _filesState.value = resource
                }
            }catch (e: Exception){
                _filesState.value = Resource.Error(e.localizedMessage ?: "An unexpected error occurred in ViewModel.")
            }
        }
    }
}
