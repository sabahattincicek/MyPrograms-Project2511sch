package com.saboon.project_2511sch.presentation.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saboon.project_2511sch.domain.alarm.IAlarmScheduler
import com.saboon.project_2511sch.domain.model.Course
import com.saboon.project_2511sch.domain.model.ProgramTable
import com.saboon.project_2511sch.domain.model.Schedule
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
class ViewModelSchedule @Inject constructor(
    private val insertNewScheduleUseCase: InsertNewScheduleUseCase,
    private val updateScheduleUseCase: UpdateScheduleUseCase,
    private val deleteScheduleUseCase: DeleteScheduleUseCase,
    private val getSchedulesByCourseIdUseCase: GetSchedulesByCourseIdUseCase,
    private val alarmScheduler: IAlarmScheduler
): ViewModel() {
    private val _insertNewScheduleEvent = Channel<Resource<Schedule>>()
    val insertNewScheduleEvent = _insertNewScheduleEvent.receiveAsFlow()

    private val _updateScheduleEvent = Channel<Resource<Schedule>>()
    val updateScheduleEvent = _updateScheduleEvent.receiveAsFlow()

    private val _deleteScheduleEvent = Channel<Resource<Schedule>>()
    val deleteScheduleEvent = _deleteScheduleEvent.receiveAsFlow()

    private val _schedulesState = MutableStateFlow<Resource<List<Schedule>>>(Resource.Idle())
    val scheduleState: StateFlow<Resource<List<Schedule>>> = _schedulesState.asStateFlow()
    fun insertNewSchedule(schedule: Schedule){
        viewModelScope.launch {
            try {
                _insertNewScheduleEvent.send(Resource.Loading())
                val insertResult = insertNewScheduleUseCase.invoke(schedule)
                _insertNewScheduleEvent.send(insertResult)
            }catch (e: Exception){
                _insertNewScheduleEvent.send(Resource.Error(e.localizedMessage ?: "An unexpected error occurred in ViewModel."))
            }
        }
    }

    fun updateSchedule(schedule: Schedule){
        viewModelScope.launch {
            try {
                _updateScheduleEvent.send(Resource.Loading())
                val updateResult = updateScheduleUseCase.invoke(schedule)
                _updateScheduleEvent.send(updateResult)
            }catch (e: Exception){
                _updateScheduleEvent.send(Resource.Error(e.localizedMessage ?: "An unexpected error occurred in ViewModel."))
            }
        }
    }

    fun deleteSchedule(schedule: Schedule){
        viewModelScope.launch {
            try {
                _deleteScheduleEvent.send(Resource.Loading())
                val deleteResult = deleteScheduleUseCase.invoke(schedule)
                _deleteScheduleEvent.send(deleteResult)
                alarmScheduler.cancel(schedule)
            }catch (e: Exception){
                _deleteScheduleEvent.send(Resource.Error(e.localizedMessage ?: "An unexpected error occurred in ViewModel."))
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

    fun setupAlarmForSchedule(programTable: ProgramTable, course: Course, schedule: Schedule){
        alarmScheduler.scheduleReminder(programTable, course, schedule)
        alarmScheduler.scheduleAbsenceReminder(programTable, course, schedule)
    }
}