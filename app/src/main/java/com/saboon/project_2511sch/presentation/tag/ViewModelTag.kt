package com.saboon.project_2511sch.presentation.tag

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saboon.project_2511sch.domain.alarm.IAlarmScheduler
import com.saboon.project_2511sch.domain.model.Tag
import com.saboon.project_2511sch.domain.repository.ICourseRepository
import com.saboon.project_2511sch.domain.repository.ITaskRepository
import com.saboon.project_2511sch.domain.usecase.tag.TagReadUseCase
import com.saboon.project_2511sch.domain.usecase.tag.TagWriteUseCase
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
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel for tags.
 * 
 * Exposes the selected tag ([tagState]), the rows of the tag list ([tagsState]) and one-shot
 * results of insert / update / delete actions ([operationEvent]). It can also resynchronize
 * the reminder alarms of every task under a tag after the tag is switched on or off.
 */
@HiltViewModel
class ViewModelTag @Inject constructor(
    private val tagWriteUseCase: TagWriteUseCase,
    private val tagReadUseCase: TagReadUseCase,
    private val courseRepository: ICourseRepository,
    private val taskRepository: ITaskRepository,
    private val alarmScheduler: IAlarmScheduler
): ViewModel() {

    private val _operationEvent = Channel<Resource<BaseVMOperationResult<Tag>>>()
    val operationEvent = _operationEvent.receiveAsFlow()

    private val _selectedId = MutableStateFlow<String?>(null)


    //STATE
    @OptIn(ExperimentalCoroutinesApi::class)
    val tagState: StateFlow<Resource<Tag>> = _selectedId
        .filterNotNull()
        .flatMapLatest { id -> tagReadUseCase.getById(id) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Resource.Idle()
        )

    val tagsState: StateFlow<Resource<List<DisplayItemTag>>> =
        tagReadUseCase.getAll()
            .map { resource -> resource.mapData { it.toTagDisplayItems() } }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = Resource.Idle()
            )


    //ACTIONS
    /**
     * Selects the tag whose details [tagState] should expose.
     */
    fun  getById(id: String){
        Timber.d("Select tag: id=%s", id)
        _selectedId.value = id
    }
    /**
     * Saves a new tag.
     */
    fun insert(tag: Tag) = executeWriteAction(OperationType.INSERT){
        tagWriteUseCase.insert(tag)
    }
    /**
     * Saves changes to an existing tag.
     */
    fun update(tag: Tag) = executeWriteAction(OperationType.UPDATE){
        tagWriteUseCase.update(tag)
    }
    /**
     * Deletes a tag; its courses stay and become untagged.
     */
    fun delete(tag: Tag) = executeWriteAction(OperationType.DELETE){
        tagWriteUseCase.delete(tag)
    }
    /**
     * Reschedules or cancels the alarms of every task under [tag] to match its active state,
     * then calls [onComplete].
     */
    fun syncAlarms(tag: Tag, onComplete: () -> Unit){
        Timber.d("Sync tag alarms: tagId=%s", tag.id)
        viewModelScope.launch {
            val allCourses = courseRepository.getAll().first().data ?: emptyList()
            val allTasks = taskRepository.getAll().first().data ?: emptyList()
            alarmScheduler.checkAndSyncTagAlarms(tag, allCourses, allTasks)
            onComplete()
        }
    }
    /**
     * Runs a write [action] and reports its progress and result through [operationEvent].
     */
    private fun executeWriteAction(type: OperationType, action: suspend () -> Resource<Tag>) {
        Timber.d("Tag write action: type=%s", type)
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
                Timber.e(e, "Tag write action threw")
                _operationEvent.send(Resource.Error(e.localizedMessage ?: "Unexpected error"))
            }
        }
    }
}