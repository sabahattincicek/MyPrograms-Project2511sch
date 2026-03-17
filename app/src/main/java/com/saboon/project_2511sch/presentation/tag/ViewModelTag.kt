package com.saboon.project_2511sch.presentation.tag

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saboon.project_2511sch.domain.alarm.IAlarmScheduler
import com.saboon.project_2511sch.domain.model.Tag
import com.saboon.project_2511sch.domain.repository.ICourseRepository
import com.saboon.project_2511sch.domain.repository.ITaskRepository
import com.saboon.project_2511sch.domain.usecase.tag.GetTagDisplayItemListUseCase
import com.saboon.project_2511sch.domain.usecase.tag.TagReadUseCase
import com.saboon.project_2511sch.domain.usecase.tag.TagWriteUseCase
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
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ViewModelTag @Inject constructor(
    private val tagWriteUseCase: TagWriteUseCase,
    private val tagReadUseCase: TagReadUseCase,
    private val getTagDisplayItemListUseCase: GetTagDisplayItemListUseCase,
    private val courseRepository: ICourseRepository,
    private val taskRepository: ITaskRepository,
    private val alarmScheduler: IAlarmScheduler
): ViewModel() {

    private val _operationEvent = Channel<Resource<Tag>>()
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
        getTagDisplayItemListUseCase.invoke()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = Resource.Idle()
            )


    //ACTIONS
    fun  getById(id: String){
        _selectedId.value = id
    }
    fun insert(tag: Tag) = executeWriteAction{
        tagWriteUseCase.insert(tag)
    }
    fun update(tag: Tag) = executeWriteAction{
        tagWriteUseCase.update(tag)
    }
    fun delete(tag: Tag) = executeWriteAction{
        tagWriteUseCase.delete(tag)
    }
    fun syncAlarms(tag: Tag, onComplete: () -> Unit){
        viewModelScope.launch {
            val allCourses = courseRepository.getAll().first().data ?: emptyList()
            val allTasks = taskRepository.getAll().first().data ?: emptyList()
            alarmScheduler.checkAndSyncTagAlarms(tag, allCourses, allTasks)
            onComplete()
        }
    }
    private fun executeWriteAction(action: suspend () -> Resource<Tag>) {
        viewModelScope.launch {
            try {
                _operationEvent.send(Resource.Loading())
                _operationEvent.send(action())
            } catch (e: Exception) {
                _operationEvent.send(Resource.Error(e.localizedMessage ?: "Unexpected error"))
            }
        }
    }
}