package com.saboon.project_2511sch.presentation.programtable

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saboon.project_2511sch.domain.model.ProgramTable
import com.saboon.project_2511sch.domain.usecase.programtable.DeleteProgramTableUseCase
import com.saboon.project_2511sch.domain.usecase.programtable.GetAllProgramTablesUseCase
import com.saboon.project_2511sch.domain.usecase.programtable.InsertNewProgramTableUseCase
import com.saboon.project_2511sch.domain.usecase.programtable.UpdateProgramTableUseCase
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
class ViewModelProgramTable @Inject constructor(
    private val insertNewProgramTableUseCase: InsertNewProgramTableUseCase,
    private val deleteProgramTableUseCase: DeleteProgramTableUseCase,
    private val updateProgramTableUseCase: UpdateProgramTableUseCase,
    private val getAllProgramTablesUseCase: GetAllProgramTablesUseCase,
): ViewModel() {


    companion object {
        private const val TAG = "ProgramTableViewModel"
    }
    private val _insertNewProgramTableEvent = Channel<Resource<ProgramTable>>()
    val insertNewProgramTableEvent = _insertNewProgramTableEvent.receiveAsFlow()

    private val _deleteProgramTableEvent = Channel<Resource<ProgramTable>>()
    val deleteProgramTableEvent = _deleteProgramTableEvent.receiveAsFlow()

    private val _updateProgramTableEvent = Channel<Resource<Unit>>()
    val updateProgramTableEvent = _updateProgramTableEvent.receiveAsFlow()

    private val _programTablesState = MutableStateFlow<Resource<List<ProgramTable>>>(Resource.Idle())
    val programTablesState: StateFlow<Resource<List<ProgramTable>>> = _programTablesState.asStateFlow()
    fun insertNewProgramTable(programTable: ProgramTable){
        viewModelScope.launch {
            try{
                Log.d(TAG, "Starting to add a new program table.")
                _insertNewProgramTableEvent.send(Resource.Loading())
                val insertResult = insertNewProgramTableUseCase.invoke(programTable)
                Log.d(TAG, "Sending insert result to event channel: $insertResult")
                _insertNewProgramTableEvent.send(insertResult)
            }catch (e: Exception){
                Log.e(TAG, "An unexpected error occurred: ${e.localizedMessage}", e)
                _insertNewProgramTableEvent.send(Resource.Error(e.localizedMessage ?: "An unexpected error occurred in ViewModel."))
            }
        }
    }
    fun getAllProgramTables(){
        Log.d(TAG, "getAllProgramTables called.")
        viewModelScope.launch {
            try {
                _programTablesState.value = Resource.Loading()
                Log.d(TAG, "Fetching all program tables.")
                getAllProgramTablesUseCase.invoke().collect { resource ->
                    _programTablesState.value = resource
                    Log.d(TAG, "Received program tables resource: $resource")                }
            } catch (e: Exception) {
                Log.e(TAG, "An unexpected error occurred while getting all program tables: ${e.localizedMessage}", e)
                _programTablesState.value = Resource.Error(e.localizedMessage ?: "An unexpected error occurred in ViewModel.")
            }
        }
    }

    fun deleteProgramTable(programTable: ProgramTable){
        Log.d(TAG, "deleteProgramTable called for program table: ${programTable.id}")
        viewModelScope.launch {
            try{
                Log.d(TAG, "Starting to delete program table.")
                _deleteProgramTableEvent.send(Resource.Loading())
                val deleteResult = deleteProgramTableUseCase.invoke(programTable)
                Log.d(TAG, "Sending delete result to event channel: $deleteResult")
                _deleteProgramTableEvent.send(deleteResult)
            }catch (e: Exception){
                Log.e(TAG, "An unexpected error occurred while deleting program table: ${e.localizedMessage}", e)
                _deleteProgramTableEvent.send(Resource.Error(e.localizedMessage ?: "An unexpected error occurred in ViewModel."))
            }
        }
    }

    fun updateProgramTable(programTable: ProgramTable){
        Log.d(TAG, "updateProgramTable called for program table: ${programTable.id}")
        viewModelScope.launch {
            try{
                Log.d(TAG, "Starting to update program table.")
                _updateProgramTableEvent.send(Resource.Loading())
                val updateResult = updateProgramTableUseCase.invoke(programTable)
                Log.d(TAG, "Sending update result to event channel: $updateResult")
                _updateProgramTableEvent.send(updateResult)
            }catch (e: Exception){
                Log.e(TAG, "An unexpected error occurred while updating program table: ${e.localizedMessage}", e)
                _updateProgramTableEvent.send(Resource.Error(e.localizedMessage ?: "An unexpected error occurred in ViewModel."))
            }
        }
    }
}