package com.saboon.project_2511sch.presentation.task

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saboon.project_2511sch.domain.alarm.IAlarmScheduler
import com.saboon.project_2511sch.domain.model.Course
import com.saboon.project_2511sch.domain.model.ProgramTable
import com.saboon.project_2511sch.domain.model.Task
import com.saboon.project_2511sch.domain.usecase.schedule.DeleteScheduleUseCase
import com.saboon.project_2511sch.domain.usecase.schedule.GetSchedulesByCourseIdUseCase
import com.saboon.project_2511sch.domain.usecase.schedule.InsertNewScheduleUseCase
import com.saboon.project_2511sch.domain.usecase.schedule.UpdateScheduleUseCase
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
class ViewModelTask @Inject constructor(
    private val insertNewScheduleUseCase: InsertNewScheduleUseCase,
    private val updateScheduleUseCase: UpdateScheduleUseCase,
    private val deleteScheduleUseCase: DeleteScheduleUseCase,
    private val getSchedulesByCourseIdUseCase: GetSchedulesByCourseIdUseCase,
    private val alarmScheduler: IAlarmScheduler
): ViewModel() {
    private val _insertNewTaskEvent = Channel<Resource<Task>>()
    val insertNewScheduleEvent = _insertNewTaskEvent.receiveAsFlow()

    private val _updateTaskEvent = Channel<Resource<Task>>()
    val updateScheduleEvent = _updateTaskEvent.receiveAsFlow()

    private val _deleteTaskEvent = Channel<Resource<Task>>()
    val deleteScheduleEvent = _deleteTaskEvent.receiveAsFlow()

    private val _schedulesState = MutableStateFlow<Resource<List<Task>>>(Resource.Idle())
    val taskState: StateFlow<Resource<List<Task>>> = _schedulesState.asStateFlow()
    fun insertNewSchedule(task: Task){
        viewModelScope.launch {
            try {
                _insertNewTaskEvent.send(Resource.Loading())
                val insertResult = insertNewScheduleUseCase.invoke(task)
                _insertNewTaskEvent.send(insertResult)
            }catch (e: Exception){
                _insertNewTaskEvent.send(Resource.Error(e.localizedMessage ?: "An unexpected error occurred in ViewModel."))
            }
        }
    }

    fun updateSchedule(task: Task){
        viewModelScope.launch {
            try {
                _updateTaskEvent.send(Resource.Loading())
                val updateResult = updateScheduleUseCase.invoke(task)
                _updateTaskEvent.send(updateResult)
            }catch (e: Exception){
                _updateTaskEvent.send(Resource.Error(e.localizedMessage ?: "An unexpected error occurred in ViewModel."))
            }
        }
    }

    fun deleteSchedule(task: Task){
        viewModelScope.launch {
            try {
                _deleteTaskEvent.send(Resource.Loading())
                val deleteResult = deleteScheduleUseCase.invoke(task)
                _deleteTaskEvent.send(deleteResult)
                alarmScheduler.cancel(task)
            }catch (e: Exception){
                _deleteTaskEvent.send(Resource.Error(e.localizedMessage ?: "An unexpected error occurred in ViewModel."))
            }
        }
    }

    fun getSchedulesByCourseId(id: String){
        viewModelScope.launch {
            try{
                _schedulesState.value = Resource.Loading()
                getSchedulesByCourseIdUseCase.invoke(id).collect { resource ->
                    _schedulesState.value = resource
                }
            }catch (e: Exception){
                _schedulesState.value = Resource.Error(e.localizedMessage ?: "An unexpected error occurred in ViewModel.")
            }
        }
    }

    fun setupAlarmForSchedule(programTable: ProgramTable, course: Course, task: Task){
        alarmScheduler.scheduleReminder(programTable, course, task)
        alarmScheduler.scheduleAbsenceReminder(programTable, course, task)
    }
}