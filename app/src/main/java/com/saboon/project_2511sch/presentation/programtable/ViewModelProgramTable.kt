package com.saboon.project_2511sch.presentation.programtable

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saboon.project_2511sch.domain.model.ProgramTable
import com.saboon.project_2511sch.domain.usecase.programtable.GetProgramTableDisplayItemListUseCase
import com.saboon.project_2511sch.domain.usecase.programtable.ProgramTableReadUseCase
import com.saboon.project_2511sch.domain.usecase.programtable.ProgramTableWriteUseCase
import com.saboon.project_2511sch.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ViewModelProgramTable @Inject constructor(
    private val programTableWriteUseCase: ProgramTableWriteUseCase,
    private val programTableReadUseCase: ProgramTableReadUseCase,
    private val getProgramTableDisplayItemListUseCase: GetProgramTableDisplayItemListUseCase,
): ViewModel() {

    private val _operationEvent = Channel<Resource<ProgramTable>>()
    val operationEvent = _operationEvent.receiveAsFlow()

    private val _selectedId = MutableStateFlow<String?>(null)


    //STATE
    @OptIn(ExperimentalCoroutinesApi::class)
    val programTableState: StateFlow<Resource<ProgramTable>> = _selectedId
        .filterNotNull()
        .flatMapLatest { id -> programTableReadUseCase.getById(id) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Resource.Idle()
        )

    val programTablesState: StateFlow<Resource<List<DisplayItemProgramTable>>> =
        getProgramTableDisplayItemListUseCase.invoke()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = Resource.Idle()
            )


    //ACTIONS
    fun  getById(id: String){
        _selectedId.value = id
    }
    fun insert(programTable: ProgramTable) = executeWriteAction{
        programTableWriteUseCase.insert(programTable)
    }
    fun update(programTable: ProgramTable) = executeWriteAction{
        programTableWriteUseCase.update(programTable)
    }
    fun delete(programTable: ProgramTable) = executeWriteAction{
        programTableWriteUseCase.delete(programTable)
    }
    fun activationById(id: String, isActive: Boolean){
        viewModelScope.launch {
            try {
                programTableWriteUseCase.activationById(id, isActive)
            }catch (e: Exception){

            }
        }
    }

    private fun executeWriteAction(action: suspend () -> Resource<ProgramTable>) {
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