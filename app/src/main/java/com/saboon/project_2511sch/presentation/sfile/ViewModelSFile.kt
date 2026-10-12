package com.saboon.project_2511sch.presentation.sfile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saboon.project_2511sch.domain.model.Course
import com.saboon.project_2511sch.domain.model.SFile
import com.saboon.project_2511sch.domain.usecase.sfile.GetFilesGroupedByCourseUseCase
import com.saboon.project_2511sch.domain.usecase.sfile.SFileWriteUseCase
import com.saboon.project_2511sch.util.Resource
import com.saboon.project_2511sch.util.mapData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel for the files attached to courses.
 * 
 * Exposes the rows of a file list ([filesState]): the files of the selected course, or the
 * files of all visible courses grouped by course when no course is selected. Insert / update /
 * delete results are reported through [operationEvent].
 */
@HiltViewModel
class ViewModelSFile @Inject constructor(
    private val sFileWriteUseCase: SFileWriteUseCase,
    private val getFilesGroupedByCourseUseCase: GetFilesGroupedByCourseUseCase
): ViewModel() {
    private val _operationEvent = Channel<Resource<SFile>>()
    val operationEvent = _operationEvent.receiveAsFlow()

    private val _selectedCourse = MutableStateFlow<Course?>(null)

    //STATE
    @OptIn(ExperimentalCoroutinesApi::class)
    val filesState = _selectedCourse.flatMapLatest { course ->
        if (course == null){
            getFilesGroupedByCourseUseCase.invoke()
                .map { resource -> resource.mapData { it.toSFileDisplayItems() } }
        }else{
            getFilesGroupedByCourseUseCase.getByCourse(course)
                .map { resource -> resource.mapData { it.toSFileDisplayItems(course) } }
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
        Timber.d("Loading files: courseId=%s", course?.id)
        _selectedCourse.value = course
    }

        //ACTIONS
    /**
     * Copies the picked file into the app storage and saves its record.
     */
    fun insert(sFile: SFile, uri: Uri) = executeWriteAction{
        sFileWriteUseCase.insert(sFile, uri)
    }
    /**
     * Saves changes to a file record.
     */
    fun update(sFile: SFile) = executeWriteAction{
        sFileWriteUseCase.update(sFile)
    }
    /**
     * Deletes a file record.
     */
    fun delete(sFile: SFile) = executeWriteAction{
        sFileWriteUseCase.delete(sFile)
    }
    /**
     * Runs a write [action] and reports its progress and result through [operationEvent].
     */
    private fun executeWriteAction(action: suspend () -> Resource<SFile>) {
        viewModelScope.launch {
            try {
                _operationEvent.send(Resource.Loading())
                _operationEvent.send(action())
            } catch (e: Exception) {
                Timber.e(e, "File write action threw")
                _operationEvent.send(Resource.Error(e.localizedMessage ?: "Unexpected error"))
            }
        }
    }
}