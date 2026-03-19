package com.saboon.project_2511sch.presentation.course

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saboon.project_2511sch.domain.alarm.IAlarmScheduler
import com.saboon.project_2511sch.domain.model.Course
import com.saboon.project_2511sch.domain.repository.ITaskRepository
import com.saboon.project_2511sch.domain.usecase.course.CourseReadUseCase
import com.saboon.project_2511sch.domain.usecase.course.CourseWriteUseCase
import com.saboon.project_2511sch.domain.usecase.course.GetCourseDisplayItemListUseCase
import com.saboon.project_2511sch.util.BaseVMOperationResult
import com.saboon.project_2511sch.util.OperationType
import com.saboon.project_2511sch.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
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
    private val taskRepository: ITaskRepository,
    private val alarmScheduler: IAlarmScheduler
) : ViewModel() {
    private val _operationEvent = Channel<Resource<BaseVMOperationResult<Course>>>()
    val operationEvent = _operationEvent.receiveAsFlow()

    private val _selectedId = MutableStateFlow<String?>(null)

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
    val coursesState = getCourseDisplayItemListUseCase.invoke()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Companion.WhileSubscribed(5000),
            initialValue = Resource.Idle()
        )

    //ACTIONS
    fun getById(id: String){
        _selectedId.value = id
    }
    fun insert(course: Course) = executeWriteAction(OperationType.INSERT){
        courseWriteUseCase.insert(course)
    }
    fun update(course: Course) = executeWriteAction(OperationType.UPDATE){
        courseWriteUseCase.update(course)
    }
    fun delete(course: Course) = executeWriteAction(OperationType.DELETE){
        courseWriteUseCase.delete(course)
    }
    fun syncAlarms(course: Course, onComplete: () -> Unit){
        viewModelScope.launch {
            val tasksResult = taskRepository.getAllByCourseId(course.id).first()
            val tasks = tasksResult.data ?: emptyList()
            alarmScheduler.checkAndSyncCourseAlarms(course, tasks)
            onComplete()
        }
    }
    private fun executeWriteAction(type: OperationType, action: suspend () -> Resource<Course>) {
        viewModelScope.launch {
            try {
                _operationEvent.send(Resource.Loading())
                val result = action()
                when(result){
                    is Resource.Error -> {_operationEvent.send(Resource.Error(result.message ?: "Error"))}
                    is Resource.Idle -> {_operationEvent.send(Resource.Idle())}
                    is Resource.Loading -> {_operationEvent.send(Resource.Loading())}
                    is Resource.Success -> {
                        _operationEvent.send(Resource.Success(BaseVMOperationResult(result.data!!, type)))
                    }
                }
            } catch (e: Exception) {
                _operationEvent.send(Resource.Error(e.localizedMessage ?: "Unexpected error"))
            }
        }
    }
}
