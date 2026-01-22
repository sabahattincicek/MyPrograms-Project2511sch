package com.saboon.project_2511sch.presentation.programtable

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saboon.project_2511sch.domain.model.ProgramTable
import com.saboon.project_2511sch.domain.usecase.programtable.ProgramTableReadUseCase
import com.saboon.project_2511sch.domain.usecase.programtable.ProgramTableWriteUseCase
import com.saboon.project_2511sch.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ViewModelProgramTable @Inject constructor(
    private val programTableWriteUseCase: ProgramTableWriteUseCase,
    private val programTableReadUseCase: ProgramTableReadUseCase,
): ViewModel() {


    companion object {
        private const val TAG = "ProgramTableViewModel"
    }
    private val _insertNewProgramTableEvent = MutableSharedFlow<Resource<ProgramTable>>()
    val insertNewProgramTableEvent = _insertNewProgramTableEvent.asSharedFlow()
    private val _updateProgramTableEvent = MutableSharedFlow<Resource<Unit>>()
    val updateProgramTableEvent = _updateProgramTableEvent.asSharedFlow()
    private val _deleteProgramTableEvent = MutableSharedFlow<Resource<ProgramTable>>()
    val deleteProgramTableEvent = _deleteProgramTableEvent.asSharedFlow()
    private val _programTablesState = MutableStateFlow<Resource<List<ProgramTable>>>(Resource.Idle())
    val programTablesState = _programTablesState.asStateFlow()


    fun insertNewProgramTable(programTable: ProgramTable){
        viewModelScope.launch {
            try{
                Log.d(TAG, "Starting to add a new program table.")
                _insertNewProgramTableEvent.emit(Resource.Loading())
                val insertResult = programTableWriteUseCase.insert(programTable)
                Log.d(TAG, "Sending insert result to event channel: $insertResult")
                _insertNewProgramTableEvent.emit(insertResult)
            }catch (e: Exception){
                Log.e(TAG, "An unexpected error occurred: ${e.localizedMessage}", e)
                _insertNewProgramTableEvent.emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred in ViewModel."))
            }
        }
    }
    fun updateProgramTable(programTable: ProgramTable){
        Log.d(TAG, "updateProgramTable called for program table: ${programTable.id}")
        viewModelScope.launch {
            try{
                Log.d(TAG, "Starting to update program table.")
                _updateProgramTableEvent.emit(Resource.Loading())
                val updateResult = programTableWriteUseCase.update(programTable)
                Log.d(TAG, "Sending update result to event channel: $updateResult")
                _updateProgramTableEvent.emit(updateResult)
            }catch (e: Exception){
                Log.e(TAG, "An unexpected error occurred while updating program table: ${e.localizedMessage}", e)
                _updateProgramTableEvent.emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred in ViewModel."))
            }
        }
    }

    fun deleteProgramTable(programTable: ProgramTable){
        Log.d(TAG, "deleteProgramTable called for program table: ${programTable.id}")
        viewModelScope.launch {
            try{
                Log.d(TAG, "Starting to delete program table.")
                _deleteProgramTableEvent.emit(Resource.Loading())
                val deleteResult = programTableWriteUseCase.delete(programTable)
                Log.d(TAG, "Sending delete result to event channel: $deleteResult")
                _deleteProgramTableEvent.emit(deleteResult)
            }catch (e: Exception){
                Log.e(TAG, "An unexpected error occurred while deleting program table: ${e.localizedMessage}", e)
                _deleteProgramTableEvent.emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred in ViewModel."))
            }
        }
    }


    fun getAllProgramTables(){
        Log.d(TAG, "getAllProgramTables called.")
        viewModelScope.launch {
            try {
                _programTablesState.value = Resource.Loading()
                Log.d(TAG, "Fetching all program tables.")
                programTableReadUseCase.getAll().collect { resource ->
                    _programTablesState.value = resource
                    Log.d(TAG, "Received program tables resource: $resource")                }
            } catch (e: Exception) {
                Log.e(TAG, "An unexpected error occurred while getting all program tables: ${e.localizedMessage}", e)
                _programTablesState.value = Resource.Error(e.localizedMessage ?: "An unexpected error occurred in ViewModel.")
            }
        }
    }

    fun getActiveProgramTableList(){
        viewModelScope.launch {
            try {
                _programTablesState.value = Resource.Loading()
                programTableReadUseCase.getAllActive().collect { resource ->
                    _programTablesState.value = resource
                }
            }catch (e: Exception){
                _programTablesState.value = Resource.Error(e.localizedMessage ?: "An unexpected error occurred in ViewModel.")
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