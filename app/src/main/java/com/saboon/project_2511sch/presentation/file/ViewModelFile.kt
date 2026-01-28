package com.saboon.project_2511sch.presentation.file

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saboon.project_2511sch.domain.model.Course
import com.saboon.project_2511sch.domain.model.File
import com.saboon.project_2511sch.domain.model.ProgramTable
import com.saboon.project_2511sch.domain.model.Task
import com.saboon.project_2511sch.domain.usecase.file.FileReadUseCase
import com.saboon.project_2511sch.domain.usecase.file.FileWriteUseCase
import com.saboon.project_2511sch.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ViewModelFile @Inject constructor(
    private val fileWriteUseCase: FileWriteUseCase,
    private val fileReadUseCase: FileReadUseCase
) : ViewModel() {

    private val TAG = "ViewModelFile"

    private val _insertNewFileEvent = Channel<Resource<File>>(Channel.BUFFERED)
    val insertNewFileEvent = _insertNewFileEvent.receiveAsFlow()

    private val _deleteFileEvent = Channel<Resource<File>>(Channel.BUFFERED)
    val deleteFileEvent = _deleteFileEvent.receiveAsFlow()

    private val _updateFileEvent = Channel<Resource<File>>(Channel.BUFFERED)
    val updateFileEvent = _updateFileEvent.receiveAsFlow()

    private val _insertNewNoteEvent = Channel<Resource<File>>(Channel.BUFFERED)
    val insertNewNoteEvent = _insertNewNoteEvent.receiveAsFlow()

    private val _insertNewLinkEvent = Channel<Resource<File>>(Channel.BUFFERED)
    val insertNewLinkEvent = _insertNewLinkEvent.receiveAsFlow()
    private val _filterState = MutableStateFlow(FilterFile())

    @OptIn(ExperimentalCoroutinesApi::class)
    val filesState = _filterState.flatMapLatest { filterFile ->
        when{
            filterFile.task != null -> fileReadUseCase.getAllByTaskId(filterFile.task.id)
            filterFile.course != null -> fileReadUseCase.getAllByCourseId(filterFile.course.id)
            filterFile.programTable != null -> fileReadUseCase.getAllByProgramTableId(filterFile.programTable.id)
            else -> fileReadUseCase.getAll()
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = Resource.Idle()
    )

    fun insertFile(file: File, uri: Uri) {
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
    fun insertNote(note: File){
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
    fun insertLink(link: File){
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
    fun updateProgramTableFilter(programTable: ProgramTable?){
        _filterState.update { current ->
            if (programTable == null){
                FilterFile()
            }else{
                current.copy(
                    programTable = programTable,
                    course = null,
                    task = null
                )
            }
        }
    }
    fun updateCourseFilter(course: Course?){
        _filterState.update { current ->
            if (course == null){
                current.copy(
                    course = null,
                    task = null
                )
            }else{
                current.copy(
                    course = course,
                    task = null
                )
            }
        }
    }
    fun updateTaskFilter(task: Task?){
        _filterState.update { current ->
            current.copy(task = task)
        }
    }





}
