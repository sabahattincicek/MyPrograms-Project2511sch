package com.saboon.project_2511sch.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saboon.project_2511sch.domain.model.ProgramTable
import com.saboon.project_2511sch.domain.usecase.home.GetHomeDisplayItemsUseCase
import com.saboon.project_2511sch.domain.usecase.programtable.GetActiveProgramTableListUseCase
import com.saboon.project_2511sch.domain.usecase.programtable.GetAllProgramTablesUseCase
import com.saboon.project_2511sch.domain.usecase.programtable.SetProgramTableActiveUseCase
import com.saboon.project_2511sch.domain.usecase.programtable.UpdateProgramTableUseCase
import com.saboon.project_2511sch.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ViewModelHome @Inject constructor(
    private val getHomeDisplayItemsUseCase: GetHomeDisplayItemsUseCase,
): ViewModel() {
    private val _displayItemsState = MutableStateFlow<Resource<List<HomeDisplayItem>>>(Resource.Idle())
    val displayItemsState = _displayItemsState.asStateFlow()

    fun getDisplayItems(programTableList: List<ProgramTable>){
        viewModelScope.launch {
            try {
                _displayItemsState.value = Resource.Loading()
                getHomeDisplayItemsUseCase.invoke(programTableList).collect { resource ->
                    _displayItemsState.value = resource
                }
            }catch (e: Exception){
                _displayItemsState.value = Resource.Error(e.localizedMessage ?: "An unexpected error occurred in ViewModel.")
            }
        }
    }

}