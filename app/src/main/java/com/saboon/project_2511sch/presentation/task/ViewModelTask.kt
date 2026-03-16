package com.saboon.project_2511sch.presentation.task

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saboon.project_2511sch.domain.alarm.IAlarmScheduler
import com.saboon.project_2511sch.domain.model.Course
import com.saboon.project_2511sch.domain.model.Tag
import com.saboon.project_2511sch.domain.model.Task
import com.saboon.project_2511sch.domain.usecase.task.GetTaskDisplayItemUseCase
import com.saboon.project_2511sch.domain.usecase.task.TaskWriteUseCase
import com.saboon.project_2511sch.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ViewModelTask @Inject constructor(
    private val taskWriteUseCase: TaskWriteUseCase,
    private val getTaskDisplayItemUseCase: GetTaskDisplayItemUseCase,
    private val alarmScheduler: IAlarmScheduler
): ViewModel() {
    private val _operationEvent = Channel<Resource<Task>>()
    val operationEvent = _operationEvent.receiveAsFlow()

    private val _selectedCourse = MutableStateFlow<Course?>(null)

    //STATE
    @OptIn(ExperimentalCoroutinesApi::class)
    val tasksState = _selectedCourse.flatMapLatest { course ->
        if (course == null) {
            // Eğer ders seçilmemişse boş bir başarı sonucu dön (veya Idle)
            flowOf(Resource.Success(emptyList()))
        } else {
            // UseCase artık doğrudan Course nesnesini bekliyor
            getTaskDisplayItemUseCase.invoke(course)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Companion.WhileSubscribed(5000),
        initialValue = Resource.Idle()
    )

    /**
     * Updates the current course to trigger a data reload for tasks.
     * @param course The selected course.
     */
    fun loadTasksBy(course: Course?) {
        _selectedCourse.value = course
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

    fun setupAlarmForSchedule(tag: Tag, course: Course, task: Task){
        alarmScheduler.scheduleReminder(tag, course, task)
        alarmScheduler.scheduleAbsenceReminder(tag, course, task)
    }
}