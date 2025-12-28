package com.saboon.project_2511sch.presentation.file

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saboon.project_2511sch.domain.model.File
import com.saboon.project_2511sch.domain.usecase.file.GetAllFilesUseCase
import com.saboon.project_2511sch.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ViewModelFile @Inject constructor(
    private val getAllFilesUseCase: GetAllFilesUseCase
): ViewModel() {
    private var _filesState = MutableStateFlow<Resource<List<File>>>(Resource.Idle())
    val filesState = _filesState.asStateFlow()

    fun getAllFiles(){
        viewModelScope.launch {
            try {
                _filesState.value = Resource.Loading()
                val result = getAllFilesUseCase.invoke().collect { resource ->
                    _filesState.value = resource
                }
            }catch (e: Exception){
                _filesState.value = Resource.Error(e.localizedMessage ?: "An unexpected error occurred in ViewModel.")
            }
        }
    }
}