package com.saboon.project_2511sch.presentation.course

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saboon.project_2511sch.domain.model.Course
import com.saboon.project_2511sch.domain.model.ProgramTable
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
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
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
    private val _operationEvent = Channel<Resource<Course>>()
    val operationEvent = _operationEvent.receiveAsFlow()

    private val _selectedId = MutableStateFlow<String?>(null)

    private val _filterState = MutableStateFlow(FilterGeneric())

    //STATE
    @OptIn(ExperimentalCoroutinesApi::class)
    val courseState: StateFlow<Resource<Course>> = _selectedId
        .filterNotNull()
        .flatMapLatest { id -> courseReadUseCase.getById(id) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Resource.Idle()
        )
    @OptIn(ExperimentalCoroutinesApi::class)
    val coursesState = _filterState.flatMapLatest { filter ->
        getCourseDisplayItemListUseCase.invoke(filter)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Companion.WhileSubscribed(5000),
        initialValue = Resource.Idle()
    )

    //FILTER
    fun updateFilter(programTable: ProgramTable?){
        _filterState.update { current ->
            if (programTable == null) FilterGeneric()
            else current.copy(programTable = programTable, course = null, task = null)
        }
    }

    //ACTIONS
    fun getById(id: String){
        _selectedId.value = id
    }
    fun insert(course: Course) = executeWriteAction{
        courseWriteUseCase.insert(course)
    }
    fun update(course: Course) = executeWriteAction{
        courseWriteUseCase.update(course)
    }
    fun delete(course: Course) = executeWriteAction{
        courseWriteUseCase.delete(course)
    }
    fun activationById(id: String, isActive: Boolean){
        viewModelScope.launch {
            try {
                courseWriteUseCase.activationById(id, isActive)
            }catch (e: Exception){

            }
        }
    }
    private fun executeWriteAction(action: suspend () -> Resource<Course>) {
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
