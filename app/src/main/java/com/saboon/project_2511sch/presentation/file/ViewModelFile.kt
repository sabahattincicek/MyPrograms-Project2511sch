package com.saboon.project_2511sch.presentation.file

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saboon.project_2511sch.domain.model.File
import com.saboon.project_2511sch.domain.usecase.file.InsertNewFileUseCase
import com.saboon.project_2511sch.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ViewModelFile @Inject constructor(
    private val insertNewFileUseCase: InsertNewFileUseCase
) : ViewModel() {
    private val _insertNewFileEvent = Channel<Resource<File>>()
    val insertNewFileEvent = _insertNewFileEvent.receiveAsFlow()

    fun insertNewFile(file: File){
        viewModelScope.launch {
            try {
                _insertNewFileEvent.send(Resource.Loading())
                val result = insertNewFileUseCase.invoke(file)
                _insertNewFileEvent.send(result)
            }catch (e: Exception){
                _insertNewFileEvent.send(Resource.Error(e.localizedMessage?:"An unexpected error occurred in ViewModel."))
            }
        }
    }
}