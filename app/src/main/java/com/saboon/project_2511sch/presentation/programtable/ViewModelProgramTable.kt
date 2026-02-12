package com.saboon.project_2511sch.presentation.programtable

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saboon.project_2511sch.domain.model.ProgramTable
import com.saboon.project_2511sch.domain.usecase.programtable.GetProgramTableDisplayItemListUseCase
import com.saboon.project_2511sch.domain.usecase.programtable.ProgramTableReadUseCase
import com.saboon.project_2511sch.domain.usecase.programtable.ProgramTableWriteUseCase
import com.saboon.project_2511sch.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ViewModelProgramTable @Inject constructor(
    private val programTableWriteUseCase: ProgramTableWriteUseCase,
    private val programTableReadUseCase: ProgramTableReadUseCase,
    private val getProgramTableDisplayItemListUseCase: GetProgramTableDisplayItemListUseCase,
): ViewModel() {
    private val _insertEvent = Channel<Resource<ProgramTable>>()
    val insertEvent = _insertEvent.receiveAsFlow()
    private val _updateEvent = Channel<Resource<ProgramTable>>()
    val updateEvent = _updateEvent.receiveAsFlow()
    private val _deleteEvent = Channel<Resource<ProgramTable>>()
    val deleteEvent = _deleteEvent.receiveAsFlow()


    private val _programTableState = MutableStateFlow<Resource<ProgramTable>>(Resource.Idle())
    val programTableState = _programTableState.asStateFlow()
    private val _programTablesState = MutableStateFlow<Resource<List<DisplayItemProgramTable>>>(Resource.Idle())
    val programTablesState = _programTablesState.asStateFlow()


    //STATE
    fun  getById(id: String){
        viewModelScope.launch {
            try {
                _programTableState.value = Resource.Loading()
                programTableReadUseCase.getById(id).collect { resource ->
                    _programTableState.value = resource
                }
            }catch (e: Exception){
                _programTableState.value = Resource.Error(e.localizedMessage ?: "An unexpected error occurred in ViewModel.")
            }
        }
    }
    fun getAllProgramTables(){
        viewModelScope.launch {
            try {
                _programTablesState.value = Resource.Loading()
                getProgramTableDisplayItemListUseCase.invoke().collect { resource ->
                    _programTablesState.value = resource
                }
            } catch (e: Exception) {
                _programTablesState.value = Resource.Error(e.localizedMessage ?: "An unexpected error occurred in ViewModel.")
            }
        }
    }

    //EVENT
    fun insert(programTable: ProgramTable){
        viewModelScope.launch {
            try{
                _insertEvent.send(Resource.Loading())
                val insertResult = programTableWriteUseCase.insert(programTable)
                _insertEvent.send(insertResult)
            }catch (e: Exception){
                _insertEvent.send(Resource.Error(e.localizedMessage ?: "An unexpected error occurred in ViewModel."))
            }
        }
    }
    fun update(programTable: ProgramTable){
        viewModelScope.launch {
            try{
                _updateEvent.send(Resource.Loading())
                val updateResult = programTableWriteUseCase.update(programTable)
                _updateEvent.send(updateResult)
            }catch (e: Exception){
                _updateEvent.send(Resource.Error(e.localizedMessage ?: "An unexpected error occurred in ViewModel."))
            }
        }
    }
    fun delete(programTable: ProgramTable){
        viewModelScope.launch {
            try{
                _deleteEvent.send(Resource.Loading())
                val deleteResult = programTableWriteUseCase.delete(programTable)
                _deleteEvent.send(deleteResult)
            }catch (e: Exception){
                _deleteEvent.send(Resource.Error(e.localizedMessage ?: "An unexpected error occurred in ViewModel."))
            }
        }
    }

    fun getAllProgramTablesCount(onResult: (Resource<Int>) -> Unit){
        viewModelScope.launch {
            try {
                val result = programTableReadUseCase.getAllCount()
                onResult(result)
            }catch (e: Exception){
                onResult(Resource.Error(e.localizedMessage ?: "An unexpected error occurred in ViewModel."))
            }
        }
    }
    fun getAllActiveProgramTablesCount(onResult: (Resource<Int>) -> Unit){
        viewModelScope.launch {
            try {
                val result = programTableReadUseCase.getAllActiveCount()
                onResult(result)
            }catch (e: Exception){
                onResult(Resource.Error(e.localizedMessage ?: "An unexpected error occurred in ViewModel."))
            }
        }
    }
}