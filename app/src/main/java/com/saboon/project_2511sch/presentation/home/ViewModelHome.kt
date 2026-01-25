package com.saboon.project_2511sch.presentation.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saboon.project_2511sch.domain.model.ProgramTable
import com.saboon.project_2511sch.domain.usecase.home.GetHomeDisplayItemsUseCase
import com.saboon.project_2511sch.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ViewModelHome @Inject constructor(
    private val getHomeDisplayItemsUseCase: GetHomeDisplayItemsUseCase,
): ViewModel() {

    private val tag = "ViewModelHome"
    private val _displayItemsState = MutableStateFlow<Resource<List<HomeDisplayItem>>>(Resource.Idle())
    val displayItemsState = _displayItemsState.asStateFlow()

    fun getDisplayItems(activeProgramTableList: List<ProgramTable>){
        Log.d(tag, "getDisplayItems: Called with list size: ${activeProgramTableList.size}")
        viewModelScope.launch {
            try {
                _displayItemsState.value = Resource.Loading()
                Log.d(tag, "getDisplayItems: State set to Loading")
                
                getHomeDisplayItemsUseCase.invoke(activeProgramTableList).collect { resource ->
                    Log.d(tag, "getDisplayItems: Resource received: ${resource::class.java.simpleName}")
                    _displayItemsState.value = resource
                }
            } catch (e: Exception){
                Log.e(tag, "getDisplayItems: Error occurred", e)
                _displayItemsState.value = Resource.Error(e.localizedMessage ?: "An unexpected error occurred in ViewModel.")
            }
        }
    }
}
