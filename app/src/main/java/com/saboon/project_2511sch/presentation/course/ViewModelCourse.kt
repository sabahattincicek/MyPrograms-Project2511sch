package com.saboon.project_2511sch.presentation.course

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saboon.project_2511sch.domain.model.Course
import com.saboon.project_2511sch.domain.usecase.course.CourseReadUseCase
import com.saboon.project_2511sch.domain.usecase.course.CourseWriteUseCase
import com.saboon.project_2511sch.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ViewModelCourse @Inject constructor(
    private val courseWriteUseCase: CourseWriteUseCase,
    private val courseReadUseCase: CourseReadUseCase,
): ViewModel() {

    // TODO: change all channels to sharedflow
    private val _insertCourseEvent = Channel<Resource<Course>>()
    val insertCourseEvent = _insertCourseEvent.receiveAsFlow()

    private val _updateCourseEvent = Channel<Resource<Course>>()
    val updateCourseEvent = _updateCourseEvent.receiveAsFlow()

    private val _deleteCourseEvent = Channel<Resource<Course>>()
    val deleteCourseEvent = _deleteCourseEvent.receiveAsFlow()

    private val _coursesState = MutableStateFlow<Resource<List<Course>>>(Resource.Idle())
    val coursesState: StateFlow<Resource<List<Course>>> = _coursesState.asStateFlow()

    fun insertNewCourse(course: Course){
        viewModelScope.launch {
            try {
                _insertCourseEvent.send(Resource.Loading())
                val insertResult = courseWriteUseCase.insert(course)
                _insertCourseEvent.send(insertResult)
            }catch (e: Exception){
                _insertCourseEvent.send(Resource.Error(e.localizedMessage?:"An unexpected error occurred in ViewModel."))
            }
        }
    }

    fun updateCourse(course: Course){
        viewModelScope.launch {
            try {
                _updateCourseEvent.send(Resource.Loading())
                val updateResult = courseWriteUseCase.update(course)
                _updateCourseEvent.send(updateResult)
            }catch (e: Exception){
                _updateCourseEvent.send(Resource.Error(e.localizedMessage?:"An unexpected error occurred in ViewModel."))
            }
        }
    }

    fun deleteCourse(course: Course){
        viewModelScope.launch {
            try {
                _deleteCourseEvent.send(Resource.Loading())
                val deleteResult = courseWriteUseCase.delete(course)
                _deleteCourseEvent.send(deleteResult)
            }catch (e: Exception){
                _deleteCourseEvent.send(Resource.Error(e.localizedMessage?:"An unexpected error occurred in ViewModel."))
            }
        }
    }

    fun getAllCoursesByProgramTableId(id: String){
        viewModelScope.launch {
            try {
                _coursesState.value = Resource.Loading()
                courseReadUseCase.getAllByProgramTableId(id).collect { resource ->
                    _coursesState.value = resource
                }
            }catch (e: Exception){
                _coursesState.value = Resource.Error(e.localizedMessage ?: "An unexpected error occurred in ViewModel.")
            }
        }
    }
}