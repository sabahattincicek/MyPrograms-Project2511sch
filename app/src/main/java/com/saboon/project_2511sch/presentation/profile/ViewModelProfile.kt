package com.saboon.project_2511sch.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saboon.project_2511sch.domain.usecase.settings.ExportDataUseCase
import com.saboon.project_2511sch.domain.usecase.settings.ImportDataUseCase
import com.saboon.project_2511sch.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ViewModelProfile @Inject constructor(
    private val exportDataUseCase: ExportDataUseCase,
    private val importDataUseCase: ImportDataUseCase
): ViewModel() {

    private val _exportState = MutableStateFlow<Resource<File>>(Resource.Idle())
    val exportState: StateFlow<Resource<File>> = _exportState.asStateFlow()

    private val _importState = MutableStateFlow<Resource<Unit>>(Resource.Idle())
    val importState: StateFlow<Resource<Unit>> = _importState.asStateFlow()

    fun exportData(){
        viewModelScope.launch {
            _exportState.value = Resource.Loading()
            _exportState.value = exportDataUseCase.execute()
        }
    }
    fun importData(zipFile: File){
        viewModelScope.launch {
            _importState.value = Resource.Loading()
            _importState.value = importDataUseCase.execute(zipFile)
        }
    }

    // İşlem bittikten sonra UI'da Toast vb. gösterince durumu sıfırlamak için
    fun resetExportOperation(){
        _exportState.value = Resource.Idle()
    }

    fun resetImportOperation(){
        _importState.value = Resource.Idle()
    }
}