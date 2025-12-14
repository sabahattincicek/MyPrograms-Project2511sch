package com.saboon.project_2511sch.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saboon.project_2511sch.domain.model.ProgramTable
import com.saboon.project_2511sch.domain.usecase.home.GetHomeDisplayItemsUseCase
import com.saboon.project_2511sch.domain.usecase.programtable.GetActiveProgramTableUseCase
import com.saboon.project_2511sch.domain.usecase.programtable.GetAllProgramTablesUseCase
import com.saboon.project_2511sch.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ViewModelHome @Inject constructor(
    private val getActiveProgramTableUseCase: GetActiveProgramTableUseCase,
    private val getHomeDisplayItemsUseCase: GetHomeDisplayItemsUseCase,
    private val getAllProgramTablesUseCase: GetAllProgramTablesUseCase
): ViewModel() {

    private val _activeProgramTableState = MutableStateFlow<Resource<ProgramTable>>(Resource.Idle())
    val activeProgramTable = _activeProgramTableState.asStateFlow()

    private val _programTablesState = MutableStateFlow<Resource<List<ProgramTable>>>(Resource.Idle())
    val programTableState = _programTablesState.asStateFlow()

    private val _displayItemsState = MutableStateFlow<Resource<List<HomeDisplayItem>>>(Resource.Idle())
    val displayItemsState = _displayItemsState.asStateFlow()


    fun getActiveProgramTable(){
        viewModelScope.launch {
            try {
                _activeProgramTableState.value = Resource.Loading()
                getActiveProgramTableUseCase.invoke().collect { resource ->
                    _activeProgramTableState.value = resource
                }
            }catch (e: Exception){
                _activeProgramTableState.value = Resource.Error(e.localizedMessage ?: "An unexpected error occurred in ViewModel.")
            }
        }
    }

    fun getAllProgramTables(){
        viewModelScope.launch {
            try {
                _programTablesState.value = Resource.Loading()
                getAllProgramTablesUseCase.invoke().collect { resource ->
                    _programTablesState.value = resource
                }
            }catch (e: Exception){
                _programTablesState.value = Resource.Error(e.localizedMessage ?: "An unexpected error occurred in ViewModel.")
            }
        }
    }

    fun getDisplayItems(programTable: ProgramTable){
        viewModelScope.launch {
            try {
                _displayItemsState.value = Resource.Loading()
                getHomeDisplayItemsUseCase.invoke(programTable).collect { resource ->
                    _displayItemsState.value = resource
                }
            }catch (e: Exception){
                _displayItemsState.value = Resource.Error(e.localizedMessage ?: "An unexpected error occurred in ViewModel.")
            }
        }
    }

}