package com.saboon.project_2511sch.presentation.course

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saboon.project_2511sch.domain.alarm.IAlarmScheduler
import com.saboon.project_2511sch.domain.model.Course
import com.saboon.project_2511sch.domain.repository.ITaskRepository
import com.saboon.project_2511sch.domain.usecase.course.CourseReadUseCase
import com.saboon.project_2511sch.domain.usecase.course.CourseWriteUseCase
import com.saboon.project_2511sch.domain.usecase.course.GetCoursesGroupedByTagUseCase
import com.saboon.project_2511sch.util.BaseVMOperationResult
import com.saboon.project_2511sch.util.OperationType
import com.saboon.project_2511sch.util.Resource
import com.saboon.project_2511sch.util.mapData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel for courses.
 * 
 * Exposes the selected course ([courseState]), the rows of the course list grouped by tag
 * ([coursesState]) and one-shot results of insert / update / delete actions ([operationEvent]).
 * It can also resynchronize the reminder alarms of the tasks of a course after the course is
 * switched on or off.
 */
@HiltViewModel
class ViewModelCourse @Inject constructor(
    private val courseWriteUseCase: CourseWriteUseCase,
    private val courseReadUseCase: CourseReadUseCase,
    private val getCoursesGroupedByTagUseCase: GetCoursesGroupedByTagUseCase,
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
    val coursesState: StateFlow<Resource<List<DisplayItemCourse>>> = getCoursesGroupedByTagUseCase.invoke()
        .map { resource -> resource.mapData { it.toCourseDisplayItems() } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Companion.WhileSubscribed(5000),
            initialValue = Resource.Idle()
        )

    //ACTIONS
    /**
     * Selects the course whose details [courseState] should expose.
     */
    fun getById(id: String){
        Timber.d("Select course: id=%s", id)
        _selectedId.value = id
    }
    /**
     * Saves a new course.
     */
    fun insert(course: Course) = executeWriteAction(OperationType.INSERT){
        courseWriteUseCase.insert(course)
    }
    /**
     * Saves changes to an existing course.
     */
    fun update(course: Course) = executeWriteAction(OperationType.UPDATE){
        courseWriteUseCase.update(course)
    }
    /**
     * Deletes a course together with its tasks and files.
     */
    fun delete(course: Course) = executeWriteAction(OperationType.DELETE){
        courseWriteUseCase.delete(course)
    }
    /**
     * Reschedules or cancels the alarms of every task of [course] to match its active state,
     * then calls [onComplete].
     */
    fun syncAlarms(course: Course, onComplete: () -> Unit){
        Timber.d("Sync course alarms: courseId=%s", course.id)
        viewModelScope.launch {
            val tasksResult = taskRepository.getAllByCourseId(course.id).first()
            val tasks = tasksResult.data ?: emptyList()
            alarmScheduler.checkAndSyncCourseAlarms(course, tasks)
            onComplete()
        }
    }
    /**
     * Runs a write [action] and reports its progress and result through [operationEvent].
     */
    private fun executeWriteAction(type: OperationType, action: suspend () -> Resource<Course>) {
        Timber.d("Course write action: type=%s", type)
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
                Timber.e(e, "Course write action threw")
                _operationEvent.send(Resource.Error(e.localizedMessage ?: "Unexpected error"))
            }
        }
    }
}
