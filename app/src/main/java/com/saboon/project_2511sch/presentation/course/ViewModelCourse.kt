package com.saboon.project_2511sch.presentation.course

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saboon.project_2511sch.domain.model.Course
import com.saboon.project_2511sch.domain.usecase.course.DecrementAbsenceUseCase
import com.saboon.project_2511sch.domain.usecase.course.DeleteCourseUseCase
import com.saboon.project_2511sch.domain.usecase.course.GetAllCoursesUseCase
import com.saboon.project_2511sch.domain.usecase.course.GetCoursesWithProgramTableIdUseCase
import com.saboon.project_2511sch.domain.usecase.course.IncrementAbsenceUseCase
import com.saboon.project_2511sch.domain.usecase.course.InsertNewCourseUseCase
import com.saboon.project_2511sch.domain.usecase.course.UpdateCourseUseCase
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
    private val insertNewCourseUseCase: InsertNewCourseUseCase,
    private val updateCourseUseCase: UpdateCourseUseCase,
    private val deleteCourseUseCase: DeleteCourseUseCase,
    private val decrementAbsenceUseCase: DecrementAbsenceUseCase,
    private val incrementAbsenceUseCase: IncrementAbsenceUseCase,
    private val getAllCoursesUseCase: GetAllCoursesUseCase,
    private val getCoursesWithProgramTableIdUseCase: GetCoursesWithProgramTableIdUseCase
): ViewModel() {

    private val _insertNewCourseEvent = Channel<Resource<Course>>()
    val insertNewCourseEvent = _insertNewCourseEvent.receiveAsFlow()

    private val _updateCourseEvent = Channel<Resource<Course>>()
    val updateCourseEvent = _updateCourseEvent.receiveAsFlow()

    private val _deleteCourseEvent = Channel<Resource<Course>>()
    val deleteCourseEvent = _deleteCourseEvent.receiveAsFlow()

    private val _coursesState = MutableStateFlow<Resource<List<Course>>>(Resource.Idle())
    val coursesState: StateFlow<Resource<List<Course>>> = _coursesState.asStateFlow()

    fun insertNewCourse(course: Course){
        viewModelScope.launch {
            try {
                _insertNewCourseEvent.send(Resource.Loading())
                val insertResult = insertNewCourseUseCase.invoke(course)
                _insertNewCourseEvent.send(insertResult)
            }catch (e: Exception){
                _insertNewCourseEvent.send(Resource.Error(e.localizedMessage?:"An unexpected error occurred in ViewModel."))
            }
        }
    }

    fun updateCourse(course: Course){
        viewModelScope.launch {
            try {
                _updateCourseEvent.send(Resource.Loading())
                val updateResult = updateCourseUseCase.invoke(course)
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
                val deleteResult = deleteCourseUseCase.invoke(course)
                _deleteCourseEvent.send(deleteResult)
            }catch (e: Exception){
                _deleteCourseEvent.send(Resource.Error(e.localizedMessage?:"An unexpected error occurred in ViewModel."))
            }
        }
    }

    fun decrementAbsence(course: Course){
        viewModelScope.launch {
            try {
                val decrementResult = decrementAbsenceUseCase.invoke(course)
            }catch (e: Exception){

            }
        }
    }
    fun incrementAbsence(course: Course){
        viewModelScope.launch {
            try {
                val incrementResult = incrementAbsenceUseCase.invoke(course)
            }catch (e: Exception){

            }
        }
    }

    fun getAllCourses(){
        viewModelScope.launch {
            try {
                _coursesState.value = Resource.Loading()
                getAllCoursesUseCase.invoke().collect { resource ->
                    _coursesState.value = resource
                }
            }catch (e: Exception){
                _coursesState.value = Resource.Error(e.localizedMessage ?: "An unexpected error occurred in ViewModel.")
            }
        }
    }

    fun getCoursesWithProgramTableId(id: String){
        viewModelScope.launch {
            try {
                _coursesState.value = Resource.Loading()
                getCoursesWithProgramTableIdUseCase.invoke(id).collect { resource ->
                    _coursesState.value = resource
                }
            }catch (e: Exception){
                _coursesState.value = Resource.Error(e.localizedMessage ?: "An unexpected error occurred in ViewModel.")
            }
        }
    }
}