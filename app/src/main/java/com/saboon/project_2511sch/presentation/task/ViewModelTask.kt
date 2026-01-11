package com.saboon.project_2511sch.presentation.task

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saboon.project_2511sch.domain.alarm.IAlarmScheduler
import com.saboon.project_2511sch.domain.model.Course
import com.saboon.project_2511sch.domain.model.ProgramTable
import com.saboon.project_2511sch.domain.model.Task
import com.saboon.project_2511sch.domain.usecase.task.DeleteTaskUseCase
import com.saboon.project_2511sch.domain.usecase.task.GetTaskDisplayItemsUseCase
import com.saboon.project_2511sch.domain.usecase.task.InsertNewTaskUseCase
import com.saboon.project_2511sch.domain.usecase.task.UpdateTaskUseCase
import com.saboon.project_2511sch.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ViewModelTask @Inject constructor(
    private val insertNewTaskUseCase: InsertNewTaskUseCase,
    private val updateTaskUseCase: UpdateTaskUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase,
    private val getTaskDisplayItemsUseCase: GetTaskDisplayItemsUseCase,
    private val alarmScheduler: IAlarmScheduler
): ViewModel() {
    private val _insertNewTaskEvent = Channel<Resource<Task>>()
    val insertNewScheduleEvent = _insertNewTaskEvent.receiveAsFlow()

    private val _updateTaskEvent = Channel<Resource<Task>>()
    val updateScheduleEvent = _updateTaskEvent.receiveAsFlow()

    private val _deleteTaskEvent = Channel<Resource<Task>>()
    val deleteScheduleEvent = _deleteTaskEvent.receiveAsFlow()

    private val _taskState = MutableStateFlow<Resource<List<Task>>>(Resource.Idle())
    val taskState = _taskState.asStateFlow()

    private val _taskDisplayItemsState = MutableStateFlow<Resource<List<TaskDisplayItem>>>(Resource.Idle())
    val taskDisplayItemsState = _taskDisplayItemsState.asStateFlow()
    fun insertNewTask(task: Task){
        viewModelScope.launch {
            try {
                _insertNewTaskEvent.send(Resource.Loading())
                val insertResult = insertNewTaskUseCase.invoke(task)
                _insertNewTaskEvent.send(insertResult)
            }catch (e: Exception){
                _insertNewTaskEvent.send(Resource.Error(e.localizedMessage ?: "An unexpected error occurred in ViewModel."))
            }
        }
    }

    fun updateTask(task: Task){
        viewModelScope.launch {
            try {
                _updateTaskEvent.send(Resource.Loading())
                val updateResult = updateTaskUseCase.invoke(task)
                _updateTaskEvent.send(updateResult)
            }catch (e: Exception){
                _updateTaskEvent.send(Resource.Error(e.localizedMessage ?: "An unexpected error occurred in ViewModel."))
            }
        }
    }

    fun deleteTask(task: Task){
        viewModelScope.launch {
            try {
                _deleteTaskEvent.send(Resource.Loading())
                val deleteResult = deleteTaskUseCase.invoke(task)
                _deleteTaskEvent.send(deleteResult)
                alarmScheduler.cancel(task)
            }catch (e: Exception){
                _deleteTaskEvent.send(Resource.Error(e.localizedMessage ?: "An unexpected error occurred in ViewModel."))
            }
        }
    }

    fun getTaskDisplayItems(course: Course){
        viewModelScope.launch {
            try {
                _taskDisplayItemsState.value = Resource.Loading()
                getTaskDisplayItemsUseCase.invoke(course).collect { resource ->
                    _taskDisplayItemsState.value = resource
                }
            }catch (e: Exception){
                _taskDisplayItemsState.value = Resource.Error(e.localizedMessage?:"An unexpected error occurred in ViewModel.")
            }
        }
    }

    fun setupAlarmForSchedule(programTable: ProgramTable, course: Course, task: Task){
        alarmScheduler.scheduleReminder(programTable, course, task)
        alarmScheduler.scheduleAbsenceReminder(programTable, course, task)
    }
}