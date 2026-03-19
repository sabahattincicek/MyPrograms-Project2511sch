package com.saboon.project_2511sch.presentation.sfile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saboon.project_2511sch.domain.model.Course
import com.saboon.project_2511sch.domain.model.SFile
import com.saboon.project_2511sch.domain.usecase.sfile.GetFileDisplayItemListUseCase
import com.saboon.project_2511sch.domain.usecase.sfile.SFileWriteUseCase
import com.saboon.project_2511sch.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ViewModelSFile @Inject constructor(
    private val sFileWriteUseCase: SFileWriteUseCase,
    private val getFileDisplayItemListUseCase: GetFileDisplayItemListUseCase
): ViewModel() {
    private val _operationEvent = Channel<Resource<SFile>>()
    val operationEvent = _operationEvent.receiveAsFlow()

    private val _selectedCourse = MutableStateFlow<Course?>(null)

    //STATE
    @OptIn(ExperimentalCoroutinesApi::class)
    val filesState = _selectedCourse.flatMapLatest { course ->
        if (course == null){
            getFileDisplayItemListUseCase.invoke()
        }else{
            getFileDisplayItemListUseCase.getByCourse(course)
        }
    }.stateIn(
        scope = viewModelScope,
        started = WhileSubscribed(5000),
        initialValue = Resource.Idle()
    )

    /**
     * Updates the selected course to filter files.
     * Pass null to see all active files from all courses.
     */
    fun loadFilesBy(course: Course?) {
        _selectedCourse.value = course
    }

        //ACTIONS
    fun insert(sFile: SFile, uri: Uri) = executeWriteAction{
        sFileWriteUseCase.insert(sFile, uri)
    }
    fun update(sFile: SFile) = executeWriteAction{
        sFileWriteUseCase.update(sFile)
    }
    fun delete(sFile: SFile) = executeWriteAction{
        sFileWriteUseCase.delete(sFile)
    }
    private fun executeWriteAction(action: suspend () -> Resource<SFile>) {
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