package com.saboon.project_2511sch.presentation.course

import android.util.Log
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
) : ViewModel() {

    companion object {
        private const val TAG = "ViewModelCourse"
    }

    // TODO: change all channels to sharedflow
    private val _insertCourseEvent = Channel<Resource<Course>>()
    val insertCourseEvent = _insertCourseEvent.receiveAsFlow()

    private val _updateCourseEvent = Channel<Resource<Course>>()
    val updateCourseEvent = _updateCourseEvent.receiveAsFlow()

    private val _deleteCourseEvent = Channel<Resource<Course>>()
    val deleteCourseEvent = _deleteCourseEvent.receiveAsFlow()

    private val _coursesState = MutableStateFlow<Resource<List<Course>>>(Resource.Idle())
    val coursesState: StateFlow<Resource<List<Course>>> = _coursesState.asStateFlow()

    fun insertNewCourse(course: Course) {
        Log.d(TAG, "insertNewCourse called: $course")
        viewModelScope.launch {
            try {
                Log.d(TAG, "insertNewCourse -> Loading")
                _insertCourseEvent.send(Resource.Loading())

                val insertResult = courseWriteUseCase.insert(course)
                Log.d(TAG, "insertNewCourse -> Result: $insertResult")

                _insertCourseEvent.send(insertResult)
            } catch (e: Exception) {
                Log.e(TAG, "insertNewCourse -> Error", e)
                _insertCourseEvent.send(
                    Resource.Error(e.localizedMessage ?: "An unexpected error occurred in ViewModel.")
                )
            }
        }
    }

    fun updateCourse(course: Course) {
        Log.d(TAG, "updateCourse called: $course")
        viewModelScope.launch {
            try {
                Log.d(TAG, "updateCourse -> Loading")
                _updateCourseEvent.send(Resource.Loading())

                val updateResult = courseWriteUseCase.update(course)
                Log.d(TAG, "updateCourse -> Result: $updateResult")

                _updateCourseEvent.send(updateResult)
            } catch (e: Exception) {
                Log.e(TAG, "updateCourse -> Error", e)
                _updateCourseEvent.send(
                    Resource.Error(e.localizedMessage ?: "An unexpected error occurred in ViewModel.")
                )
            }
        }
    }

    fun deleteCourse(course: Course) {
        Log.d(TAG, "deleteCourse called: $course")
        viewModelScope.launch {
            try {
                Log.d(TAG, "deleteCourse -> Loading")
                _deleteCourseEvent.send(Resource.Loading())

                val deleteResult = courseWriteUseCase.delete(course)
                Log.d(TAG, "deleteCourse -> Result: $deleteResult")

                _deleteCourseEvent.send(deleteResult)
            } catch (e: Exception) {
                Log.e(TAG, "deleteCourse -> Error", e)
                _deleteCourseEvent.send(
                    Resource.Error(e.localizedMessage ?: "An unexpected error occurred in ViewModel.")
                )
            }
        }
    }

    fun getAllCourse() {
        Log.d(TAG, "getAllCourse called")
        viewModelScope.launch {
            try {
                Log.d(TAG, "getAllCourse -> Loading")
                _coursesState.value = Resource.Loading()

                courseReadUseCase.getAll().collect { resource ->
                    Log.d(TAG, "getAllCourse -> Emitted: $resource")
                    _coursesState.value = resource
                }
            } catch (e: Exception) {
                Log.e(TAG, "getAllCourse -> Error", e)
                _coursesState.value =
                    Resource.Error(e.localizedMessage ?: "An unexpected error occurred in ViewModel.")
            }
        }
    }

    fun getAllCoursesByProgramTableId(id: String) {
        Log.d(TAG, "getAllCoursesByProgramTableId called: id=$id")
        viewModelScope.launch {
            try {
                Log.d(TAG, "getAllCoursesByProgramTableId -> Loading")
                _coursesState.value = Resource.Loading()

                courseReadUseCase.getAllByProgramTableId(id).collect { resource ->
                    Log.d(TAG, "getAllCoursesByProgramTableId -> Emitted: $resource")
                    _coursesState.value = resource
                }
            } catch (e: Exception) {
                Log.e(TAG, "getAllCoursesByProgramTableId -> Error", e)
                _coursesState.value =
                    Resource.Error(e.localizedMessage ?: "An unexpected error occurred in ViewModel.")
            }
        }
    }

    fun getAllCoursesCount(onResult: (Resource<Int>) -> Unit) {
        Log.d(TAG, "getAllCoursesCount called")
        viewModelScope.launch {
            try {
                val result = courseReadUseCase.getAllCount()
                Log.d(TAG, "getAllCoursesCount -> Result: $result")
                onResult(result)
            } catch (e: Exception) {
                Log.e(TAG, "getAllCoursesCount -> Error", e)
                onResult(
                    Resource.Error(e.localizedMessage ?: "An unexpected error occurred in ViewModel.")
                )
            }
        }
    }
}
