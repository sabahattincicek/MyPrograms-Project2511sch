package com.saboon.project_2511sch.presentation.task

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saboon.project_2511sch.domain.alarm.IAlarmScheduler
import com.saboon.project_2511sch.domain.model.Course
import com.saboon.project_2511sch.domain.model.ProgramTable
import com.saboon.project_2511sch.domain.model.Task
import com.saboon.project_2511sch.domain.usecase.task.GetTaskDisplayItemUseCase
import com.saboon.project_2511sch.domain.usecase.task.TaskReadUseCase
import com.saboon.project_2511sch.domain.usecase.task.TaskWriteUseCase
import com.saboon.project_2511sch.presentation.common.FilterGeneric
import com.saboon.project_2511sch.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ViewModelTask @Inject constructor(
    private val taskWriteUseCase: TaskWriteUseCase,
    private val taskReadUseCase: TaskReadUseCase,
    private val getTaskDisplayItemUseCase: GetTaskDisplayItemUseCase,
    private val alarmScheduler: IAlarmScheduler
): ViewModel() {
    private val _insertEvent = Channel<Resource<Task>>()
    val insertEvent = _insertEvent.receiveAsFlow()
    private val _updateEvent = Channel<Resource<Task>>()
    val updateEvent = _updateEvent.receiveAsFlow()
    private val _deleteEvent = Channel<Resource<Task>>()
    val deleteEvent = _deleteEvent.receiveAsFlow()


    private val _filterState = MutableStateFlow(FilterGeneric())

    //STATE
    @OptIn(ExperimentalCoroutinesApi::class)
    val tasksState = _filterState.flatMapLatest { filter ->
        getTaskDisplayItemUseCase.invoke(filter)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Companion.WhileSubscribed(5000),
        initialValue = Resource.Idle()
    )

    //FILTER
    fun updateFilter(programTable: ProgramTable?, course: Course?){
        _filterState.update { current ->
            if (programTable == null) FilterGeneric()
            else current.copy(programTable = programTable, course = course, task = null)
        }
    }

    //EVENT
    fun insert(task: Task){
        viewModelScope.launch {
            try {
                _insertEvent.send(Resource.Loading())
                val insertResult = taskWriteUseCase.insert(task)
                _insertEvent.send(insertResult)
            }catch (e: Exception){
                _insertEvent.send(Resource.Error(e.localizedMessage ?: "An unexpected error occurred in ViewModel."))
            }
        }
    }
    fun update(task: Task){
        viewModelScope.launch {
            try {
                _updateEvent.send(Resource.Loading())
                val updateResult = taskWriteUseCase.update(task)
                _updateEvent.send(updateResult)
            }catch (e: Exception){
                _updateEvent.send(Resource.Error(e.localizedMessage ?: "An unexpected error occurred in ViewModel."))
            }
        }
    }
    fun delete(task: Task){
        viewModelScope.launch {
            try {
                _deleteEvent.send(Resource.Loading())
                val deleteResult = taskWriteUseCase.delete(task)
                _deleteEvent.send(deleteResult)
                alarmScheduler.cancel(task)
            }catch (e: Exception){
                _deleteEvent.send(Resource.Error(e.localizedMessage ?: "An unexpected error occurred in ViewModel."))
            }
        }
    }

    fun setupAlarmForSchedule(programTable: ProgramTable, course: Course, task: Task){
        alarmScheduler.scheduleReminder(programTable, course, task)
        alarmScheduler.scheduleAbsenceReminder(programTable, course, task)
    }
}