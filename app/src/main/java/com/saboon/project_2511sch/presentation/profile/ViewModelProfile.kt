package com.saboon.project_2511sch.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saboon.project_2511sch.domain.usecase.settings.ExportDataUseCase
import com.saboon.project_2511sch.util.Resource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

class ViewModelProfile @Inject constructor(
    private val exportDataUseCase: ExportDataUseCase
): ViewModel() {

    private val _exportState = MutableStateFlow<Resource<File>>(Resource.Idle())
    val exportState: StateFlow<Resource<File>> = _exportState.asStateFlow()

    fun exportData(){
        viewModelScope.launch {
            _exportState.value = Resource.Loading()
            _exportState.value = exportDataUseCase.execute()
        }
    }

    // İşlem bittikten sonra UI'da Toast vb. gösterince durumu sıfırlamak için
    fun resetExportOperation(){
        _exportState.value = Resource.Idle()
    }
}