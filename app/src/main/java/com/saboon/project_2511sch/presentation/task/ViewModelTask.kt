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
    private val _operationEvent = Channel<Resource<Task>>()
    val operationEvent = _operationEvent.receiveAsFlow()

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

    //ACTIONS
    fun insert(task: Task) = executeWriteAction{
        taskWriteUseCase.insert(task)
    }
    fun update(task: Task) = executeWriteAction{
        taskWriteUseCase.update(task)
    }
    fun delete(task: Task) = executeWriteAction{
        taskWriteUseCase.delete(task)
    }
    private fun executeWriteAction(action: suspend () -> Resource<Task>) {
        viewModelScope.launch {
            try {
                _operationEvent.send(Resource.Loading())
                _operationEvent.send(action())
            } catch (e: Exception) {
                _operationEvent.send(Resource.Error(e.localizedMessage ?: "Unexpected error"))
            }
        }
    }

    fun setupAlarmForSchedule(programTable: ProgramTable, course: Course, task: Task){
        alarmScheduler.scheduleReminder(programTable, course, task)
        alarmScheduler.scheduleAbsenceReminder(programTable, course, task)
    }
}