package com.saboon.project_2511sch.presentation.sfile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saboon.project_2511sch.domain.model.Course
import com.saboon.project_2511sch.domain.model.ProgramTable
import com.saboon.project_2511sch.domain.model.SFile
import com.saboon.project_2511sch.domain.model.Task
import com.saboon.project_2511sch.domain.usecase.sfile.GetFileDisplayItemListUseCase
import com.saboon.project_2511sch.domain.usecase.sfile.SFileWriteUseCase
import com.saboon.project_2511sch.presentation.common.FilterGeneric
import com.saboon.project_2511sch.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ViewModelSFile @Inject constructor(
    private val sFileWriteUseCase: SFileWriteUseCase,
    private val getFileDisplayItemListUseCase: GetFileDisplayItemListUseCase
): ViewModel() {
    private val _insertEvent = Channel<Resource<SFile>>()
    val insertEvent = _insertEvent.receiveAsFlow()
    private val _updateEvent = Channel<Resource<SFile>>()
    val updateEvent = _updateEvent.receiveAsFlow()
    private val _deleteEvent = Channel<Resource<SFile>>()
    val deleteEvent = _deleteEvent.receiveAsFlow()


    private val _filterState = MutableStateFlow(FilterGeneric())

    //STATE
    @OptIn(ExperimentalCoroutinesApi::class)
    val filesState = _filterState.flatMapLatest { filter ->
        getFileDisplayItemListUseCase.invoke(filter)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Companion.WhileSubscribed(5000),
        initialValue = Resource.Idle()
    )

    //FILTER
    fun updateProgramTable(programTable: ProgramTable?, includeSubItems: Boolean = true) {
        _filterState.update { current ->
            if (programTable == null) FilterGeneric()
            else current.copy(programTable = programTable, programTableIncludeSubItems = includeSubItems, course = null, task = null)
        }
    }
    fun updateCourse(course: Course?, includeSubItems: Boolean = true) {
        _filterState.update { current ->
            current.copy(course = course, courseIncludeSubItems = includeSubItems, task = null)
        }
    }
    fun updateTask(task: Task?) {
        _filterState.update { current ->
            current.copy(task = task)
        }
    }

    //EVENT
    fun insert(sFile: SFile, uri: Uri){
        viewModelScope.launch {
            try {
                _insertEvent.send(Resource.Loading())
                val result = sFileWriteUseCase.insert(sFile, uri)
                _insertEvent.send(result)
            }catch (e: Exception){
                _insertEvent.send(Resource.Error(e.localizedMessage ?: "An unexpected error occurred in ViewModel."))
            }
        }
    }
    fun update(sFile: SFile){
        viewModelScope.launch {
            try {
                _updateEvent.send(Resource.Loading())
                val result = sFileWriteUseCase.update(sFile)
                _updateEvent.send(result)
            }catch (e: Exception){
                _updateEvent.send(Resource.Error(e.localizedMessage ?: "An unexpected error occurred in ViewModel."))
            }
        }
    }
    fun delete(sFile: SFile){
        viewModelScope.launch {
            try {
                _deleteEvent.send(Resource.Loading())
                val result = sFileWriteUseCase.delete(sFile)
                _deleteEvent.send(result)
            }catch (e: Exception){
                _deleteEvent.send(Resource.Error(e.localizedMessage ?: "An unexpected error occurred in ViewModel."))
            }
        }
    }
}