package com.saboon.project_2511sch.presentation.course

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saboon.project_2511sch.domain.model.Course
import com.saboon.project_2511sch.domain.model.ProgramTable
import com.saboon.project_2511sch.domain.model.Task
import com.saboon.project_2511sch.domain.usecase.course.CourseReadUseCase
import com.saboon.project_2511sch.domain.usecase.course.CourseWriteUseCase
import com.saboon.project_2511sch.domain.usecase.course.GetCourseDisplayItemListUseCase
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
class ViewModelCourse @Inject constructor(
    private val courseWriteUseCase: CourseWriteUseCase,
    private val courseReadUseCase: CourseReadUseCase,
    private val getCourseDisplayItemListUseCase: GetCourseDisplayItemListUseCase,
) : ViewModel() {
    private val _insertEvent = Channel<Resource<Course>>()
    val insertEvent = _insertEvent.receiveAsFlow()
    private val _updateEvent = Channel<Resource<Course>>()
    val updateEvent = _updateEvent.receiveAsFlow()
    private val _deleteEvent = Channel<Resource<Course>>()
    val deleteEvent = _deleteEvent.receiveAsFlow()


    private val _courseState = MutableStateFlow<Resource<Course>>(Resource.Idle())
    val courseState = _courseState.asStateFlow()

    private val _filterState = MutableStateFlow(FilterGeneric())

    //STATE
    @OptIn(ExperimentalCoroutinesApi::class)
    val coursesState = _filterState.flatMapLatest { filter ->
        getCourseDisplayItemListUseCase.invoke(filter)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Companion.WhileSubscribed(5000),
        initialValue = Resource.Idle()
    )

    fun getById(id: String){
        viewModelScope.launch {
            try {
                _courseState.value = Resource.Loading()
                courseReadUseCase.getById(id).collect { resource ->
                    _courseState.value = resource
                }
            } catch (e: Exception) {
                _courseState.value =
                    Resource.Error(e.localizedMessage ?: "An unexpected error occurred in ViewModel.")
            }
        }
    }

    //FILTER
    fun updateFilter(programTable: ProgramTable?){
        _filterState.update { current ->
            if (programTable == null) FilterGeneric()
            else current.copy(programTable = programTable, course = null, task = null)
        }
    }

    //EVENT
    fun insert(course: Course) {
        viewModelScope.launch {
            try {
                _insertEvent.send(Resource.Loading())
                val insertResult = courseWriteUseCase.insert(course)
                _insertEvent.send(insertResult)
            } catch (e: Exception) {
                _insertEvent.send(
                    Resource.Error(e.localizedMessage ?: "An unexpected error occurred in ViewModel.")
                )
            }
        }
    }
    fun update(course: Course) {
        viewModelScope.launch {
            try {
                _updateEvent.send(Resource.Loading())
                val updateResult = courseWriteUseCase.update(course)
                _updateEvent.send(updateResult)
            } catch (e: Exception) {
                _updateEvent.send(
                    Resource.Error(e.localizedMessage ?: "An unexpected error occurred in ViewModel.")
                )
            }
        }
    }
    fun deleteCourse(course: Course) {
        viewModelScope.launch {
            try {
                _deleteEvent.send(Resource.Loading())

                val deleteResult = courseWriteUseCase.delete(course)

                _deleteEvent.send(deleteResult)
            } catch (e: Exception) {
                _deleteEvent.send(
                    Resource.Error(e.localizedMessage ?: "An unexpected error occurred in ViewModel.")
                )
            }
        }
    }
    fun activationById(id: String, isActive: Boolean){
        viewModelScope.launch {
            try {
                courseWriteUseCase.activationById(id, isActive)
            }catch (e: Exception){

            }
        }
    }

    fun getAllCount(onResult: (Resource<Int>) -> Unit) {
        viewModelScope.launch {
            try {
                val result = courseReadUseCase.getAllCount()
                onResult(result)
            } catch (e: Exception) {
                onResult(
                    Resource.Error(e.localizedMessage ?: "An unexpected error occurred in ViewModel.")
                )
            }
        }
    }
    fun getAllActivesCount(onResult: (Resource<Int>) -> Unit){
        viewModelScope.launch {
            try {
                val result = courseReadUseCase.getAllActiveCount()
                onResult(result)
            } catch (e: Exception) {
                onResult(
                    Resource.Error(e.localizedMessage ?: "An unexpected error occurred in ViewModel.")
                )
            }
        }
    }
}
