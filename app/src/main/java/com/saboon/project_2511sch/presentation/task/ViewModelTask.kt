package com.saboon.project_2511sch.presentation.task

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saboon.project_2511sch.domain.alarm.IAlarmScheduler
import com.saboon.project_2511sch.domain.model.AbsenceRecord
import com.saboon.project_2511sch.domain.model.Course
import com.saboon.project_2511sch.domain.model.Task
import com.saboon.project_2511sch.domain.usecase.task.GetCourseAbsenceRecordsUseCase
import com.saboon.project_2511sch.domain.usecase.task.GetTaskDisplayItemUseCase
import com.saboon.project_2511sch.domain.usecase.task.TaskWriteUseCase
import com.saboon.project_2511sch.util.BaseVMOperationResult
import com.saboon.project_2511sch.util.OperationType
import com.saboon.project_2511sch.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel for the tasks (lessons, exams, homeworks) of a single course.
 *
 * Used by the course details and home screens. It exposes:
 * - [tasksState]: the display list of the selected course's tasks,
 * - [absenceRecordsState]: the absences of the selected course's lessons, derived from [tasksState],
 * - [operationEvent]: one-shot results of insert / update / delete actions.
 *
 * Write actions also keep the reminder alarms in sync through [IAlarmScheduler], so a task
 * is never saved without its alarm being scheduled, rescheduled or cancelled accordingly.
 */
@HiltViewModel
class ViewModelTask @Inject constructor(
    private val taskWriteUseCase: TaskWriteUseCase,
    private val getTaskDisplayItemUseCase: GetTaskDisplayItemUseCase,
    private val getCourseAbsenceRecordsUseCase: GetCourseAbsenceRecordsUseCase,
    private val alarmScheduler: IAlarmScheduler
): ViewModel() {
    private val _operationEvent = Channel<Resource<BaseVMOperationResult<Task>>>()
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

    // Derived from tasksState so the absence list always matches the tasks shown on screen.
    val absenceRecordsState: StateFlow<List<AbsenceRecord>> = tasksState.map { resource ->
        val tasks = (resource.data as? List<*>).orEmpty()
            .filterIsInstance<DisplayItemTask.ContentTask>()
            .map { it.task }
        getCourseAbsenceRecordsUseCase(tasks)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Companion.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    /**
     * Updates the current course to trigger a data reload for tasks.
     * @param course The selected course.
     */
    fun loadTasksBy(course: Course?) {
        Timber.d("Loading tasks: courseId=%s", course?.id)
        _selectedCourse.value = course
    }

    //ACTIONS
    /**
     * Saves a new task and schedules its reminder alarm when the save succeeds.
     */
    fun insert(course: Course, task: Task){
        Timber.d("Insert task: courseId=%s, taskId=%s", course.id, task.id)
        executeWriteAction(OperationType.INSERT){
            val result = taskWriteUseCase.insert(task)
            if (result is Resource.Success){
                alarmScheduler.schedule(course, task)
            }
            result
        }
    }

    /**
     * Saves changes to an existing task and reschedules its reminder alarm when the save succeeds.
     */
    fun update(course: Course, task: Task){
        Timber.d("Update task: courseId=%s, taskId=%s", course.id, task.id)
        executeWriteAction(OperationType.UPDATE) {
            val result = taskWriteUseCase.update(task)
            if (result is Resource.Success) {
                alarmScheduler.cancel(course, task)
                alarmScheduler.schedule(course, task)
            }
            result
        }
    }

    /**
     * Deletes a task and cancels its reminder alarm when the delete succeeds.
     */
    fun delete(course: Course, task: Task){
        Timber.d("Delete task: courseId=%s, taskId=%s", course.id, task.id)
        executeWriteAction(OperationType.DELETE) {
            val result = taskWriteUseCase.delete(task)
            if (result is Resource.Success) {
                alarmScheduler.cancel(course, task)
            }
            result
        }
    }

    /**
     * Runs a write [action] and reports its progress and result through [operationEvent].
     * Exceptions are caught and reported as [Resource.Error] so the UI never crashes on a failed write.
     */
    private fun executeWriteAction(type: OperationType, action: suspend () -> Resource<Task>) {
        viewModelScope.launch {
            try {
                _operationEvent.send(Resource.Loading())
                val result = action()
                when(result){
                    is Resource.Error -> {
                        Timber.w("Write action failed: type=%s, message=%s", type, result.message)
                        _operationEvent.send(Resource.Error(result.message ?: "Error"))
                    }
                    is Resource.Idle -> {_operationEvent.send(Resource.Idle())}
                    is Resource.Loading -> {_operationEvent.send(Resource.Loading())}
                    is Resource.Success -> {
                        Timber.d("Write action succeeded: type=%s", type)
                        _operationEvent.send(Resource.Success(BaseVMOperationResult(result.data!!, type)))
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Write action threw: type=%s", type)
                _operationEvent.send(Resource.Error(e.localizedMessage ?: "Unexpected error"))
            }
        }
    }
}
